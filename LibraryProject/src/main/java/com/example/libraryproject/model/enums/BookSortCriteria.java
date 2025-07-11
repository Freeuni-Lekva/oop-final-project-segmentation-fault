package com.example.libraryproject.model.enums;

import lombok.Getter;

/**
 * Enum representing different criteria for sorting books
 */
@Getter
public enum BookSortCriteria {
    TITLE("title"),
    AUTHOR("author"), 
    RATING("rating"),
    AVAILABLE("available"),
    DATE("date"), // Sort by publication date, most recent first
    RECENT("recent"), // Sort by when added to system (ID), most recent first
    DEFAULT("rating"); // Default sorting by rating
    
    private final String value;
    
    BookSortCriteria(String value) {
        this.value = value;
    }

    public static BookSortCriteria fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return DEFAULT;
        }
        
        for (BookSortCriteria criteria : BookSortCriteria.values()) {
            if (criteria.getValue().equalsIgnoreCase(value.trim())) {
                return criteria;
            }
        }
        return DEFAULT;
    }
} 