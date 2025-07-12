package com.example.libraryproject.integration;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.entity.Book;
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
import com.example.libraryproject.servlet.ReviewServlet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReviewServletTest {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServletTest.class);
    private static Server server;
    private static SessionFactory sessionFactory;
    private static String BASE_URL; // Will be set dynamically based on server port
    private static final CookieManager cookieManager = new CookieManager();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .cookieHandler(cookieManager)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String OBJECT_MAPPER_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.object-mapper");
    private static final String USER_SERVICE_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.user-service");
    private static final String BOOK_SERVICE_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.book-service");

    // Test data constants
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_BOOK_ID = "test-book";
    private static final String BANNED_USERNAME = "banneduser";

    @BeforeAll
    public static void setUpServer() throws Exception {
        logger.info("Starting integration test setup...");

        try {
            // Create test SessionFactory
            logger.info("Creating test SessionFactory...");
            sessionFactory = new Configuration()
                    .configure("hibernate-test.cfg.xml")
                    .buildSessionFactory();
            logger.info("SessionFactory created successfully");

            // Create embedded Jetty server for testing
            logger.info("Creating Jetty server...");
            server = new Server(0); // Use port 0 to get any available port
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");

            // Create repositories
            BookRepository bookRepository = new BookRepository(sessionFactory);
            ReviewRepository reviewRepository = new ReviewRepository(sessionFactory);
            UserRepository userRepository = new UserRepository(sessionFactory);
            OrderRepository orderRepository = new OrderRepository(sessionFactory);
            
            // Mock services
            MailService mailService = mock(MailService.class);
            
            // Create real services
            BookService bookService = new BookServiceImpl(bookRepository, reviewRepository);
            UserService userService = new UserServiceImpl(userRepository, bookRepository, reviewRepository, orderRepository, mailService);

            // Add the ReviewServlet
            context.addServlet(new ServletHolder(new ReviewServlet()), "/api/reviews/*");
            
            // Add the session setter servlet
            context.addServlet(new ServletHolder(new SessionSetterServlet()), "/set-session");

            // Set up servlet context attributes
            context.setAttribute(OBJECT_MAPPER_ATTRIBUTE_NAME, objectMapper);
            context.setAttribute(BOOK_SERVICE_ATTRIBUTE_NAME, bookService);
            context.setAttribute(USER_SERVICE_ATTRIBUTE_NAME, userService);
            context.setAttribute("sessionFactory", sessionFactory);

            server.setHandler(context);

            logger.info("Starting server...");
            server.start();
            
            ServerConnector connector = (ServerConnector) server.getConnectors()[0];
            int port = connector.getLocalPort();
            BASE_URL = "http://localhost:" + port;
            logger.info("Server started successfully on port {}", port);

            // Wait a bit for server to fully start
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
        // Clear cookies before each test
        cookieManager.getCookieStore().removeAll();
        
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
            
            // Create test users
            User user = createUser(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL, Role.USER);
            User bannedUser = createUser(BANNED_USERNAME, TEST_PASSWORD, "banned@example.com", Role.USER);
            bannedUser.setStatus(com.example.libraryproject.model.enums.UserStatus.BANNED);
            
            session.persist(user);
            session.persist(bannedUser);
            
            // Create test book
            Book book = createBook("Test Book", "Test Author", "Fiction", 4.0);
            book.setPublicId(TEST_BOOK_ID);
            session.persist(book);
            
            // Add book to user's read books to allow reviewing
            user.getReadBooks().add(book);
            
            transaction.commit();
            logger.info("Test data setup completed successfully");
        } catch (Exception e) {
            logger.error("Error setting up test data", e);
            throw new RuntimeException("Failed to set up test data", e);
        }
    }
    
    private User createUser(String username, String password, String email, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setMail(email);
        user.setRole(role);
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
        book.setDateAdded(LocalDateTime.now());
        book.setDescription("Description of " + name);
        book.setImageUrl("images/" + name.toLowerCase().replaceAll("[^a-z0-9]", "_") + ".jpg");
        book.setVolume(1L);
        return book;
    }
    
    private void createSessionCookie(String username) {
        HttpCookie sessionCookie = new HttpCookie("JSESSIONID", "test-session-id");
        sessionCookie.setPath("/");
        sessionCookie.setDomain("localhost");
        cookieManager.getCookieStore().add(URI.create(BASE_URL), sessionCookie);
        
        // Set the session attribute on the server
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/set-session?username=" + username))
                    .GET()
                    .build();
            
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            logger.error("Failed to set session attribute", e);
        }
    }

    @Test
    @Order(1)
    public void testSubmitReviewUnauthenticated() throws Exception {
        logger.info("Starting testSubmitReviewUnauthenticated...");
        
        String requestBody = String.format("bookId=%s&rating=5&reviewText=%s",
                TEST_BOOK_ID,
                URLEncoder.encode("Great book!", StandardCharsets.UTF_8));
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/reviews/submit"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Submit review unauthenticated response: {} - {}", response.statusCode(), response.body());
        assertEquals(401, response.statusCode(), "Unauthenticated review submission should return 401");
        
        JsonNode jsonResponse = objectMapper.readTree(response.body());
        assertFalse(jsonResponse.get("success").asBoolean(), "Response should indicate failure");
        assertTrue(jsonResponse.get("message").asText().contains("log in"), "Message should mention logging in");
    }
    
    @Test
    @Order(2)
    public void testSubmitReviewMissingFields() throws Exception {
        logger.info("Starting testSubmitReviewMissingFields...");
        
        // Create a session with the test username
        createSessionCookie(TEST_USERNAME);
        
        // Missing rating
        String requestBody = String.format("bookId=%s&reviewText=%s",
                TEST_BOOK_ID,
                URLEncoder.encode("Great book!", StandardCharsets.UTF_8));
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/reviews/submit"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Submit review missing fields response: {} - {}", response.statusCode(), response.body());
        assertEquals(400, response.statusCode(), "Missing fields should return 400");
        
        JsonNode jsonResponse = objectMapper.readTree(response.body());
        assertFalse(jsonResponse.get("success").asBoolean(), "Response should indicate failure");
        assertTrue(jsonResponse.get("message").asText().contains("Missing"), "Message should mention missing fields");
    }
    
    @Test
    @Order(3)
    public void testSubmitReviewInvalidRating() throws Exception {
        logger.info("Starting testSubmitReviewInvalidRating...");
        
        // Create a session with the test username
        createSessionCookie(TEST_USERNAME);
        
        // Invalid rating (out of range)
        String requestBody = String.format("bookId=%s&rating=6&reviewText=%s",
                TEST_BOOK_ID,
                URLEncoder.encode("Great book!", StandardCharsets.UTF_8));
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/reviews/submit"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Submit review invalid rating response: {} - {}", response.statusCode(), response.body());
        assertEquals(400, response.statusCode(), "Invalid rating should return 400");
        
        JsonNode jsonResponse = objectMapper.readTree(response.body());
        assertFalse(jsonResponse.get("success").asBoolean(), "Response should indicate failure");
        assertTrue(jsonResponse.get("message").asText().contains("between 1 and 5"), "Message should mention valid rating range");
    }
    
    @Test
    @Order(4)
    public void testSubmitReviewEmptyText() throws Exception {
        logger.info("Starting testSubmitReviewEmptyText...");
        
        // Create a session with the test username
        createSessionCookie(TEST_USERNAME);
        
        // Empty review text
        String requestBody = String.format("bookId=%s&rating=5&reviewText=%s",
                TEST_BOOK_ID,
                URLEncoder.encode("", StandardCharsets.UTF_8));
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/reviews/submit"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Submit review empty text response: {} - {}", response.statusCode(), response.body());
        assertEquals(400, response.statusCode(), "Empty review text should return 400");
        
        JsonNode jsonResponse = objectMapper.readTree(response.body());
        assertFalse(jsonResponse.get("success").asBoolean(), "Response should indicate failure");
        assertTrue(jsonResponse.get("message").asText().contains("empty"), "Message should mention empty text");
    }
    
    @Test
    @Order(5)
    public void testSubmitReviewBannedUser() throws Exception {
        logger.info("Starting testSubmitReviewBannedUser...");
        
        // Create a session with the banned username
        createSessionCookie(BANNED_USERNAME);
        
        String requestBody = String.format("bookId=%s&rating=5&reviewText=%s",
                TEST_BOOK_ID,
                URLEncoder.encode("Great book!", StandardCharsets.UTF_8));
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/reviews/submit"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Submit review banned user response: {} - {}", response.statusCode(), response.body());
        assertEquals(400, response.statusCode(), "Banned user should return 400");
        
        JsonNode jsonResponse = objectMapper.readTree(response.body());
        assertFalse(jsonResponse.get("success").asBoolean(), "Response should indicate failure");
        assertTrue(jsonResponse.get("message").asText().contains("banned"), "Message should mention banned status");
    }
    
    @Test
    @Order(6)
    public void testSubmitReviewNonexistentBook() throws Exception {
        logger.info("Starting testSubmitReviewNonexistentBook...");
        
        // Create a session with the test username
        createSessionCookie(TEST_USERNAME);
        
        String requestBody = String.format("bookId=%s&rating=5&reviewText=%s",
                "nonexistent-book",
                URLEncoder.encode("Great book!", StandardCharsets.UTF_8));
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/reviews/submit"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Submit review nonexistent book response: {} - {}", response.statusCode(), response.body());
        assertEquals(404, response.statusCode(), "Nonexistent book should return 404");
        
        JsonNode jsonResponse = objectMapper.readTree(response.body());
        assertFalse(jsonResponse.get("success").asBoolean(), "Response should indicate failure");
    }
    
    @Test
    @Order(7)
    public void testSubmitReviewSuccess() throws Exception {
        logger.info("Starting testSubmitReviewSuccess...");
        
        // Create a session with the test username
        createSessionCookie(TEST_USERNAME);
        
        String requestBody = String.format("bookId=%s&rating=5&reviewText=%s",
                TEST_BOOK_ID,
                URLEncoder.encode("Great book!", StandardCharsets.UTF_8));
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/reviews/submit"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Submit review success response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Successful review submission should return 200");
        
        JsonNode jsonResponse = objectMapper.readTree(response.body());
        assertTrue(jsonResponse.get("success").asBoolean(), "Response should indicate success");
        assertTrue(jsonResponse.get("message").asText().contains("success"), "Message should mention success");
        
        // Verify that the review was actually saved in the database
        try (var session = sessionFactory.openSession()) {
            long reviewCount = session.createQuery("SELECT COUNT(*) FROM Review", Long.class).getSingleResult();
            assertEquals(1, reviewCount, "There should be one review in the database");
        }
    }
    
    @Test
    @Order(8)
    public void testSubmitDuplicateReview() throws Exception {
        logger.info("Starting testSubmitDuplicateReview...");
        
        // Create a session with the test username
        createSessionCookie(TEST_USERNAME);
        
        // First, submit an initial review
        String initialReviewBody = String.format("bookId=%s&rating=5&reviewText=%s",
                TEST_BOOK_ID,
                URLEncoder.encode("Initial review!", StandardCharsets.UTF_8));
        
        HttpRequest initialRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/reviews/submit"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(initialReviewBody))
                .build();

        HttpResponse<String> initialResponse = httpClient.send(initialRequest, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Initial review response: {} - {}", initialResponse.statusCode(), initialResponse.body());
        assertEquals(200, initialResponse.statusCode(), "Initial review submission should return 200");
        
        // Verify the review was saved in the database
        try (var session = sessionFactory.openSession()) {
            long reviewCount = session.createQuery("SELECT COUNT(*) FROM Review", Long.class).getSingleResult();
            logger.info("Number of reviews in database after initial submission: {}", reviewCount);
            
            // Check if the user has reviews
            var user = session.createQuery("FROM User WHERE username = :username", User.class)
                    .setParameter("username", TEST_USERNAME)
                    .getSingleResult();
            logger.info("User {} has {} reviews", user.getUsername(), user.getReviews().size());
        }
        
        // Now try to submit a duplicate review
        String duplicateReviewBody = String.format("bookId=%s&rating=4&reviewText=%s",
                TEST_BOOK_ID,
                URLEncoder.encode("Another review!", StandardCharsets.UTF_8));
        
        HttpRequest duplicateRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/reviews/submit"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(duplicateReviewBody))
                .build();

        HttpResponse<String> duplicateResponse = httpClient.send(duplicateRequest, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Submit duplicate review response: {} - {}", duplicateResponse.statusCode(), duplicateResponse.body());
        assertEquals(400, duplicateResponse.statusCode(), "Duplicate review should return 400");
        
        JsonNode jsonResponse = objectMapper.readTree(duplicateResponse.body());
        assertFalse(jsonResponse.get("success").asBoolean(), "Response should indicate failure");
        assertTrue(jsonResponse.get("message").asText().contains("already reviewed"), "Message should mention already reviewed");
    }
    
    @Test
    @Order(9)
    public void testInvalidEndpoint() throws Exception {
        logger.info("Starting testInvalidEndpoint...");
        
        // Create a session with the test username
        createSessionCookie(TEST_USERNAME);
        
        // Use POST instead of GET since ReviewServlet only handles POST requests
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/reviews/invalid"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Invalid endpoint response: {} - {}", response.statusCode(), response.body());
        assertEquals(404, response.statusCode(), "Invalid endpoint should return 404");
    }
    
    /**
     * A mock servlet to set session attributes for testing
     */
    public static class SessionSetterServlet extends jakarta.servlet.http.HttpServlet {
        @Override
        protected void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) 
                throws java.io.IOException {
            String username = req.getParameter("username");
            if (username != null && !username.isEmpty()) {
                req.getSession(true).setAttribute("username", username);
                resp.setStatus(200);
                resp.getWriter().write("Session set for " + username);
            } else {
                resp.setStatus(400);
                resp.getWriter().write("Username parameter is required");
            }
        }
    }
}
