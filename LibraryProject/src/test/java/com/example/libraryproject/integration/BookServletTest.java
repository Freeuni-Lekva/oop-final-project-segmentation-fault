package com.example.libraryproject.integration;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.Role;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.OrderRepository;
import com.example.libraryproject.repository.ReviewRepository;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.service.BookService;
import com.example.libraryproject.service.MailService;
import com.example.libraryproject.service.UserService;
import com.example.libraryproject.service.implementation.BookServiceImpl;
import com.example.libraryproject.service.implementation.UserServiceImpl;
import org.mockito.Mockito;
import com.example.libraryproject.servlet.BookServlet;
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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookServletTest {

    private static final Logger logger = LoggerFactory.getLogger(BookServletTest.class);
    private static Server server;
    private static SessionFactory sessionFactory;
    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Test data constants
    private static final String FANTASY_GENRE = "Fantasy";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_EMAIL = "test@example.com";
    
    // Generate unique username for each test run to avoid conflicts
    private String testUsername;

    @BeforeAll
    public static void setUpServer() throws Exception {
        logger.info("Starting integration test setup...");

        try {

            logger.info("Creating test SessionFactory...");
            sessionFactory = new Configuration()
                    .configure("hibernate-test.cfg.xml")
                    .buildSessionFactory();
            logger.info("SessionFactory created successfully");

            // Create embedded Jetty server for testing
            logger.info("Creating Jetty server...");
            server = new Server(8080);
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");

            // Create repositories
            BookRepository bookRepository = new BookRepository(sessionFactory);
            ReviewRepository reviewRepository = new ReviewRepository(sessionFactory);
            UserRepository userRepository = new UserRepository(sessionFactory);
            OrderRepository orderRepository = new OrderRepository(sessionFactory);
            
            // Mock mail service
            MailService mailService = Mockito.mock(MailService.class);
            
            // Create real services
            BookService bookService = new BookServiceImpl(bookRepository, reviewRepository);
            UserService userService = new UserServiceImpl(userRepository, bookRepository, reviewRepository, orderRepository, mailService);

            // Add servlets
            logger.info("Adding servlets...");
            context.addServlet(new ServletHolder(new BookServlet()), "/api/books/*");

            // Set up servlet context attributes
            context.setAttribute(ApplicationProperties.OBJECT_MAPPER_ATTRIBUTE_NAME, objectMapper);
            context.setAttribute(ApplicationProperties.BOOK_SERVICE_ATTRIBUTE_NAME, bookService);
            context.setAttribute(ApplicationProperties.USER_SERVICE_ATTRIBUTE_NAME, userService);

            server.setHandler(context);

            logger.info("Starting server...");
            server.start();
            logger.info("Server started successfully on port 8080");

            // Wait a bit for server to fully start
            Thread.sleep(1000);

        } catch (Exception e) {
            logger.error("Failed to set up test server", e);
            throw e;
        }
    }

    @AfterAll
    public static void tearDownServer() throws Exception {
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
        // Generate a unique username for this test run
        testUsername = "testuser_" + System.currentTimeMillis();
        cleanDatabase();
        setupTestData();
    }

    private void cleanDatabase() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            try {
                // Clean all tables in correct order to avoid constraint violations
                // First clear join tables
                session.createNativeQuery("DELETE FROM borrowed_books", Integer.class).executeUpdate();
                session.createNativeQuery("DELETE FROM read_books", Integer.class).executeUpdate();
                session.createNativeQuery("DELETE FROM user_reviews", Integer.class).executeUpdate();
                
                // Then delete entities
                session.createQuery("DELETE FROM Review").executeUpdate();
                session.createQuery("DELETE FROM Order").executeUpdate();
                session.createQuery("DELETE FROM Book").executeUpdate();
                session.createQuery("DELETE FROM User").executeUpdate();
                
                logger.info("Database cleaned successfully");
            } catch (Exception e) {
                logger.warn("Could not clean tables (might not exist yet): {}", e.getMessage());
                // If there's an error, try to continue with the transaction
                if (transaction.isActive()) {
                    transaction.commit();
                }
                // Create a new transaction for the next operations
                transaction = session.beginTransaction();
            }
            
            if (transaction.isActive()) {
                transaction.commit();
            }
        } catch (Exception e) {
            logger.error("Error cleaning database", e);
            throw new RuntimeException("Failed to clean database", e);
        }
    }

    private void setupTestData() {
        logger.info("Setting up test data...");
        
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            User user = createUser(testUsername, TEST_PASSWORD, TEST_EMAIL);
            session.persist(user);

            Book fantasyBook1 = createBook("Fantasy Book 1", "Author A", FANTASY_GENRE, 4.5);
            Book fantasyBook2 = createBook("Fantasy Book 2", "Author B", FANTASY_GENRE, 4.2);
            Book fictionBook = createBook("Fiction Book", "Author C", "Fiction", 4.0);
            Book unavailableBook = createBook("Unavailable Book", "Author D", "Mystery", 3.8);
            unavailableBook.setCurrentAmount(0L);
            
            session.persist(fantasyBook1);
            session.persist(fantasyBook2);
            session.persist(fictionBook);
            session.persist(unavailableBook);

            Review review1 = createReview(user, fantasyBook1, 5, "Great book!");
            Review review2 = createReview(user, fictionBook, 4, "Good book");
            
            session.persist(review1);
            session.persist(review2);

            user.getBorrowedBooks().add(fantasyBook1);
            
            transaction.commit();
            logger.info("Test data setup completed successfully");
        } catch (Exception e) {
            logger.error("Error setting up test data", e);
            throw new RuntimeException("Failed to set up test data", e);
        }
    }
    
    private User createUser(String username, String password, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setMail(email);
        user.setRole(Role.USER);
        user.setBorrowedBooks(new HashSet<>());
        user.setReadBooks(new HashSet<>());
        user.setReviews(new HashSet<>());
        return user;
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
    
    private Review createReview(User user, Book book, int rating, String comment) {
        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setRating(rating);
        review.setComment(comment);
        review.setPublicId(UUID.randomUUID());
        return review;
    }

    @Test
    @Order(1)
    public void testGetAllBooks() throws Exception {
        logger.info("Starting testGetAllBooks...");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/books/all"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Get all books response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Getting all books should succeed");
        
        JsonNode books = objectMapper.readTree(response.body());
        assertTrue(books.isArray(), "Response should be an array");
        assertEquals(4, books.size(), "Should return all 4 books");
    }
    
    @Test
    @Order(2)
    public void testGetBooksByGenre() throws Exception {
        logger.info("Starting testGetBooksByGenre...");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/books/get-books-by-genre/" + FANTASY_GENRE))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Get books by genre response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Getting books by genre should succeed");
        
        JsonNode books = objectMapper.readTree(response.body());
        assertTrue(books.isArray(), "Response should be an array");
        assertEquals(2, books.size(), "Should return 2 fantasy books");
        
        for (JsonNode book : books) {
            assertEquals(FANTASY_GENRE, book.get("genre").asText(), "All books should be of Fantasy genre");
        }
    }
    
    @Test
    @Order(3)
    public void testGetAvailableBooks() throws Exception {
        logger.info("Starting testGetAvailableBooks...");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/books/available"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Get available books response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Getting available books should succeed");
        
        JsonNode books = objectMapper.readTree(response.body());
        assertTrue(books.isArray(), "Response should be an array");

        int availableCount = 0;
        int unavailableCount = 0;
        
        for (JsonNode book : books) {
            long currentAmount = book.get("currentAmount").asLong();
            if (currentAmount > 0) {
                availableCount++;
            } else {
                unavailableCount++;
                assertEquals("Unavailable Book", book.get("name").asText(), 
                        "Only the book named 'Unavailable Book' should have currentAmount = 0");
            }
        }

        assertEquals(3, availableCount, "Should have 3 available books");
        assertEquals(1, unavailableCount, "Should have 1 unavailable book");
        assertEquals(4, books.size(), "Should return all 4 books");
    }
    
    @Test
    @Order(4)
    public void testGetBookDetails() throws Exception {
        logger.info("Starting testGetBookDetails...");

        String bookPublicId = "Fantasy_Book_1";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/books/details/" + bookPublicId))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Get book details response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Getting book details should succeed");
        
        JsonNode book = objectMapper.readTree(response.body());
        assertEquals("Fantasy Book 1", book.get("name").asText(), "Should return correct book");
        assertEquals("Author A", book.get("author").asText(), "Should return correct author");
        assertEquals(FANTASY_GENRE, book.get("genre").asText(), "Should return correct genre");
        assertEquals(4.5, book.get("rating").asDouble(), "Should return correct rating");
    }
    
    @Test
    @Order(5)
    public void testGetBookReviews() throws Exception {
        logger.info("Starting testGetBookReviews...");

        String bookPublicId = "Fantasy_Book_1";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/books/book/" + bookPublicId))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Get book reviews response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Getting book reviews should succeed");
        
        JsonNode reviews = objectMapper.readTree(response.body());
        assertTrue(reviews.isArray(), "Response should be an array");
        assertEquals(1, reviews.size(), "Should return 1 review for Fantasy Book 1");
        
        JsonNode review = reviews.get(0);
        assertEquals(5, review.get("rating").asInt(), "Should return correct rating");
        assertEquals("Great book!", review.get("comment").asText(), "Should return correct comment");
    }
    
    @Test
    @Order(6)
    public void testGetBookDetailsWithInvalidId() throws Exception {
        logger.info("Starting testGetBookDetailsWithInvalidId...");
        
        String invalidBookId = "nonexistent-book";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/books/details/" + invalidBookId))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Get book details with invalid ID response: {} - {}", response.statusCode(), response.body());
        assertEquals(500, response.statusCode(), "Should return 500 for invalid book ID");
    }
    
    @Test
    @Order(7)
    public void testGetBooksByGenreWithSorting() throws Exception {
        logger.info("Starting testGetBooksByGenreWithSorting...");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/books/get-books-by-genre/" + FANTASY_GENRE + "?sort=rating"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Get books by genre with sorting response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Getting books by genre with sorting should succeed");
        
        JsonNode books = objectMapper.readTree(response.body());
        assertTrue(books.isArray(), "Response should be an array");
        assertEquals(2, books.size(), "Should return 2 fantasy books");

        double previousRating = Double.MAX_VALUE;
        for (JsonNode book : books) {
            double currentRating = book.get("rating").asDouble();
            assertTrue(currentRating <= previousRating, "Books should be sorted by rating (highest first)");
            previousRating = currentRating;
        }
    }
    
    @Test
    @Order(8)
    public void testMissingBookIdForDetails() throws Exception {
        logger.info("Starting testMissingBookIdForDetails...");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/books/details/"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Missing book ID response: {} - {}", response.statusCode(), response.body());
        assertEquals(400, response.statusCode(), "Should return 400 for missing book ID");
        
        JsonNode errorResponse = objectMapper.readTree(response.body());
        assertTrue(errorResponse.has("error"), "Response should contain error message");
        assertEquals("Book ID is required", errorResponse.get("error").asText());
    }
    
    @Test
    @Order(9)
    public void testCheckReservation() throws Exception {
        logger.info("Starting testCheckReservation...");

        String bookPublicId = "Fantasy_Book_1";

        HttpRequest loginRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/books/check-reservation/" + bookPublicId))
                .header("Cookie", "JSESSIONID=dummy-session-id")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Check reservation response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Check reservation should succeed");
        
        JsonNode reservationStatus = objectMapper.readTree(response.body());
        assertTrue(reservationStatus.has("reserved"), "Response should contain reservation status");
        assertFalse(reservationStatus.get("reserved").asBoolean(), "Book should not be reserved by default");
    }
}
