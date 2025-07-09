package com.example.libraryproject.service.implementation;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Order;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.OrderStatus;
import com.example.libraryproject.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {
    private UserRepository userRepository;
    private ReviewRepository reviewRepository;
    private UserServiceImpl userServiceImpl;
    private BookRepository bookRepository;
    private OrderRepository orderRepository;
    private User user;

    private Book book1, book2, book3;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        reviewRepository = mock(ReviewRepository.class);
        bookRepository = mock(BookRepository.class);
        orderRepository = mock(OrderRepository.class);
        userServiceImpl = new UserServiceImpl(userRepository, bookRepository, reviewRepository, orderRepository);

        book1 = new Book(
                "Shadow_Realms",
                "Shadow Realms",
                "Fantasy",
                "Author A",
                LocalDate.of(2020, 5, 10),
                "A gripping tale of magic and destiny.",
                300L, 1L, 1L, 4.0, ""
        );

        book2 = new Book(
                "Shadow_Realms_II",
                "Shadow Realms II",
                "Fantasy",
                "Author A",
                LocalDate.of(2021, 6, 15),
                "The saga continues with greater peril.",
                300L, 0L, 2L, 5.0, ""
        );

        book3 = new Book(
                "Echoes_of_Power",
                "Echoes of Power",
                "Fantasy",
                "Author A",
                LocalDate.of(2019, 4, 20),
                "Ancient secrets resurface to test the realm.",
                300L, 3L, 3L, 3.0, ""
        );

        user = new User("rezi", "1234");
        user.setId(1L);
        user.setPassword(BCrypt.hashpw("1234", BCrypt.gensalt()));
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

        // Mock OrderRepository
        when(orderRepository.findOrdersByUserId(1L)).thenReturn(new HashSet<>());
    }

    @Test
    public void testReserve() {
        // Test successful reservation
        assertDoesNotThrow(() -> userServiceImpl.reserveBook(user.getUsername(), book1.getPublicId()));

        book2.setCurrentAmount(0L);
        assertFalse(userServiceImpl.reserveBook(user.getUsername(), book2.getPublicId()));

        // Test reservation with non-existent user
        book2.setCurrentAmount(1L);
        assertFalse(userServiceImpl.reserveBook("nonexistent", book1.getPublicId()));

        // Test reservation with non-existent book
        assertFalse( userServiceImpl.reserveBook(user.getUsername(), "nonexistent"));
    }

    @Test
    public void testCancelReservation() {
        // Create a mock order
        Order mockOrder = new Order(
                UUID.randomUUID(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(22),
                OrderStatus.RESERVED,
                user,
                book1
        );

        Set<Order> userOrders = new HashSet<>();
        userOrders.add(mockOrder);

        // Mock OrderRepository
        when(orderRepository.findOrdersByUserId(1L)).thenReturn(userOrders);

        // Test successful cancellation
        assertDoesNotThrow(() -> userServiceImpl.cancelReservation(user.getUsername(), book1.getPublicId()));

        //return empty set for not reserved book
        when(orderRepository.findOrdersByUserId(1L)).thenReturn(new HashSet<>());

        // Test cancellation when book is not reserved
        assertFalse(userServiceImpl.cancelReservation(user.getUsername(), book3.getPublicId()));

        // Test cancellation with non-existent user
        assertFalse(userServiceImpl.cancelReservation("nonexistent", book1.getPublicId()));

        // Test cancellation with non-existent book
        assertFalse(userServiceImpl.cancelReservation(user.getUsername(), "nonexistent"));
    }

    @Test
    public void testChangePassword() {
        // Test with wrong old password
        assertThrows(IllegalArgumentException.class,
                () -> userServiceImpl.changePassword(user.getUsername(), "wrongPassword", "newPassword"));

        // Test with correct old password
        assertDoesNotThrow(() -> userServiceImpl.changePassword(user.getUsername(), "1234", "newPassword"));
        assertTrue(BCrypt.checkpw("newPassword", user.getPassword()));

        // Test with non-existent user
        assertThrows(IllegalArgumentException.class,
                () -> userServiceImpl.changePassword("nonexistent", "1234", "newPassword"));
    }

    @Test
    public void testReview() {
        // Add book1 to user's read books
        Set<Book> updatedReadBooks = new HashSet<>(user.getReadBooks());
        updatedReadBooks.add(book1);
        user.setReadBooks(updatedReadBooks);

        // Test review for book not read by user
        assertFalse(userServiceImpl.reviewBook(user.getUsername(), book2.getPublicId(), 4, "good"));

        // Test successful review for read book
        assertTrue(userServiceImpl.reviewBook(user.getUsername(), book1.getPublicId(), 4, "good"));

        // Test review with non-existent user
        assertFalse(userServiceImpl.reviewBook("nonexistent", book1.getPublicId(), 4, "good"));

        // Test review with non-existent book
        assertFalse(userServiceImpl.reviewBook(user.getUsername(), "nonexistent", 4, "good"));
        
        // Verify that bookRepository.update was called to update the book's rating
        verify(bookRepository, atLeastOnce()).update(any(Book.class));
    }
}