package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.BookDTO;

import java.util.Set;

public interface BookRecommendationService {

    Set<BookDTO> recommendBooks(String username);

}
