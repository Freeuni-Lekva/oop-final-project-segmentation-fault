package com.example.libraryproject.servlet;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.service.BookRecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

import static com.example.libraryproject.configuration.ApplicationProperties.BOOK_RECOMMENDATION_SERVICE_ATTRIBUTE_NAME;

@WebServlet(name = "BookRecomendationServlet", urlPatterns = "/api/recommend/*")
public class BookRecommendationServlet extends HttpServlet {


    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ObjectMapper objectMapper = (ObjectMapper) request.getServletContext().getAttribute(BOOK_RECOMMENDATION_SERVICE_ATTRIBUTE_NAME);

        BookRecommendationService bookRecommendationService = (BookRecommendationService) getServletContext().
                getAttribute(BOOK_RECOMMENDATION_SERVICE_ATTRIBUTE_NAME);

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing username path");
            return;
        }

        String[] pathParts = pathInfo.substring(1).split("/");

        if (pathParts.length == 2 && pathParts[0].equals("username")) {
            String username = pathParts[1];
            Set<Book> recommendedBooks = bookRecommendationService.recommendBooks(username);

            objectMapper.writeValue(response.getWriter(), recommendedBooks);

        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
        }

    }
}
