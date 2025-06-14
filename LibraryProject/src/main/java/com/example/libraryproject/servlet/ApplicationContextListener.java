package com.example.libraryproject.servlet;

import com.example.libraryproject.configuration.DBConnectionConfig;
import com.example.libraryproject.repository.BookKeeperRepository;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.service.AuthorizationService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.hibernate.Session;

@WebListener
public class ApplicationContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            Session session = DBConnectionConfig.getSessionFactory().openSession();
            UserRepository userRepository = new UserRepository(session);
            BookKeeperRepository bookKeeperRepository = new BookKeeperRepository(session);
            AuthorizationService authorizationService = new AuthorizationService(userRepository, bookKeeperRepository);
            event.getServletContext().setAttribute(AuthorizationService.ATTRIBUTE_NAME, authorizationService);

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
