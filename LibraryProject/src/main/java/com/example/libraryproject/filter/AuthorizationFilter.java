package com.example.libraryproject.filter;

import com.example.libraryproject.model.enums.Role;
import com.example.libraryproject.service.AuthorizationService;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import static com.example.libraryproject.configuration.ApplicationProperties.AUTHORIZATION_SERVICE_ATTRIBUTE_NAME;

@WebFilter(filterName = "BookKeeperAuthFilter", urlPatterns = {"/api/bookkeeper/*", "/api/user/*"})
public class AuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession(false);

        AuthorizationService authorizationService = (AuthorizationService) request.getServletContext().getAttribute(AUTHORIZATION_SERVICE_ATTRIBUTE_NAME);

        String username;
        if (session == null) {
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access");
            return;
        }
        boolean authenticated = false;
        username = (String) session.getAttribute("username");

        String path = request.getServletPath().split("/")[2];

        Role role =  Role.valueOf(session.getAttribute("role").toString());
        if (path.equals("user") && role == Role.USER) {
            authenticated = authorizationService.checkUser(username);
        }
        else if (path.equals("bookkeeper") && role == Role.BOOKKEEPER) {
            authenticated = authorizationService.checkBookkeeper(username);
        }
        if (authenticated) {
            request.setAttribute("username", username);
            request.setAttribute("role", role.name());
            chain.doFilter(servletRequest, servletResponse);
        } else {
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access");
        }
    }
    private void sendJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        String json = String.format("{\"success\": false, \"error\": \"%s\"}", message);
        response.getWriter().write(json);
    }
}