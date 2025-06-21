package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.BookAdditionRequest;
import com.example.libraryproject.model.dto.UserDTO;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Order;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.OrderStatus;
import com.example.libraryproject.model.enums.UserStatus;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.OrderRepository;
import com.example.libraryproject.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Part;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.example.libraryproject.configuration.ApplicationProperties.IMAGE_DIR;

@RequiredArgsConstructor
public class BookKeeperService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private static final Logger logger = LoggerFactory.getLogger(BookKeeperService.class);


    public void addBook(BookAdditionRequest bookRequest) {
        Optional<Book> existingBook = bookRepository.findByTitle(bookRequest.title());

        if (existingBook.isPresent()) {
            Book bookInLibrary = existingBook.get();
            bookInLibrary.setAmountInLib(bookInLibrary.getAmountInLib() + 1);
            bookRepository.update(bookInLibrary);
        } else {
            Book book = new Book();
            book.setName(bookRequest.title());
            book.setAuthor(bookRequest.author());
            book.setDescription(bookRequest.description());
            book.setGenre(bookRequest.genre());
            book.setPublicId(bookRequest.title().replaceAll("[^a-zA-Z0-9.\\-]", "_"));
            book.setImageUrl(bookRequest.imageUrl());

            book.setAmountInLib(1L);
            bookRepository.save(book);
        }
        logger.info("Book with title '{}' added successfully", bookRequest.title());
    }

    public void deleteBook(String bookPublicId) {
        Optional<Book> bookOptional = bookRepository.findByPublicId(bookPublicId);
        if (bookOptional.isEmpty()) {
            throw new IllegalArgumentException("Book not found");
        }
        Book book = bookOptional.get();
        Optional<Book> existingBook = bookRepository.findByTitle(book.getName());
        if (existingBook.isPresent()) {
            Book bookInLibrary = existingBook.get();
            long amount = bookInLibrary.getAmountInLib();
            if (amount > 1) {
                bookInLibrary.setAmountInLib(amount - 1);
                bookRepository.update(bookInLibrary);
            } else {
                bookRepository.delete(bookInLibrary);
            }
            logger.info("Book with title '{}' deleted successfully", book.getName());
        } else {
            logger.info("Attempted to delete a book that does not exist in the library: {}", book.getName());
            throw new IllegalArgumentException("Book not found. Try again.");
        }
    }

    public void tookBook(String orderPublicId) {
        Optional<Order> orderOptional = orderRepository.findByPublicId(orderPublicId);
        if (orderOptional.isEmpty()) {
            throw new IllegalArgumentException("Order not found");
        }
        Order order = orderOptional.get();

        order.setStatus(OrderStatus.BORROWED);
        Book book = order.getBook();
        book.setAmountInLib(book.getAmountInLib() - 1);

        bookRepository.update(book);
        orderRepository.update(order);
        logger.info("Order with public ID '{}' marked as BORROWED", orderPublicId);
    }

    public void banUser(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User user = userOptional.get();
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
        user.setStatus(UserStatus.ACTIVE);
        userRepository.update(user);
        logger.info("User with username '{}' has been unbanned", username);
    }

    public Set<UserDTO> getUsers() {
        Set<User> users = userRepository.findAll();
        Set<UserDTO> usersWithStatus = new HashSet<>();
        for (User user : users) {
            usersWithStatus.add(UserDTO.convertUser(user));
        }
        return usersWithStatus;
    }

    public String downloadImage(Part filePart, String contextPath) throws IOException, ServletException {
        String submittedFileName = Path.of(filePart.getSubmittedFileName()).getFileName().toString();
        String safeFileName = submittedFileName.replaceAll("[^a-zA-Z0-9.\\-]", "_");

        Path imagesDir = Paths.get(IMAGE_DIR);
        if (!Files.exists(imagesDir)) {
            Files.createDirectories(imagesDir);
        }

        Path filePath = imagesDir.resolve(safeFileName);
        filePart.write(filePath.toString());

        logger.info("Downloading image file: {}", filePath);

        return contextPath + "/images/" + safeFileName;
    }
}
