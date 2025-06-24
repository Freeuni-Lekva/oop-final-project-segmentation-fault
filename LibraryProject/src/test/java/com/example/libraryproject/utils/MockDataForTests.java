package com.example.libraryproject.utils;

import com.example.libraryproject.model.entity.Book;

import java.time.LocalDate;

public class MockDataForTests {

    public static  Book createTestBook(String name, String author, String genre, Long rating, Long originalAmount, Long currentAmount, String imageUrl) {
        return new Book(
                name.replaceAll("[^a-zA-Z0-9.\\-]", "_"),
                name,
                genre,
                author,
                LocalDate.of(2023, 4, 1),
                "Test description for " + name,
                300L,
                originalAmount,
                currentAmount,
                rating,
                imageUrl
        );
    }
}
