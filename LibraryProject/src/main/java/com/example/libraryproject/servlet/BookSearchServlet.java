package com.example.libraryproject.servlet;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.dto.BookDTO;
import com.example.libraryproject.service.implementation.BookServiceImpl;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


@WebServlet("/api/books/search")
public class BookSearchServlet extends HttpServlet {

    private BookServiceImpl bookService;
    private static final String BOOK_SERVICE_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.book-service");

    @Override
    public void init() throws ServletException {
        bookService = (BookServiceImpl) getServletContext().getAttribute(BOOK_SERVICE_ATTRIBUTE_NAME);

        if (bookService == null) {
            throw new ServletException("BookService not found in servlet context.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String searchTerm = request.getParameter("term");
        String sortBy = request.getParameter("sort");
        String availability = request.getParameter("availability");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\": \"Search term is required\"}");
            return;
        }

        if (bookService == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Service not initialized\"}");
            return;
        }

        try {
            List<BookDTO> books = bookService.searchBooks(searchTerm.trim(), sortBy, availability);

            JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

            for (BookDTO book : books) {
                JsonObjectBuilder bookJson = Json.createObjectBuilder()
                        .add("publicId", book.publicId())
                        .add("name", book.name() != null ? book.name() : "")
                        .add("description", book.description() != null ? book.description() : "")
                        .add("genre", book.genre() != null ? book.genre() : "")
                        .add("author", book.author() != null ? book.author() : "")
                        .add("imageUrl", book.imageUrl() != null ? book.imageUrl() : "")
                        .add("totalAmount", book.totalAmount())
                        .add("currentAmount", book.currentAmount())
                        .add("volume", book.volume())
                        .add("rating", book.rating())
                        .add("date", book.date() != null ? book.date() : "");

                jsonArrayBuilder.add(bookJson);
            }

            out.write(jsonArrayBuilder.build().toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}