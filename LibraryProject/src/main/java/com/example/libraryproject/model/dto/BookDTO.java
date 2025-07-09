package com.example.libraryproject.model.dto;

public record BookDTO(
        String publicId,
        String name,
        String description,
        String genre,
        String author,
        String imageUrl,
        Long originalAmount,
        Long currentAmount,
        Long volume,
        Long rating,
        String date
) {}
