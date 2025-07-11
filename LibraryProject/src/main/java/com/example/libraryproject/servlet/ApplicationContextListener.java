package com.example.libraryproject.servlet;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.configuration.DBConnectionConfig;
import com.example.libraryproject.model.dto.RegistrationRequest;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.Role;
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


@WebListener
public class ApplicationContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationContextListener.class);

    private static final String AUTHORIZATION_SERVICE_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.authorization-service");
    private static final String BOOKKEEPER_SERVICE_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.bookkeeper-service");
    private static final String SCHEDULER_SERVICE_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.scheduler-service");
    private static final String BOOK_SERVICE_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.book-service");
    private static final String BOOK_RECOMMENDATION_SERVICE_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.book-recommendation-service");
    private static final String USER_SERVICE_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.user-service");
    private static final String GOOGLE_BOOKS_API_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.google-books-api");
    private static final String OBJECT_MAPPER_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.object-mapper");
    private static final String ACCOUNT_ACTIVATION_SERVICE_ATTRIBUTE_NAME = ApplicationProperties.get("attribute.account-activation-service");
    private static final String ADMIN_EMAIL = ApplicationProperties.get("email.admin-email");
    private static final String ACTIVATION_BASE_URL = ApplicationProperties.get("activation.base-url");

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            SessionFactory sessionFactory = DBConnectionConfig.getSessionFactory();

            UserRepository userRepository = new UserRepository(sessionFactory);
            BookRepository bookRepository = new BookRepository(sessionFactory);
            OrderRepository orderRepository = new OrderRepository(sessionFactory);
            ReviewRepository reviewRepository = new ReviewRepository(sessionFactory);
            AccountActivationRepository accountActivationRepository = new AccountActivationRepository(sessionFactory);

            MailService mailService = new MailServiceImpl();

            AccountActivationService accountActivationService = new AccountActivationServiceImpl(accountActivationRepository, userRepository, mailService);
            event.getServletContext().setAttribute(ACCOUNT_ACTIVATION_SERVICE_ATTRIBUTE_NAME, accountActivationService);
            logger.info("AccountActivationService initialized and registered successfully");

            AuthorizationService authorizationService = new AuthorizationServiceImpl(userRepository, mailService);
            event.getServletContext().setAttribute(AUTHORIZATION_SERVICE_ATTRIBUTE_NAME, authorizationService);

            if (userRepository.findByUsername("gmerti").isEmpty()) {
                RegistrationRequest request = new RegistrationRequest("gmerti", "123", ADMIN_EMAIL, Role.BOOKKEEPER);
                User adminUser = authorizationService.register(request);
                // For admin user, use configured URL since we don't have request context here
                accountActivationService.createActivation(adminUser, ACTIVATION_BASE_URL + "/activate");
                logger.info("Default bookkeeper 'gmerti' created with activation email");
            } else {
                logger.info("Default bookkeeper 'gmerti' already exists");
            }

            BookKeeperService bookKeeperService = new BookKeeperServiceImpl(bookRepository, userRepository, orderRepository, reviewRepository, mailService);
            event.getServletContext().setAttribute(BOOKKEEPER_SERVICE_ATTRIBUTE_NAME, bookKeeperService);

            SchedulerService schedulerService = new SchedulerServiceImpl(userRepository, orderRepository, accountActivationRepository, mailService);
            event.getServletContext().setAttribute(SCHEDULER_SERVICE_ATTRIBUTE_NAME, schedulerService);
            schedulerService.start();

            BookService bookService = new BookServiceImpl(bookRepository, reviewRepository);
            event.getServletContext().setAttribute(BOOK_SERVICE_ATTRIBUTE_NAME, bookService);

            BookRecommendationService bookRecommendationService = new BookRecommendationServiceImpl(bookRepository, userRepository);
            event.getServletContext().setAttribute(BOOK_RECOMMENDATION_SERVICE_ATTRIBUTE_NAME, bookRecommendationService);

            UserService userService = new UserServiceImpl(userRepository, bookRepository, reviewRepository, orderRepository, mailService);
            event.getServletContext().setAttribute(USER_SERVICE_ATTRIBUTE_NAME, userService);

            GoogleBooksApiService googleBooksAPIService = new GoogleBooksApiServiceImpl(bookRepository);
            event.getServletContext().setAttribute(GOOGLE_BOOKS_API_ATTRIBUTE_NAME, googleBooksAPIService);

            Thread fetcherThread = new Thread(googleBooksAPIService::fetchAndSaveBooks);
            fetcherThread.setDaemon(true);
            fetcherThread.start();

            ObjectMapper objectMapper = new ObjectMapper();
            event.getServletContext().setAttribute(OBJECT_MAPPER_ATTRIBUTE_NAME, objectMapper);

            logger.info("application context initialized successfully");

        } catch (Exception e) {
            logger.error("failed to initialize application context: {}", e.getMessage(), e);
            throw new RuntimeException("Application initialization failed", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        try {
            DBConnectionConfig.getSessionFactory().close();
            logger.info("Session closed successfully");
        } catch (Exception e) {
            logger.error("Error closing Session: {}", e.getMessage(), e);
        }
    }
}
