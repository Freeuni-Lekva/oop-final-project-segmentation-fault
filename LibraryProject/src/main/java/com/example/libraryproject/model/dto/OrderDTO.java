package com.example.libraryproject.model.dto;

public record OrderDTO(
        String orderPublicId,
        String username,
        String bookTitle,
        String bookPublicId,
        String status,
        String reservedDate,
        String dueDate,
        String borrowedDate,
        String returnDate,
        boolean isOverdue
) {} 