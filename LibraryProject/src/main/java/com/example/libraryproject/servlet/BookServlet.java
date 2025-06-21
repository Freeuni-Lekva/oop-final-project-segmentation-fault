package com.example.libraryproject.servlet;

import com.example.libraryproject.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.example.libraryproject.configuration.ApplicationProperties.BOOK_SERVICE_ATTRIBUTE_NAME;
import static com.example.libraryproject.configuration.ApplicationProperties.OBJECT_MAPPER_ATTRIBUTE_NAME;

@WebServlet(name = "BookServlet",urlPatterns = {"/api/books/*"})
public class BookServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ObjectMapper objectMapper = (ObjectMapper) getServletContext().getAttribute(OBJECT_MAPPER_ATTRIBUTE_NAME);

        BookService bookService = (BookService) getServletContext().getAttribute(BOOK_SERVICE_ATTRIBUTE_NAME);

        String[] pathParts = request.getPathInfo().substring(1).split("/");
        String path = pathParts[0];

        switch (path) {
            case "all":
                objectMapper.writeValue(response.getWriter(), bookService.getAllBooks());
                break;
            case "details":
                objectMapper.writeValue(response.getWriter(), bookService.getBookDetails(pathParts[1]));
                break;
            case "available":
                objectMapper.writeValue(response.getWriter(), bookService.getAvailableBooks());
                break;
            case "get-books-by-genre":
                objectMapper.writeValue(response.getWriter(), bookService.getBooksByGenre(pathParts[1]));
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

}