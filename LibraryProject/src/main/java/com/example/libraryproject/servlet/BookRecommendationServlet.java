package com.example.libraryproject.servlet;

import com.example.libraryproject.model.dto.BookDTO;
import com.example.libraryproject.service.BookRecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

import static com.example.libraryproject.configuration.ApplicationProperties.BOOK_RECOMMENDATION_SERVICE_ATTRIBUTE_NAME;

@WebServlet(name = "BookRecommendationServlet", urlPatterns = "/api/user/recommend")

public class BookRecommendationServlet extends HttpServlet {


    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ObjectMapper objectMapper = (ObjectMapper) request.getServletContext().getAttribute(BOOK_RECOMMENDATION_SERVICE_ATTRIBUTE_NAME);

        BookRecommendationService bookRecommendationService = (BookRecommendationService) getServletContext().
                getAttribute(BOOK_RECOMMENDATION_SERVICE_ATTRIBUTE_NAME);

        String pathInfo = request.getPathInfo();

        String username = request.getAttribute("username").toString();

        Set<BookDTO> recommendedBooks = bookRecommendationService.recommendBooks(username);

        objectMapper.writeValue(response.getWriter(), recommendedBooks);

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
