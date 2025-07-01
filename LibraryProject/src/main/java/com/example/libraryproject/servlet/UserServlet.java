package com.example.libraryproject.servlet;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.example.libraryproject.configuration.ApplicationProperties.OBJECT_MAPPER_ATTRIBUTE_NAME;

@WebServlet(name = "UserServlet", urlPatterns = "/api/user/*")
public class UserServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        ObjectMapper objectMapper = (ObjectMapper) getServletContext().getAttribute(OBJECT_MAPPER_ATTRIBUTE_NAME);
        UserService userService = (UserService) req.getServletContext()
                .getAttribute(ApplicationProperties.USER_SERVICE_ATTRIBUTE_NAME);

        String path = req.getPathInfo();

        try {
            JsonNode jsonNode = objectMapper.readTree(req.getReader());
            String username = req.getAttribute("username").toString();
            Map<String, Object> response = new HashMap<>();

            switch (path) {
                case "/review" -> {
                    String publicId = jsonNode.get("publicId").asText();
                    int rating = jsonNode.get("rating").asInt();
                    String comment = jsonNode.get("comment").asText();

                    userService.reviewBook(username, publicId, rating, comment);
                    response.put("success", true);
                    response.put("message", "Review submitted successfully.");
                }
                case "/reserve" -> {
                    String publicId = jsonNode.get("publicId").asText();

                    userService.reserveBook(username, publicId);
                    response.put("success", true);
                    response.put("message", "Book reserved successfully.");
                }
                case "/cancel" -> {
                    String publicId = jsonNode.get("publicId").asText();

                    userService.cancelReservation(username, publicId);

                    response.put("success", true);
                    response.put("message", "Reservation cancelled successfully.");
                }
                case "/change-password" -> {
                    String oldPassword = jsonNode.get("oldPassword").asText();
                    String newPassword = jsonNode.get("newPassword").asText();

                    userService.changePassword(username, oldPassword, newPassword);
                    response.put("success", true);
                    response.put("message", "Password changed successfully.");
                }
                default -> {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.put("success", false);
                    response.put("error", "Invalid endpoint.");
                }
            }

            // Write JSON response
            objectMapper.writeValue(resp.getWriter(), response);

        } catch (IllegalArgumentException | IllegalStateException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            objectMapper.writeValue(resp.getWriter(), errorResponse);

        } catch (Exception e) {

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "An unexpected error occurred.");
            objectMapper.writeValue(resp.getWriter(), errorResponse);

        }
    }
}