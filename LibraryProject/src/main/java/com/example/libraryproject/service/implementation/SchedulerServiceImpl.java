package com.example.libraryproject.service.implementation;

import com.example.libraryproject.model.entity.Order;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.UserStatus;
import com.example.libraryproject.repository.OrderRepository;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.service.SchedulerService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.libraryproject.configuration.ApplicationProperties.SCHEDULER_UPDATE_INTERVAL_HRS;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class SchedulerServiceImpl implements SchedulerService {
    private UserRepository userRepository;
    private OrderRepository orderRepository;
    private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);


    public void start() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::banDueUsers, 0, SCHEDULER_UPDATE_INTERVAL_HRS, TimeUnit.HOURS);
        scheduler.scheduleAtFixedRate(this::deleteStaleOrders,0,1, TimeUnit.HOURS);
    }

    private void deleteStaleOrders() {
        Set<Order> staleOrders = orderRepository.findStaleOrders();
        orderRepository.deleteAll(staleOrders);
        logger.info("Deleted stale orders: {}", staleOrders.size());
    }


    private void banDueUsers() {
        Set<Order> dueOrders = orderRepository.findDueOrders();
        Set<User> dueUsers = new HashSet<>();
        for (Order order : dueOrders) {
            User user = order.getUser();
            user.setStatus(UserStatus.BANNED);
            dueUsers.add(user);
        }
        userRepository.updateAll(dueUsers);
        logger.info("Banned users with due orders: {}", dueUsers.size());
    }

}
