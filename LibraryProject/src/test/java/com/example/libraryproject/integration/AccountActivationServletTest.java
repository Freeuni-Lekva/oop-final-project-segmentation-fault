package com.example.libraryproject.integration;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.dto.ActivationResult;
import com.example.libraryproject.model.entity.AccountActivation;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.Role;
import com.example.libraryproject.model.enums.UserStatus;
import com.example.libraryproject.repository.AccountActivationRepository;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.service.AccountActivationService;
import com.example.libraryproject.service.MailService;
import com.example.libraryproject.service.implementation.AccountActivationServiceImpl;
import com.example.libraryproject.servlet.AccountActivationServlet;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountActivationServletTest {

    static class TestableAccountActivationServlet extends AccountActivationServlet {
        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            super.doGet(request, response);
        }
    }

    private static Server server;
    private static SessionFactory sessionFactory;
    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String OBJECT_MAPPER_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.object-mapper");
    private static final String ACCOUNT_ACTIVATION_SERVICE_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.account-activation-service");

    private static MailService mailService;
    private static AccountActivationService activationService;
    private static UserRepository userRepository;
    private static AccountActivationRepository activationRepository;
    private static User testUser;
    private static UUID validToken;

    @BeforeAll
    public static void setUpServer() throws Exception {
        sessionFactory = new Configuration()
                .configure("hibernate-test.cfg.xml")
                .buildSessionFactory();

        userRepository = new UserRepository(sessionFactory);
        activationRepository = new AccountActivationRepository(sessionFactory);
        mailService = mock(MailService.class);

        activationService = new AccountActivationServiceImpl(
                activationRepository, userRepository, mailService
        );

        testUser = new User();
        testUser.setUsername("activationtest");
        testUser.setPassword("password123");
        testUser.setMail("activation@test.com");
        testUser.setStatus(UserStatus.INACTIVE);
        testUser.setRole(Role.USER);
        userRepository.save(testUser);

        AccountActivation activation = new AccountActivation();
        validToken = UUID.randomUUID();
        activation.setToken(validToken);
        activation.setEmail(testUser.getMail());
        activation.setUser(testUser);
        activation.setExpirationDate(LocalDateTime.now().plusDays(1));
        activation.setActivated(false);
        activationRepository.save(activation);

        server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        context.addServlet(new ServletHolder(new AccountActivationServlet()), "/api/activation/*");

        context.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                resp.setContentType("text/plain");
                resp.setStatus(HttpServletResponse.SC_OK);
                Object error = req.getAttribute("error");
                Object success = req.getAttribute("success");
                Object username = req.getAttribute("username");

                if (error != null) {
                    resp.getWriter().write("ERROR: " + error.toString());
                } else if (success != null) {
                    resp.getWriter().write("SUCCESS: " + success.toString());
                    if (username != null) {
                        resp.getWriter().write(" USERNAME: " + username.toString());
                    }
                } else {
                    resp.getWriter().write("NO MESSAGE");
                }
            }
        }), "/activation-result.jsp");

        context.setAttribute(OBJECT_MAPPER_ATTRIBUTE_NAME, objectMapper);
        context.setAttribute(ACCOUNT_ACTIVATION_SERVICE_ATTRIBUTE_NAME, activationService);
        context.setAttribute("sessionFactory", sessionFactory);

        server.setHandler(context);
        server.start();

        Thread.sleep(1000);
    }

    @AfterAll
    public static void tearDownServer() throws Exception {
        if (server != null) server.stop();
        if (sessionFactory != null) sessionFactory.close();
    }

    @Test
    @Order(1)
    public void testInvalidTokenMissingParameter() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/activation/"))
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Invalid activation link - no token provided"),
                "Should return invalid token message");
    }

    @Test
    @Order(2)
    public void testResendActivationEmail_InvalidUser() throws Exception {
        String json = """
                {
                    "username": "nonexistent"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/activation/resend"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Failed to send activation email"));
    }

    @Test
    @Order(3)
    public void testUnknownPostPath() throws Exception {
        String json = """
                {
                    "username": "any"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/activation/unknown"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Unknown endpoint"));
    }

    @Test
    @Order(4)
    public void testSuccessfulActivation() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/activation/?token=" + validToken))
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("SUCCESS: Account activated successfully"));
        assertTrue(response.body().contains("USERNAME: activationtest"));
        
        Optional<User> updatedUser = userRepository.findByUsername("activationtest");
        assertTrue(updatedUser.isPresent());
        assertEquals(UserStatus.ACTIVE, updatedUser.get().getStatus());
    }

    @Test
    @Order(5)
    public void testAlreadyActivatedAccount() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/activation/?token=" + validToken))
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("ERROR: Account is already activated") || 
                   response.body().contains("ERROR: Invalid or expired activation token"));
    }

    @Test
    @Order(6)
    public void testInvalidToken() throws Exception {
        UUID invalidToken = UUID.randomUUID();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/activation/?token=" + invalidToken))
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("ERROR: Invalid or expired activation token"));
    }

    @Test
    @Order(7)
    public void testSuccessfulResendActivation() throws Exception {
        User resendUser = new User();
        resendUser.setUsername("resenduser");
        resendUser.setPassword("password123");
        resendUser.setMail("resend@test.com");
        resendUser.setStatus(UserStatus.INACTIVE);
        resendUser.setRole(Role.USER);
        userRepository.save(resendUser);

        doNothing().when(mailService).sendHtmlEmail(any(), anyString(), anyString());

        String json = """
                {
                    "username": "resenduser"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/activation/resend"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Activation email sent successfully"));

        Optional<AccountActivation> activation = activationRepository.findByUser(resendUser);
        assertTrue(activation.isPresent());
    }

    @Test
    @Order(8)
    public void testMalformedJsonInResendRequest() throws Exception {
        String malformedJson = """
                {
                    "username": "resenduser"
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/activation/resend"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(malformedJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(500, response.statusCode());
        assertTrue(response.body().contains("Failed to resend activation email"));
    }

    @Test
    @Order(9)
    public void testServiceInitializationError() throws Exception {
        TestableAccountActivationServlet servlet = new TestableAccountActivationServlet();
        
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ServletContext mockContext = mock(ServletContext.class);
        RequestDispatcher mockDispatcher = mock(RequestDispatcher.class);
        
        when(mockRequest.getParameter("token")).thenReturn("some-token");
        when(mockRequest.getServletContext()).thenReturn(mockContext);
        when(mockContext.getAttribute(ACCOUNT_ACTIVATION_SERVICE_ATTRIBUTE_NAME)).thenReturn(null);
        when(mockRequest.getRequestDispatcher("/activation-result.jsp")).thenReturn(mockDispatcher);
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(mockResponse.getWriter()).thenReturn(writer);
        
        servlet.doGet(mockRequest, mockResponse);
        
        verify(mockRequest).setAttribute(eq("error"), eq("Server configuration error - activation service unavailable"));
        verify(mockDispatcher).forward(mockRequest, mockResponse);
    }

    @Test
    @Order(10)
    public void testResendActivationWithException() throws Exception {
        AccountActivationService mockService = mock(AccountActivationService.class);
        when(mockService.resendActivationEmail(any(), any(HttpServletRequest.class)))
                .thenThrow(new RuntimeException("Simulated error"));
        
        ServletContextHandler context = (ServletContextHandler) server.getHandler();
        AccountActivationService originalService = 
                (AccountActivationService) context.getServletContext().getAttribute(ACCOUNT_ACTIVATION_SERVICE_ATTRIBUTE_NAME);
        
        try {
            context.getServletContext().setAttribute(ACCOUNT_ACTIVATION_SERVICE_ATTRIBUTE_NAME, mockService);
            
            String json = """
                    {
                        "username": "activationtest"
                    }
                    """;
    
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/activation/resend"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
    
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(400, response.statusCode());
            assertTrue(response.body().contains("Failed to send activation email"));
        } finally {
            context.getServletContext().setAttribute(ACCOUNT_ACTIVATION_SERVICE_ATTRIBUTE_NAME, originalService);
        }
    }

    @Test
    @Order(11)
    public void testNoPathInfo() throws Exception {
        String json = """
                {
                    "username": "any"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/activation"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Invalid endpoint"));
    }
}
