package com.example.libraryproject.integration;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.OrderRepository;
import com.example.libraryproject.repository.ReviewRepository;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.repository.AccountActivationRepository;
import com.example.libraryproject.service.BookKeeperService;
import com.example.libraryproject.service.GoogleBooksApiService;
import com.example.libraryproject.service.MailService;
import com.example.libraryproject.service.AccountActivationService;
import com.example.libraryproject.service.implementation.AccountActivationServiceImpl;
import com.example.libraryproject.service.implementation.AuthorizationServiceImpl;
import com.example.libraryproject.service.implementation.BookKeeperServiceImpl;
import com.example.libraryproject.service.implementation.GoogleBooksApiServiceImpl;
import com.example.libraryproject.servlet.AuthorizationServlet;
import com.example.libraryproject.servlet.BookKeeperServlet;
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

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookKeeperServletTest {

    private static final Logger logger = LoggerFactory.getLogger(BookKeeperServletTest.class);
    private static Server server;
    private static SessionFactory sessionFactory;
    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BOOKKEEPER_USERNAME = "zurathebookkeeper";
    private static final String BOOKKEEPER_PASSWORD = "password123";
    private static final String BOOKKEEPER_EMAIL = "bookkeeper@example.com";
    
    private static final String USER_USERNAME = "zura";
    private static final String USER_PASSWORD = "password123";
    private static final String USER_EMAIL = "user@example.com";

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
            GoogleBooksApiService googleBooksApiService = Mockito.mock(GoogleBooksApiServiceImpl.class);
            
            // Create AccountActivationService with real implementation but mocked dependencies
            AccountActivationService accountActivationService = new AccountActivationServiceImpl(
                accountActivationRepository, 
                userRepository, 
                mailService
            );

            AuthorizationServiceImpl authorizationService = new AuthorizationServiceImpl(userRepository, mailService);
            BookKeeperService bookKeeperService = new BookKeeperServiceImpl(
                bookRepository, 
                userRepository,
                orderRepository,
                reviewRepository,
                mailService
            );

            logger.info("Adding servlets...");
            context.addServlet(new ServletHolder(new AuthorizationServlet()), "/api/authorization/*");
            context.addServlet(new ServletHolder(new BookKeeperServlet()), "/api/bookkeeper/*");

            context.setAttribute(ApplicationProperties.OBJECT_MAPPER_ATTRIBUTE_NAME, objectMapper);
            context.setAttribute(ApplicationProperties.AUTHORIZATION_SERVICE_ATTRIBUTE_NAME, authorizationService);
            context.setAttribute(ApplicationProperties.ACCOUNT_ACTIVATION_SERVICE_ATTRIBUTE_NAME, accountActivationService);
            context.setAttribute(ApplicationProperties.BOOKKEEPER_SERVICE_ATTRIBUTE_NAME, bookKeeperService);
            context.setAttribute(ApplicationProperties.GOOGLE_BOOKS_API_ATTRIBUTE_NAME, googleBooksApiService);
            context.setAttribute("sessionFactory", sessionFactory);

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
    }

    private void cleanDatabase() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            try {
                session.createQuery("DELETE FROM Book").executeUpdate();
                session.createQuery("DELETE FROM User").executeUpdate();
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

        String bookkeeperPayload = String.format("""
            {
                "username": "%s",
                "password": "%s",
                "mail": "%s",
                "role": "BOOKKEEPER"
            }
            """, BOOKKEEPER_USERNAME, BOOKKEEPER_PASSWORD, BOOKKEEPER_EMAIL);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/authorization/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(bookkeeperPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 201) {
            throw new RuntimeException("Failed to register bookkeeper: " + response.body());
        }

        String userPayload = String.format("""
            {
                "username": "%s",
                "password": "%s",
                "mail": "%s",
                "role": "USER"
            }
            """, USER_USERNAME, USER_PASSWORD, USER_EMAIL);

        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/authorization/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(userPayload))
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 201) {
            throw new RuntimeException("Failed to register user: " + response.body());
        }
        
        // Activate both users after registration
        activateUser(BOOKKEEPER_USERNAME);
        activateUser(USER_USERNAME);
        
        logger.info("Test users registered and activated successfully");
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
    
    private String loginAndGetSessionCookie(String username, String password) throws Exception {
        String loginPayload = String.format("""
            {
                "username": "%s",
                "password": "%s"
            }
            """, username, password);

        HttpRequest loginRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/authorization/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(loginPayload))
                .build();

        HttpResponse<String> loginResponse = httpClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, loginResponse.statusCode(), "Login should succeed");

        return loginResponse.headers()
                .firstValue("Set-Cookie")
                .orElseThrow(() -> new AssertionError("No session cookie received"));
    }

    @Test
    @Order(1)
    public void testAddBook() throws Exception {
        logger.info("Starting testAddBook...");

        String sessionCookie = loginAndGetSessionCookie(BOOKKEEPER_USERNAME, BOOKKEEPER_PASSWORD);

        String bookPayload = """
            {
                "title": "Test Book",
                "author": "Test Author",
                "genre": "Fiction",
                "description": "A test book for integration testing",
                "publicationDate": "2023-01-01",
                "copiesInLibrary": 5,
                "imageUrl": "http://example.com/test.jpg"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/bookkeeper/add-book"))
                .header("Content-Type", "application/json")
                .header("Cookie", sessionCookie)
                .POST(HttpRequest.BodyPublishers.ofString(bookPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Add book response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Book addition should succeed");

        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/bookkeeper/books"))
                .header("Cookie", sessionCookie)
                .GET()
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Get books response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Getting books should succeed");
        
        JsonNode books = objectMapper.readTree(response.body());
        assertTrue(books.isArray(), "Response should be an array");
        assertTrue(books.size() > 0, "Books array should not be empty");
        
        boolean bookFound = false;
        for (JsonNode book : books) {
            if ("Test Book".equals(book.get("name").asText())) {
                bookFound = true;
                assertEquals("Test Author", book.get("author").asText());
                assertEquals("Fiction", book.get("genre").asText());
                assertEquals(1, book.get("totalAmount").asInt());
                break;
            }
        }
        
        assertTrue(bookFound, "Added book should be found in the books list");
    }

    @Test
    @Order(2)
    public void testGetUsers() throws Exception {
        logger.info("Starting testGetUsers...");

        String sessionCookie = loginAndGetSessionCookie(BOOKKEEPER_USERNAME, BOOKKEEPER_PASSWORD);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/bookkeeper/users"))
                .header("Cookie", sessionCookie)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Get users response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Getting users should succeed");
        
        JsonNode users = objectMapper.readTree(response.body());
        assertTrue(users.isArray(), "Response should be an array");
        assertTrue(users.size() > 0, "Users array should not be empty");

        boolean userFound = false;
        
        for (JsonNode user : users) {
            String username = user.get("username").asText();
            if (USER_USERNAME.equals(username)) {
                userFound = true;
                // Check if mail matches what we expect
                assertEquals(USER_EMAIL, user.get("mail").asText(), "User email should match");
                break;
            }
        }
        
        assertTrue(userFound, "At least the regular user should be found in users list");
    }

    @Test
    @Order(3)
    public void testBanAndUnbanUser() throws Exception {
        logger.info("Starting testBanAndUnbanUser...");

        String sessionCookie = loginAndGetSessionCookie(BOOKKEEPER_USERNAME, BOOKKEEPER_PASSWORD);

        String banPayload = String.format("""
            {
                "username": "%s"
            }
            """, USER_USERNAME);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/bookkeeper/ban-user"))
                .header("Content-Type", "application/json")
                .header("Cookie", sessionCookie)
                .POST(HttpRequest.BodyPublishers.ofString(banPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Ban user response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Banning user should succeed");

        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/bookkeeper/users"))
                .header("Cookie", sessionCookie)
                .GET()
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        JsonNode users = objectMapper.readTree(response.body());
        boolean userIsBanned = false;
        
        for (JsonNode user : users) {
            if (USER_USERNAME.equals(user.get("username").asText())) {
                if (user.has("status") && "BANNED".equals(user.get("status").asText())) {
                    userIsBanned = true;
                }
                break;
            }
        }
        
        assertTrue(userIsBanned, "User should be banned");

        String unbanPayload = String.format("""
            {
                "username": "%s"
            }
            """, USER_USERNAME);

        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/bookkeeper/unban-user"))
                .header("Content-Type", "application/json")
                .header("Cookie", sessionCookie)
                .POST(HttpRequest.BodyPublishers.ofString(unbanPayload))
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Unban user response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Unbanning user should succeed");

        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/bookkeeper/users"))
                .header("Cookie", sessionCookie)
                .GET()
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        users = objectMapper.readTree(response.body());
        boolean userIsActive = false;
        
        for (JsonNode user : users) {
            if (USER_USERNAME.equals(user.get("username").asText())) {
                if (user.has("status") && "ACTIVE".equals(user.get("status").asText())) {
                    userIsActive = true;
                }
                break;
            }
        }
        
        assertTrue(userIsActive, "User should be active after unbanning");
    }

    @Test
    @Order(4)
    public void testDeleteBook() throws Exception {
        logger.info("Starting testDeleteBook...");

        String sessionCookie = loginAndGetSessionCookie(BOOKKEEPER_USERNAME, BOOKKEEPER_PASSWORD);

        String bookPayload = """
            {
                "title": "Book To Delete",
                "author": "Delete Author",
                "genre": "Fiction",
                "description": "A book that will be deleted",
                "publicationDate": "2023-01-01",
                "copiesInLibrary": 1,
                "imageUrl": "https://example.com/delete.jpg"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/bookkeeper/add-book"))
                .header("Content-Type", "application/json")
                .header("Cookie", sessionCookie)
                .POST(HttpRequest.BodyPublishers.ofString(bookPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Book addition should succeed");

        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/bookkeeper/books"))
                .header("Cookie", sessionCookie)
                .GET()
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        JsonNode books = objectMapper.readTree(response.body());
        String bookId = null;
        
        for (JsonNode book : books) {
            if ("Book To Delete".equals(book.get("name").asText())) {
                bookId = book.get("publicId").asText();
                break;
            }
        }
        
        assertNotNull(bookId, "Book ID should be found");

        String deletePayload = String.format("""
            {
                "bookPublicId": "%s"
            }
            """, bookId);

        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/bookkeeper/delete-book"))
                .header("Content-Type", "application/json")
                .header("Cookie", sessionCookie)
                .method("DELETE", HttpRequest.BodyPublishers.ofString(deletePayload))
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Delete book response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Book deletion should succeed");

        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/bookkeeper/books"))
                .header("Cookie", sessionCookie)
                .GET()
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        books = objectMapper.readTree(response.body());
        boolean bookFound = false;
        
        for (JsonNode book : books) {
            if ("Book To Delete".equals(book.get("name").asText())) {
                bookFound = true;
                break;
            }
        }
        
        assertFalse(bookFound, "Deleted book should not be found in the books list");
    }
    
    @Test
    @Order(5)
    public void testGetOrders() throws Exception {
        logger.info("Starting testGetOrders...");

        String sessionCookie = loginAndGetSessionCookie(BOOKKEEPER_USERNAME, BOOKKEEPER_PASSWORD);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/bookkeeper/orders"))
                .header("Cookie", sessionCookie)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Get orders response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Getting orders should succeed");

        JsonNode orders = objectMapper.readTree(response.body());
        assertTrue(orders.isArray(), "Response should be an array");
    }
}
