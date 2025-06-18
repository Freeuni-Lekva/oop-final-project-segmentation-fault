package com.example.libraryproject.service;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.repository.BookKeeperRepository;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.ReviewRepository;
import com.example.libraryproject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    private UserRepository userRepository;
    private BookKeeperRepository bookKeeperRepository;
    private ReviewRepository reviewRepository;
    private UserService userService;
    private BookRepository bookRepository;
    private User user;

    private Book book1, book2, book3;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        bookKeeperRepository = mock(BookKeeperRepository.class);
        reviewRepository = mock(ReviewRepository.class);
        bookRepository = mock(BookRepository.class); // This was missing!
        userService = new UserService(userRepository, bookRepository, reviewRepository);

        book1 = new Book(
                "Shadow_Realms",
                "Shadow Realms",
                "Fantasy",
                "Author A",
                LocalDate.of(2020, 5, 10),
                "A gripping tale of magic and destiny.",
                1L, 1L, 4L, ""
        );

        book2 = new Book(
                "Shadow_Realms_II",
                "Shadow Realms II",
                "Fantasy",
                "Author A",
                LocalDate.of(2021, 6, 15),
                "The saga continues with greater peril.",
                2L, 0L, 5L, ""
        );

        book3 = new Book(
                "Echoes_of_Power",
                "Echoes of Power",
                "Fantasy",
                "Author A",
                LocalDate.of(2019, 4, 20),
                "Ancient secrets resurface to test the realm.",
                3L, 3L, 3L, ""
        );

        user = new User("rezi", "1234");
        user.setBorrowedBooks(new HashSet<>());
        user.setReadBooks(new HashSet<>());

        // Mock repository behavior
        when(userRepository.findByUsername("rezi")).thenReturn(Optional.of(user));
        when(bookRepository.findByPublicId("Shadow_Realms")).thenReturn(Optional.of(book1));
        when(bookRepository.findByPublicId("Shadow_Realms_II")).thenReturn(Optional.of(book2));
        when(bookRepository.findByPublicId("Echoes_of_Power")).thenReturn(Optional.of(book3));

        // Mock for non-existent entities
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(bookRepository.findByPublicId("nonexistent")).thenReturn(Optional.empty());
    }

    @Test
    public void testReserve() {
        // Test successful reservation
        assertDoesNotThrow(() -> userService.reserveBook(user.getUsername(), book1.getPublicId()));

        // Test reservation when book is already reserved (book2 has 0 amount)
        assertThrows(IllegalStateException.class,
                () -> userService.reserveBook(user.getUsername(), book2.getPublicId()));

        // Test reservation with non-existent user
        assertThrows(IllegalArgumentException.class,
                () -> userService.reserveBook("nonexistent", book1.getPublicId()));

        // Test reservation with non-existent book
        assertThrows(IllegalArgumentException.class,
                () -> userService.reserveBook(user.getUsername(), "nonexistent"));
    }

    @Test
    public void testCancelReservation() {
        // First reserve a book
        assertDoesNotThrow(() -> userService.reserveBook(user.getUsername(), book1.getPublicId()));

        // Test successful cancellation
        assertDoesNotThrow(() -> userService.cancelReservation(user.getUsername(), book1.getPublicId()));

        // Test cancellation when book is not reserved
        assertThrows(IllegalStateException.class,
                () -> userService.cancelReservation(user.getUsername(), book3.getPublicId()));

        // Test cancellation with non-existent user
        assertThrows(IllegalArgumentException.class,
                () -> userService.cancelReservation("nonexistent", book1.getPublicId()));

        // Test cancellation with non-existent book
        assertThrows(IllegalArgumentException.class,
                () -> userService.cancelReservation(user.getUsername(), "nonexistent"));
    }

    @Test
    public void testChangePassword() {
        // Test with wrong old password
        assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword(user.getUsername(), "wrongPassword", "newPassword"));

        // Test with correct old password
        assertDoesNotThrow(() -> userService.changePassword(user.getUsername(), "1234", "newPassword"));
        assertEquals("newPassword", user.getPassword());

        // Test with non-existent user
        assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword("nonexistent", "1234", "newPassword"));
    }

    @Test
    public void testReview() {
        // Add book1 to user's read books
        Set<Book> updatedReadBooks = new HashSet<>(user.getReadBooks());
        updatedReadBooks.add(book1);
        user.setReadBooks(updatedReadBooks);

        // Test review for book not read by user
        assertThrows(IllegalStateException.class,
                () -> userService.reviewBook(user.getUsername(), book2.getPublicId(), 4, "good"));

        // Test successful review for read book
        assertDoesNotThrow(() -> userService.reviewBook(user.getUsername(), book1.getPublicId(), 4, "good"));

        // Test review with non-existent user
        assertThrows(IllegalArgumentException.class,
                () -> userService.reviewBook("nonexistent", book1.getPublicId(), 4, "good"));

        // Test review with non-existent book
        assertThrows(IllegalArgumentException.class,
                () -> userService.reviewBook(user.getUsername(), "nonexistent", 4, "good"));
    }
}