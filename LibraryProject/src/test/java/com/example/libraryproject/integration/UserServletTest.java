package com.example.libraryproject.integration;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.OrderRepository;
import com.example.libraryproject.repository.ReviewRepository;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.repository.AccountActivationRepository;
import com.example.libraryproject.service.MailService;
import com.example.libraryproject.service.UserService;
import com.example.libraryproject.service.AccountActivationService;
import com.example.libraryproject.service.implementation.AccountActivationServiceImpl;
import com.example.libraryproject.service.implementation.AuthorizationServiceImpl;
import com.example.libraryproject.service.implementation.UserServiceImpl;
import com.example.libraryproject.servlet.AuthorizationServlet;
import com.example.libraryproject.servlet.UserServlet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import org.eclipse.jetty.servlet.FilterHolder;
import com.example.libraryproject.model.entity.User;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServletTest {

    private static final Logger logger = LoggerFactory.getLogger(UserServletTest.class);
    private static Server server;
    private static SessionFactory sessionFactory;
    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_EMAIL = "testuser@example.com";

    private static final String SECOND_USERNAME = "seconduser";
    private static final String SECOND_PASSWORD = "password123";
    private static final String SECOND_EMAIL = "seconduser@example.com";

    private static String testBookId;

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

            UserRepository userRepository = new UserRepository(sessionFactory);
            BookRepository bookRepository = new BookRepository(sessionFactory);
            OrderRepository orderRepository = new OrderRepository(sessionFactory);
            ReviewRepository reviewRepository = new ReviewRepository(sessionFactory);
            AccountActivationRepository accountActivationRepository = new AccountActivationRepository(sessionFactory);

            MailService mailService = Mockito.mock(MailService.class);
            
            // Create AccountActivationService with real implementation but mocked dependencies
            AccountActivationService accountActivationService = new AccountActivationServiceImpl(
                accountActivationRepository, 
                userRepository, 
                mailService
            );

            AuthorizationServiceImpl authorizationService = new AuthorizationServiceImpl(userRepository, mailService);
            UserService userService = new UserServiceImpl(
                    userRepository,
                    bookRepository,
                    reviewRepository,
                    orderRepository,
                    mailService
            );

            logger.info("Adding servlets...");
            context.addServlet(new ServletHolder(new AuthorizationServlet()), "/api/authorization/*");
            context.addServlet(new ServletHolder(new UserServlet()), "/api/user/*");

            context.setAttribute(ApplicationProperties.OBJECT_MAPPER_ATTRIBUTE_NAME, objectMapper);
            context.setAttribute(ApplicationProperties.AUTHORIZATION_SERVICE_ATTRIBUTE_NAME, authorizationService);
            context.setAttribute(ApplicationProperties.ACCOUNT_ACTIVATION_SERVICE_ATTRIBUTE_NAME, accountActivationService);
            context.setAttribute(ApplicationProperties.USER_SERVICE_ATTRIBUTE_NAME, userService);
            context.setAttribute("sessionFactory", sessionFactory);


            context.addFilter(new FilterHolder(new jakarta.servlet.Filter() {
                @Override
                public void init(jakarta.servlet.FilterConfig filterConfig) {}

                @Override
                public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response,
                                     jakarta.servlet.FilterChain chain) throws java.io.IOException, jakarta.servlet.ServletException {
                    jakarta.servlet.http.HttpServletRequest httpRequest = (jakarta.servlet.http.HttpServletRequest) request;
                    String cookie = httpRequest.getHeader("Cookie");

                    if (cookie != null) {
                        // For test purposes, we'll extract the username directly from the cookie
                        // In a real application, this would involve validating the session
                        if (cookie.contains("testuser")) {
                            httpRequest.setAttribute("username", "testuser");
                            logger.info("Set username attribute to 'testuser'");
                        } else if (cookie.contains("seconduser")) {
                            httpRequest.setAttribute("username", "seconduser");
                            logger.info("Set username attribute to 'seconduser'");
                        }
                    }

                    chain.doFilter(request, response);
                }

                @Override
                public void destroy() {}
            }), "/*", null);

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
    public void setUp() throws Exception {
        logger.info("Setting up test case...");
        cleanDatabase();
        registerTestUsers();
        createTestBook();
    }

    private void cleanDatabase() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            try {
                // Delete in the correct order to respect foreign key constraints
                session.createNativeQuery("DELETE FROM user_reviews", Void.class).executeUpdate();
                session.createNativeQuery("DELETE FROM reviews_table", Void.class).executeUpdate();
                session.createNativeQuery("DELETE FROM borrowed_books", Void.class).executeUpdate();
                session.createNativeQuery("DELETE FROM read_books", Void.class).executeUpdate();
                session.createNativeQuery("DELETE FROM orders_table", Void.class).executeUpdate();
                session.createNativeQuery("DELETE FROM books_table", Void.class).executeUpdate();
                // Delete AccountActivation before User due to foreign key constraint
                session.createNativeQuery("DELETE FROM account_activation_table", Void.class).executeUpdate();
                session.createNativeQuery("DELETE FROM users_table", Void.class).executeUpdate();
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

    private void registerTestUsers() throws Exception {
        logger.info("Registering test users...");

        // Register first user if needed
        if (registerUserIfNeeded(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL)) {
            logger.info("Registered test user: {}", TEST_USERNAME);
        }

        // Register second user if needed
        if (registerUserIfNeeded(SECOND_USERNAME, SECOND_PASSWORD, SECOND_EMAIL)) {
            logger.info("Registered second test user: {}", SECOND_USERNAME);
        }

        // Activate both users after registration
        activateUser(TEST_USERNAME);
        activateUser(SECOND_USERNAME);

        logger.info("Test users registration and activation completed");
    }

    private boolean registerUserIfNeeded(String username, String password, String email) throws Exception {
        // First check if user exists
        try (var session = sessionFactory.openSession()) {
            var existingUser = session.createQuery("FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getResultList();

            if (!existingUser.isEmpty()) {
                logger.info("User {} already exists, skipping registration", username);
                return false;
            }
        }

        String userPayload = String.format("""
            {
                "username": "%s",
                "password": "%s",
                "mail": "%s",
                "role": "USER"
            }
            """, username, password, email);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/authorization/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(userPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201) {
            throw new RuntimeException("Failed to register user: " + response.body());
        }

        return true;
    }
    
    private void activateUser(String username) {
        logger.info("Activating user: {}", username);
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            session.createQuery("UPDATE User u SET u.status = :status WHERE u.username = :username")
                    .setParameter("status", com.example.libraryproject.model.enums.UserStatus.ACTIVE)
                    .setParameter("username", username)
                    .executeUpdate();
            transaction.commit();
            logger.info("User {} activated successfully", username);
        } catch (Exception e) {
            logger.error("Error activating user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to activate user", e);
        }
    }

    private void createTestBook() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            Book book = new Book();
            book.setName("Test Book");
            book.setAuthor("Test Author");
            book.setDescription("Test Description");
            book.setGenre("Fiction");
            book.setDate(LocalDate.now().minusYears(1));
            book.setPublicId(UUID.randomUUID().toString());
            book.setTotalAmount(5L);
            book.setCurrentAmount(5L);
            book.setRating(0.0);
            book.setVolume(1L);
            book.setImageUrl("/images/default.jpg");

            session.persist(book);
            transaction.commit();

            testBookId = book.getPublicId();
            logger.info("Test book created with ID: {}", testBookId);
        }
    }

    private String loginAndGetSessionCookie(String username, String password) throws Exception {
        String loginPayload = String.format("""
            {
                "username": "%s",
                "password": "%s"
            }
            """, username, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/authorization/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(loginPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to login: " + response.body());
        }

        // For testing purposes, create a cookie with the username to identify the user
        // This simulates the session cookie that would normally be set by the server
        return "JSESSIONID=test-session-id-" + username;
    }

    @Test
    @Order(1)
    public void testGetUserInfo() throws Exception {
        logger.info("Starting testGetUserInfo...");

        String sessionCookie = loginAndGetSessionCookie(TEST_USERNAME, TEST_PASSWORD);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/user/" + TEST_USERNAME))
                .header("Cookie", sessionCookie)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Should get user info successfully");

        JsonNode responseJson = objectMapper.readTree(response.body());
        assertTrue(responseJson.has("user"), "Response should contain user data");
        assertEquals(TEST_USERNAME, responseJson.get("user").get("username").asText(), "Username should match");
        assertTrue(responseJson.get("isSelf").asBoolean(), "Should recognize as self");

        // Test getting another user's profile
        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/user/" + SECOND_USERNAME))
                .header("Cookie", sessionCookie)
                .GET()
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Should get other user info successfully");

        responseJson = objectMapper.readTree(response.body());
        assertTrue(responseJson.has("user"), "Response should contain user data");
        assertEquals(SECOND_USERNAME, responseJson.get("user").get("username").asText(), "Username should match");
        assertFalse(responseJson.get("isSelf").asBoolean(), "Should not recognize as self");
    }

    @Test
    @Order(2)
    public void testChangeBio() throws Exception {
        logger.info("Starting testChangeBio...");

        String sessionCookie = loginAndGetSessionCookie(TEST_USERNAME, TEST_PASSWORD);
        String newBio = "This is my new bio for testing purposes";

        String bioPayload = String.format("""
            {
                "newBio": "%s"
            }
            """, newBio);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/user/change-bio"))
                .header("Content-Type", "application/json")
                .header("Cookie", sessionCookie)
                .POST(HttpRequest.BodyPublishers.ofString(bioPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Bio change should succeed");

        JsonNode responseJson = objectMapper.readTree(response.body());
        assertTrue(responseJson.get("success").asBoolean(), "Response should indicate success");

        // Verify bio was updated by getting user info
        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/user/" + TEST_USERNAME))
                .header("Cookie", sessionCookie)
                .GET()
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        responseJson = objectMapper.readTree(response.body());
        assertEquals(newBio, responseJson.get("user").get("bio").asText(), "Bio should be updated");
    }

    @Test
    @Order(3)
    public void testChangePassword() throws Exception {
        logger.info("Starting testChangePassword...");

        String sessionCookie = loginAndGetSessionCookie(TEST_USERNAME, TEST_PASSWORD);
        String newPassword = "newPassword456";

        String passwordPayload = String.format("""
            {
                "oldPassword": "%s",
                "newPassword": "%s"
            }
            """, TEST_PASSWORD, newPassword);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/user/change-password"))
                .header("Content-Type", "application/json")
                .header("Cookie", sessionCookie)
                .POST(HttpRequest.BodyPublishers.ofString(passwordPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Password change should succeed");

        JsonNode responseJson = objectMapper.readTree(response.body());
        assertTrue(responseJson.get("success").asBoolean(), "Response should indicate success");

        // Verify by logging in with new password
        String loginPayload = String.format("""
            {
                "username": "%s",
                "password": "%s"
            }
            """, TEST_USERNAME, newPassword);

        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/authorization/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(loginPayload))
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Should be able to login with new password");
    }

    @Test
    @Order(4)
    public void testReserveBook() throws Exception {
        logger.info("Starting testReserveBook...");

        String sessionCookie = loginAndGetSessionCookie(TEST_USERNAME, TEST_PASSWORD);

        String reservePayload = String.format("""
            {
                "bookId": "%s",
                "duration": 7
            }
            """, testBookId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/user/reserve"))
                .header("Content-Type", "application/json")
                .header("Cookie", sessionCookie)
                .POST(HttpRequest.BodyPublishers.ofString(reservePayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Book reservation should succeed");

        JsonNode responseJson = objectMapper.readTree(response.body());
        assertTrue(responseJson.get("success").asBoolean(), "Response should indicate success");
        assertTrue(responseJson.get("message").asText().contains("Book reserved successfully"),
                "Message should indicate successful reservation");
    }

    @Test
    @Order(5)
    public void testReviewBook() throws Exception {
        logger.info("Starting testReviewBook...");

        // First reserve the book
        String sessionCookie = loginAndGetSessionCookie(TEST_USERNAME, TEST_PASSWORD);

        String reservePayload = String.format("""
            {
                "bookId": "%s",
                "duration": 7
            }
            """, testBookId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/user/reserve"))
                .header("Content-Type", "application/json")
                .header("Cookie", sessionCookie)
                .POST(HttpRequest.BodyPublishers.ofString(reservePayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Book reservation should succeed");

        // Add the book to the user's borrowed books collection
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            // Get the book and user
            var user = session.createQuery("FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", TEST_USERNAME)
                    .getSingleResult();

            var book = session.createQuery("FROM Book b WHERE b.publicId = :publicId", Book.class)
                    .setParameter("publicId", testBookId)
                    .getSingleResult();

            // Add book to user's borrowed books
            if (user.getBorrowedBooks() == null) {
                user.setBorrowedBooks(new java.util.HashSet<>());
            }
            user.getBorrowedBooks().add(book);

            session.merge(user);
            transaction.commit();

            logger.info("Added book to user's borrowed books collection");
        }

        // Now submit the review
        String reviewPayload = String.format("""
            {
                "publicId": "%s",
                "rating": 5,
                "comment": "This is a great test book!"
            }
            """, testBookId);

        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/user/review"))
                .header("Content-Type", "application/json")
                .header("Cookie", sessionCookie)
                .POST(HttpRequest.BodyPublishers.ofString(reviewPayload))
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Review submission should succeed");

        JsonNode responseJson = objectMapper.readTree(response.body());
        assertTrue(responseJson.get("success").asBoolean(), "Response should indicate success");
        assertEquals("Review submitted successfully.", responseJson.get("message").asText(),
                "Message should indicate successful review");
    }

    @Test
    @Order(6)
    public void testCancelReservation() throws Exception {
        logger.info("Starting testCancelReservation...");

        // First reserve a book
        String sessionCookie = loginAndGetSessionCookie(TEST_USERNAME, TEST_PASSWORD);

        String reservePayload = String.format("""
            {
                "bookId": "%s",
                "duration": 7
            }
            """, testBookId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/user/reserve"))
                .header("Content-Type", "application/json")
                .header("Cookie", sessionCookie)
                .POST(HttpRequest.BodyPublishers.ofString(reservePayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Book reservation should succeed");

        // Now cancel the reservation
        String cancelPayload = String.format("""
            {
                "bookId": "%s"
            }
            """, testBookId);

        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/user/cancel-reservation"))
                .header("Content-Type", "application/json")
                .header("Cookie", sessionCookie)
                .POST(HttpRequest.BodyPublishers.ofString(cancelPayload))
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Cancellation should succeed");
    }

    @Test
    @Order(7)
    public void testInvalidEndpoint() throws Exception {
        logger.info("Starting testInvalidEndpoint...");

        String sessionCookie = loginAndGetSessionCookie(TEST_USERNAME, TEST_PASSWORD);

        String payload = "{}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/user/invalid-endpoint"))
                .header("Content-Type", "application/json")
                .header("Cookie", sessionCookie)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Invalid endpoint should return 404");

        JsonNode responseJson = objectMapper.readTree(response.body());
        assertFalse(responseJson.get("success").asBoolean(), "Response should indicate failure");
        assertEquals("Invalid endpoint.", responseJson.get("error").asText(),
                "Error message should indicate invalid endpoint");
    }

    @Test
    @Order(8)
    public void testUnauthorizedAccess() throws Exception {
        logger.info("Starting testUnauthorizedAccess...");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/user/" + TEST_USERNAME))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(401, response.statusCode(), "Unauthenticated request should return 401");

        JsonNode responseJson = objectMapper.readTree(response.body());
        assertFalse(responseJson.get("success").asBoolean(), "Response should indicate failure");
        assertEquals("Authentication required", responseJson.get("error").asText(),
                "Error message should indicate authentication required");
    }
} 