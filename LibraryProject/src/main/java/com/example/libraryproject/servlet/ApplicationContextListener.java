package com.example.libraryproject.servlet;

import com.example.libraryproject.configuration.DBConnectionConfig;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.hibernate.Session;
import org.hibernate.Transaction;

@WebListener
public class ApplicationContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            Session session = DBConnectionConfig.getSessionFactory().openSession();

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
