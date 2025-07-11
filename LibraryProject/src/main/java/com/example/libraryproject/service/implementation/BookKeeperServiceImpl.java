package com.example.libraryproject.service.implementation;

import com.example.libraryproject.model.dto.BookAdditionRequest;
import com.example.libraryproject.model.dto.OrderDTO;
import com.example.libraryproject.model.dto.UserDTO;
import com.example.libraryproject.model.dto.BookDTO;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Order;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.OrderStatus;
import com.example.libraryproject.model.enums.Role;
import com.example.libraryproject.model.enums.UserStatus;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.OrderRepository;
import com.example.libraryproject.repository.ReviewRepository;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.service.BookKeeperService;
import com.example.libraryproject.service.MailService;
import com.example.libraryproject.utilities.Mappers;
import jakarta.servlet.http.Part;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class BookKeeperServiceImpl implements BookKeeperService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final MailService mailService;
    private static final Logger logger = LoggerFactory.getLogger(BookKeeperServiceImpl.class);


    public void addBook(BookAdditionRequest bookRequest) {
        Long copies = bookRequest.copies() != null ? bookRequest.copies() : 1L;
        Optional<Book> existingBook = bookRepository.findByTitle(bookRequest.title());

        if (existingBook.isPresent()) {
            Book bookInLibrary = existingBook.get();
            // Add copies to both total and current amount
            bookInLibrary.setTotalAmount(bookInLibrary.getTotalAmount() + copies);
            bookInLibrary.setCurrentAmount(bookInLibrary.getCurrentAmount() + copies);
            bookRepository.update(bookInLibrary);
        } else {
            Book book = new Book();
            book.setName(bookRequest.title());
            book.setAuthor(bookRequest.author());
            book.setDescription(bookRequest.description());
            book.setGenre(bookRequest.genre());
            book.setPublicId(bookRequest.title().replaceAll("[^a-zA-Z0-9.\\-]", "_"));
            if (bookRequest.imageUrl() != null && !bookRequest.imageUrl().trim().isEmpty()) {
                book.setImageUrl(bookRequest.imageUrl());
            } else {
                book.setImageUrl(bookRequest.title().replaceAll("[^a-zA-Z0-9.\\-]", "_") + ".jpg");
            }
            // Handle volume conversion from String to Long
            if (bookRequest.volume() != null && !bookRequest.volume().trim().isEmpty()) {
                try {
                    // Try to parse as Long first
                    Long volumeNumber = Long.parseLong(bookRequest.volume().trim());
                    book.setVolume(volumeNumber);
                } catch (NumberFormatException e) {
                    // If that fails, try to extract number from string like "Volume 1"
                    String numStr = bookRequest.volume().replaceAll("[^0-9]", "");
                    if (!numStr.isEmpty()) {
                        book.setVolume(Long.parseLong(numStr));
                    } else {
                        // Default to 1 if no number found
                        book.setVolume(1L);
                    }
                }
            } else {
                book.setVolume(1L); // Default volume
            }
            // Parse and set the publication date
            if (bookRequest.publicationDate() != null && !bookRequest.publicationDate().isEmpty()) {
                book.setDate(java.time.LocalDate.parse(bookRequest.publicationDate()));
            } else {
                book.setDate(java.time.LocalDate.now()); // Fallback to current date
            }
            // Set both total and current amount to the number of copies
            book.setTotalAmount(copies);
            book.setCurrentAmount(copies);
            book.setRating(0.0);
            bookRepository.save(book);
        }
        logger.info("Book with title '{}' added successfully with {} copies (ID: {})", bookRequest.title(), copies,
                   existingBook.isPresent() ? existingBook.get().getId() : "new book added to database");
    }

    public void deleteBook(String bookPublicId) {
        Optional<Book> bookOptional = bookRepository.findByPublicId(bookPublicId);
        if (bookOptional.isEmpty()) {
            throw new IllegalArgumentException("Book not found");
        }

        Book book = bookOptional.get();

        // Check if there are any active orders for this book
        Set<Order> activeOrders = orderRepository.findOrdersByBookId(book.getId());
        long activeOrderCount = activeOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.RESERVED || order.getStatus() == OrderStatus.BORROWED)
                .count();

        if (activeOrderCount > 0) {
            throw new IllegalStateException("Cannot delete book with active reservations or borrowed copies");
        }

        reviewRepository.deleteAll(reviewRepository.findReviewsByBookId(book.getId()));
        orderRepository.deleteAll(orderRepository.findOrdersByBookId(book.getId()));
        bookRepository.delete(book);
        logger.info("Book with title '{}' and publicId '{}' deleted successfully", book.getName(), bookPublicId);
    }

    public void tookBook(String orderPublicId) {
        Optional<Order> orderOptional = orderRepository.findByPublicId(orderPublicId);
        if (orderOptional.isEmpty()) {
            throw new IllegalArgumentException("Order not found");
        }
        Order order = orderOptional.get();
        if (order.getStatus() != OrderStatus.RESERVED) {
            logger.info("Attempted to mark order {} as BORROWED but it has status {}", orderPublicId, order.getStatus());
            throw new IllegalStateException("Invalid status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.BORROWED);
        orderRepository.update(order);

        // Add book to borrowed books collection
        User user = order.getUser();
        Book book = order.getBook();
        user.getBorrowedBooks().add(book);
        userRepository.update(user);
        try {
            mailService.sendEmail(
                    List.of(user.getMail()),
                    "Book Taken",
                    "Dear " + user.getUsername() + ",\n\n" +
                            "This is a confirmation that you have successfully borrowed the book \"" + book.getName() + "\" from the library.\n" +
                            "Please make sure to return it by the due date as per library policy.\n\n" +
                            "Thank you,\n" +
                            "Library Team"
            );
        } catch (Exception e) {
            logger.error("Failed to send confirmation email to {}: {}", user.getMail(), e.getMessage());
        }

        logger.info("Order with public ID '{}' marked as BORROWED and book '{}' added to user '{}' borrowed books", 
                orderPublicId, book.getName(), user.getUsername());
    }

    public void returnBook(String orderPublicId) {

        Optional<Order> orderOptional = orderRepository.findByPublicId(orderPublicId);

        if (orderOptional.isEmpty()) {
            throw new IllegalArgumentException("Order not found");
        }

        Order order = orderOptional.get();

        if (order.getStatus() != OrderStatus.BORROWED) {
            logger.info("Attempted to mark order {} as RETURNED but it has status {}", orderPublicId, order.getStatus());
            throw new IllegalStateException("Invalid status: " + order.getStatus());
        }

        User user = order.getUser();
        Book book = order.getBook();

        logger.debug("Returning book: {} by user: {}, borrowed books count before: {}",
                book.getName(), user.getUsername(), user.getBorrowedBooks().size());

        boolean removed = user.getBorrowedBooks().remove(book);
        logger.debug("Book removed from borrowed collection: {}", removed);

        userRepository.update(user);

        try {
            user.getReadBooks().add(book);
            userRepository.update(user);
            logger.debug("Book added to read books collection");
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                logger.info("Book already exists in read books, but removal from borrowed books was successful");
            } else {
                logger.warn("Error adding book to read collection, but book removal from borrowed was successful: {}", e.getMessage());
            }
        }

        logger.debug("Borrowed books count after removal: {}", user.getBorrowedBooks().size());
        orderRepository.delete(order);

        if (book.getCurrentAmount() == 0) {

            Optional<Order> waitingOrderOptional = orderRepository.findFirstWaitingOrderByBookId(book.getId());
            if (waitingOrderOptional.isPresent()) {
                Order waitingOrder = waitingOrderOptional.get();
                waitingOrder.setStatus(OrderStatus.RESERVED);
                waitingOrder.setBorrowDate(LocalDateTime.now().plusDays(1));
                waitingOrder.setDueDate(LocalDateTime.now().plusDays(1).plusDays(waitingOrder.getRequestedDurationInDays()));

                orderRepository.update(waitingOrder);
                logger.info("User {} canceled reservation for book {}, next user in waitlist has been reserved",
                        user.getUsername(), book.getPublicId());
                try {
                    mailService.sendEmail(
                            List.of(waitingOrder.getUser().getMail()),
                            "Book Reservation Confirmation",
                            String.format("""
                                            Dear %s,
                                            
                                            Your reservation for the book '%s' has been confirmed.
                                            Reservation ID: %s
                                            Borrow Date: %s
                                            Due Date: %s
                                            
                                            Please pick up the book within 72 hours, otherwise reservation will be cancelled.
                                            
                                            Thank you for using our library service!
                                            Best regards,
                                            Library Team""",
                                    waitingOrder.getUser().getUsername(),
                                    book.getName(),
                                    waitingOrder.getPublicId(),
                                    waitingOrder.getBorrowDate().toLocalDate(),
                                    waitingOrder.getDueDate().toLocalDate())
                    );
                } catch (Exception e) {
                    logger.error("Failed to send confirmation email to {}: {}", user.getMail(), e.getMessage());
                }
            }
            else {
                book.setCurrentAmount(book.getCurrentAmount()+1);
                bookRepository.update(book);
            }
            logger.info("User {} canceled reservation for book {}, no users in waitlist", user.getUsername(), book.getPublicId());

        }
        else {
            book.setCurrentAmount(book.getCurrentAmount()+1);
            bookRepository.update(book);
        }

        order.setStatus(OrderStatus.RETURNED);
        orderRepository.update(order);


        logger.info("Book '{}' returned by user '{}', moved to read books and order deleted",
                book.getName(), user.getUsername());
    }

    public void banUser(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User user = userOptional.get();

        if (user.getRole() == Role.BOOKKEEPER) {
            throw new IllegalArgumentException("Cannot ban a bookkeeper");
        }

        if (user.getStatus() == UserStatus.BANNED) {
            throw new IllegalArgumentException("User is already banned");
        }

        user.setStatus(UserStatus.BANNED);
        userRepository.update(user);
        try {
            mailService.sendEmail(
                    List.of(user.getMail()),
                    "Library Account Banned",
                    "Dear " + user.getUsername() + ",\n\n" +
                            "We regret to inform you that your library account has been banned due to policy violations or other issues.\n" +
                            "You will not be able to borrow books or access your account until further notice.\n\n" +
                            "If you believe this was a mistake, please contact the library administration.\n\n" +
                            "Sincerely,\n" +
                            "Library Team"
            );
        } catch (Exception e) {
            logger.error("Failed to send ban notification email to {}: {}", user.getMail(), e.getMessage());
        }
        logger.info("User with username '{}' has been banned", username);
    }

    public void unbanUser(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User user = userOptional.get();

        if (user.getRole() == Role.BOOKKEEPER) {
            throw new IllegalArgumentException("Cannot unban a bookkeeper");
        }

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new IllegalArgumentException("User is already unbanned");
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.update(user);
        try {
            mailService.sendEmail(
                    List.of(user.getMail()),
                    "Library Account Unbanned",
                    "Dear " + user.getUsername() + ",\n\n" +
                            "Good news! Your library account has been reactivated. You may now borrow books and access all library services again.\n\n" +
                            "Thank you for your cooperation.\n\n" +
                            "Best regards,\n" +
                            "Library Team"
            );
        } catch (Exception e) {
            logger.error("Failed to send unban notification email to {}: {}", user.getMail(), e.getMessage());
        }
        logger.info("User with username '{}' has been unbanned", username);
    }

    public Set<UserDTO> getUsers() {
        Set<User> users = userRepository.findByRole(Role.USER);
        Set<UserDTO> usersWithStatus = new HashSet<>();
        for (User user : users) {
            usersWithStatus.add(Mappers.convertUserToDTO(user));
        }
        return usersWithStatus;
    }

    public String downloadImage(Part filePart) throws IOException {
        logger.info("Starting image upload process...");

        String submittedFileName = filePart.getSubmittedFileName();
        if (submittedFileName == null || submittedFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("No file name provided");
        }

        String fileName = Path.of(submittedFileName).getFileName().toString();
        String safeFileName = fileName.replaceAll("[^a-zA-Z0-9.\\-]", "_");

        // Add timestamp to make filename unique
        String timestamp = String.valueOf(System.currentTimeMillis());
        String extension = "";
        int lastDot = safeFileName.lastIndexOf('.');
        if (lastDot > 0) {
            extension = safeFileName.substring(lastDot);
            safeFileName = safeFileName.substring(0, lastDot) + "_" + timestamp + extension;
        } else {
            safeFileName = safeFileName + "_" + timestamp;
        }

        logger.info("Original filename: {}, Safe filename: {}", fileName, safeFileName);

        // Try multiple directory options
        Path imagesDir = null;

        // Option 1: Environment variable
        String imageDirEnv = System.getenv("IMAGE_DIR");
        if (imageDirEnv != null && !imageDirEnv.isBlank()) {
            try {
                imagesDir = Paths.get(imageDirEnv);
                logger.info("Using IMAGE_DIR environment variable: {}", imagesDir);
            } catch (Exception e) {
                logger.warn("Invalid IMAGE_DIR environment variable: {}", e.getMessage());
            }
        }

        // Option 2: Webapp images directory (fallback)
        if (imagesDir == null) {
            try {
                // Get the webapp root and create images directory
                String webappRoot = System.getProperty("catalina.base");
                if (webappRoot != null) {
                    imagesDir = Paths.get(webappRoot, "webapps", "LibraryProject_war_exploded", "images");
                    logger.info("Using webapp images directory: {}", imagesDir);
                } else {
                    // Fallback to relative path in src/main/webapp/images
                    imagesDir = Paths.get("src", "main", "webapp", "images");
                    logger.info("Using relative webapp images directory: {}", imagesDir);
                }
            } catch (Exception e) {
                logger.warn("Could not determine webapp directory: {}", e.getMessage());
            }
        }

        // Option 3: Temp directory (last resort)
        if (imagesDir == null) {
            imagesDir = Paths.get(System.getProperty("java.io.tmpdir"), "library_images");
            logger.info("Using temp directory as fallback: {}", imagesDir);
        }

        // Create directory if it doesn't exist
        try {
            if (!Files.exists(imagesDir)) {
                Files.createDirectories(imagesDir);
                logger.info("Created images directory: {}", imagesDir);
            }

            // Check if directory is writable
            if (!Files.isWritable(imagesDir)) {
                throw new IOException("Directory is not writable: " + imagesDir);
            }

        } catch (Exception e) {
            logger.error("Failed to create or access images directory {}: {}", imagesDir, e.getMessage());
            throw new IOException("Cannot access images directory: " + e.getMessage());
        }

        // Write the file
        Path filePath = imagesDir.resolve(safeFileName);
        try {
            logger.info("Writing file to: {}", filePath);
            filePart.write(filePath.toString());

            // Verify file was written
            if (!Files.exists(filePath)) {
                throw new IOException("File was not written successfully");
            }

            long fileSize = Files.size(filePath);
            logger.info("Image file successfully written: {} (size: {} bytes)", filePath, fileSize);

        } catch (Exception e) {
            logger.error("Failed to write image file {}: {}", filePath, e.getMessage());
            throw new IOException("Cannot write image file: " + e.getMessage());
        }

        return safeFileName;
    }

    public Set<BookDTO> getBooks() {
        List<Book> books = bookRepository.findAll();
        Set<BookDTO> bookDTOs = new HashSet<>();
        for (Book book : books) {
            bookDTOs.add(Mappers.mapBookToDTO(book));
        }
        return bookDTOs;
    }

    public Set<OrderDTO> getOrdersByUsername(String username) {
        Set<Order> orders = orderRepository.findOrdersByUsername(username);
        Set<OrderDTO> orderDTOs = new HashSet<>();
        for (Order order : orders) {
            orderDTOs.add(Mappers.mapOrderToDTO(order));
        }
        return orderDTOs;
    }

    public Set<OrderDTO> getAllActiveOrders() {
        Set<Order> orders = orderRepository.findActiveOrders();
        Set<OrderDTO> orderDTOs = new HashSet<>();
        for (Order order : orders) {
            orderDTOs.add(Mappers.mapOrderToDTO(order));
        }
        return orderDTOs;
    }

    public Set<OrderDTO> getOverdueOrders() {
        Set<Order> orders = orderRepository.findOverdueOrders();
        Set<OrderDTO> orderDTOs = new HashSet<>();
        for (Order order : orders) {
            orderDTOs.add(Mappers.mapOrderToDTO(order));
        }
        return orderDTOs;
    }
}
