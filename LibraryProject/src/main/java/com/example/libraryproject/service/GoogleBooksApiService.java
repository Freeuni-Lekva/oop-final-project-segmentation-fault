package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.BookAdditionFromGoogleRequest;

public interface GoogleBooksApiService {

    void fetchAndSaveBooks();

    boolean fetchBook(BookAdditionFromGoogleRequest request, int copies);
}
