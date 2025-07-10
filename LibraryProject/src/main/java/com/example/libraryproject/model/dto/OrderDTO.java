package com.example.libraryproject.model.dto;

public record OrderDTO(
        String orderPublicId,
        String username,
        BookDTO book,
        String status,
        String reservedDate,
        String dueDate,
        String borrowedDate,
        String returnDate,
        boolean isOverdue
) {} 