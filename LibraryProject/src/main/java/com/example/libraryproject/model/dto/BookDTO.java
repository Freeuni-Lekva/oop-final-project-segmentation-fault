package com.example.libraryproject.model.dto;

public record BookDTO(
        String title,
        String description,
        String genre,
        String author,
        String imageUrl,
        Long originalAmount,
        Long currentAmount,
        Long volume,
        Long rating
) {

}
