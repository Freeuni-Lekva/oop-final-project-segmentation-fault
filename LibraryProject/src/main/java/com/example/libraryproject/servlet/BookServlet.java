package com.example.libraryproject.servlet;

import com.example.libraryproject.model.enums.BookSortCriteria;
import com.example.libraryproject.service.BookService;
import com.example.libraryproject.service.UserService;
import com.example.libraryproject.service.implementation.BookServiceImpl;
import com.example.libraryproject.service.implementation.UserServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
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
                checkUserReservation(request, response, objectMapper, pathParts[1]);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    private void checkUserReservation(HttpServletRequest request, HttpServletResponse response,
                                      ObjectMapper objectMapper, String bookId) throws IOException {
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ObjectMapper objectMapper = (ObjectMapper) getServletContext().getAttribute(OBJECT_MAPPER_ATTRIBUTE_NAME);
        UserService userService = (UserServiceImpl) getServletContext().getAttribute(USER_SERVICE_ATTRIBUTE_NAME);

        String[] pathParts = request.getPathInfo().substring(1).split("/");
        String path = pathParts[0];

        switch (path) {
            case "reserve":
                handleReserveBook(request, response, objectMapper, userService);
                break;
            case "cancel-reservation":
                handleCancelReservation(request, response, objectMapper, userService);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    private void handleReserveBook(HttpServletRequest request, HttpServletResponse response,
                                   ObjectMapper objectMapper, UserService userService) throws IOException {
        try {
            HttpSession session = request.getSession();
            String username = (String) session.getAttribute("username");

            if (username == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"success\": false, \"message\": \"Please log in to reserve books\"}");
                return;
            }

            BufferedReader reader = request.getReader();
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JsonNode jsonNode = objectMapper.readTree(jsonBuilder.toString());
            String bookId = jsonNode.get("bookId").asText();

            boolean success = userService.reserveBook(username, bookId);

            if (success) {
                response.getWriter().write("{\"success\": true, \"message\": \"Book reserved successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\": false, \"message\": \"Failed to reserve book. It might be unavailable.\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\": false, \"message\": \"Server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleCancelReservation(HttpServletRequest request, HttpServletResponse response,
                                         ObjectMapper objectMapper, UserService userService) throws IOException {
        try {
            HttpSession session = request.getSession();
            String username = (String) session.getAttribute("username");

            if (username == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"success\": false, \"message\": \"Please log in to cancel reservation\"}");
                return;
            }

            BufferedReader reader = request.getReader();
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JsonNode jsonNode = objectMapper.readTree(jsonBuilder.toString());
            String bookId = jsonNode.get("bookId").asText();

            boolean success = userService.cancelReservation(username, bookId);

            if (success) {
                response.getWriter().write("{\"success\": true, \"message\": \"Reservation canceled successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\": false, \"message\": \"Failed to cancel reservation\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\": false, \"message\": \"Server error: " + e.getMessage() + "\"}");
        }
    }

}