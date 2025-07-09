package com.example.libraryproject.model.dto;

public record BookDTO(
        String publicId,
        String name,
        String description,
        String genre,
        String author,
        String imageUrl,
        Long totalAmount,
        Long currentAmount,
        Long volume,
        Double rating,
        String date
) {}
