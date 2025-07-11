package com.example.libraryproject.servlet;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.dto.BookAdditionFromGoogleRequest;
import com.example.libraryproject.model.dto.BookAdditionRequest;
import com.example.libraryproject.model.dto.BookDTO;
import com.example.libraryproject.model.dto.OrderDTO;
import com.example.libraryproject.model.dto.UserDTO;
import com.example.libraryproject.service.BookKeeperService;
import com.example.libraryproject.service.GoogleBooksApiService;
import com.example.libraryproject.service.implementation.BookKeeperServiceImpl;
import com.example.libraryproject.service.implementation.GoogleBooksApiServiceImpl;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@MultipartConfig
@WebServlet(name = "BookKeeperServlet", urlPatterns = "/api/bookkeeper/*")
public class BookKeeperServlet extends HttpServlet {
    private ObjectMapper objectMapper;

    @Override
    public void init() {
        objectMapper = (ObjectMapper) getServletContext().getAttribute(ApplicationProperties.OBJECT_MAPPER_ATTRIBUTE_NAME);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        BookKeeperService bookKeeperService = (BookKeeperServiceImpl) request.getServletContext()
                .getAttribute(ApplicationProperties.BOOKKEEPER_SERVICE_ATTRIBUTE_NAME);

        GoogleBooksApiService googleBooksAPIService = (GoogleBooksApiServiceImpl) request.getServletContext()
                .getAttribute(ApplicationProperties.GOOGLE_BOOKS_API_ATTRIBUTE_NAME);

        String path = request.getPathInfo();

        switch (path) {
            case "/upload-image":
                try {
                    String imageUrl = handleUploadImage(request, bookKeeperService);
                    Map<String, String> responseMap = new HashMap<>();
                    responseMap.put("status", "success");
                    responseMap.put("imageUrl", imageUrl);
                    responseMap.put("message", "Image uploaded successfully");
                    objectMapper.writeValue(response.getWriter(), responseMap);
                    response.setStatus(HttpServletResponse.SC_OK);
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    Map<String, String> errorMap = new HashMap<>();
                    errorMap.put("status", "error");
                    errorMap.put("message", "Image upload failed: " + e.getMessage());
                    objectMapper.writeValue(response.getWriter(), errorMap);
                }
                break;

            case "/add-book":
                handleAddBook(request, response, bookKeeperService);
                break;

            case "/add-book-from-google":

                handleAddBookFromGoogleAPI(request, response, googleBooksAPIService);
                break;

            case "/mark-borrowed":
                try {
                    handleMarkBorrowed(request, response, bookKeeperService);
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, String> responseMap = new HashMap<>();
                    responseMap.put("status", "error");
                    responseMap.put("message", e.getMessage());
                    objectMapper.writeValue(response.getWriter(), responseMap);
                }
                break;

            case "/return-book":
                try {
                    handleReturnBook(request, response, bookKeeperService);
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, String> responseMap = new HashMap<>();
                    responseMap.put("status", "error");
                    responseMap.put("message", e.getMessage());
                    objectMapper.writeValue(response.getWriter(), responseMap);
                }
                break;

            case "/ban-user":
                try {
                    handleBanUser(request, response, bookKeeperService);
                    response.setStatus(HttpServletResponse.SC_OK);
                    Map<String, String> responseMap = new HashMap<>();

                    responseMap.put("status", "success");
                    responseMap.put("message", "User banned successfully");

                    objectMapper.writeValue(response.getWriter(), responseMap);
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, String> responseMap = new HashMap<>();

                    responseMap.put("status", "error");
                    responseMap.put("message", e.getMessage());

                    objectMapper.writeValue(response.getWriter(), responseMap);
                }
                break;

            case "/unban-user":
                try {
                    handleUnbanUser(request, response, bookKeeperService);
                    response.setStatus(HttpServletResponse.SC_OK);
                    Map<String, String> responseMap = new HashMap<>();

                    responseMap.put("status", "success");
                    responseMap.put("message", "User unbanned successfully");

                    objectMapper.writeValue(response.getWriter(), responseMap);
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, String> responseMap = new HashMap<>();

                    responseMap.put("status", "error");
                    responseMap.put("message", e.getMessage());

                    objectMapper.writeValue(response.getWriter(), responseMap);
                }
                break;

            default:

                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        BookKeeperService bookKeeperService = (BookKeeperServiceImpl) request.getServletContext()
                .getAttribute(ApplicationProperties.BOOKKEEPER_SERVICE_ATTRIBUTE_NAME);
        String path = request.getPathInfo();

        if ("/users".equals(path)) {
            handleGetUsers(response, bookKeeperService);
        } else if ("/books".equals(path)) {
            handleGetBooks(response, bookKeeperService);
        } else if ("/orders".equals(path)) {
            handleGetOrders(request, response, bookKeeperService);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleAddBookFromGoogleAPI(HttpServletRequest req,
                                            HttpServletResponse response,
                                            GoogleBooksApiService googleBooksAPIService) throws IOException {

        JsonNode params = objectMapper.readTree(req.getReader());

        String title = params.get("title").asText();
        String author = params.get("author").asText();
        int copies = params.has("copies") ? params.get("copies").asInt(1) : 1;
        
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required to add a new book.");
        }
        
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author is required for accurate Google Books search.");
        }
        
        BookAdditionFromGoogleRequest request = new BookAdditionFromGoogleRequest(title, author);
        boolean success = googleBooksAPIService.fetchBook(request, copies);
        
        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("status", "success");
            responseMap.put("message", "Book successfully added from Google Books");
            objectMapper.writeValue(response.getWriter(), responseMap);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("status", "error");
            responseMap.put("message", "Book not found in Google Books or already exists in library");
            objectMapper.writeValue(response.getWriter(), responseMap);
        }
    }

    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        BookKeeperService bookKeeperService = (BookKeeperServiceImpl) req.getServletContext()
                .getAttribute(ApplicationProperties.BOOKKEEPER_SERVICE_ATTRIBUTE_NAME);

        String path = req.getPathInfo();

        if (path.equals("/delete-book")) {
            try {
                handleDeleteBook(req, bookKeeperService);
                resp.setStatus(HttpServletResponse.SC_OK);
                
                Map<String, String> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Book deleted successfully");
                objectMapper.writeValue(resp.getWriter(), response);
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                
                Map<String, String> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", e.getMessage());
                objectMapper.writeValue(resp.getWriter(), response);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private String handleUploadImage(HttpServletRequest req, BookKeeperService bookKeeperService) throws IOException, ServletException {
        Part filePart = req.getPart("image");
        if (filePart == null || filePart.getSize() == 0) {
            throw new IllegalArgumentException("Image file is required.");
        }
        
        // Validate file type
        String contentType = filePart.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed.");
        }
        
        // Validate file size (5MB limit)
        final long maxSize = 5 * 1024 * 1024; // 5MB
        if (filePart.getSize() > maxSize) {
            throw new IllegalArgumentException("Image file size must be less than 5MB.");
        }
        
        return bookKeeperService.downloadImage(filePart);
    }

    private void handleAddBook(HttpServletRequest req, HttpServletResponse response, BookKeeperService bookKeeperService) throws IOException {
        try {
            // Log incoming request
            System.out.println("=== ADD BOOK REQUEST STARTED ===");
            
            // Read and log the raw JSON
            String requestBody = req.getReader().lines().collect(java.util.stream.Collectors.joining("\n"));
            System.out.println("Raw request body: " + requestBody);
            
            // Parse the request
            JsonNode jsonNode = objectMapper.readTree(requestBody);
            System.out.println("Parsed JSON: " + jsonNode.toString());
            
            // Extract and validate fields
            String title = jsonNode.has("title") ? jsonNode.get("title").asText() : null;
            String author = jsonNode.has("author") ? jsonNode.get("author").asText() : null;
            String description = jsonNode.has("description") ? jsonNode.get("description").asText() : "";
            String genre = jsonNode.has("genre") ? jsonNode.get("genre").asText() : null;
            Long volume = jsonNode.has("volume") ? jsonNode.get("volume").asLong(1L) : 1L;
            Long copies = jsonNode.has("copies") ? jsonNode.get("copies").asLong(1L) : 1L;
            String publicationDate = jsonNode.has("publicationDate") ? jsonNode.get("publicationDate").asText() : null;
            String imageUrl = jsonNode.has("imageUrl") ? jsonNode.get("imageUrl").asText() : null;
            
            System.out.println("Extracted fields - Title: " + title + ", Author: " + author + ", Genre: " + genre + 
                             ", Volume: " + volume + ", Copies: " + copies + ", Date: " + publicationDate + ", ImageUrl: " + imageUrl);
            
            // Validate required fields
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Title is required to add a new book.");
            }
            
            if (author == null || author.trim().isEmpty()) {
                throw new IllegalArgumentException("Author is required to add a new book.");
            }
            
            if (genre == null || genre.trim().isEmpty()) {
                throw new IllegalArgumentException("Genre is required to add a new book.");
            }
            
            if (publicationDate == null || publicationDate.trim().isEmpty()) {
                throw new IllegalArgumentException("Publication date is required to add a new book.");
            }
            
            // Create the request object
            BookAdditionRequest request = new BookAdditionRequest(
                title.trim(),
                author.trim(), 
                description.trim(),
                genre.trim(),
                volume,
                copies,
                publicationDate.trim(),
                imageUrl != null ? imageUrl.trim() : null
            );
            
            System.out.println("Created BookAdditionRequest: " + request.toString());
            
            // Call the service
            bookKeeperService.addBook(request);
            
            System.out.println("Book added successfully!");
            
            // Return success response
            response.setStatus(HttpServletResponse.SC_OK);
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("status", "success");
            responseMap.put("message", "Book successfully added to library");
            objectMapper.writeValue(response.getWriter(), responseMap);
            
            System.out.println("=== ADD BOOK REQUEST COMPLETED SUCCESSFULLY ===");
            
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error in add book: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("status", "error");
            responseMap.put("message", e.getMessage());
            objectMapper.writeValue(response.getWriter(), responseMap);
            
        } catch (Exception e) {
            System.err.println("Unexpected error in add book: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("status", "error");
            responseMap.put("message", "Failed to add book: " + e.getMessage());
            objectMapper.writeValue(response.getWriter(), responseMap);
        }
    }

    private void handleMarkBorrowed(HttpServletRequest req, HttpServletResponse response, BookKeeperService bookKeeperService) throws IOException {

        JsonNode params = objectMapper.readTree(req.getReader());

        String orderPublicId = params.get("orderPublicId").asText();

        if (orderPublicId == null || orderPublicId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order Public ID is required to mark book as borrowed.");
        }

        bookKeeperService.tookBook(orderPublicId);
        
        // Return success response
        response.setStatus(HttpServletResponse.SC_OK);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("status", "success");
        responseMap.put("message", "Book pickup confirmed successfully");
        objectMapper.writeValue(response.getWriter(), responseMap);
    }

    private void handleReturnBook(HttpServletRequest req, HttpServletResponse response, BookKeeperService bookKeeperService) throws IOException {

        JsonNode params = objectMapper.readTree(req.getReader());

        String orderPublicId = params.get("orderPublicId").asText();

        if (orderPublicId == null || orderPublicId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order Public ID is required to return book.");
        }

        bookKeeperService.returnBook(orderPublicId);
        
        // Return success response
        response.setStatus(HttpServletResponse.SC_OK);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("status", "success");
        responseMap.put("message", "Book return confirmed successfully");
        objectMapper.writeValue(response.getWriter(), responseMap);
    }

    private void handleDeleteBook(HttpServletRequest req, BookKeeperService bookKeeperService) throws IOException {

        JsonNode params = objectMapper.readTree(req.getReader());

        String bookPublicId = params.get("bookPublicId").asText();

        if (bookPublicId == null) {
            throw new IllegalArgumentException("Book ID is required to delete a book.");
        }

        bookKeeperService.deleteBook(bookPublicId);
    }

    private void handleBanUser(HttpServletRequest req, HttpServletResponse response, BookKeeperService bookKeeperService) throws IOException {

        JsonNode params = objectMapper.readTree(req.getReader());

        String username = params.get("username").asText();

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required to ban a user.");
        }
        bookKeeperService.banUser(username);
    }

    private void handleUnbanUser(HttpServletRequest req, HttpServletResponse response, BookKeeperService bookKeeperService) throws IOException {

        JsonNode params = objectMapper.readTree(req.getReader());

        String username = params.get("username").asText();

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required to unban a user.");
        }
        bookKeeperService.unbanUser(username);
    }

    private void handleGetUsers(HttpServletResponse resp, BookKeeperService bookKeeperService)
            throws IOException {
        Set<UserDTO> users = bookKeeperService.getUsers();
        objectMapper.writeValue(resp.getWriter(), users);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleGetBooks(HttpServletResponse resp, BookKeeperService bookKeeperService)
            throws IOException {
        Set<BookDTO> books = bookKeeperService.getBooks();
        objectMapper.writeValue(resp.getWriter(), books);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleGetOrders(HttpServletRequest req, HttpServletResponse resp, BookKeeperService bookKeeperService)
            throws IOException {
        String username = req.getParameter("username");
        String overdueParameter = req.getParameter("overdue");
        boolean overdueOnly = "true".equals(overdueParameter);

        Set<OrderDTO> orders;

        if (username != null && !username.trim().isEmpty() && overdueOnly) {
            Set<OrderDTO> userOrders = bookKeeperService.getOrdersByUsername(username);
            orders = userOrders.stream()
                    .filter(OrderDTO::isOverdue)
                    .collect(Collectors.toSet());
        } else if (username != null && !username.trim().isEmpty()) {
            orders = bookKeeperService.getOrdersByUsername(username);
        } else if (overdueOnly) {
            orders = bookKeeperService.getOverdueOrders();
        } else {
            orders = bookKeeperService.getAllActiveOrders();
        }

        objectMapper.writeValue(resp.getWriter(), orders);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

}
