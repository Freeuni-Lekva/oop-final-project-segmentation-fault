package com.example.libraryproject.integration;

import com.example.libraryproject.servlet.ProfileViewServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProfileViewServletTest {

    private static final Logger logger = LoggerFactory.getLogger(ProfileViewServletTest.class);
    private static Server server;
    private static final String BASE_URL = "http://localhost:8080";
    private static final CookieManager cookieManager = new CookieManager();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .cookieHandler(cookieManager)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    private static final String TEST_USERNAME = "testuser";

    @BeforeAll
    public static void setUpServer() throws Exception {
        logger.info("Starting integration test setup...");

        try {
            // Create embedded Jetty server for testing
            logger.info("Creating Jetty server...");
            server = new Server(8080);
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            
            // Add the ProfileViewServlet
            context.addServlet(new ServletHolder(new ProfileViewServlet()), "/user/*");
            
            // Add mock JSP servlets to handle forwarded requests
            context.addServlet(new ServletHolder(new MockJspServlet("login.jsp")), "/login.jsp");
            context.addServlet(new ServletHolder(new MockJspServlet("my-books.jsp")), "/my-books.jsp");
            context.addServlet(new ServletHolder(new MockJspServlet("profile.html")), "/profile.html");
            
            // Add the session setter servlet
            context.addServlet(new ServletHolder(new SessionSetterServlet()), "/set-session");

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
    }
    
    @BeforeEach
    public void setUp() {
        // Clear cookies before each test
        cookieManager.getCookieStore().removeAll();
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
    public void testUnauthenticatedAccess() throws Exception {
        logger.info("Starting testUnauthenticatedAccess...");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/user/" + TEST_USERNAME))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Unauthenticated access response: {} - {}", response.statusCode(), response.body());
        assertEquals(302, response.statusCode(), "Unauthenticated access should redirect");
        
        String location = response.headers().firstValue("Location").orElse("");
        assertTrue(location.endsWith("/login.jsp"), "Should redirect to login page");
    }
    
    @Test
    @Order(2)
    public void testAccessOwnProfile() throws Exception {
        logger.info("Starting testAccessOwnProfile...");
        
        // Create a session with the test username
        createSessionCookie(TEST_USERNAME);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/user/" + TEST_USERNAME))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Access own profile response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Access to own profile should succeed");
        
        // The MockJspServlet should return the name of the JSP file
        assertEquals("profile.html", response.body(), "Should forward to profile.html");
    }
    
    @Test
    @Order(3)
    public void testAccessOtherProfile() throws Exception {
        logger.info("Starting testAccessOtherProfile...");
        
        // Create a session with the test username
        createSessionCookie(TEST_USERNAME);
        
        // Try to access another user's profile
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/user/otheruser"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Access other profile response: {} - {}", response.statusCode(), response.body());
        assertEquals(403, response.statusCode(), "Access to other user's profile should be forbidden");
    }
    
    @Test
    @Order(4)
    public void testAccessMyBooks() throws Exception {
        logger.info("Starting testAccessMyBooks...");
        
        // Create a session with the test username
        createSessionCookie(TEST_USERNAME);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/user/" + TEST_USERNAME + "/my-books"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Access my-books response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Access to my-books should succeed");
        
        // The MockJspServlet should return the name of the JSP file
        assertEquals("my-books.jsp", response.body(), "Should forward to my-books.jsp");
    }
    
    @Test
    @Order(5)
    public void testProfileWithGridView() throws Exception {
        logger.info("Starting testProfileWithGridView...");
        
        // Create a session with the test username
        createSessionCookie(TEST_USERNAME);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/user/" + TEST_USERNAME + "?view=grid"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Profile with grid view response: {} - {}", response.statusCode(), response.body());
        assertEquals(200, response.statusCode(), "Access to profile with grid view should succeed");
        
        // The MockJspServlet should return the name of the JSP file
        assertEquals("my-books.jsp", response.body(), "Should forward to my-books.jsp");
    }
    
    @Test
    @Order(6)
    public void testInvalidPath() throws Exception {
        logger.info("Starting testInvalidPath...");
        
        // Create a session with the test username
        createSessionCookie(TEST_USERNAME);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/user/"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Invalid path response: {} - {}", response.statusCode(), response.body());
        assertEquals(404, response.statusCode(), "Invalid path should return 404");
    }
    
    /**
     * A mock servlet that returns its name as the response body
     */
    private static class MockJspServlet extends jakarta.servlet.http.HttpServlet {
        private final String name;
        
        public MockJspServlet(String name) {
            this.name = name;
        }
        
        @Override
        protected void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) 
                throws jakarta.servlet.ServletException, java.io.IOException {
            resp.setContentType("text/plain");
            resp.getWriter().write(name);
        }
    }
    
    /**
     * A mock servlet to set session attributes for testing
     */
    @jakarta.servlet.annotation.WebServlet("/set-session")
    public static class SessionSetterServlet extends jakarta.servlet.http.HttpServlet {
        @Override
        protected void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) 
                throws jakarta.servlet.ServletException, java.io.IOException {
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
