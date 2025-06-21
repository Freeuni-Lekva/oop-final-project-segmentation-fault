package com.example.libraryproject.servlet;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.dto.BookAdditionFromGoogleRequest;
import com.example.libraryproject.model.dto.BookAdditionRequest;
import com.example.libraryproject.model.dto.UserDTO;
import com.example.libraryproject.service.BookKeeperService;
import com.example.libraryproject.service.GoogleBooksAPIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;


import java.io.IOException;

import java.util.Set;


@MultipartConfig
@WebServlet(name = "BookKeeperServlet", urlPatterns = "/api/bookkeeper/*")
public class BookKeeperServlet extends HttpServlet {
    private ObjectMapper objectMapper;

    @Override
    public void init(){
        objectMapper = (ObjectMapper) getServletContext().getAttribute(ApplicationProperties.OBJECT_MAPPER_ATTRIBUTE_NAME);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        BookKeeperService bookKeeperService = (BookKeeperService) request.getServletContext()
                .getAttribute(ApplicationProperties.BOOKKEEPER_SERVICE_ATTRIBUTE_NAME);
        GoogleBooksAPIService googleBooksAPIService = (GoogleBooksAPIService) request.getServletContext()
                .getAttribute(ApplicationProperties.GOOGLE_BOOKS_API_ATTRIBUTE_NAME);

        String path = request.getPathInfo();
        JsonNode params = objectMapper.readTree(request.getReader());

        switch(path) {
            case "/add-book":
                handleAddBook(request, bookKeeperService, params);
                break;
            case "/add-book-from-google":
                handleAddBookFromGoogleAPI(request, bookKeeperService, googleBooksAPIService, params);
                break;
            case "/mark-borrowed":
                handleMarkBorrowed(request, bookKeeperService, params);
                break;

            case "/ban-user":
                handleBanUser(request, bookKeeperService, params);
                break;

            case "/unban-user":
                handleUnbanUser(request, bookKeeperService, params);
                break;

            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        BookKeeperService bookKeeperService = (BookKeeperService) request.getServletContext()
                .getAttribute(ApplicationProperties.BOOKKEEPER_SERVICE_ATTRIBUTE_NAME);
        String path = request.getPathInfo();

        if ("/users".equals(path)) {
            handleGetUsers(request, response, bookKeeperService);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleAddBookFromGoogleAPI(HttpServletRequest req, BookKeeperService bookKeeperService,
                                            GoogleBooksAPIService googleBooksAPIService, JsonNode params) {
        String title = params.get("title").asText();
        String author = params.get("author").asText();
        if(title == null){
            throw new IllegalArgumentException("Title is required to add a new book.");
        }
        BookAdditionFromGoogleRequest request = new BookAdditionFromGoogleRequest(title, author);
        googleBooksAPIService.fetchBook(request);
    }

    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        BookKeeperService bookKeeperService = (BookKeeperService) req.getServletContext()
                .getAttribute(ApplicationProperties.BOOKKEEPER_SERVICE_ATTRIBUTE_NAME);

        String path = req.getPathInfo();

        JsonNode params = objectMapper.readTree(req.getReader());

        switch (path) {
            case "/delete-book":
                handleDeleteBook(req, bookKeeperService, params);
                break;

            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleAddBook(HttpServletRequest req, BookKeeperService bookKeeperService, JsonNode params) throws IOException, ServletException {

        String title = params.get("title").asText();
        String author = params.get("author").asText();
        String description = params.get("description").asText();
        String genre = params.get("genre").asText();
        Part filePart = req.getPart("image");
        String imageUrl = "";

        if (filePart != null && filePart.getSize() > 0) {
            imageUrl = bookKeeperService.downloadImage(filePart, req.getContextPath());
        }

        if(title == null || author == null){
            throw new IllegalArgumentException("Title and author are required to add a new book.");
        }

        BookAdditionRequest bookAdditionRequest = new BookAdditionRequest(title, author, description, genre, imageUrl);
        bookKeeperService.addBook(bookAdditionRequest);
    }

    private void handleMarkBorrowed(HttpServletRequest req, BookKeeperService bookKeeperService, JsonNode params) {

        String orderPublicId = params.get("orderPublicId").asText();

        if(orderPublicId == null){
            throw new IllegalArgumentException("Order Public ID is required to mark book as borrowed.");
        }

        bookKeeperService.tookBook(orderPublicId);
    }

    private void handleDeleteBook(HttpServletRequest req, BookKeeperService bookKeeperService, JsonNode params) {

        String bookPublicId = params.get("bookPublicId").asText();

        if(bookPublicId == null){
            throw new IllegalArgumentException("Order Public ID is required to add a new book.");
        }

        bookKeeperService.deleteBook(bookPublicId);
    }

    private void handleBanUser(HttpServletRequest req, BookKeeperService bookKeeperService, JsonNode params) {

        String username = params.get("username").asText();

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required to ban a user.");
        }
        bookKeeperService.banUser(username);
    }

    private void handleUnbanUser(HttpServletRequest req, BookKeeperService bookKeeperService, JsonNode params) {
        String username = params.get("username").asText();
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required to unban a user.");
        }
        bookKeeperService.unbanUser(username);
    }

    private void handleGetUsers(HttpServletRequest req, HttpServletResponse resp, BookKeeperService bookKeeperService)
            throws IOException {

        Set<UserDTO> users = bookKeeperService.getUsers();
        objectMapper.writeValue(resp.getWriter(), users);
    }

}
