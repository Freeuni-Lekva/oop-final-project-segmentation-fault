package com.example.libraryproject.service;

import com.example.libraryproject.configuration.DBConnectionConfig;
import com.example.libraryproject.model.dto.GoogleBooksResponse;
import com.example.libraryproject.repository.BookRepository;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GoogleBooksApiServiceTest {

    private GoogleBooksAPIService googleBooksApiService;

    @BeforeEach
    public void setUp() {
        Session session = DBConnectionConfig.getSessionFactory().openSession();
        BookRepository bookRepository = new BookRepository(session);
        googleBooksApiService = new GoogleBooksAPIService(bookRepository);
    }

    @Test
    public void testGetBookDetails() {
        List<GoogleBooksResponse> books= googleBooksApiService.fetchBooks();
        assertEquals(40, books.size());
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
