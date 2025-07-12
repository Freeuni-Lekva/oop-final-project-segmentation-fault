package com.example.libraryproject.service.implementation;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.dto.BookAdditionFromGoogleRequest;
import com.example.libraryproject.model.dto.GoogleBooksResponse;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.enums.BookStatus;
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


@RequiredArgsConstructor
public class GoogleBooksApiServiceImpl implements GoogleBooksApiService {

    private final BookRepository bookRepository;
    private static final Logger logger = LoggerFactory.getLogger(GoogleBooksApiServiceImpl.class);
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private static final String GOOGLE_API_URL = ApplicationProperties.get("google.books.api.url");
    private static final int GOOGLE_BOOKS_API_MAX_PAGE = Integer.parseInt(ApplicationProperties.get("google.books.api.max-page"));
    private static final int BOOKS_PER_REQUEST = Integer.parseInt(ApplicationProperties.get("google.books.api.books-per-request"));
    private static final String[] GOOGLE_BOOKS_GENRES = ApplicationProperties.get("google.books.api.genres").split(",");

    public boolean fetchBook(BookAdditionFromGoogleRequest request, int copies) {
        Book book = getBookFromGoogle(request.title(), request.author());
        if (book == null) {
            return false;
        }

        // Check if book already exists in our library (including deleted ones)
        Optional<Book> existingBook = bookRepository.findByTitleAnyStatus(book.getName());

        if (existingBook.isPresent()) {
            Book bookInLibrary = existingBook.get();
            
            // If book was deleted, reactivate it and update all fields
            if (bookInLibrary.getStatus() == BookStatus.DELETED) {
                bookInLibrary.setStatus(BookStatus.ACTIVE);
                bookInLibrary.setAuthor(book.getAuthor());
                bookInLibrary.setDescription(book.getDescription());
                bookInLibrary.setGenre(book.getGenre());
                bookInLibrary.setVolume(book.getVolume());
                bookInLibrary.setImageUrl(book.getImageUrl());
                // Always set current date when reactivating deleted book to appear as recently added
                bookInLibrary.setDate(java.time.LocalDate.now());
                bookInLibrary.setDateAdded(java.time.LocalDateTime.now());
                
                // Set copies for reactivated book
                bookInLibrary.setTotalAmount((long) copies);
                bookInLibrary.setCurrentAmount((long) copies);
                logger.info("Book '{}' reactivated from Google Books with {} copies", book.getName(), copies);
            } else {
                // Book exists and is active, update fields and add copies
                bookInLibrary.setAuthor(book.getAuthor());
                bookInLibrary.setDescription(book.getDescription());
                bookInLibrary.setGenre(book.getGenre());
                bookInLibrary.setVolume(book.getVolume());
                bookInLibrary.setImageUrl(book.getImageUrl());
                bookInLibrary.setDate(book.getDate());
                
                // Set dateAdded to now when book is updated to appear as recently added
                bookInLibrary.setDateAdded(java.time.LocalDateTime.now());
                
                // Add copies to existing amounts
                bookInLibrary.setTotalAmount(bookInLibrary.getTotalAmount() + copies);
                bookInLibrary.setCurrentAmount(bookInLibrary.getCurrentAmount() + copies);
                logger.info("Updated existing book '{}' from Google Books with {} additional copies", book.getName(), copies);
            }
            
            bookRepository.update(bookInLibrary);
        } else {
            // Book doesn't exist, create new one
            book.setStatus(BookStatus.ACTIVE);
            book.setTotalAmount((long) copies);
            book.setCurrentAmount((long) copies);
            book.setRating(0.0);
            book.setDateAdded(java.time.LocalDateTime.now());

            bookRepository.save(book);
            logger.info("Successfully saved new book from Google Books: {} with {} copies", book.getName(), copies);
        }

        return true;
    }

    public void fetchAndSaveBooks() {
        if (!bookRepository.findAll().isEmpty()) return;

        deleteImages();

        ExecutorService imageDownloadExecutor = Executors.newFixedThreadPool(8);

        Set<GoogleBooksResponse> googleBooks = fetchBooks(imageDownloadExecutor);
        imageDownloadExecutor.shutdown();
        try {
            if (!imageDownloadExecutor.awaitTermination(2, TimeUnit.MINUTES)) {
                logger.warn("Image download tasks didn't finish in time");
                imageDownloadExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            imageDownloadExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Continue with saving
        List<Book> books = googleBooks.stream()
                .map(googleBook -> {
                    Book book = Mappers.mapGoogleBookToBook(googleBook);
                    long randomCopies = ThreadLocalRandom.current().nextInt(1, 16);
                    book.setStatus(BookStatus.ACTIVE);
                    book.setTotalAmount(randomCopies);
                    book.setCurrentAmount(randomCopies);
                    book.setRating(0.0);
                    book.setDateAdded(java.time.LocalDateTime.now());
                    return book;
                })
                .toList();

        logger.info("Fetched {} books from Google Books API", books.size());
        bookRepository.saveAll(books);
    }

    Set<GoogleBooksResponse> fetchBooks(ExecutorService imageDownloadExecutor) {
        Set<GoogleBooksResponse> allBooks = ConcurrentHashMap.newKeySet();

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(10, GOOGLE_BOOKS_GENRES.length));
        List<Callable<Void>> tasks = new ArrayList<>();

        for (String genre : GOOGLE_BOOKS_GENRES) {
            tasks.add(() -> {
                Set<GoogleBooksResponse> genreBooks = fetchBooksFromGenre(genre, BOOKS_PER_REQUEST, imageDownloadExecutor);
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
            executor.shutdown(); // ❗ Only shuts down genre-fetching, not image download
        }

        return allBooks;
    }

    Set<GoogleBooksResponse> fetchBooksFromGenre(String genre, int booksPerRequest, ExecutorService imageDownloadExecutor) {
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
                    String thumbnail;
                    JsonObject imageLinks = volumeInfo.getJsonObject("imageLinks");
                    if (imageLinks != null) {
                        thumbnail = imageLinks.getString("thumbnail", null);
                        if (thumbnail != null && !thumbnail.isEmpty()) {
                            imageDownloadExecutor.submit(() -> {
                                try {
                                    downloadAndSaveImage(thumbnail, safeTitle);
                                } catch (Exception ex) {
                                    logger.warn("Failed to download image for '{}': {}", title, ex.getMessage());
                                }
                            });
                        }
                    } else {
                        thumbnail = null;
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

                // Extract genre from categories array
                String genre = "Unknown";
                JsonArray categories = volumeInfo.getJsonArray("categories");
                if (categories != null && !categories.isEmpty()) {
                    String fullCategory = categories.getString(0);
                    logger.debug("Google Books category for '{}': {}", bookTitle, fullCategory);

                    // Google Books categories can be like "Fiction / Fantasy" or "Computers / Programming"
                    // Take the first part before any slash for simplicity
                    if (fullCategory.contains("/")) {
                        genre = fullCategory.split("/")[0].trim();
                    } else {
                        genre = fullCategory.trim();
                    }

                    // Map common Google Books categories to our standard genres
                    String originalGenre = genre;
                    genre = mapGoogleGenreToStandard(genre);
                    logger.debug("Mapped genre for '{}': '{}' -> '{}'", bookTitle, originalGenre, genre);
                } else {
                    logger.debug("No categories found for book: {}", bookTitle);
                }

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

    /**
     * Maps Google Books API categories to our standard application genres
     */
    private String mapGoogleGenreToStandard(String googleGenre) {
        if (googleGenre == null || googleGenre.trim().isEmpty()) {
            return "Unknown";
        }

        String genre = googleGenre.toLowerCase().trim();

        // Map common Google Books categories to our standard genres
        return switch (genre) {
            case "fiction", "literary fiction", "general fiction" -> "Fiction";
            case "science fiction", "science fiction & fantasy", "science fiction/fantasy" -> "Sci-Fi";
            case "fantasy", "fantasy fiction" -> "Fantasy";
            case "mystery", "mystery & detective", "mystery/thriller", "detective", "thriller", "crime" -> "Mystery";
            case "romance", "romantic fiction", "love stories" -> "Romance";
            case "biography", "biography & autobiography", "autobiography", "memoirs" -> "Biography";
            case "history", "historical", "world history", "american history", "european history" -> "History";
            case "non-fiction", "nonfiction", "science", "technology", "computers", "business", "self-help", "health",
                 "cooking", "travel", "reference", "education", "philosophy", "religion", "psychology", "politics" ->
                    "Non-Fiction";
            default ->
                // If we can't map it to a standard genre, return the cleaned-up version
                // Capitalize first letter of each word
                    capitalizeWords(googleGenre);
        };
    }

    /**
     * Capitalizes the first letter of each word in a string
     */
    private String capitalizeWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Unknown";
        }

        String[] words = text.toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            if (!words[i].isEmpty()) {
                result.append(words[i].substring(0, 1).toUpperCase())
                        .append(words[i].substring(1));
            }
        }

        return result.toString();
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
                        // Dzalis_Gamoghvidzeba.jpg is a special image that should never be deleted
                        // and always be in the repository
                        if (file.getFileName().toString().equals("Dzalis_Gamoghvidzeba.jpg")) {
                            continue;
                        }
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

}
