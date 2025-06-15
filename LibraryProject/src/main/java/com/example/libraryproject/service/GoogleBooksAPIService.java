package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.GoogleBooksResponse;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.utilities.Mappers;
import jakarta.json.*;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static com.example.libraryproject.configuration.ApplicationProperties.*;

@RequiredArgsConstructor
public class GoogleBooksAPIService {


    private final BookRepository bookRepository;

    private final Random random = new Random();


    public static List<String> getRandomGenres(int n) {
        List<String> genreList = new ArrayList<>(Arrays.asList(GOOGLE_BOOKS_GENRES));
        Collections.shuffle(genreList);
        return genreList.subList(0, n);
    }

    public void fetchAndSaveBooks() {
        if (!bookRepository.findAll().isEmpty()) {
            return;
        }

        HashSet<GoogleBooksResponse> googleBooks = fetchBooks();
        List<Book> books = googleBooks.stream()
                .map(Mappers::mapGoogleBookToBook)
                .toList();
        System.out.println(books.size());
        bookRepository.saveAll(books);
    }

    HashSet<GoogleBooksResponse> fetchBooks() {
        HashSet<GoogleBooksResponse> allBooks = new HashSet<>();
        int requestsNeeded = TOTAL_BOOKS_TARGET / BOOKS_PER_REQUEST;

        List<String> chosenGenres =  getRandomGenres(requestsNeeded);

        for (int i = 0; i < requestsNeeded; i++) {
            HashSet<GoogleBooksResponse> booksFromGenre = fetchBooksFromGenre(chosenGenres.get(i));
            allBooks.addAll(booksFromGenre);

        }

        return allBooks;
    }

    private HashSet<GoogleBooksResponse> fetchBooksFromGenre(String genre) {
        HashSet<GoogleBooksResponse> books = new HashSet<>();

        try {
            String fullUrl = GOOGLE_API_URL + "?q=subject:" + genre + "&maxResults=" + BOOKS_PER_REQUEST;

            URL url = new URL(fullUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "LibraryProject/1.0");

            try (InputStream is = conn.getInputStream();
                 JsonReader reader = Json.createReader(is)) {

                JsonObject root = reader.readObject();
                JsonArray items = root.getJsonArray("items");

                if (items == null) {
                    return books;
                }

                for (JsonValue itemVal : items) {
                    JsonObject item = itemVal.asJsonObject();
                    JsonObject volumeInfo = item.getJsonObject("volumeInfo");

                    Long pageCount = volumeInfo.getJsonNumber("pageCount") != null ?
                            volumeInfo.getJsonNumber("pageCount").longValue() : 0;

                    String title = volumeInfo.getString("title", "No Title");
                    String publishedDate = volumeInfo.getString("publishedDate", "Unknown Date");
                    String description = volumeInfo.getString("description", "No Description");

                    String author = "Unknown Author";
                    JsonArray authors = volumeInfo.getJsonArray("authors");
                    if (authors != null && !authors.isEmpty()) {
                        author = authors.getString(0);
                    }

                    String thumbnail = null;
                    JsonObject imageLinks = volumeInfo.getJsonObject("imageLinks");
                    if (imageLinks != null) {
                        thumbnail = imageLinks.getString("thumbnail", null);
                    }

                    books.add(new GoogleBooksResponse(title, publishedDate, author, description, thumbnail, genre, pageCount));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return books;
    }
}