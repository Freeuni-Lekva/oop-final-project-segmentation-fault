package com.example.libraryproject.servlet;

import com.example.libraryproject.configuration.DBConnectionConfig;
import com.example.libraryproject.repository.BookKeeperRepository;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.OrderRepository;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.service.AuthorizationService;
import com.example.libraryproject.service.BookKeeperService;
import com.example.libraryproject.service.GoogleBooksAPIService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.hibernate.Session;

import static com.example.libraryproject.configuration.ApplicationProperties.AUTHORIZATION_SERVICE_ATTRIBUTE_NAME;
import static com.example.libraryproject.configuration.ApplicationProperties.BOOKKEEPER_SERVICE_ATTRIBUTE_NAME;

@WebListener
public class ApplicationContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            Session session = DBConnectionConfig.getSessionFactory().openSession();

            UserRepository userRepository = new UserRepository(session);
            BookKeeperRepository bookKeeperRepository = new BookKeeperRepository(session);
            BookRepository bookRepository = new BookRepository(session);
            OrderRepository orderRepository = new OrderRepository(session);


            GoogleBooksAPIService googleBooksAPIService = new GoogleBooksAPIService(bookRepository);
            Thread fetcherThread = new Thread(googleBooksAPIService::fetchAndSaveBooks);
            fetcherThread.setDaemon(true);
            fetcherThread.start();

            AuthorizationService authorizationService = new AuthorizationService(userRepository, bookKeeperRepository);
            event.getServletContext().setAttribute(AUTHORIZATION_SERVICE_ATTRIBUTE_NAME, authorizationService);


            BookKeeperService bookKeeperService = new BookKeeperService(bookRepository, userRepository, orderRepository);
            event.getServletContext().setAttribute(BOOKKEEPER_SERVICE_ATTRIBUTE_NAME, bookKeeperService);

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
