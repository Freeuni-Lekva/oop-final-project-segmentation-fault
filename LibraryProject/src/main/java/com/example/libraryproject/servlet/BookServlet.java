package com.example.libraryproject.servlet;

import com.example.libraryproject.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.example.libraryproject.configuration.ApplicationProperties.BOOK_SERVICE_ATTRIBUTE_NAME;

@WebServlet(name = "BookServlet",urlPatterns = {"/api/books/*"})
public class BookServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BookService bookService = (BookService) getServletContext().getAttribute(BOOK_SERVICE_ATTRIBUTE_NAME);
        String[] pathParts = request.getPathInfo().substring(1).split("/");
        String path = pathParts[0];
        ObjectMapper map = new ObjectMapper();
        switch (path) {
            case "all":
                map.writeValue(response.getWriter(), bookService.getAllBooks());
                break;
            case "details":
                map.writeValue(response.getWriter(), bookService.getBookDetails(pathParts[1]));
                break;
            case "available":
                map.writeValue(response.getWriter(), bookService.getAvailableBooks());
                break;
            case "get-books-by-genre":
                map.writeValue(response.getWriter(), bookService.getBooksByGenre(pathParts[1]));
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

}