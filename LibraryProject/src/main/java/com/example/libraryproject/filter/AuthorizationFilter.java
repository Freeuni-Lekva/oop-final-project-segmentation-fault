package com.example.libraryproject.filter;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.enums.Role;
import com.example.libraryproject.service.implementation.AuthorizationServiceImpl;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebFilter(filterName = "BookKeeperAuthFilter", urlPatterns = {"/api/bookkeeper/*", "/api/user/*"})
public class AuthorizationFilter implements Filter {

    private static final String AUTHORIZATION_SERVICE_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.authorization-service");

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession(false);

        AuthorizationServiceImpl authorizationServiceImpl = (AuthorizationServiceImpl) request.getServletContext().getAttribute(AUTHORIZATION_SERVICE_ATTRIBUTE_NAME);

        String username;
        if (session == null) {
            logger.warn("No session found, returning 401");
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access");
            return;
        }

        boolean authenticated = false;
        username = (String) session.getAttribute("username");
        Object roleObj = session.getAttribute("role");

        if (username == null || roleObj == null) {
            logger.warn("Username or role is null in session");
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access");
            return;
        }

        String path = request.getServletPath().split("/")[2];

        try {
            Role role = Role.valueOf(roleObj.toString());
            logger.info("Parsed role enum: {}", role);

            if (path.equals("user") && role == Role.USER) {
                logger.info("Checking USER permissions for username: {}", username);
                authenticated = authorizationServiceImpl.checkUser(username);
                logger.info("USER authentication result: {}", authenticated);
            }
            else if (path.equals("bookkeeper") && role == Role.BOOKKEEPER) {
                logger.info("Checking BOOKKEEPER permissions for username: {}", username);
                authenticated = authorizationServiceImpl.checkBookkeeper(username);
                logger.info("BOOKKEEPER authentication result: {}", authenticated);
            }

        } catch (IllegalArgumentException e) {
            logger.error("Invalid role in session: {}", roleObj, e);
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid role");
            return;
        }

        if (authenticated) {
            logger.info("Authentication successful, proceeding with request");
            request.setAttribute("username", username);
            request.setAttribute("role", roleObj.toString());
            chain.doFilter(servletRequest, servletResponse);
        } else {
            logger.warn("Authentication failed for user: {} with role: {} accessing path: {}", username, roleObj, path);
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