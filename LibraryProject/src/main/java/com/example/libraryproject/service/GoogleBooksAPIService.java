package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.GoogleBooksResponse;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.utilities.Mappers;
import jakarta.json.*;
import lombok.RequiredArgsConstructor;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
public class GoogleBooksAPIService {

    public static final String ATTRIBUTE_NAME = "google_api_service";

    private static final String[] GENRES = {
            "romance", "mystery", "fantasy", "thriller", "science-fiction",
            "horror", "adventure", "drama", "comedy",
            "biography", "memoir", "poetry", "philosophy", "psychology",
            "art", "music", "religion", "politics", "history"
    };

    private static final int BOOKS_PER_REQUEST = 2;

    private static final String GOOGLE_API_URL = "https://www.googleapis.com/books/v1/volumes";
    private static final int TOTAL_BOOKS_TARGET = 40;

    private final BookRepository bookRepository;

    private final Random random = new Random();


    public void fetchAndSaveBooks() {
        if (!bookRepository.findAll().isEmpty()) {
            return;
        }

        List<GoogleBooksResponse> googleBooks = fetchBooks();

        bookRepository.saveAll(googleBooks.stream()
                .map(Mappers::mapGoogleBookToBook)
                .toList());
    }

    List<GoogleBooksResponse> fetchBooks() {
        System.out.println("VAIMEE " + GOOGLE_API_URL);
        List<GoogleBooksResponse> allBooks = new ArrayList<>();
        int requestsNeeded = TOTAL_BOOKS_TARGET / BOOKS_PER_REQUEST;

        for (int i = 0; i < requestsNeeded; i++) {
            String randomGenre = getRandomGenre();
            List<GoogleBooksResponse> booksFromGenre = fetchBooksFromGenre(randomGenre);
            allBooks.addAll(booksFromGenre);

        }

        return allBooks;
    }

    private String getRandomGenre() {
        int randomIndex = random.nextInt(GENRES.length);
        return GENRES[randomIndex];
    }

    private List<GoogleBooksResponse> fetchBooksFromGenre(String genre) {
        List<GoogleBooksResponse> books = new ArrayList<>();

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

                    books.add(new GoogleBooksResponse(title, publishedDate, author, description, thumbnail));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return books;
    }
}