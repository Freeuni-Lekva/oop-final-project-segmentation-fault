package com.example.libraryproject.service.implementation;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.entity.Order;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.OrderStatus;
import com.example.libraryproject.model.enums.UserStatus;
import com.example.libraryproject.repository.AccountActivationRepository;
import com.example.libraryproject.repository.OrderRepository;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.service.MailService;
import com.example.libraryproject.service.SchedulerService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class SchedulerServiceImpl implements SchedulerService {
    private UserRepository userRepository;
    private OrderRepository orderRepository;
    private AccountActivationRepository accountActivationRepository;
    private MailService mailService;
    private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);
    private static final int SCHEDULER_UPDATE_INTERVAL_HRS = Integer.parseInt(ApplicationProperties.get("scheduler.update-interval-hours"));
    private static final int SCHEDULER_BOOK_REMINDER_INTERVAL_HRS = Integer.parseInt(ApplicationProperties.get("scheduler.book-reminder-interval-hours"));


    public void start() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::banDueUsers, 0, SCHEDULER_UPDATE_INTERVAL_HRS, TimeUnit.HOURS);
        scheduler.scheduleAtFixedRate(this::deleteStaleOrders, 0, 1, TimeUnit.HOURS);
        scheduler.scheduleAtFixedRate(this::remindBorrowedUsers, 0, SCHEDULER_BOOK_REMINDER_INTERVAL_HRS, TimeUnit.HOURS);
        scheduler.scheduleAtFixedRate(this::cleanupExpiredTokens, 0, 24, TimeUnit.HOURS);
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

    private void remindBorrowedUsers() {
        Set<Order> borrowedOrders = orderRepository.findOrdersByStatus(OrderStatus.BORROWED);
        Set<String> notifiedUserNames = new HashSet<>();

        for (Order order : borrowedOrders) {
            User user = order.getUser();

            if (notifiedUserNames.contains(user.getUsername())) continue;

            try {
                LocalDateTime returnDateTime = order.getReturnDate();

                if (returnDateTime == null) {
                    logger.warn("Skipping reminder: return date is null for user {}", user.getUsername());
                    continue;
                }

                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), returnDateTime.toLocalDate());

                mailService.sendEmail(
                        List.of(user.getMail()),
                        "Reminder: Borrowed Book",
                        "Dear " + user.getUsername() + ",\n\n" +
                                "This is a friendly reminder that you have borrowed the book \"" + order.getBook().getName() + "\" from the library.\n" +
                                "It is due on: " + returnDateTime.toLocalDate() + " (" + daysLeft + " day(s) left).\n" +
                                "Please remember to return it on time to avoid penalties.\n\n" +
                                "Thank you,\n" +
                                "Library Team"
                );

                notifiedUserNames.add(user.getUsername());
            } catch (Exception e) {
                logger.error("Failed to send reminder to {}: {}", user.getMail(), e.getMessage());
            }
        }

        logger.info("Sent reminder emails to {} users with borrowed books", notifiedUserNames.size());
    }

    private void cleanupExpiredTokens() {
        try {
            accountActivationRepository.deleteExpiredActivations();
            logger.info("Cleaned up expired activation tokens");
        } catch (Exception e) {
            logger.error("Failed to cleanup expired activation tokens: {}", e.getMessage(), e);
        }
    }

}
