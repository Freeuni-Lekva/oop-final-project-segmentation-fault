package com.example.libraryproject.servlet;

import com.example.libraryproject.configuration.DBConnectionConfig;
import com.example.libraryproject.repository.*;
import com.example.libraryproject.service.*;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import static com.example.libraryproject.configuration.ApplicationProperties.*;


@WebListener
public class ApplicationContextListener implements ServletContextListener {


    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            SessionFactory sessionFactory = DBConnectionConfig.getSessionFactory();

            UserRepository userRepository = new UserRepository(sessionFactory);
            BookKeeperRepository bookKeeperRepository = new BookKeeperRepository(sessionFactory);
            BookRepository bookRepository = new BookRepository(sessionFactory);
            OrderRepository orderRepository = new OrderRepository(sessionFactory);
            ReviewRepository reviewRepository = new ReviewRepository(sessionFactory);

            GoogleBooksAPIService googleBooksAPIService = new GoogleBooksAPIService(bookRepository);
            Thread fetcherThread = new Thread(googleBooksAPIService::fetchAndSaveBooks);
            fetcherThread.setDaemon(true);
            fetcherThread.start();

            AuthorizationService authorizationService = new AuthorizationService(userRepository, bookKeeperRepository);
            event.getServletContext().setAttribute(AUTHORIZATION_SERVICE_ATTRIBUTE_NAME, authorizationService);


            BookKeeperService bookKeeperService = new BookKeeperService(bookRepository, userRepository, orderRepository);
            event.getServletContext().setAttribute(BOOKKEEPER_SERVICE_ATTRIBUTE_NAME, bookKeeperService);

            SchedulerService schedulerService = new SchedulerService(userRepository, orderRepository);
            event.getServletContext().setAttribute(SCHEDULER_SERVICE_ATTRIBUTE_NAME, schedulerService);
            schedulerService.start();

            BookService bookService = new BookService(bookRepository, reviewRepository);
            event.getServletContext().setAttribute(BOOK_SERVICE_ATTRIBUTE_NAME, bookService);

            BookRecommendationService bookRecomendationService = new BookRecommendationService(bookRepository, userRepository);
            event.getServletContext().setAttribute(BOOK_RECOMMENDATION_SERVICE_ATTRIBUTE_NAME, bookRecomendationService);

            UserService userService = new UserService(userRepository,bookRepository,reviewRepository);
            event.getServletContext().setAttribute(USER_SERVICE_ATTRIBUTE_NAME, userService);

            System.out.println("✅ Hibernate schema created or validated successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ Failed to initialize Hibernate schema", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DBConnectionConfig.getSessionFactory().close();
    }
}
