package com.example.libraryproject.service.implementation;

import com.example.libraryproject.model.dto.UserDTO;
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
import com.example.libraryproject.utilities.Mappers;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
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

    /**
     * Calculates the  average rating for a book based on all its reviews
     * @param bookPublicId The public ID of the book
     * @return The average rating, or 0.0 if no reviews exist
     */
    private double calculateAverageRating(String bookPublicId) {
        Set<Review> reviews = reviewRepository.findReviewsByBookPublicId(bookPublicId);
        
        if (reviews.isEmpty()) {
            return 0.0;
        }
        
        double sum = reviews.stream()
                .mapToInt(Review::getRating)
                .sum();
        
        double average = sum / reviews.size();
        
        // Round to 1 decimal place for cleaner display
        return Math.round(average * 10.0) / 10.0;
    }

    public boolean reviewBook(String username, String publicId, int rating, String comment) {
        // Check if user is logged in
        if (username == null) {
            logger.info("Review attempt failed: no user logged in");
            return false;
        }
        
        User user;
        Book book;
        Optional<Book> optionalBook = bookRepository.findByPublicId(publicId);
        if (optionalBook.isEmpty()) {
            logger.info("Book with publicId {} not found", publicId);
            return false;
        }
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            logger.info("User with username {} not found", username);
            return false;
        }
        book = optionalBook.get();
        user = optionalUser.get();

        // COMMENT THIS TO WRITE REVIEWS
       if (!user.getReadBooks().contains(book)) {
           logger.info("User {} hasn't read book {}", username, publicId);
           return false;
       }

        Set<Review> reviews = user.getReviews();

        if(!reviews.stream().filter(r -> r.getBook().equals(book)).toList().isEmpty()) {
            logger.info("User {} already left a review for book {}", username, publicId);
            return false;
        }

        Review review = new Review();
        review.setUser(user);
        review.setPublicId(UUID.randomUUID());
        review.setBook(book);
        review.setRating(rating);
        review.setComment(comment);

        // Save the review to the repository
        reviewRepository.save(review);

        Set<Review> updatedReviews = new HashSet<>(user.getReviews());
        updatedReviews.add(review);
        user.setReviews(updatedReviews);

        userRepository.update(user);
        
        // Recalculate and update book rating
        double newAverageRating = calculateAverageRating(publicId);
        book.setRating(newAverageRating);
        bookRepository.update(book);
        
        logger.info("User {} reviewed book {} with rating {} and comment '{}'. Book rating updated to {}", 
                   username, publicId, rating, comment, newAverageRating);
        return true;
    }

    public void changeBio(String username, String bio){
        User user;
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            logger.info("bio change attempt failed for non-existing user: {}", username);
            throw new IllegalArgumentException("user doesn't exist");
        }
        user = optionalUser.get();

        user.setBio(bio);
        userRepository.update(user);
        logger.info("User {} changed bio successfully", username);
    }

    public boolean reserveBook(String username, String publicId) {
        Optional<Book> optionalBook = bookRepository.findByPublicId(publicId);
        if (optionalBook.isEmpty()) {
            logger.info("Book with publicId {} not found", publicId);
            return false;
        }
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            logger.info("User with username {} not found", username);
            return false;
        }
        Book book = optionalBook.get();
        User user = optionalUser.get();
        if (book.getCurrentAmount() <= 0) {
            logger.info("Book with publicId {} is not available for reservation", publicId);
            return false;
        }

        Set<Order> userOrders = orderRepository.findOrdersByUserId(user.getId());
        boolean hasActiveOrder = userOrders.stream().anyMatch(order -> order.getBook().getPublicId().equals(publicId) &&
                (order.getStatus() == OrderStatus.RESERVED ||
                        order.getStatus() == OrderStatus.BORROWED)
        );
        if (hasActiveOrder) {
            logger.info("User {} already has an active order for book {}", username, publicId);
            return false;
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

        book.setCurrentAmount(book.getCurrentAmount() - 1);
        bookRepository.update(book);
        orderRepository.save(order);
        logger.info("User {} reserved book {} with order ID {}", username, publicId, order.getPublicId());
        return true;
    }

    public boolean cancelReservation(String username, String publicId) {
        Optional<Book> optionalBook = bookRepository.findByPublicId(publicId);
        if (optionalBook.isEmpty()) {
            logger.info("Book with publicId {} not found", publicId);
            return false;
        }
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            logger.info("User with username {} not found", username);
            return false;
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
            return false;
        }

        book.setCurrentAmount(book.getCurrentAmount() + 1);
        bookRepository.update(book);
        orderRepository.delete(reservation.get());
        logger.info("User {} canceled reservation for book {}", username, publicId);
        return true;
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

    public UserDTO getUserInfo(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("user doesn't exist");
        }

        return Mappers.convertUser(user.get());
    }

    @Override
    public boolean hasUserReservedBook(String username, String bookId) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            logger.info("User with username {} not found", username);
            return false;
        }
        
        Optional<Book> optionalBook = bookRepository.findByPublicId(bookId);
        if (optionalBook.isEmpty()) {
            logger.info("Book with publicId {} not found", bookId);
            return false;
        }
        
        User user = optionalUser.get();
        Set<Order> userOrders = orderRepository.findOrdersByUserId(user.getId());
        
        return userOrders.stream().anyMatch(order -> 
            order.getBook().getPublicId().equals(bookId) && 
            order.getStatus() == OrderStatus.RESERVED
        );
    }


}
