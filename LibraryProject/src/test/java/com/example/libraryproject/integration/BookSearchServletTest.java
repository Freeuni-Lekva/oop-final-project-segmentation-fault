package com.example.libraryproject.integration;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.ReviewRepository;
import com.example.libraryproject.service.BookService;
import com.example.libraryproject.service.implementation.BookServiceImpl;
import com.example.libraryproject.servlet.BookSearchServlet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookSearchServletTest {

    private static final Logger logger = LoggerFactory.getLogger(BookSearchServletTest.class);
    private static Server server;
    private static SessionFactory sessionFactory;
    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BOOK_SERVICE_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.book-service");

    @BeforeAll
    public static void setUpServer() throws Exception {
        logger.info("Starting integration test setup...");

        try {
            logger.info("Creating test SessionFactory...");
            sessionFactory = new Configuration()
                    .configure("hibernate-test.cfg.xml")
                    .buildSessionFactory();
            logger.info("SessionFactory created successfully");

            logger.info("Creating Jetty server...");
            server = new Server(8080);
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");

            BookRepository bookRepository = new BookRepository(sessionFactory);
            ReviewRepository reviewRepository = new ReviewRepository(sessionFactory);

            BookService bookService = new BookServiceImpl(bookRepository, reviewRepository);

            logger.info("Adding servlets...");
            context.addServlet(new ServletHolder(new BookSearchServlet()), "/api/books/search");

            context.setAttribute(BOOK_SERVICE_ATTRIBUTE_NAME, bookService);

            server.setHandler(context);

            logger.info("Starting server...");
            server.start();
            logger.info("Server started successfully on port 8080");

            Thread.sleep(1000);

        } catch (Exception e) {
            logger.error("Failed to set up test server", e);
            throw e;
        }
    }

    @AfterAll
    public static void tearDownServer() {
        logger.info("Shutting down test server...");

        try {
            if (server != null && server.isRunning()) {
                server.stop();
                logger.info("Server stopped successfully");
            }
        } catch (Exception e) {
            logger.error("Error stopping server", e);
        }

        try {
            if (sessionFactory != null) {
                sessionFactory.close();
                logger.info("SessionFactory closed successfully");
            }
        } catch (Exception e) {
            logger.error("Error closing SessionFactory", e);
        }
    }

    @BeforeEach
    public void setUp() {
        logger.info("Setting up test case...");
        cleanDatabase();
        setupTestData();
    }

    private void cleanDatabase() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            try {
                // Clean all tables in correct order to avoid constraint violations
                session.createQuery("DELETE FROM Book").executeUpdate();
                logger.info("Database cleaned successfully");
            } catch (Exception e) {
                logger.warn("Could not clean tables (might not exist yet): {}", e.getMessage());
            }
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error cleaning database", e);
            throw new RuntimeException("Failed to clean database", e);
        }
    }

    private void setupTestData() {
        logger.info("Setting up test data...");
        
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            Book book1 = createBook("Harry Potter and the Philosopher's Stone", "J.K. Rowling", "Fantasy", 4.5);
            Book book2 = createBook("Harry Potter and the Chamber of Secrets", "J.K. Rowling", "Fantasy", 4.3);
            Book book3 = createBook("The Lord of the Rings", "J.R.R. Tolkien", "Fantasy", 4.8);
            Book book4 = createBook("Pride and Prejudice", "Jane Austen", "Romance", 4.2);
            Book book5 = createBook("To Kill a Mockingbird", "Harper Lee", "Fiction", 4.7);
            Book book6 = createBook("The Great Gatsby", "F. Scott Fitzgerald", "Fiction", 4.0);
            Book book7 = createBook("The Hobbit", "J.R.R. Tolkien", "Fantasy", 4.6);
            Book book8 = createBook("1984", "George Orwell", "Dystopian", 4.4);

            //unavailable book
            book6.setCurrentAmount(0L);
            
            session.persist(book1);
            session.persist(book2);
            session.persist(book3);
            session.persist(book4);
            session.persist(book5);
            session.persist(book6);
            session.persist(book7);
            session.persist(book8);
            
            transaction.commit();
            logger.info("Test books added successfully");
        } catch (Exception e) {
            logger.error("Error setting up test data", e);
            throw new RuntimeException("Failed to set up test data", e);
        }
    }
    
    private Book createBook(String name, String author, String genre, double rating) {
        Book book = new Book();
        book.setName(name);
        book.setAuthor(author);
        book.setGenre(genre);
        book.setRating(rating);
        book.setPublicId(name.replaceAll("[^a-zA-Z0-9.\\-]", "_"));
        book.setTotalAmount(5L);
        book.setCurrentAmount(5L);
        book.setDate(LocalDate.now());
        book.setDescription("Description of " + name);
        book.setImageUrl("images/" + name.toLowerCase().replaceAll("[^a-z0-9]", "_") + ".jpg");
        book.setVolume(1L);
        return book;
    }

    @Test
    @Order(1)
    public void testSearchByTitle() throws Exception {
        logger.info("Starting testSearchByTitle...");
        
        String searchTerm = "Harry Potter";
        String encodedSearchTerm = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/books/search?term=" + encodedSearchTerm))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Search response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Search should succeed");
        
        JsonNode books = objectMapper.readTree(response.body());
        assertTrue(books.isArray(), "Response should be an array");
        assertEquals(2, books.size(), "Should find 2 Harry Potter books");

        boolean foundBook1 = false;
        boolean foundBook2 = false;
        
        for (JsonNode book : books) {
            String title = book.get("name").asText();
            if (title.contains("Philosopher's Stone")) {
                foundBook1 = true;
            } else if (title.contains("Chamber of Secrets")) {
                foundBook2 = true;
            }
        }
        
        assertTrue(foundBook1, "Should find 'Harry Potter and the Philosopher's Stone'");
        assertTrue(foundBook2, "Should find 'Harry Potter and the Chamber of Secrets'");
    }
    
    @Test
    @Order(2)
    public void testSearchByAuthor() throws Exception {
        logger.info("Starting testSearchByAuthor...");
        
        String searchTerm = "Tolkien";
        String encodedSearchTerm = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/books/search?term=" + encodedSearchTerm))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Search response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Search should succeed");
        
        JsonNode books = objectMapper.readTree(response.body());
        assertTrue(books.isArray(), "Response should be an array");
        assertEquals(2, books.size(), "Should find 2 Tolkien books");

        boolean foundLOTR = false;
        boolean foundHobbit = false;
        
        for (JsonNode book : books) {
            String title = book.get("name").asText();
            if (title.contains("Lord of the Rings")) {
                foundLOTR = true;
            } else if (title.contains("Hobbit")) {
                foundHobbit = true;
            }
        }
        
        assertTrue(foundLOTR, "Should find 'The Lord of the Rings'");
        assertTrue(foundHobbit, "Should find 'The Hobbit'");
    }
    
    @Test
    @Order(3)
    public void testSearchWithSorting() throws Exception {
        logger.info("Starting testSearchWithSorting...");
        
        String searchTerm = "Fantasy";
        String encodedSearchTerm = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
        String sortBy = "rating";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/books/search?term=" + encodedSearchTerm + "&sort=" + sortBy))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Search response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Search should succeed");
        
        JsonNode books = objectMapper.readTree(response.body());
        assertTrue(books.isArray(), "Response should be an array");

        double previousRating = Double.MAX_VALUE;
        for (JsonNode book : books) {
            double currentRating = book.get("rating").asDouble();
            assertTrue(currentRating <= previousRating, "Books should be sorted by rating (highest first)");
            previousRating = currentRating;
        }
    }
    
    @Test
    @Order(4)
    public void testSearchWithAvailabilityFilter() throws Exception {
        logger.info("Starting testSearchWithAvailabilityFilter...");

        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            Book unavailableBook = createBook("Fiction Test Book", "Test Author", "Test Genre", 3.5);
            unavailableBook.setCurrentAmount(0L);
            session.persist(unavailableBook);
            transaction.commit();
        }
        
        String searchTerm = "Fiction";
        String encodedSearchTerm = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
        String availability = "available";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/books/search?term=" + encodedSearchTerm + "&availability=" + availability))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Search response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Search should succeed");
        
        JsonNode books = objectMapper.readTree(response.body());
        assertTrue(books.isArray(), "Response should be an array");

        for (JsonNode book : books) {
            long currentAmount = book.get("currentAmount").asLong();
            assertTrue(currentAmount > 0, "Only available books should be returned");

            assertNotEquals("The Great Gatsby", book.get("name").asText(), 
                    "Unavailable book 'The Great Gatsby' should not be in results");

            assertNotEquals("Fiction Test Book", book.get("name").asText(), 
                    "Unavailable book 'Fiction Test Book' should not be in results");
        }
    }
    
    @Test
    @Order(5)
    public void testSearchWithInvalidRequest() throws Exception {
        logger.info("Starting testSearchWithInvalidRequest...");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/books/search?term="))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Search response: {} - {}", response.statusCode(), response.body());
        assertEquals(400, response.statusCode(), "Should return 400 Bad Request for empty search term");
        
        JsonNode errorResponse = objectMapper.readTree(response.body());
        assertTrue(errorResponse.has("error"), "Response should contain error message");
        assertEquals("Search term is required", errorResponse.get("error").asText());

        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/books/search"))
                .GET()
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Search response: {} - {}", response.statusCode(), response.body());
        assertEquals(400, response.statusCode(), "Should return 400 Bad Request for missing search term");
    }
} 