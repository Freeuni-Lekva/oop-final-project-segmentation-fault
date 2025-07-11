package com.example.libraryproject.model.dto;

public record ActivationResult(
        boolean success,
        String message,
        String username
) {
} 