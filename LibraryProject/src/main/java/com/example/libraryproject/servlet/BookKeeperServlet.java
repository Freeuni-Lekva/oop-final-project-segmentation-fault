package com.example.libraryproject.servlet;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.dto.BookAdditionFromGoogleRequest;
import com.example.libraryproject.model.dto.BookAdditionRequest;
import com.example.libraryproject.service.BookKeeperService;
import com.example.libraryproject.service.GoogleBooksAPIService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.example.libraryproject.configuration.ApplicationProperties.GOOGLE_BOOKS_API_ATTRIBUTE_NAME;


import java.io.IOException;
import java.util.UUID;


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
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {}

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
        String userIdParam = req.getParameter("userId");
        if (userIdParam == null) {
            throw new IllegalArgumentException("User ID is required to ban a user.");
        }

        UUID userId = UUID.fromString(userIdParam);
        bookKeeperService.banUser(userId);
    }

    private void handleUnbanUser(HttpServletRequest req, BookKeeperService bookKeeperService) {
        String userIdParam = req.getParameter("userId");
        if (userIdParam == null) {
            throw new IllegalArgumentException("User ID is required to unban a user.");
        }

        Long userId = Long.parseLong(userIdParam);
        bookKeeperService.unbanUser(userId);
    }

}
