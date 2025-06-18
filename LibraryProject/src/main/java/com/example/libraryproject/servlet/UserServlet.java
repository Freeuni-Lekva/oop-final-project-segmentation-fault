package com.example.libraryproject.servlet;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.dto.LoginRequest;
import com.example.libraryproject.model.dto.RegistrationRequest;
import com.example.libraryproject.model.enums.Role;
import com.example.libraryproject.service.AuthorizationService;
import com.example.libraryproject.service.UserService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@WebServlet(name = "UserServlet", urlPatterns = "/api/user/*")
public class UserServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserService userService = (UserService) req.getServletContext()
                .getAttribute(ApplicationProperties.USER_SERVICE_ATTRIBUTE_NAME);

        String path = req.getPathInfo();
        String username = req.getParameter("username");

        try {
            switch (path) {
                case "/review" -> {
                    String publicId = req.getParameter("publicId");
                    int rating = Integer.parseInt(req.getParameter("rating"));
                    String comment = req.getParameter("comment");
                    userService.reviewBook(username, publicId, rating, comment);
                    //resp.getWriter().write("Review submitted successfully.");
                }
                case "/reserve" -> {
                    String publicId = req.getParameter("publicId");
                    userService.reserveBook(username, publicId);
                    //resp.getWriter().write("Book reserved successfully.");
                }
                case "/cancel" -> {
                    String publicId = req.getParameter("publicId");
                    userService.cancelReservation(username, publicId);
                    //resp.getWriter().write("Reservation cancelled successfully.");
                }
                case "/change-password" -> {
                    String oldPassword = req.getParameter("oldPassword");
                    String newPassword = req.getParameter("newPassword");
                    userService.changePassword(username, oldPassword, newPassword);
                    //resp.getWriter().write("Password changed successfully.");
                }
                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid endpoint.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
