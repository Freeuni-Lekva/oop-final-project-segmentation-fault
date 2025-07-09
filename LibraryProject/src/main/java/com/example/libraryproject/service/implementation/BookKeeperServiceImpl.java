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
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.service.BookKeeperService;
import com.example.libraryproject.utilities.Mappers;
import jakarta.servlet.http.Part;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class BookKeeperServiceImpl implements BookKeeperService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
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
            book.setImageUrl(bookRequest.title().replaceAll("[^a-zA-Z0-9.\\-]", "_") + ".jpg");
            book.setVolume(bookRequest.volume());
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
        logger.info("Book with title '{}' added successfully with {} copies", bookRequest.title(), copies);
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
        
        // Completely delete the book from the database
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
        logger.debug("book removed from borrowed books");

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

        book.setCurrentAmount(book.getCurrentAmount() + 1);
        bookRepository.update(book);
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
        
        user.setStatus(UserStatus.BANNED);
        userRepository.update(user);
        logger.info("User with username '{}' has been banned", username);
    }

    public void unbanUser(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if(userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User user = userOptional.get();

        if (user.getRole() == Role.BOOKKEEPER) {
            throw new IllegalArgumentException("Cannot unban a bookkeeper");
        }
        
        user.setStatus(UserStatus.ACTIVE);
        userRepository.update(user);
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

    public String downloadImage(Part filePart ) throws IOException  {
        String submittedFileName = Path.of(filePart.getSubmittedFileName()).getFileName().toString();
        String safeFileName = submittedFileName.replaceAll("[^a-zA-Z0-9.\\-]", "_");

        String imageDirEnv = System.getenv("IMAGE_DIR");
        if (imageDirEnv == null || imageDirEnv.isBlank()) {
            throw new IllegalStateException("Environment variable 'IMAGE_DIR' is not set or is empty. Please configure it to specify the image directory.");
        }
        Path imagesDir = Paths.get(imageDirEnv);
        if (!Files.exists(imagesDir)) {
            Files.createDirectories(imagesDir);
        }

        Path filePath = imagesDir.resolve(safeFileName);
        filePart.write(filePath.toString());

        logger.info("Downloading image file: {}", filePath);

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
