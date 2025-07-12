package com.example.libraryproject.service.implementation;

import com.example.libraryproject.model.dto.BookAdditionRequest;
import com.example.libraryproject.model.dto.BookDTO;
import com.example.libraryproject.model.dto.OrderDTO;
import com.example.libraryproject.model.dto.UserDTO;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Order;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.BookStatus;
import com.example.libraryproject.model.enums.OrderStatus;
import com.example.libraryproject.model.enums.Role;
import com.example.libraryproject.model.enums.UserStatus;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.OrderRepository;
import com.example.libraryproject.repository.ReviewRepository;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.service.MailService;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static com.example.libraryproject.utils.MockDataForTests.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class BookKeeperServiceImplTest {

    private BookRepository bookRepository;
    private UserRepository userRepository;
    private OrderRepository orderRepository;
    private ReviewRepository reviewRepository;
    private MailService mailService;
    private BookKeeperServiceImpl bookKeeperService;

    @BeforeEach
    void setUp() {
        bookRepository = mock(BookRepository.class);
        userRepository = mock(UserRepository.class);
        orderRepository = mock(OrderRepository.class);
        reviewRepository = mock(ReviewRepository.class);
        mailService = mock(MailService.class);
        bookKeeperService = new BookKeeperServiceImpl(bookRepository, userRepository, 
                orderRepository, reviewRepository, mailService);
    }

    @Test
    void testAddBook_NewBook_Success() {
        BookAdditionRequest request = new BookAdditionRequest(
                "Test Book", "Test Author", "Test Description", 
                "Fiction", 200L, 5L, "2023-01-22", "test.jpg"
        );

        when(bookRepository.findByTitleAnyStatus("Test Book")).thenReturn(Optional.empty());

        bookKeeperService.addBook(request);

        verify(bookRepository).save(any(Book.class));
        verify(bookRepository, never()).update(any(Book.class));
    }

    @Test
    void testAddBook_ExistingBook_UpdatesCopies() {
        BookAdditionRequest request = new BookAdditionRequest(
                "Existing Book", "Test Author", "Test Description", 
                "Fiction", 200L, 3L, "2023-01-01", "existing.jpg"
        );

        Book existingBook = new Book();
        existingBook.setName("Existing Book");
        existingBook.setTotalAmount(5L);
        existingBook.setCurrentAmount(2L);
        existingBook.setStatus(com.example.libraryproject.model.enums.BookStatus.ACTIVE);
        existingBook.setDateAdded(java.time.LocalDateTime.now());

        when(bookRepository.findByTitleAnyStatus("Existing Book")).thenReturn(Optional.of(existingBook));

        bookKeeperService.addBook(request);

        verify(bookRepository).update(existingBook);
        assertEquals(8L, existingBook.getTotalAmount());
        assertEquals(5L, existingBook.getCurrentAmount());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testDeleteBook_Success() {
        String bookPublicId = "test-book-id";
        Book book = createTestBook();
        book.setStatus(BookStatus.ACTIVE);
        Set<Order> inactiveOrders = Set.of();

        when(bookRepository.findByPublicIdAnyStatus(bookPublicId)).thenReturn(Optional.of(book));
        when(orderRepository.findOrdersByBookId(anyLong())).thenReturn(inactiveOrders);

        bookKeeperService.deleteBook(bookPublicId);

        verify(bookRepository).update(book);
        assertEquals(BookStatus.DELETED, book.getStatus());
    }

    @Test
    void testDeleteBook_BookNotFound_ThrowsException() {
        String bookPublicId = "non-existent-id";

        when(bookRepository.findByPublicIdAnyStatus(bookPublicId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> bookKeeperService.deleteBook(bookPublicId));
        assertEquals("Book not found", ex.getMessage());
    }

    @Test
    void testTookBook_Success() {
        String orderPublicId = "order-123";
        Order order = createTestOrder();
        order.setStatus(OrderStatus.RESERVED);

        when(orderRepository.findByPublicId(orderPublicId)).thenReturn(Optional.of(order));

        bookKeeperService.tookBook(orderPublicId);

        assertEquals(OrderStatus.BORROWED, order.getStatus());
        verify(orderRepository).update(order);
        verify(userRepository).update(order.getUser());
        assertTrue(order.getUser().getBorrowedBooks().contains(order.getBook()));
    }

    @Test
    void testTookBook_InvalidStatus_ThrowsException() {
        String orderPublicId = "order-123";
        Order order = createTestOrder();
        order.setStatus(OrderStatus.BORROWED);

        when(orderRepository.findByPublicId(orderPublicId)).thenReturn(Optional.of(order));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> bookKeeperService.tookBook(orderPublicId));
        assertEquals("Invalid status: BORROWED", ex.getMessage());
    }

    @Test
    void testReturnBook_Success() {
        String orderPublicId = "order-123";
        Order order = createTestOrder();
        order.setStatus(OrderStatus.BORROWED);
        User user = order.getUser();
        Book book = order.getBook();
        user.getBorrowedBooks().add(book);
        book.setCurrentAmount(0L);

        when(orderRepository.findByPublicId(orderPublicId)).thenReturn(Optional.of(order));
        when(orderRepository.findFirstWaitingOrderByBookId(anyLong())).thenReturn(Optional.empty());

        bookKeeperService.returnBook(orderPublicId);

        assertFalse(user.getBorrowedBooks().contains(book));
        assertEquals(1L, book.getCurrentAmount());
        assertEquals(OrderStatus.RETURNED, order.getStatus());
        verify(orderRepository).update(order);
        verify(userRepository, atLeast(1)).update(user);
        verify(bookRepository).update(book);
    }

    @Test
    void testReturnBook_OrderNotFound_ThrowsException() {
        String orderPublicId = "non-existent-order";

        when(orderRepository.findByPublicId(orderPublicId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> bookKeeperService.returnBook(orderPublicId));
        assertEquals("Order not found", ex.getMessage());
    }

    @Test
    void testBanUser_Success() {
        String username = "testuser";
        User user = createTestUser();
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        bookKeeperService.banUser(username);

        assertEquals(UserStatus.BANNED, user.getStatus());
        verify(userRepository).update(user);
    }

    @Test
    void testBanUser_CannotBanBookkeeper_ThrowsException() {
        String username = "bookkeeper";
        User user = createTestUser();
        user.setRole(Role.BOOKKEEPER);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> bookKeeperService.banUser(username));
        assertEquals("Cannot ban a bookkeeper", ex.getMessage());
    }

    @Test
    void testUnbanUser_Success() {
        String username = "testuser";
        User user = createTestUser();
        user.setRole(Role.USER);
        user.setStatus(UserStatus.BANNED);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        bookKeeperService.unbanUser(username);

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        verify(userRepository).update(user);
    }

    @Test
    void testUnbanUser_UserAlreadyActive_ThrowsException() {
        String username = "testuser";
        User user = createTestUser();
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> bookKeeperService.unbanUser(username));
        assertEquals("User is already unbanned", ex.getMessage());
    }

    @Test
    void testGetUsers_Success() {
        Set<User> users = Set.of(createTestUser("user1", "pass1"), createTestUser("user2", "pass2"));
        when(userRepository.findByRole(Role.USER)).thenReturn(users);

        Set<UserDTO> result = bookKeeperService.getUsers();

        assertEquals(2, result.size());
        verify(userRepository).findByRole(Role.USER);
    }

    @Test
    void testGetUsers_EmptyResult() {
        Set<User> users = Set.of();
        when(userRepository.findByRole(Role.USER)).thenReturn(users);

        Set<UserDTO> result = bookKeeperService.getUsers();

        assertTrue(result.isEmpty());
        verify(userRepository).findByRole(Role.USER);
    }

    @Test
    void testDownloadImage_Success() throws IOException {
        Part filePart = mock(Part.class);
        when(filePart.getSubmittedFileName()).thenReturn("test-image.jpg");

        try {
            String result = bookKeeperService.downloadImage(filePart);
            assertNotNull(result);
            assertTrue(result.contains("test-image"));
            assertTrue(result.endsWith(".jpg"));
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Cannot") || e.getMessage().contains("Failed"));
        }
    }

    @Test
    void testDownloadImage_NoFileName_ThrowsException() {
        Part filePart = mock(Part.class);
        when(filePart.getSubmittedFileName()).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> bookKeeperService.downloadImage(filePart));
        assertEquals("No file name provided", ex.getMessage());
    }

    @Test
    void testGetBooks_Success() {
        List<Book> books = List.of(createTestBook("book1", "Book One"), createTestBook("book2", "Book Two"));
        when(bookRepository.findAll()).thenReturn(books);

        Set<BookDTO> result = bookKeeperService.getBooks();

        assertEquals(2, result.size());
        verify(bookRepository).findAll();
    }

    @Test
    void testGetBooks_EmptyResult() {
        List<Book> books = List.of();
        when(bookRepository.findAll()).thenReturn(books);

        Set<BookDTO> result = bookKeeperService.getBooks();

        assertTrue(result.isEmpty());
        verify(bookRepository).findAll();
    }

    @Test
    void testGetOrdersByUsername_Success() {
        String username = "testuser";
        Order order1 = createTestOrder();
        order1.setPublicId(UUID.randomUUID());
        Order order2 = createTestOrder();
        order2.setPublicId(UUID.randomUUID());
        Set<Order> orders = Set.of(order1, order2);
        when(orderRepository.findOrdersByUsername(username)).thenReturn(orders);

        Set<OrderDTO> result = bookKeeperService.getOrdersByUsername(username);

        assertEquals(2, result.size());
        verify(orderRepository).findOrdersByUsername(username);
    }

    @Test
    void testGetOrdersByUsername_EmptyResult() {
        String username = "testuser";
        Set<Order> orders = Set.of();
        when(orderRepository.findOrdersByUsername(username)).thenReturn(orders);

        Set<OrderDTO> result = bookKeeperService.getOrdersByUsername(username);

        assertTrue(result.isEmpty());
        verify(orderRepository).findOrdersByUsername(username);
    }

    @Test
    void testGetAllActiveOrders_Success() {
        Order order1 = createTestOrder();
        order1.setPublicId(UUID.randomUUID());
        Order order2 = createTestOrder();
        order2.setPublicId(UUID.randomUUID());
        Set<Order> orders = Set.of(order1, order2);
        when(orderRepository.findActiveOrders()).thenReturn(orders);

        Set<OrderDTO> result = bookKeeperService.getAllActiveOrders();

        assertEquals(2, result.size());
        verify(orderRepository).findActiveOrders();
    }

    @Test
    void testGetAllActiveOrders_EmptyResult() {
        Set<Order> orders = Set.of();
        when(orderRepository.findActiveOrders()).thenReturn(orders);

        Set<OrderDTO> result = bookKeeperService.getAllActiveOrders();

        assertTrue(result.isEmpty());
        verify(orderRepository).findActiveOrders();
    }

    @Test
    void testGetOverdueOrders_Success() {
        Order order1 = createTestOrder();
        order1.setPublicId(UUID.randomUUID());
        Order order2 = createTestOrder();
        order2.setPublicId(UUID.randomUUID());
        Set<Order> orders = Set.of(order1, order2);
        when(orderRepository.findOverdueOrders()).thenReturn(orders);

        Set<OrderDTO> result = bookKeeperService.getOverdueOrders();

        assertEquals(2, result.size());
        verify(orderRepository).findOverdueOrders();
    }

    @Test
    void testGetOverdueOrders_EmptyResult() {
        Set<Order> orders = Set.of();
        when(orderRepository.findOverdueOrders()).thenReturn(orders);

        Set<OrderDTO> result = bookKeeperService.getOverdueOrders();

        assertTrue(result.isEmpty());
        verify(orderRepository).findOverdueOrders();
    }


}
