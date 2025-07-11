package com.example.libraryproject.servlet;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.dto.LoginRequest;
import com.example.libraryproject.model.dto.LoginResult;
import com.example.libraryproject.model.dto.RegistrationRequest;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.Role;
import com.example.libraryproject.service.AccountActivationService;
import com.example.libraryproject.service.AuthorizationService;
import com.example.libraryproject.service.implementation.AuthorizationServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "AuthorizationServlet", urlPatterns = "/api/authorization/*")
public class AuthorizationServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationServlet.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        ObjectMapper objectMapper = (ObjectMapper) request.getServletContext()
                .getAttribute(ApplicationProperties.OBJECT_MAPPER_ATTRIBUTE_NAME);

        AuthorizationService authorizationService = (AuthorizationServiceImpl) request.getServletContext()
                .getAttribute(ApplicationProperties.AUTHORIZATION_SERVICE_ATTRIBUTE_NAME);

        AccountActivationService accountActivationService = (AccountActivationService) request.getServletContext()
                .getAttribute(ApplicationProperties.ACCOUNT_ACTIVATION_SERVICE_ATTRIBUTE_NAME);

        if (accountActivationService == null) {
            logger.warn("AccountActivationService is not configured in the servlet context.");
        }
        String path = request.getPathInfo();
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        switch (path) {
            case "/register" -> {
                try {

                    RegistrationRequest registrationRequest = objectMapper.readValue(request.getInputStream(), RegistrationRequest.class);
                    User user = authorizationService.register(registrationRequest);

                    // Handle activation email after successful registration using dynamic URL
                    boolean emailSent = accountActivationService.createActivation(user, request);
                    if (!emailSent) {
                        logger.warn("Failed to send activation email to user: {}", user.getUsername());
                    }

                    response.setStatus(HttpServletResponse.SC_CREATED);
                    
                    // Redirect to registration success page with email parameter for resend functionality
                    String redirectPath = request.getContextPath() + "/registration-success.jsp?email=" + 
                        java.net.URLEncoder.encode(user.getMail(), StandardCharsets.UTF_8);

                    objectMapper.writeValue(response.getWriter(),
                            new JsonResponse(
                                    "Registration successful! Please check your email for activation instructions.",
                                    redirectPath)
                    );

                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    objectMapper.writeValue(response.getWriter(),
                            new JsonResponse("Registration failed: " + e.getMessage(), null)
                    );
                }
            }
            case "/login" -> {
                try {

                    LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
                    LoginResult loginResult = authorizationService.login(loginRequest);

                    String redirectPath = request.getContextPath();
                    if (loginResult.role() == Role.BOOKKEEPER) {
                        redirectPath = redirectPath + "/bookkeeper-admin.jsp";
                    } else {
                        redirectPath = redirectPath + "/main-page.jsp";
                    }

                    HttpSession session = request.getSession(true);
                    session.setAttribute("username", loginRequest.username());
                    session.setAttribute("role", loginResult.role().name());

                    response.setStatus(HttpServletResponse.SC_OK);
                    objectMapper.writeValue(response.getWriter(),
                            new JsonResponse("Login successful", redirectPath)
                    );


                } catch (IllegalArgumentException e) {
                    // Invalid credentials should return 401
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    objectMapper.writeValue(response.getWriter(),
                            new JsonResponse("Invalid credentials: " + e.getMessage(), null)
                    );
                } catch (Exception e) {
                    // Other errors still return 400
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    objectMapper.writeValue(response.getWriter(),
                            new JsonResponse("Login failed: " + e.getMessage(), null)
                    );
                }
            }
            case "/logout" -> {
                try {
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        session.invalidate();
                    }

                    String redirectPath = request.getContextPath() + "/main-page.jsp";
                    response.setStatus(HttpServletResponse.SC_OK);
                    objectMapper.writeValue(response.getWriter(),
                            new JsonResponse("Logout successful", redirectPath)
                    );

                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    objectMapper.writeValue(response.getWriter(),
                            new JsonResponse("Logout failed: " + e.getMessage(), null)
                    );
                }
            }
            default -> {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(),
                        new JsonResponse("Unknown endpoint: " + path, null)
                );
            }
        }
    }

    @Getter
    public static class JsonResponse {
        public String message;
        public String redirect;

        public JsonResponse(String message, String redirect) {
            this.message = message;
            this.redirect = redirect;
        }
    }
}
