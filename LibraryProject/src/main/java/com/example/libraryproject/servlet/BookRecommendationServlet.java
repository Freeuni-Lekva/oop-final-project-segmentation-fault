package com.example.libraryproject.servlet;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.dto.BookDTO;
import com.example.libraryproject.service.BookRecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Set;


@WebServlet(name = "BookRecommendationServlet", urlPatterns = "/api/user/recommend")
public class BookRecommendationServlet extends HttpServlet {

    private static final String BOOK_RECOMMENDATION_SERVICE_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.book-recommendation-service");

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        String username = session.getAttribute("username").toString();

        BookRecommendationService bookRecommendationService =
                (BookRecommendationService) getServletContext().getAttribute(BOOK_RECOMMENDATION_SERVICE_ATTRIBUTE_NAME);

        try {
            Set<BookDTO> recommendedBooks = bookRecommendationService.recommendBooks(username);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(response.getWriter(), recommendedBooks);

            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Failed to get recommendations: " + e.getMessage() + "\"}");
        }
    }
}