package com.example.libraryproject.servlet;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.dto.ActivationRequest;
import com.example.libraryproject.model.dto.ActivationResult;
import com.example.libraryproject.service.AccountActivationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AccountActivationServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AccountActivationServlet.class);
    private static final String ACCOUNT_ACTIVATION_SERVICE_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.account-activation-service");
    private static final String OBJECT_MAPPER_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.object-mapper");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        
        if (token == null || token.trim().isEmpty()) {
            request.setAttribute("error", "Invalid activation link - no token provided");
            response.setStatus(HttpServletResponse.SC_OK);
            forwardToActivationPage(request, response);
            return;
        }

        AccountActivationService activationService = (AccountActivationService) request.getServletContext()
                .getAttribute(ACCOUNT_ACTIVATION_SERVICE_ATTRIBUTE_NAME);

        if (activationService == null) {
            logger.error("AccountActivationService is null - service not properly initialized");
            request.setAttribute("error", "Server configuration error - activation service unavailable");
            forwardToActivationPage(request, response);
            return;
        }

        ActivationResult result = activationService.activateAccount(token.trim());
        
        if (result.success()) {
            request.setAttribute("success", result.message());
            request.setAttribute("username", result.username());
        } else {
            request.setAttribute("error", result.message());
        }
        
        forwardToActivationPage(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = (ObjectMapper) request.getServletContext()
                .getAttribute(OBJECT_MAPPER_ATTRIBUTE_NAME);

        AccountActivationService activationService = (AccountActivationService) request.getServletContext()
                .getAttribute(ACCOUNT_ACTIVATION_SERVICE_ATTRIBUTE_NAME);

        String path = request.getPathInfo();
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        if (path == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(response.getWriter(),
                    new JsonResponse("Invalid endpoint", null)
            );
            return;
        }

        switch (path) {
            case "/resend" -> {
                try {
                    ActivationRequest activationRequest = objectMapper.readValue(request.getInputStream(), ActivationRequest.class);
                    boolean emailSent = activationService.resendActivationEmail(activationRequest, request);

                    if (emailSent) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        objectMapper.writeValue(response.getWriter(),
                                new JsonResponse("Activation email sent successfully", null)
                        );
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        objectMapper.writeValue(response.getWriter(),
                                new JsonResponse("Failed to send activation email. User may not exist or already be active.", null)
                        );
                    }

                } catch (Exception e) {
                    logger.error("Error resending activation email: {}", e.getMessage(), e);
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    objectMapper.writeValue(response.getWriter(),
                            new JsonResponse("Failed to resend activation email: " + e.getMessage(), null)
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

    private void forwardToActivationPage(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/activation-result.jsp");
        dispatcher.forward(request, response);
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