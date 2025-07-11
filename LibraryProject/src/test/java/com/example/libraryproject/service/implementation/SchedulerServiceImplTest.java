package com.example.libraryproject.service.implementation;

import com.example.libraryproject.model.entity.Order;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.OrderStatus;
import com.example.libraryproject.model.enums.UserStatus;
import com.example.libraryproject.repository.OrderRepository;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.service.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.libraryproject.utils.MockDataForTests.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchedulerServiceImplTest {

    private SchedulerServiceImpl schedulerService;
    private UserRepository userRepository;
    private OrderRepository orderRepository;
    private MailService mailService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        orderRepository = mock(OrderRepository.class);
        mailService = mock(MailService.class);
        schedulerService = new SchedulerServiceImpl(userRepository, orderRepository, mailService);
    }



    @Test
    void test1() {
        assertDoesNotThrow(() -> {
            schedulerService.start();
        });
    }

    @Test
    void test2() {
        assertDoesNotThrow(() -> {
            schedulerService.start();
        });
        
        assertNotNull(schedulerService);
    }

    @Test
    void test3() throws Exception {
        Method method = SchedulerServiceImpl.class.getDeclaredMethod("deleteStaleOrders");
        method.setAccessible(true);

        Set<Order> staleOrders = new HashSet<>();
        staleOrders.add(createTestOrder(1L, createTestUserWithEmail("user1", "user1@test.com"), OrderStatus.RESERVED));
        
        when(orderRepository.findStaleOrders()).thenReturn(staleOrders);

        assertDoesNotThrow(() -> {
            try {
                method.invoke(schedulerService);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        verify(orderRepository).findStaleOrders();
        verify(orderRepository).deleteAll(staleOrders);
    }

    @Test
    void test4() throws Exception {
        Method method = SchedulerServiceImpl.class.getDeclaredMethod("deleteStaleOrders");
        method.setAccessible(true);

        when(orderRepository.findStaleOrders()).thenReturn(new HashSet<>());

        assertDoesNotThrow(() -> {
            try {
                method.invoke(schedulerService);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        verify(orderRepository).findStaleOrders();
        verify(orderRepository).deleteAll(any());
    }

    @Test
    void test5() throws Exception {
        Method method = SchedulerServiceImpl.class.getDeclaredMethod("banDueUsers");
        method.setAccessible(true);

        User user1 = createTestUserWithEmail("user1", "user1@test.com");
        Order dueOrder = createTestOrder(1L, user1, OrderStatus.BORROWED);
        
        Set<Order> dueOrders = new HashSet<>();
        dueOrders.add(dueOrder);
        
        when(orderRepository.findDueOrders()).thenReturn(dueOrders);

        assertDoesNotThrow(() -> {
            try {
                method.invoke(schedulerService);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        verify(orderRepository).findDueOrders();
        verify(userRepository).updateAll(any());
        assertEquals(UserStatus.BANNED, user1.getStatus());
    }

    @Test
    void test6() throws Exception {
        Method method = SchedulerServiceImpl.class.getDeclaredMethod("banDueUsers");
        method.setAccessible(true);

        when(orderRepository.findDueOrders()).thenReturn(new HashSet<>());

        assertDoesNotThrow(() -> {
            try {
                method.invoke(schedulerService);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        verify(orderRepository).findDueOrders();
        verify(userRepository).updateAll(any());
    }

    @Test
    void test7() throws Exception {
        Method method = SchedulerServiceImpl.class.getDeclaredMethod("remindBorrowedUsers");
        method.setAccessible(true);

        User user1 = createTestUserWithEmail("user1", "user1@test.com");
        Order borrowedOrder = createTestOrder(1L, user1, OrderStatus.BORROWED);
        
        Set<Order> borrowedOrders = new HashSet<>();
        borrowedOrders.add(borrowedOrder);
        
        when(orderRepository.findOrdersByStatus(OrderStatus.BORROWED)).thenReturn(borrowedOrders);

        assertDoesNotThrow(() -> {
            try {
                method.invoke(schedulerService);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        verify(orderRepository).findOrdersByStatus(OrderStatus.BORROWED);
        verify(mailService).sendEmail(eq(List.of(user1.getMail())), anyString(), anyString());
    }

    @Test
    void test8() throws Exception {
        Method method = SchedulerServiceImpl.class.getDeclaredMethod("remindBorrowedUsers");
        method.setAccessible(true);

        when(orderRepository.findOrdersByStatus(OrderStatus.BORROWED)).thenReturn(new HashSet<>());

        assertDoesNotThrow(() -> {
            try {
                method.invoke(schedulerService);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        verify(orderRepository).findOrdersByStatus(OrderStatus.BORROWED);
        verify(mailService, never()).sendEmail(anyList(), anyString(), anyString());
    }
}
