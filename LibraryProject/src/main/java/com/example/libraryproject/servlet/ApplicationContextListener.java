package com.example.libraryproject.servlet;

import com.example.libraryproject.configuration.DBConnectionConfig;
import com.example.libraryproject.repository.*;
import com.example.libraryproject.service.*;
import com.example.libraryproject.service.implementation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.libraryproject.configuration.ApplicationProperties.*;


@WebListener
public class ApplicationContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            SessionFactory sessionFactory = DBConnectionConfig.getSessionFactory();

            UserRepository userRepository = new UserRepository(sessionFactory);
            BookKeeperRepository bookKeeperRepository = new BookKeeperRepository(sessionFactory);
            BookRepository bookRepository = new BookRepository(sessionFactory);
            OrderRepository orderRepository = new OrderRepository(sessionFactory);
            ReviewRepository reviewRepository = new ReviewRepository(sessionFactory);

            AuthorizationService authorizationService = new AuthorizationServiceImpl(userRepository, bookKeeperRepository);
            event.getServletContext().setAttribute(AUTHORIZATION_SERVICE_ATTRIBUTE_NAME, authorizationService);

            BookKeeperService bookKeeperService = new BookKeeperServiceImpl(bookRepository, userRepository, orderRepository);
            event.getServletContext().setAttribute(BOOKKEEPER_SERVICE_ATTRIBUTE_NAME, bookKeeperService);

            SchedulerService schedulerService = new SchedulerServiceImpl(userRepository, orderRepository);
            event.getServletContext().setAttribute(SCHEDULER_SERVICE_ATTRIBUTE_NAME, schedulerService);
            schedulerService.start();

            BookService bookService = new BookServiceImpl(bookRepository);
            event.getServletContext().setAttribute(BOOK_SERVICE_ATTRIBUTE_NAME, bookService);

            BookRecommendationService bookRecommendationService = new BookRecommendationServiceImpl(bookRepository, userRepository);
            event.getServletContext().setAttribute(BOOK_RECOMMENDATION_SERVICE_ATTRIBUTE_NAME, bookRecommendationService);

            UserService userService = new UserServiceImpl(userRepository, bookRepository, reviewRepository, orderRepository);
            event.getServletContext().setAttribute(USER_SERVICE_ATTRIBUTE_NAME, userService);

            ObjectMapper objectMapper = new ObjectMapper();
            event.getServletContext().setAttribute(OBJECT_MAPPER_ATTRIBUTE_NAME, objectMapper);

            GoogleBooksApiService googleBooksAPIService = new GoogleBooksApiServiceImpl(bookRepository);
            event.getServletContext().setAttribute(GOOGLE_BOOKS_API_ATTRIBUTE_NAME, googleBooksAPIService);

            Thread fetcherThread = new Thread(googleBooksAPIService::fetchAndSaveBooks);
            fetcherThread.setDaemon(true);
            fetcherThread.start();

            logger.info("Hibernate schema created or validated successfully.");

        } catch (Exception e) {
            logger.error("Failed to initialize Hibernate schema", e);
            throw new RuntimeException("Failed to initialize Hibernate schema", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DBConnectionConfig.getSessionFactory().close();
    }
}
