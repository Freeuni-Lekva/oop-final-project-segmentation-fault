package com.example.libraryproject.service;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.ReviewRepository;
import com.example.libraryproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    public void reviewBook(String username, String publicId, int rating, String comment) {
        User user;
        Book book;
        Optional<Book> optionalBook = bookRepository.findByPublicId(publicId);
        if (optionalBook.isEmpty()) {
            throw new IllegalArgumentException("book doesn't exist");
        }
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("user doesn't exist");
        }
        book = optionalBook.get();
        user = optionalUser.get();
        
        if (!user.getReadBooks().contains(book)) {
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
    }

    public void reserveBook(String username, String publicId) {
        User user;
        Book book;
        Optional<Book> optionalBook = bookRepository.findByPublicId(publicId);
        if (optionalBook.isEmpty()) {
            throw new IllegalArgumentException("book doesn't exist");
        }
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("user doesn't exist");
        }
        book = optionalBook.get();
        user = optionalUser.get();
        if (book.getAmountInLib() <= 0) {
            throw new IllegalStateException("book not in storage");
        }

        book.setAmountInLib(book.getAmountInLib() - 1);

        Set<Book> updatedBorrowedBooks = new HashSet<>(user.getBorrowedBooks());
        updatedBorrowedBooks.add(book);
        user.setBorrowedBooks(updatedBorrowedBooks);

        bookRepository.update(book);
        userRepository.update(user);
    }

    public void cancelReservation(String username, String publicId) {
        User user;
        Book book;
        Optional<Book> optionalBook = bookRepository.findByPublicId(publicId);
        if (optionalBook.isEmpty()) {
            throw new IllegalArgumentException("book doesn't exist");
        }
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("user doesn't exist");
        }
        book = optionalBook.get();
        user = optionalUser.get();
        if (!user.getBorrowedBooks().contains(book)) {
            throw new IllegalStateException("user doesn't have this book reserved");
        }

        user.getBorrowedBooks().remove(book);
        book.setAmountInLib(book.getAmountInLib() + 1);

        userRepository.update(user);
        bookRepository.update(book);
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        User user;
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("user doesn't exist");
        }
        user = optionalUser.get();
        if (!user.getPassword().equals(oldPassword)) {
            throw new IllegalArgumentException("password is incorrect");
        }

        user.setPassword(newPassword);
        userRepository.update(user);
    }
}
