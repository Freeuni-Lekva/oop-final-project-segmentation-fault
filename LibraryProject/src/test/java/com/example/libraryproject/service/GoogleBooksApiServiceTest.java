package com.example.libraryproject.service;

import com.example.libraryproject.configuration.DBConnectionConfig;
import com.example.libraryproject.model.dto.GoogleBooksResponse;
import com.example.libraryproject.model.entity.BookKeeper;
import com.example.libraryproject.repository.BookRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class GoogleBooksApiServiceTest {

    private SessionFactory sessionFactory;
    private Session session;
    private GoogleBooksAPIService googleBooksApiService;

    @BeforeEach
    public void setUp() {
        Configuration configuration = new Configuration()
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
                .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.show_sql", "true")
                .addAnnotatedClass(BookKeeper.class);

        sessionFactory = configuration.buildSessionFactory();
        session = sessionFactory.openSession();

        BookRepository bookRepository = new BookRepository(session);
        googleBooksApiService = new GoogleBooksAPIService(bookRepository);
    }

    @Test
    public void testGetBookDetails() {
        HashSet<GoogleBooksResponse> books= googleBooksApiService.fetchBooks();
        assertNotEquals(0, books.size());
        for (var book : books) {
            assertNotEquals("No Title", book.title());
            assertNotEquals("No Author", book.author());
            assertNotEquals("No Thumbnail", book.thumbnailUrl());
            assertNotNull(book.title());
            assertNotNull(book.description());
            assertNotNull(book.author());
        }
    }

}
