package com.example.libraryproject.servlet;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.dto.BookAdditionRequest;
import com.example.libraryproject.service.BookKeeperService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static com.example.libraryproject.configuration.ApplicationProperties.IMAGE_DIR;

@WebServlet(name = "BookKeeperServlet", urlPatterns = "/api/bookkeeper/*")
public class BookKeeperServlet extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        BookKeeperService bookKeeperService = (BookKeeperService) req.getServletContext()
                .getAttribute(ApplicationProperties.BOOKKEEPER_SERVICE_ATTRIBUTE_NAME);

        String path = req.getPathInfo();

        switch(path) {
            case "/add-book":
                handleAddBook(req, bookKeeperService);
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
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {}

    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        BookKeeperService bookKeeperService = (BookKeeperService) req.getServletContext()
                .getAttribute(ApplicationProperties.BOOKKEEPER_SERVICE_ATTRIBUTE_NAME);

        String path = req.getPathInfo();

        switch(path) {
            case "/delete-book":
                handleDeleteBook(req, bookKeeperService);
                break;

            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private String downloadBook(HttpServletRequest req) throws IOException, ServletException {
        Part filePart = req.getPart("image");
        String submittedFileName = Path.of(filePart.getSubmittedFileName()).getFileName().toString();
        String safeFileName = submittedFileName.replaceAll("[^a-zA-Z0-9.\\-]", "_");

        Path imagesDir = Paths.get(IMAGE_DIR);
        if (!Files.exists(imagesDir)) {
            Files.createDirectories(imagesDir);
        }

        Path filePath = imagesDir.resolve(safeFileName);
        filePart.write(filePath.toString());

        //System.out.println("Image saved to: " + filePath);

        return req.getContextPath() + "/images/" + safeFileName;
    }

    private void handleAddBook(HttpServletRequest req, BookKeeperService bookKeeperService) throws IOException, ServletException {
        String title = req.getParameter("title");
        String author = req.getParameter("author");
        String description = req.getParameter("description");
        String genre = req.getParameter("genre");
        Part filePart = req.getPart("image");
        String imageUrl = "";

        if (filePart != null && filePart.getSize() > 0) {
            imageUrl = downloadBook(req);
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
