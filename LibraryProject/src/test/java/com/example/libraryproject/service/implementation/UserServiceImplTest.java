package com.example.libraryproject.service.implementation;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Order;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.OrderStatus;
import com.example.libraryproject.model.enums.UserStatus;
import com.example.libraryproject.repository.*;
import com.example.libraryproject.service.MailService;
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
    private final MailService mailService = mock(MailService.class);
    private User user;

    private Book book1, book2, book3;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        reviewRepository = mock(ReviewRepository.class);
        bookRepository = mock(BookRepository.class);
        orderRepository = mock(OrderRepository.class);

        userServiceImpl = new UserServiceImpl(userRepository, bookRepository, reviewRepository, orderRepository, mailService);

        book1 = new Book(
                "Shadow_Realms",
                "Shadow Realms",
                "Fantasy",
                "Author A",
                LocalDate.of(2020, 5, 10),
                LocalDateTime.now(),
                "A gripping tale of magic and destiny.",
                300L, 1L, 1L, 4.0, ""
        );

        book2 = new Book(
                "Shadow_Realms_II",
                "Shadow Realms II",
                "Fantasy",
                "Author A",
                LocalDate.of(2021, 6, 15),
                LocalDateTime.now(),
                "The saga continues with greater peril.",
                300L, 0L, 2L, 5.0, ""
        );

        book3 = new Book(
                "Echoes_of_Power",
                "Echoes of Power",
                "Fantasy",
                "Author A",
                LocalDate.of(2019, 4, 20),
                LocalDateTime.now(),
                "Ancient secrets resurface to test the realm.",
                300L, 3L, 3L, 3.0, ""
        );

        user = new User("rezi", "1234", "froste3110@gmail.com");
        user.setId(1L);
        user.setPassword(BCrypt.hashpw("1234", BCrypt.gensalt()));
        user.setBorrowedBooks(new HashSet<>());
        user.setReadBooks(new HashSet<>());
        user.setStatus(UserStatus.ACTIVE);

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
        assertDoesNotThrow(() -> userServiceImpl.reserveBook(user.getUsername(), book1.getPublicId(), 2L));

        // Test reservation with non-existent user
        book2.setCurrentAmount(1L);
        assertThrows(IllegalArgumentException.class,
                () -> userServiceImpl.reserveBook("nonexistent", book1.getPublicId(), 2L));

        // Test reservation with non-existent book
        assertThrows(IllegalArgumentException.class,
                () -> userServiceImpl.reserveBook(user.getUsername(), "nonexistent", 2L));
    }

    @Test
    public void testCancelReservation() {
        // Create a mock order
        Order mockOrder = Order.builder()
                .publicId(UUID.randomUUID())
                .borrowDate(LocalDateTime.now().plusDays(1))
                .dueDate(LocalDateTime.now().plusDays(22))
                .requestedDurationInDays(21L) // 22 - 1
                .status(OrderStatus.RESERVED)
                .user(user)
                .book(book1)
                .build();

        Set<Order> userOrders = new HashSet<>();
        userOrders.add(mockOrder);

        // Mock OrderRepository
        when(orderRepository.findOrdersByUserId(1L)).thenReturn(userOrders);

        // Test successful cancellation
        assertDoesNotThrow(() -> userServiceImpl.cancelReservation(user.getUsername(), book1.getPublicId()));

        //return empty set for not reserved book
        when(orderRepository.findOrdersByUserId(1L)).thenReturn(new HashSet<>());

        // Test cancellation when book is not reserved
        assertThrows(IllegalStateException.class,
                () -> userServiceImpl.cancelReservation(user.getUsername(), book3.getPublicId()));

        // Test cancellation with non-existent user
        assertThrows(IllegalStateException.class,
                () -> userServiceImpl.cancelReservation("nonexistent", book1.getPublicId()));

        // Test cancellation with non-existent book
        assertThrows(IllegalStateException.class,
                () -> userServiceImpl.cancelReservation(user.getUsername(), "nonexistent"));
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
        assertThrows(IllegalStateException.class,
                () -> userServiceImpl.reviewBook(user.getUsername(), book2.getPublicId(), 4, "good"));

        // Test successful review for read book
        assertDoesNotThrow(() -> userServiceImpl.reviewBook(user.getUsername(), book1.getPublicId(), 4, "good"));

        // Test review with non-existent user
        assertThrows(IllegalStateException.class,
                () -> userServiceImpl.reviewBook("nonexistent", book1.getPublicId(), 4, "good"));

        // Test review with non-existent book
        assertThrows(IllegalStateException.class,
                () -> userServiceImpl.reviewBook(user.getUsername(), "nonexistent", 4, "good"));

        // Verify that bookRepository.update was called to update the book's rating
        verify(bookRepository, atLeastOnce()).update(any(Book.class));
    }

    @Test
    void test5() throws Exception {
        java.lang.reflect.Method method = UserServiceImpl.class.getDeclaredMethod("calculateAverageRating", String.class);
        method.setAccessible(true);

        Set<com.example.libraryproject.model.entity.Review> reviews = new HashSet<>();
        com.example.libraryproject.model.entity.Review review1 = new com.example.libraryproject.model.entity.Review();
        review1.setPublicId(UUID.randomUUID());
        review1.setRating(4);
        com.example.libraryproject.model.entity.Review review2 = new com.example.libraryproject.model.entity.Review();
        review2.setPublicId(UUID.randomUUID());
        review2.setRating(5);
        reviews.add(review1);
        reviews.add(review2);

        when(reviewRepository.findReviewsByBookPublicId("testBook")).thenReturn(reviews);

        double result = (double) method.invoke(userServiceImpl, "testBook");

        assertEquals(4.5, result);
        verify(reviewRepository).findReviewsByBookPublicId("testBook");
    }

    @Test
    void test6() throws Exception {
        java.lang.reflect.Method method = UserServiceImpl.class.getDeclaredMethod("calculateAverageRating", String.class);
        method.setAccessible(true);

        when(reviewRepository.findReviewsByBookPublicId("emptyBook")).thenReturn(new HashSet<>());

        double result = (double) method.invoke(userServiceImpl, "emptyBook");

        assertEquals(0.0, result);
        verify(reviewRepository).findReviewsByBookPublicId("emptyBook");
    }

    @Test
    void test7() {
        String newBio = "This is my new bio";

        assertDoesNotThrow(() -> userServiceImpl.changeBio("rezi", newBio));

        assertEquals(newBio, user.getBio());
        verify(userRepository).update(user);
    }

    @Test
    void test8() {
        assertThrows(IllegalArgumentException.class, () -> {
            userServiceImpl.changeBio("nonexistent", "new bio");
        });

        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    void test9() {
        com.example.libraryproject.model.dto.UserDTO result = userServiceImpl.getUserInfo("rezi");

        assertNotNull(result);
        assertEquals("rezi", result.username());
        assertEquals(user.getBio(), result.bio());
        assertEquals("ACTIVE", result.status());
        assertEquals("froste3110@gmail.com", result.mail());
    }

    @Test
    void test10() {
        assertThrows(IllegalArgumentException.class, () -> {
            userServiceImpl.getUserInfo("nonexistent");
        });
    }

    @Test
    void test11() {
        when(orderRepository.hasReservation(user.getId(), book1.getId())).thenReturn(true);

        boolean result = userServiceImpl.hasUserReservedBook("rezi", "Shadow_Realms");

        assertTrue(result);
        verify(orderRepository).hasReservation(user.getId(), book1.getId());
    }

    @Test
    void test12() {
        when(orderRepository.hasReservation(user.getId(), book1.getId())).thenReturn(false);

        boolean result = userServiceImpl.hasUserReservedBook("rezi", "Shadow_Realms");

        assertFalse(result);
        verify(orderRepository).hasReservation(user.getId(), book1.getId());
    }

    @Test
    void test13() {
        Order mockOrder = Order.builder()
                .publicId(UUID.randomUUID())
                .borrowDate(LocalDateTime.now().plusDays(1))
                .dueDate(LocalDateTime.now().plusDays(22))
                .requestedDurationInDays(21L)
                .status(OrderStatus.RESERVED)
                .user(user)
                .book(book1)
                .build();

        Set<Order> userOrders = new HashSet<>();
        userOrders.add(mockOrder);

        when(orderRepository.findOrdersByUserId(1L)).thenReturn(userOrders);

        assertDoesNotThrow(() -> userServiceImpl.cancelReservation("rezi", "Shadow_Realms"));

        verify(orderRepository).findOrdersByUserId(1L);
        verify(orderRepository).update(mockOrder);
    }

    @Test
    void test14() {
        when(orderRepository.findOrdersByUserId(1L)).thenReturn(new HashSet<>());

        assertThrows(IllegalStateException.class, () -> {
            userServiceImpl.cancelReservation("rezi", "Shadow_Realms");
        });

        verify(orderRepository).findOrdersByUserId(1L);
        verify(orderRepository, never()).update(any(Order.class));
    }
}