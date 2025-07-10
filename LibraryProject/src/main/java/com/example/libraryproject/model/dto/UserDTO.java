package com.example.libraryproject.model.dto;

import java.util.List;

public record UserDTO(
        String username,
        String bio,
        int booksRead,
        int reviewsGiven,
        List<ReviewDTO> reviews,
        List<BookDTO> currentlyReading,
        List<OrderDTO> orders,
        List<BookDTO> readBooks,
        String status,
        String mail
) {}
