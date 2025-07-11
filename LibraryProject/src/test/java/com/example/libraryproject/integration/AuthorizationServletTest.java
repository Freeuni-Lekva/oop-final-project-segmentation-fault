package com.example.libraryproject.integration;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.enums.UserStatus;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.repository.AccountActivationRepository;
import com.example.libraryproject.service.MailService;
import com.example.libraryproject.service.AccountActivationService;
import com.example.libraryproject.service.implementation.AccountActivationServiceImpl;
import com.example.libraryproject.servlet.AuthorizationServlet;
import com.example.libraryproject.service.implementation.AuthorizationServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
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
public class AuthorizationServletTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationServletTest.class);
    private static Server server;
    private static SessionFactory sessionFactory;
    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String OBJECT_MAPPER_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.object-mapper");
    private static final String AUTHORIZATION_SERVICE_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.authorization-service");
    private static final String ACCOUNT_ACTIVATION_SERVICE_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.account-activation-service");

    // Test data constants
    private static final String TEST_USERNAME = "zura";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_EMAIL = "zura@gmail.com";

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
            AccountActivationRepository accountActivationRepository = new AccountActivationRepository(sessionFactory);

            MailService mailService = Mockito.mock(MailService.class);
            
            AccountActivationService accountActivationService = new AccountActivationServiceImpl(
                accountActivationRepository, 
                userRepository, 
                mailService
            );

            AuthorizationServiceImpl authorizationService = new AuthorizationServiceImpl(userRepository, mailService);

            logger.info("Adding servlets...");
            context.addServlet(new ServletHolder(new AuthorizationServlet()), "/api/authorization/*");

            context.setAttribute(OBJECT_MAPPER_ATTRIBUTE_NAME, objectMapper);
            context.setAttribute(AUTHORIZATION_SERVICE_ATTRIBUTE_NAME, authorizationService);
            context.setAttribute(ACCOUNT_ACTIVATION_SERVICE_ATTRIBUTE_NAME, accountActivationService);
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
    public void setUp() throws Exception {
        logger.info("Setting up test case...");
        cleanDatabase();
        registerTestUser();
        activateTestUser(); // Activate the user after registration
    }

    private void cleanDatabase() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            try {
                // Delete AccountActivation first due to foreign key constraint with User
                session.createQuery("DELETE FROM AccountActivation").executeUpdate();
                session.createQuery("DELETE FROM User u").executeUpdate();
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

    private void registerTestUser() throws Exception {
        logger.info("Registering test user...");
        String registrationPayload = String.format("""
            {
                "username": "%s",
                "password": "%s",
                "mail": "%s",
                "role": "USER"
            }
            """, TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/authorization/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(registrationPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 201) {
            throw new RuntimeException("Failed to register test user: " + response.body());
        }
        logger.info("Test user registered successfully");
    }

    private void activateTestUser() {
        logger.info("Activating test user...");
        updateUserStatus(TEST_USERNAME, UserStatus.ACTIVE);
        logger.info("Test user activated successfully");
    }

    @Test
    @Order(1)
    public void testUserRegistration() throws Exception {
        logger.info("Starting testUserRegistration...");

        String registrationPayload = """
            {
                "username": "newuser",
                "password": "newpass123",
                "mail": "new@example.com",
                "role": "USER"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/authorization/register"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(registrationPayload))
                .build();

        logger.info("Sending registration request...");
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        logger.info("Registration response: {} - {}", response.statusCode(), response.body());
        assertEquals(201, response.statusCode(), "Registration should succeed with status 201 (Created)");
    }

    @Test
    @Order(2)
    public void testValidLogin() throws Exception {
        logger.info("Starting testValidLogin...");

        String loginPayload = String.format("""
            {
                "username": "%s",
                "password": "%s"
            }
            """, TEST_USERNAME, TEST_PASSWORD);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/authorization/login"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(loginPayload))
                .build();

        logger.info("Sending login request...");
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
        
        logger.info("Login response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Valid login should succeed with status 200");

        String responseBody = response.body();
        assertTrue(responseBody.contains("\"message\":\"Login successful\""), 
            "Response should contain success message");
        assertTrue(responseBody.contains("\"redirect\":\"/main-page.jsp\""), 
            "Response should contain correct redirect path for USER role");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() throws Exception {
        logger.info("Starting testInvalidLogin...");

        String loginPayload = """
            {
                "username": "nonexistent",
                "password": "wrongpass"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/authorization/login"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(loginPayload))
                .build();

        logger.info("Sending login request...");
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        logger.info("Login response: {} - {}", response.statusCode(), response.body());
        assertEquals(401, response.statusCode(), "Invalid login should return 401");
    }

    @Test
    @Order(0)
    public void testServerIsRunning() throws Exception {
        logger.info("Testing if server is running...");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/authorization/"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        logger.info("Health check response: {} - {}", response.statusCode(), response.body());

        // We expect some response (even if it's 404 or 405), not a connection error
        assertTrue(response.statusCode() > 0, "Server should be responding");
    }

    @Test
    @Order(4)
    public void testLogout() throws Exception {
        logger.info("Starting testLogout...");

        String loginPayload = String.format("""
            {
                "username": "%s",
                "password": "%s"
            }
            """, TEST_USERNAME, TEST_PASSWORD);

        HttpRequest loginRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/authorization/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(loginPayload))
                .build();

        HttpResponse<String> loginResponse = httpClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, loginResponse.statusCode(), "Login should succeed");

        String sessionCookie = loginResponse.headers()
                .firstValue("Set-Cookie")
                .orElseThrow(() -> new AssertionError("No session cookie received"));

        HttpRequest logoutRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/authorization/logout"))
                .header("Content-Type", "application/json")
                .header("Cookie", sessionCookie)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> logoutResponse = httpClient.send(logoutRequest, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Logout response: {} - {}", logoutResponse.statusCode(), logoutResponse.body());
        assertEquals(200, logoutResponse.statusCode(), "Logout should succeed");
        assertTrue(logoutResponse.body().contains("\"message\":\"Logout successful\""),
                "Response should contain success message");
        assertTrue(logoutResponse.body().contains("\"redirect\":\"/main-page.jsp\""),
                "Response should contain correct redirect path");
    }

    @Test
    @Order(5)
    public void testDuplicateUsernameRegistration() throws Exception {
        logger.info("Starting testDuplicateUsernameRegistration...");

        String registrationPayload = String.format("""
            {
                "username": "%s",
                "password": "different123",
                "mail": "different@example.com",
                "role": "USER"
            }
            """, TEST_USERNAME);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/authorization/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(registrationPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Registration response: {} - {}", response.statusCode(), response.body());
        assertEquals(400, response.statusCode(), "Duplicate username registration should fail");
        assertTrue(response.body().contains("This username already exists"),
                "Response should indicate username already exists");
    }

    @Test
    @Order(6)
    public void testDuplicateEmailRegistration() throws Exception {
        logger.info("Starting testDuplicateEmailRegistration...");

        String registrationPayload = String.format("""
            {
                "username": "different_user",
                "password": "different123",
                "mail": "%s",
                "role": "USER"
            }
            """, TEST_EMAIL);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/authorization/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(registrationPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Registration response: {} - {}", response.statusCode(), response.body());
        assertEquals(400, response.statusCode(), "Duplicate email registration should fail");
        assertTrue(response.body().contains("Account with this mail already exists"),
                "Response should indicate email already exists");
    }

    @Test
    @Order(7)
    public void testBookkeeperRegistration() throws Exception {
        logger.info("Starting testBookkeeperRegistration...");

        String registrationPayload = """
            {
                "username": "bookkeeper1",
                "password": "keeper123",
                "mail": "keeper@library.com",
                "role": "BOOKKEEPER"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/authorization/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(registrationPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Registration response: {} - {}", response.statusCode(), response.body());
        assertEquals(201, response.statusCode(), "Bookkeeper registration should succeed");
        assertTrue(response.body().contains("registration-success.jsp"),
                "Response should contain registration success redirect");

        // Activate the bookkeeper before attempting login
        updateUserStatus("bookkeeper1", UserStatus.ACTIVE);

        String loginPayload = """
            {
                "username": "bookkeeper1",
                "password": "keeper123"
            }
            """;

        HttpRequest loginRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/authorization/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(loginPayload))
                .build();

        HttpResponse<String> loginResponse = httpClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Bookkeeper login response: {} - {}", loginResponse.statusCode(), loginResponse.body());
        assertEquals(200, loginResponse.statusCode(), "Bookkeeper login should succeed");
        assertTrue(loginResponse.body().contains("\"/bookkeeper-admin.jsp\""),
                "Login response should contain bookkeeper-specific redirect path");
    }


    private void updateUserStatus(String username, UserStatus status) {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            session.createQuery("UPDATE User u SET u.status = :status WHERE u.username = :username")
                    .setParameter("status", status)
                    .setParameter("username", username)
                    .executeUpdate();
            transaction.commit();
            logger.info("Updated user {} status to {}", username, status);
        } catch (Exception e) {
            logger.error("Error updating user status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update user status", e);
        }
    }

    @Test
    @Order(8)
    public void testBannedUserLogin() throws Exception {
        logger.info("Starting testBannedUserLogin...");

        // Ban the test user
        updateUserStatus(TEST_USERNAME, UserStatus.BANNED);

        String loginPayload = String.format("""
            {
                "username": "%s",
                "password": "%s"
            }
            """, TEST_USERNAME, TEST_PASSWORD);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/authorization/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(loginPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Banned user login response: {} - {}", response.statusCode(), response.body());
        assertEquals(401, response.statusCode(), "Banned user login should fail with 401");
        assertTrue(response.body().contains("Your account has been banned"),
                "Response should indicate account is banned");
    }
}