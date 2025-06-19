package com.example.libraryproject.model.dto;

public record BookAdditionRequest(
        String title,
        String author,
        String description,
        String genre,
        String imageUrl
) {
}
