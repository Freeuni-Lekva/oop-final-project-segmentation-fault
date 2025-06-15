package com.example.libraryproject.model.dto;

public record GoogleBooksResponse(
        String title,
        String publishedDate,
        String author,
        String description,
        String thumbnailUrl,
        String genre,
        Long volume
) {
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GoogleBooksResponse that = (GoogleBooksResponse) obj;
        return title != null && title.equalsIgnoreCase(that.title);
    }

    @Override
    public int hashCode() {
        return title != null ? title.toLowerCase().hashCode() : 0;
    }
}
