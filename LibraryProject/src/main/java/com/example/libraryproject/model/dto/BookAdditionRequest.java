package com.example.libraryproject.model.dto;

public record BookAdditionRequest(
        String title,
        String author,
        String description,
        String genre,
        String volume,
        Long copies,
        String publicationDate,
        String imageUrl
) {
}
