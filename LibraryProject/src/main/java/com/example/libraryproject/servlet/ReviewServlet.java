package com.example.libraryproject.servlet;

import com.example.libraryproject.service.BookService;
import com.example.libraryproject.service.UserService;
import com.example.libraryproject.service.implementation.BookServiceImpl;
import com.example.libraryproject.service.implementation.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import static com.example.libraryproject.configuration.ApplicationProperties.*;

@WebServlet(name = "ReviewServlet", urlPatterns = {"/api/reviews/*"})
public class ReviewServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ObjectMapper objectMapper = (ObjectMapper) getServletContext().getAttribute(OBJECT_MAPPER_ATTRIBUTE_NAME);
        BookService bookService = (BookServiceImpl) getServletContext().getAttribute(BOOK_SERVICE_ATTRIBUTE_NAME);

        UserService userService = (UserServiceImpl) getServletContext().getAttribute(USER_SERVICE_ATTRIBUTE_NAME);
        String[] pathParts = request.getPathInfo().substring(1).split("/");
        String path = pathParts[0];

        switch (path) {
            case "submit":
                handleWriteReview(request, response, objectMapper, bookService,userService);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    private void handleWriteReview(HttpServletRequest request, HttpServletResponse response,
                                   ObjectMapper objectMapper, BookService bookService, UserService userService) throws IOException {
        try {
            HttpSession session = request.getSession();
            String username = (String) session.getAttribute("username");

            // Check if user is logged in
            if (username == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"success\": false, \"message\": \"Please log in to write a review\"}");
                return;
            }

            String bookId = request.getParameter("bookId");
            String reviewText = request.getParameter("reviewText");
            String ratingStr = request.getParameter("rating");

            if (bookId == null || reviewText == null || ratingStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\": false, \"message\": \"Missing required fields\"}");
                return;
            }

            if (reviewText.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\": false, \"message\": \"Review text cannot be empty\"}");
                return;
            }

            int rating;
            try {
                rating = Integer.parseInt(ratingStr);
                if (rating < 1 || rating > 5) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"success\": false, \"message\": \"Rating must be between 1 and 5\"}");
                    return;
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\": false, \"message\": \"Invalid rating format\"}");
                return;
            }

            try {
                userService.reviewBook(username, bookId, rating, reviewText);
                // Success response
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"success\": true, \"message\": \"Review submitted successfully\"}");
                return;
            } catch (IllegalStateException e) {
                // Handle specific validation errors with proper messages
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
                return;
            } catch (IllegalArgumentException e) {
                // Handle not found errors
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
                return;
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\": false, \"message\": \"Server error: " + e.getMessage() + "\"}");
        }
    }
}