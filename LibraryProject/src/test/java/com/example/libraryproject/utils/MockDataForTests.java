package com.example.libraryproject.utils;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Order;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.OrderStatus;
import com.example.libraryproject.model.enums.Role;
import com.example.libraryproject.model.enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

public class MockDataForTests {

    public static Book createTestBook(String name, String author, String genre, double rating, Long originalAmount, Long currentAmount, String imageUrl) {
        return new Book(
                name.replaceAll("[^a-zA-Z0-9.\\-]", "_"),
                name,
                genre,
                author,
                LocalDate.of(2023, 4, 1),
                "Test description for " + name,
                300L,
                originalAmount,
                currentAmount,
                rating,
                imageUrl
        );
    }

    public static Book createTestBook() {
        return createTestBook("test-book-id", "Test Book");
    }

    public static Book createTestBook(String publicId, String name) {
        Book book = new Book();
        book.setPublicId(publicId);
        book.setName(name);
        book.setAuthor("Test Author");
        book.setGenre("Fiction");
        book.setDescription("Test Description");
        book.setDate(LocalDate.now());
        book.setVolume(200L);
        book.setTotalAmount(5L);
        book.setCurrentAmount(3L);
        book.setRating(4.5);
        book.setImageUrl("test.jpg");
        return book;
    }

    public static Book createTestBook(String publicId, String name, String author, String genre) {
        Book book = new Book();
        book.setPublicId(publicId);
        book.setName(name);
        book.setAuthor(author);
        book.setGenre(genre);
        book.setDescription("Test description");
        book.setDate(LocalDate.now());
        book.setVolume(300L);
        book.setTotalAmount(5L);
        book.setCurrentAmount(3L);
        book.setRating(4.0);
        book.setImageUrl("test.jpg");
        return book;
    }

    public static User createTestUser() {
        return createTestUser("testuser", "password");
    }

    public static User createTestUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setMail(username + "@example.com");
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setBio("Test bio");
        user.setBorrowedBooks(new HashSet<>());
        user.setReadBooks(new HashSet<>());
        user.setReviews(new HashSet<>());
        user.setReviewCount(0L);
        return user;
    }

    public static User createTestUserWithEmail(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("password123");
        user.setMail(email);
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setBio("");
        user.setBorrowedBooks(new HashSet<>());
        user.setReadBooks(new HashSet<>());
        user.setReviewCount(0L);
        return user;
    }

    public static Order createTestOrder() {
        Order order = new Order();
        order.setPublicId(UUID.randomUUID());
        order.setCreateDate(LocalDateTime.now());
        order.setDueDate(LocalDateTime.now().plusDays(14));
        order.setRequestedDurationInDays(14L);
        order.setStatus(OrderStatus.RESERVED);
        order.setUser(createTestUser());
        order.setBook(createTestBook());
        return order;
    }

    public static Order createTestOrder(Long id, User user, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setPublicId(UUID.randomUUID());
        order.setCreateDate(LocalDateTime.now());
        order.setRequestedDurationInDays(7L);
        order.setStatus(status);
        order.setUser(user);
        order.setBook(createTestBook("Book1", "Author1", "Fiction", 4.0, 1L, 1L, "book1.jpg"));
        order.setReturnDate(LocalDateTime.now().plusDays(7));
        return order;
    }

    public static Review createTestReview(String username, int rating, String comment) {
        Review review = new Review();
        review.setPublicId(UUID.randomUUID());
        review.setRating(rating);
        review.setComment(comment);

        User user = new User();
        user.setUsername(username);
        review.setUser(user);

        Book book = createTestBook("test-book", "Test Book", "Test Author", "Test Genre");
        review.setBook(book);

        return review;
    }
}
