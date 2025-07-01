package com.example.libraryproject.servlet;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.dto.LoginRequest;
import com.example.libraryproject.model.dto.LoginResult;
import com.example.libraryproject.model.dto.RegistrationRequest;
import com.example.libraryproject.model.enums.Role;
import com.example.libraryproject.service.AuthorizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "AuthorizationServlet", urlPatterns = "/api/authorization/*")
public class AuthorizationServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        ObjectMapper objectMapper = (ObjectMapper) request.getServletContext()
                .getAttribute(ApplicationProperties.OBJECT_MAPPER_ATTRIBUTE_NAME);

        AuthorizationService authorizationService = (AuthorizationService) request.getServletContext()
                .getAttribute(ApplicationProperties.AUTHORIZATION_SERVICE_ATTRIBUTE_NAME);

        String path = request.getPathInfo();
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        switch (path) {
            case "/register" -> {
                try {

                    RegistrationRequest registrationRequest = objectMapper.readValue(request.getInputStream(), RegistrationRequest.class);
                    authorizationService.register(registrationRequest);

                    response.setStatus(HttpServletResponse.SC_CREATED);
                    String redirectPath = request.getContextPath();

                    if (registrationRequest.role() == Role.BOOKKEEPER) {
                        redirectPath = redirectPath + "/bookkeeper-admin.jsp";
                    } else redirectPath = redirectPath + "/main-page.jsp";

                    HttpSession session = request.getSession(true);
                    session.setAttribute("username", registrationRequest.username());
                    session.setAttribute("role", registrationRequest.role().name());

                    objectMapper.writeValue(response.getWriter(),
                            new JsonResponse(
                                    "Bookkeeper registered successfully",
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


                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    objectMapper.writeValue(response.getWriter(),
                            new JsonResponse("Login failed: " + e.getMessage(), null)
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
