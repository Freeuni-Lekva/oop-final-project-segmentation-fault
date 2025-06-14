package com.example.libraryproject.model.dto;

public record GoogleBooksResponse(
        String title,
        String publishedDate,
        String author,
        String description,
        String thumbnailUrl
) {}
