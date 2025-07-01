package com.example.libraryproject.service.implementation;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Order;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.OrderStatus;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.OrderRepository;
import com.example.libraryproject.repository.ReviewRepository;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);


    public void reviewBook(String username, String publicId, int rating, String comment) {
        User user;
        Book book;
        Optional<Book> optionalBook = bookRepository.findByPublicId(publicId);
        if (optionalBook.isEmpty()) {
            logger.info("Book with publicId {} not found", publicId);
            throw new IllegalArgumentException("book doesn't exist");
        }
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            logger.info("User with username {} not found", username);
            throw new IllegalArgumentException("user doesn't exist");
        }
        book = optionalBook.get();
        user = optionalUser.get();
        
        if (!user.getReadBooks().contains(book)) {
            logger.info("User {} hasn't read book {}", username, publicId);
            throw new IllegalStateException("user hasn't read the book");
        }

        Review review = new Review();
        review.setUser(user);
        review.setPublicId(UUID.randomUUID());
        review.setBook(book);
        review.setRating(rating);
        review.setComment(comment);

        Set<Review> updatedReviews = new HashSet<>(user.getReviews());
        updatedReviews.add(review);
        user.setReviews(updatedReviews);

        userRepository.update(user);
        reviewRepository.save(review);
        logger.info("User {} reviewed book {} with rating {} and comment '{}'", username, publicId, rating, comment);
    }

    public void reserveBook(String username, String publicId) {
        Optional<Book> optionalBook = bookRepository.findByPublicId(publicId);
        if (optionalBook.isEmpty()) {
            logger.info("Book with publicId {} not found", publicId);
            throw new IllegalArgumentException("book doesn't exist");
        }
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            logger.info("User with username {} not found", username);
            throw new IllegalArgumentException("user doesn't exist");
        }
        Book book = optionalBook.get();
        User user = optionalUser.get();
        if (book.getTotalAmount() <= 0) {
            logger.info("Book with publicId {} is not available for reservation", publicId);
            throw new IllegalStateException("book not in storage");
        }

        Set<Order> userOrders = orderRepository.findOrdersByUserId(user.getId());
        boolean hasActiveOrder = userOrders.stream().anyMatch(order -> order.getBook().getPublicId().equals(publicId) &&
                (order.getStatus() == OrderStatus.RESERVED ||
                        order.getStatus() == OrderStatus.BORROWED)
        );
        if (hasActiveOrder) {
            logger.info("User {} already has an active order for book {}", username, publicId);
            throw new IllegalStateException("user already has an active order for this book");
        }

        LocalDateTime now = LocalDateTime.now();
        Order order = new Order(
                UUID.randomUUID(),
                now.plusDays(1),
                now.plusDays(22),
                OrderStatus.RESERVED,
                user,
                book
        );

        book.setTotalAmount(book.getTotalAmount() - 1);
        bookRepository.update(book);
        orderRepository.save(order);
        logger.info("User {} reserved book {} with order ID {}", username, publicId, order.getPublicId());
    }

    public void cancelReservation(String username, String publicId) {
        Optional<Book> optionalBook = bookRepository.findByPublicId(publicId);
        if (optionalBook.isEmpty()) {
            logger.info("Book with publicId {} not found", publicId);
            throw new IllegalArgumentException("book doesn't exist");
        }
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            logger.info("User with username {} not found", username);
            throw new IllegalArgumentException("user doesn't exist");
        }
        Book book = optionalBook.get();
        User user = optionalUser.get();

        Set<Order> userOrders = orderRepository.findOrdersByUserId(user.getId());
        Optional<Order> reservation = userOrders.stream()
                .filter(order ->
                        order.getBook().getPublicId().equals(publicId) &&
                                order.getStatus() == OrderStatus.RESERVED
                )
                .findFirst();

        if (reservation.isEmpty()) {
            logger.info("User {} does not have book {} reserved", username, publicId);
            throw new IllegalStateException("book is not reserved");
        }

        book.setTotalAmount(book.getTotalAmount() + 1);
        bookRepository.update(book);
        orderRepository.delete(reservation.get());
        logger.info("User {} canceled reservation for book {}", username, publicId);
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        User user;
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            logger.info("Password change attempt failed for non-existing user: {}", username);
            throw new IllegalArgumentException("user doesn't exist");
        }
        user = optionalUser.get();

        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            logger.info("Password change attempt failed for user {}: incorrect old password", username);
            throw new IllegalArgumentException("password is incorrect");
        }

        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        userRepository.update(user);
        logger.info("User {} changed password successfully", username);
    }
}
