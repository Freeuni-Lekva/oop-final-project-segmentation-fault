package com.example.libraryproject.servlet;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.dto.LoginRequest;
import com.example.libraryproject.model.dto.RegistrationRequest;
import com.example.libraryproject.model.enums.Role;
import com.example.libraryproject.service.AuthorizationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/authorization/*")
public class AuthorizationServlet extends HttpServlet {


    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException  {

        AuthorizationService authorizationService = (AuthorizationService) request.getServletContext()
                .getAttribute(ApplicationProperties.AUTHORIZATION_SERVICE_ATTRIBUTE_NAME);

        String path = request.getPathInfo();
        String username = request.getParameter("username");
        String password = request.getParameter("password");


        switch (path) {
            case "/register":
                Role role = Role.valueOf(request.getParameter("role").toUpperCase());
                authorizationService.register(
                        new RegistrationRequest(username,
                        password,
                        role));
                response.setStatus(HttpServletResponse.SC_OK);
                break;
            case "/login":
                authorizationService.login(
                        new LoginRequest(username, password)
                );
                response.setStatus(HttpServletResponse.SC_OK);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }
}
