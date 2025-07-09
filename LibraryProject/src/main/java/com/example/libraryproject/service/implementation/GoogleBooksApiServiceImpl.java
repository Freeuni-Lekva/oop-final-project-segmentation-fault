package com.example.libraryproject.service.implementation;

import com.example.libraryproject.model.dto.BookAdditionFromGoogleRequest;
import com.example.libraryproject.model.dto.GoogleBooksResponse;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.service.GoogleBooksApiService;
import com.example.libraryproject.utilities.Mappers;
import jakarta.json.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

import static com.example.libraryproject.configuration.ApplicationProperties.*;

@RequiredArgsConstructor
public class GoogleBooksApiServiceImpl implements GoogleBooksApiService {

    private final BookRepository bookRepository;
    private static final Logger logger = LoggerFactory.getLogger(GoogleBooksApiServiceImpl.class);
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public void fetchAndSaveBooks() {
        if (!bookRepository.findAll().isEmpty()) {
            return;
        }

        deleteImages();

        Set<GoogleBooksResponse> googleBooks = fetchBooks();
        List<Book> books = googleBooks.stream()
                .map(Mappers::mapGoogleBookToBook)
                .toList();

        logger.info("Fetched {} books from Google Books API", books.size());
        bookRepository.saveAll(books);
    }

    private void deleteImages() {
        try {
            Path imagesDir = Paths.get(System.getenv("IMAGE_DIR"));

            if (!Files.exists(imagesDir) || !Files.isDirectory(imagesDir)) {
                return;
            }

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(imagesDir, "*.{jpg,jpeg,png,gif}")) {
                for (Path file : stream) {
                    try {
                        Files.delete(file);
                    } catch (Exception e) {
                        logger.warn("Could not delete file {}: {}", file.getFileName(), e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Failed to delete images: {}", e.getMessage());
        }
    }

    public boolean fetchBook(BookAdditionFromGoogleRequest request) {
        Book book = getBookFromGoogle(request.title(), request.author());
        if (book == null) {
            return false;
        }
        bookRepository.save(book);
        logger.info("Successfully saved book: {}", book.getName());
        return true;
    }

    Book getBookFromGoogle(String title, String author) {
        try {
            String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
            String query = GOOGLE_API_URL + "?q=intitle:" + encodedTitle;

            if (author != null && !author.isBlank()) {
                String encodedAuthor = URLEncoder.encode(author, StandardCharsets.UTF_8);
                query += "+inauthor:" + encodedAuthor;
            }

            query += "&maxResults=1";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(query))
                    .header("User-Agent", "LibraryProject/1.0")
                    .GET()
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            try (InputStream is = response.body(); JsonReader reader = Json.createReader(is)) {
                JsonObject root = reader.readObject();
                JsonArray items = root.getJsonArray("items");

                if (items == null || items.isEmpty()) {
                    logger.warn("No book found for title '{}' and author '{}'", title, author);
                    return null;
                }

                JsonObject item = items.getJsonObject(0);
                JsonObject volumeInfo = item.getJsonObject("volumeInfo");

                String bookTitle = volumeInfo.getString("title", "Unknown Title");
                String publishedDate = volumeInfo.getString("publishedDate", "Unknown Date");
                String description = volumeInfo.getString("description", "No Description");

                String bookAuthor = "Unknown Author";
                JsonArray authors = volumeInfo.getJsonArray("authors");
                if (authors != null && !authors.isEmpty()) {
                    bookAuthor = authors.getString(0);
                }

                Long pageCount = volumeInfo.containsKey("pageCount")
                        ? volumeInfo.getJsonNumber("pageCount").longValue()
                        : 0L;

                String genre = "Unknown";

                String safeTitle = bookTitle.replaceAll("[^a-zA-Z0-9.\\-]", "_");
                String thumbnail = null;
                JsonObject imageLinks = volumeInfo.getJsonObject("imageLinks");
                if (imageLinks != null) {
                    thumbnail = imageLinks.getString("thumbnail", null);
                    if (thumbnail != null && !thumbnail.isEmpty()) {
                        try {
                            downloadAndSaveImage(thumbnail, safeTitle);
                        } catch (Exception ex) {
                            logger.warn("Could not download image for '{}': {}", bookTitle, ex.getMessage());
                        }
                    }
                }

                GoogleBooksResponse gBook = new GoogleBooksResponse(
                        bookTitle,
                        publishedDate,
                        bookAuthor,
                        description,
                        (thumbnail != null ? safeTitle + ".jpg" : null),
                        genre,
                        pageCount
                );

                Book bookEntity = Mappers.mapGoogleBookToBook(gBook);
                logger.info("Successfully fetched book: {}", bookTitle);
                return bookEntity;
            }

        } catch (Exception e) {
            logger.error("Failed to fetch book with title '{}' and author '{}': {}", title, author, e.getMessage());
        }
        return null;
    }

    Set<GoogleBooksResponse> fetchBooks() {
        Set<GoogleBooksResponse> allBooks = ConcurrentHashMap.newKeySet();

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(10, GOOGLE_BOOKS_GENRES.length));
        List<Callable<Void>> tasks = new ArrayList<>();

        for (String genre : GOOGLE_BOOKS_GENRES) {
            tasks.add(() -> {
                Set<GoogleBooksResponse> genreBooks = fetchBooksFromGenre(genre, BOOKS_PER_REQUEST);
                allBooks.addAll(genreBooks);
                return null;
            });
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            logger.error("Parallel genre fetching interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }

        return allBooks;
    }

    Set<GoogleBooksResponse> fetchBooksFromGenre(String genre, int booksPerRequest) {
        Set<GoogleBooksResponse> books = new HashSet<>();

        try {
            int random = ThreadLocalRandom.current().nextInt(0, GOOGLE_BOOKS_API_MAX_PAGE);
            String fullUrl = GOOGLE_API_URL + "?q=subject:" + genre + "&startIndex=" + random + "&maxResults=" + booksPerRequest;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .header("User-Agent", "LibraryProject/1.0")
                    .GET()
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            try (InputStream is = response.body(); JsonReader reader = Json.createReader(is)) {
                JsonObject root = reader.readObject();
                JsonArray items = root.getJsonArray("items");

                if (items == null) return books;

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
                    String thumbnail = null;
                    JsonObject imageLinks = volumeInfo.getJsonObject("imageLinks");
                    if (imageLinks != null) {
                        thumbnail = imageLinks.getString("thumbnail", null);
                        if (thumbnail != null && !thumbnail.isEmpty()) {
                            try {
                                downloadAndSaveImage(thumbnail, safeTitle);
                            } catch (Exception ex) {
                                logger.warn("Failed to download image for '{}': {}", title, ex.getMessage());
                            }
                        }
                    }

                    books.add(new GoogleBooksResponse(
                            title,
                            publishedDate,
                            author,
                            description,
                            (thumbnail != null ? safeTitle + ".jpg" : "NOT_FOUND"),
                            genre,
                            pageCount
                    ));

                    logger.info("Added '{}' to books set", title);
                }
            }

        } catch (Exception e) {
            logger.error("Error fetching books for genre '{}': {}", genre, e.getMessage());
        }

        return books;
    }


    void downloadAndSaveImage(String imageUrl, String title) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Image download failed with status: " + response.statusCode());
        }

        String safeTitle = title.replaceAll("[^a-zA-Z0-9.\\-]", "_");
        Path imagesDir = Paths.get(System.getenv("IMAGE_DIR"));
        Files.createDirectories(imagesDir);
        Path filePath = imagesDir.resolve(safeTitle + ".jpg");

        try (InputStream is = response.body()) {
            Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Image saved to: {}", filePath);
        }
    }
}
