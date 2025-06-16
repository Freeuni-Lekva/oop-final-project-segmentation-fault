package com.example.libraryproject.repository;


import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Order;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.OrderStatus;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestOrderRepository {

    private SessionFactory sessionFactory;
    private Session session;
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
        session = sessionFactory.openSession();
        orderRepository = new OrderRepository(session);
        User user = new User("gubaz","541541");
        Book book = new Book("Oddysey", "Sci-Fi", "Arthur C. Clarke", LocalDate.of(1968, 7, 1),
                "A journey through space and time", 1L, 10L, 5L, "oddysey.jpg");
        UserRepository userRepository = new UserRepository(session);
        BookRepository bookRepository = new BookRepository(session);
        userRepository.save(user);
        bookRepository.save(book);
        order = new Order(LocalDateTime.now(), LocalDateTime.now().plusDays(14),
                OrderStatus.RESERVED, user, book);
        orderRepository.save(order);
    }

    @AfterEach
    public void tearDown() {
        // Close session here
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    @Test
    public void testSave() {
        Order foundOrder = orderRepository.findById(order.getId());
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
        Order updatedOrder = orderRepository.findById(order.getId());
        assertNotNull(updatedOrder);
        assertEquals(OrderStatus.BORROWED, updatedOrder.getStatus());
    }

    @Test
    public void testDelete() {
        orderRepository.delete(order);
        Order deletedOrder = orderRepository.findById(order.getId());
        assertNull(deletedOrder);
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
        Order newOrder = new Order(LocalDateTime.now().plusDays(40), LocalDateTime.now().plusDays(40+14),
                OrderStatus.RESERVED, order.getUser(), order.getBook());
        orderRepository.save(newOrder);
        Set<Order> allOrders = orderRepository.findAll();
        assertFalse(allOrders.isEmpty());
        assertTrue(allOrders.contains(order));
        assertTrue(allOrders.contains(newOrder));
    }


    @Test
    public void testFindOrdersByStatus() {
        Set<Order> foundOrders = orderRepository.findOrdersByStatus(OrderStatus.RESERVED);
        assertFalse(foundOrders.isEmpty());
        assertTrue(foundOrders.contains(order));

        Set<Order> emptyOrders = orderRepository.findOrdersByStatus(OrderStatus.RETURNED);
        assertTrue(emptyOrders.isEmpty());
    }


}
