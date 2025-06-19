package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.GoogleBooksResponse;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.utilities.Mappers;
import jakarta.json.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static com.example.libraryproject.configuration.ApplicationProperties.*;

@RequiredArgsConstructor
public class GoogleBooksAPIService {

    private final BookRepository bookRepository;
    private static final Logger logger = LoggerFactory.getLogger(GoogleBooksAPIService.class);


    public static List<String> getRandomGenres(int n) {
        List<String> genreList = new ArrayList<>(Arrays.asList(GOOGLE_BOOKS_GENRES));
        Collections.shuffle(genreList);
        logger.info("Selected genres: {}", genreList.subList(0, n));
        return genreList.subList(0, n);
    }

    public void fetchAndSaveBooks() {
        if (!bookRepository.findAll().isEmpty()) {
            return;
        }

        HashSet<GoogleBooksResponse> googleBooks = fetchBooks(TOTAL_BOOKS_TARGET, BOOKS_PER_REQUEST);
        List<Book> books = googleBooks.stream()
                .map(Mappers::mapGoogleBookToBook)
                .toList();
        logger.info("Fetched {} books from Google Books API", books.size());
        bookRepository.saveAll(books);
    }

    HashSet<GoogleBooksResponse> fetchBooks(int amountOfBooks, int booksPerRequest) {
        HashSet<GoogleBooksResponse> allBooks = new HashSet<>();
        int requestsNeeded = amountOfBooks / booksPerRequest;

        List<String> chosenGenres = getRandomGenres(requestsNeeded);

        for (int i = 0; i < requestsNeeded; i++) {
            HashSet<GoogleBooksResponse> booksFromGenre = fetchBooksFromGenre(chosenGenres.get(i), booksPerRequest);
            allBooks.addAll(booksFromGenre);
        }
        return allBooks;
    }

    HashSet<GoogleBooksResponse> fetchBooksFromGenre(String genre, int booksPerRequest) {
        HashSet<GoogleBooksResponse> books = new HashSet<>();

        try {
            String fullUrl = GOOGLE_API_URL + "?q=subject:" + genre + "&maxResults=" + booksPerRequest;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .header("User-Agent", "LibraryProject/1.0")
                    .GET()
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            try (InputStream is = response.body();
                 JsonReader reader = Json.createReader(is)) {

                JsonObject root = reader.readObject();
                JsonArray items = root.getJsonArray("items");

                if (items == null) {
                    return books;
                }

                for (JsonValue itemVal : items) {
                    JsonObject item = itemVal.asJsonObject();
                    JsonObject volumeInfo = item.getJsonObject("volumeInfo");

                    Long pageCount = volumeInfo.containsKey("pageCount") ?
                            volumeInfo.getJsonNumber("pageCount").longValue() : 0;

                    String title = volumeInfo.getString("title", "No Title");
                    String publishedDate = volumeInfo.getString("publishedDate", "Unknown Date");
                    String description = volumeInfo.getString("description", "No Description");

                    String author = "Unknown Author";
                    JsonArray authors = volumeInfo.getJsonArray("authors");
                    if (authors != null && !authors.isEmpty()) {
                        author = authors.getString(0);
                    }
                    String safeTitle = title.replaceAll("[^a-zA-Z0-9.\\-]", "_");
                    String thumbnail;
                    JsonObject imageLinks = volumeInfo.getJsonObject("imageLinks");
                    if (imageLinks != null) {
                        thumbnail = imageLinks.getString("thumbnail", null);
                        if (thumbnail != null && !thumbnail.isEmpty()) {
                            try {
                                downloadAndSaveImage(thumbnail, safeTitle);
                            } catch (Exception ex) {
                                System.err.printf("Failed to download thumbnail for '%s': %s%n", title, ex.getMessage());
                            }
                        }
                    }

                    books.add(new GoogleBooksResponse(title, publishedDate, author, description, safeTitle + ".jpg", genre, pageCount));
                    logger.info("added {} to books table",  title);
                }
            }

        } catch (Exception e) {
            logger.error("Error fetching books for genre {}: {}", genre, e.getMessage());
        }

        return books;
    }

    void downloadAndSaveImage(String imageUrl, String title) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .GET()
                .build();

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Image download failed with status: " + response.statusCode());
        }

        String safeTitle = title.replaceAll("[^a-zA-Z0-9.\\-]", "_");

        String projectRoot = System.getProperty("user.dir");
        Path imagesDir = Paths.get(projectRoot, "src", "main", "webapp", "images");

        Files.createDirectories(imagesDir);
        Path filePath = imagesDir.resolve(safeTitle + ".jpg");

        try (InputStream is = response.body()) {
            Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Image saved to: {}", filePath);
        }
    }

}
