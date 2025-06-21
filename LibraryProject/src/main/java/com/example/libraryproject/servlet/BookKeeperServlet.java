package com.example.libraryproject.servlet;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.dto.BookAdditionFromGoogleRequest;
import com.example.libraryproject.model.dto.BookAdditionRequest;
import com.example.libraryproject.model.dto.UserDTO;
import com.example.libraryproject.service.BookKeeperService;
import com.example.libraryproject.service.GoogleBooksAPIService;
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


    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        BookKeeperService bookKeeperService = (BookKeeperService) req.getServletContext()
                .getAttribute(ApplicationProperties.BOOKKEEPER_SERVICE_ATTRIBUTE_NAME);
        GoogleBooksAPIService googleBooksAPIService = (GoogleBooksAPIService) req.getServletContext()
                .getAttribute(ApplicationProperties.GOOGLE_BOOKS_API_ATTRIBUTE_NAME);

        String path = req.getPathInfo();

        switch(path) {
            case "/add-book":
                handleAddBook(req, bookKeeperService);
                break;
            case "/add-book-from-google":
                handleAddBookFromGoogleAPI(req, bookKeeperService, googleBooksAPIService);
                break;
            case "/mark-borrowed":
                handleMarkBorrowed(req, bookKeeperService);
                break;

            case "/ban-user":
                handleBanUser(req, bookKeeperService);
                break;

            case "/unban-user":
                handleUnbanUser(req, bookKeeperService);
                break;

            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BookKeeperService bookKeeperService = (BookKeeperService) request.getServletContext()
                .getAttribute(ApplicationProperties.BOOKKEEPER_SERVICE_ATTRIBUTE_NAME);
        String path = request.getPathInfo();
        if ("/users".equals(path)) {
            handleGetUsers(request, response, bookKeeperService);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleAddBookFromGoogleAPI(HttpServletRequest req, BookKeeperService bookKeeperService, GoogleBooksAPIService googleBooksAPIService) {
        String title = req.getParameter("title");
        String author = req.getParameter("author");
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

        switch (path) {
            case "/delete-book":
                handleDeleteBook(req, bookKeeperService);
                break;

            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleAddBook(HttpServletRequest req, BookKeeperService bookKeeperService) throws IOException, ServletException {
        String title = req.getParameter("title");
        String author = req.getParameter("author");
        String description = req.getParameter("description");
        String genre = req.getParameter("genre");
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

    private void handleMarkBorrowed(HttpServletRequest req, BookKeeperService bookKeeperService) {
        String orderPublicId = req.getParameter("orderPublicId");
        if(orderPublicId == null){
            throw new IllegalArgumentException("Order Public ID is required to mark book as borrowed.");
        }

        bookKeeperService.tookBook(orderPublicId);
    }

    private void handleDeleteBook(HttpServletRequest req, BookKeeperService bookKeeperService) {
        String bookPublicId = req.getParameter("bookPublicId");
        if(bookPublicId == null){
            throw new IllegalArgumentException("Order Public ID is required to add a new book.");
        }

        bookKeeperService.deleteBook(bookPublicId);
    }

    private void handleBanUser(HttpServletRequest req, BookKeeperService bookKeeperService) {
        String username = req.getParameter("username");
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required to ban a user.");
        }
        bookKeeperService.banUser(username);
    }

    private void handleUnbanUser(HttpServletRequest req, BookKeeperService bookKeeperService) {
        String username = req.getParameter("username");
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required to unban a user.");
        }
        bookKeeperService.unbanUser(username);
    }

    private void handleGetUsers(HttpServletRequest req, HttpServletResponse resp, BookKeeperService bookKeeperService)
            throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Set<UserDTO> users = bookKeeperService.getUsers();
        objectMapper.writeValue(resp.getWriter(), users);
    }

}
