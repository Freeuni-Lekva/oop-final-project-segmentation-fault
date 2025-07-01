package com.example.libraryproject.model.dto;

public record ReviewDTO(
        String bookTitle,
        String author,
        int rating,
        String comment
) {}
