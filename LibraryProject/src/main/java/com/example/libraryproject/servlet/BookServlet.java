package com.example.libraryproject.servlet;

import com.example.libraryproject.model.enums.BookSortCriteria;
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


@WebServlet(name = "BookServlet",urlPatterns = {"/api/books/*"})
public class BookServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ObjectMapper objectMapper = (ObjectMapper) getServletContext().getAttribute(OBJECT_MAPPER_ATTRIBUTE_NAME);

        BookService bookService = (BookServiceImpl) getServletContext().getAttribute(BOOK_SERVICE_ATTRIBUTE_NAME);

        String[] pathParts = request.getPathInfo().substring(1).split("/");
        String path = pathParts[0];

        String sortParam = request.getParameter("sort");
        BookSortCriteria sortCriteria = BookSortCriteria.fromValue(sortParam);

        switch (path) {
            case "all":
                objectMapper.writeValue(response.getWriter(), bookService.getAllBooks(sortCriteria));
                break;
            case "details":
                if (pathParts.length < 2) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\": \"Book ID is required\"}");
                    return;
                }
                objectMapper.writeValue(response.getWriter(), bookService.getBookDetails(pathParts[1]));
                break;
            case "available":
                if (sortParam != null && !sortParam.trim().isEmpty()) {
                    objectMapper.writeValue(response.getWriter(), bookService.getAvailableBooks(sortCriteria));
                } else {
                    objectMapper.writeValue(response.getWriter(), bookService.getAvailableBooks());
                }
                break;
            case "get-books-by-genre":
                if (pathParts.length < 2) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\": \"Genre is required\"}");
                    return;
                }
                if (sortParam != null && !sortParam.trim().isEmpty()) {
                    objectMapper.writeValue(response.getWriter(), bookService.getBooksByGenre(pathParts[1], sortCriteria));
                } else {
                    objectMapper.writeValue(response.getWriter(), bookService.getBooksByGenre(pathParts[1]));
                }
                break;
            case "book":
                if (pathParts.length < 2) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\": \"Book ID is required\"}");
                    return;
                }
                objectMapper.writeValue(response.getWriter(), bookService.getReviewsByBook(pathParts[1]));
                break;
            case "check-reservation":
                if (pathParts.length < 2) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\": \"Book ID is required\"}");
                    return;
                }
                checkUserReservation(request, response, pathParts[1]);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    private void checkUserReservation(HttpServletRequest request, HttpServletResponse response, String bookId) throws IOException {
        try {
            HttpSession session = request.getSession();
            String username = (String) session.getAttribute("username");

            if (username == null) {
                response.getWriter().write("{\"reserved\": false}");
                return;
            }

            UserService userService = (UserServiceImpl) getServletContext().getAttribute(USER_SERVICE_ATTRIBUTE_NAME);
            boolean hasReserved = userService.hasUserReservedBook(username, bookId);

            response.getWriter().write("{\"reserved\": " + hasReserved + "}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Server error: " + e.getMessage() + "\"}");
        }
    }
}