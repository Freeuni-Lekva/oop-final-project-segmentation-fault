package com.example.libraryproject.servlet;

import com.example.libraryproject.model.dto.RegistrationRequest;
import com.example.libraryproject.model.enums.Role;
import com.example.libraryproject.service.AuthorizationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
@WebServlet(name = "LoginServlet", urlPatterns = "/api/authorization/*")
public class AuthorizationServlet extends HttpServlet {

    private final AuthorizationService authorizationService;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String path = request.getPathInfo();
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        Role role = Role.valueOf(request.getParameter("role").toUpperCase());

        RegistrationRequest registrationRequest = new RegistrationRequest(username, password, role);

        switch (path) {
            case "/register":
                authorizationService.register(registrationRequest);
                break;
            case "/login":
                authorizationService.login(registrationRequest);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }
}
