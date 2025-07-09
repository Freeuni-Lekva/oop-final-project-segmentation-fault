package com.example.libraryproject.repository;


import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Order;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.OrderStatus;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static com.example.libraryproject.configuration.ApplicationProperties.STALE_ORDER_TIMEOUT_HRS;


public class TestOrderRepository {

    private SessionFactory sessionFactory;
    private OrderRepository orderRepository;
    private Order order;

    @BeforeEach
    public void setUp() {
        Configuration configuration = new Configuration()
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
                .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.show_sql", "true")
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Book.class)
                .addAnnotatedClass(Review.class)
                .addAnnotatedClass(Order.class);

        sessionFactory = configuration.buildSessionFactory();
        orderRepository = new OrderRepository(sessionFactory);
        User user = new User("gubaz","541541");
        Book book = new Book("Oddysey","Oddysey", "Sci-Fi", "Arthur C. Clarke", LocalDate.of(1968, 7, 1),
                "A journey through space and time", 300L, 1L, 10L, 5.0, "oddysey.jpg");
        UserRepository userRepository = new UserRepository(sessionFactory);
        BookRepository bookRepository = new BookRepository(sessionFactory);
        userRepository.save(user);
        bookRepository.save(book);

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        order = new Order(UUID.randomUUID(), now, now.plusDays(14), OrderStatus.BORROWED, user, book);

        orderRepository.save(order);
    }

    @AfterEach
    public void tearDown() {
        sessionFactory.close();
    }

    @Test
    public void testSave() {
        Optional<Order> foundOrderOptional = orderRepository.findById(order.getId());
        assertTrue(foundOrderOptional.isPresent());
        Order foundOrder = foundOrderOptional.get();
        assertEquals(order.getId(), foundOrder.getId());
        assertEquals(order.getUser().getUsername(), foundOrder.getUser().getUsername());
        assertEquals(order.getBook().getName(), foundOrder.getBook().getName());
        assertEquals(order.getStatus(), foundOrder.getStatus());
        assertEquals(order.getBorrowDate(), foundOrder.getBorrowDate());
        assertEquals(order.getDueDate(), foundOrder.getDueDate());
        assertEquals(order.getReturnDate(), foundOrder.getReturnDate());
    }

    @Test
    public void testUpdate() {
        order.setStatus(OrderStatus.BORROWED);
        orderRepository.update(order);
        Optional<Order> updatedOrderOptional = orderRepository.findById(order.getId());
        assertTrue(updatedOrderOptional.isPresent());
        Order updatedOrder = updatedOrderOptional.get();
        assertNotNull(updatedOrder);
        assertEquals(OrderStatus.BORROWED, updatedOrder.getStatus());
    }

    @Test
    public void testDelete() {
        orderRepository.delete(order);
        Optional<Order> deletedOrderOptional = orderRepository.findById(order.getId());

        assertTrue(deletedOrderOptional.isEmpty());
    }

    @Test
    public void testFindByUserId() {
        Set<Order> foundOrder = orderRepository.findOrdersByUserId(order.getUser().getId());
        assertEquals(1, foundOrder.size());
        assertEquals(order.getId(), foundOrder.iterator().next().getId());
    }

    @Test
    public void testFindByBookId() {
        Set<Order> foundOrder = orderRepository.findOrdersByBookId(order.getBook().getId());
        assertEquals(1, foundOrder.size());
        assertEquals(order.getId(), foundOrder.iterator().next().getId());
    }

    @Test
    public void testFindAll() {
        Order newOrder = new Order(UUID.randomUUID(),LocalDateTime.now().plusDays(40), LocalDateTime.now().plusDays(40+14),
                OrderStatus.RESERVED, order.getUser(), order.getBook());
        orderRepository.save(newOrder);
        Set<Order> allOrders = orderRepository.findAll();
        assertFalse(allOrders.isEmpty());
        assertTrue(allOrders.contains(order));
        assertTrue(allOrders.contains(newOrder));
    }


    @Test
    public void testFindOrdersByStatus() {
        Set<Order> foundOrders = orderRepository.findOrdersByStatus(OrderStatus.BORROWED);
        assertFalse(foundOrders.isEmpty());
        assertTrue(foundOrders.contains(order));

        Set<Order> emptyOrders = orderRepository.findOrdersByStatus(OrderStatus.RETURNED);
        assertTrue(emptyOrders.isEmpty());
    }

    @Test
    public void testFindDueOrders() {
        order.setDueDate(LocalDateTime.now().minusDays(1));
        orderRepository.update(order);
        Set<Order> dueOrders = orderRepository.findDueOrders();
        assertFalse(dueOrders.isEmpty());
        assertTrue(dueOrders.contains(order));

        Order notDueOrder = new Order(UUID.randomUUID(),LocalDateTime.now().plusDays(20), LocalDateTime.now().plusDays(20 + 14),
                OrderStatus.RESERVED, order.getUser(), order.getBook());
        orderRepository.save(notDueOrder);

        Set<Order> allDueOrders = orderRepository.findDueOrders();
        assertEquals(1, allDueOrders.size());
        assertTrue(allDueOrders.contains(order));
    }

    @Test
    public void testFindStaleOrders() {
        order.setStatus(OrderStatus.RESERVED);
        order.setCreateDate(LocalDateTime.now().minusHours(STALE_ORDER_TIMEOUT_HRS + 1));
        orderRepository.update(order);
        Set<Order> staleOrders = orderRepository.findStaleOrders();
        assertFalse(staleOrders.isEmpty());
        assertTrue(staleOrders.contains(order));

        Order notStaleOrder = new Order(UUID.randomUUID(),LocalDateTime.now().minusHours(10), LocalDateTime.now().plusDays(14),
                OrderStatus.BORROWED, order.getUser(), order.getBook());
        orderRepository.save(notStaleOrder);

        Set<Order> allStaleOrders = orderRepository.findStaleOrders();
        assertEquals(1, allStaleOrders.size());
        assertTrue(allStaleOrders.contains(order));
    }

    @Test
    public void testFindByPublicId() {
        Optional<Order> foundOrder = orderRepository.findByPublicId(order.getPublicId().toString());
        assertTrue(foundOrder.isPresent());
        assertEquals(order.getId(), foundOrder.get().getId());
        assertEquals(order.getPublicId(), foundOrder.get().getPublicId());
    }

    @Test
    public void testDeleteAll() {
        Set<Order> staleOrders = new HashSet<>();
        staleOrders.add(order);

        orderRepository.deleteAll(staleOrders);

        Set<Order> allOrders = orderRepository.findAll();
        assertTrue(allOrders.isEmpty());
    }


}
