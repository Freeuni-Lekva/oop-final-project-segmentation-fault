package com.example.libraryproject.service;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.ReviewRepository;
import com.example.libraryproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


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
        if (book.getAmountInLib() <= 0) {
            logger.info("Book with publicId {} is not available for reservation", publicId);
            throw new IllegalStateException("book not in storage");
        }

        book.setAmountInLib(book.getAmountInLib() - 1);

        Set<Book> updatedBorrowedBooks = new HashSet<>(user.getBorrowedBooks());
        updatedBorrowedBooks.add(book);
        user.setBorrowedBooks(updatedBorrowedBooks);

        bookRepository.update(book);
        userRepository.update(user);
        logger.info("User {} reserved book {}", username, publicId);
    }

    public void cancelReservation(String username, String publicId) {
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
        if (!user.getBorrowedBooks().contains(book)) {
            logger.info("User {} does not have book {} reserved", username, publicId);
            throw new IllegalStateException("user doesn't have this book reserved");
        }

        user.getBorrowedBooks().remove(book);
        book.setAmountInLib(book.getAmountInLib() + 1);

        userRepository.update(user);
        bookRepository.update(book);
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
