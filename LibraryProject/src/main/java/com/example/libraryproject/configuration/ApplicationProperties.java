package com.example.libraryproject.configuration;

public class ApplicationProperties {

    public static final String GOOGLE_BOOKS_API_ATTRIBUTE_NAME = "google_api_service";
    public static final String BOOKKEEPER_SERVICE_ATTRIBUTE_NAME = "BookKeeperService";
    public static final String AUTHORIZATION_SERVICE_ATTRIBUTE_NAME = "AuthorizationService";



    public static final String[] GOOGLE_BOOKS_GENRES = {
            "romance", "mystery", "fantasy", "thriller", "science-fiction",
            "horror", "adventure", "drama", "comedy",
            "biography", "memoir", "poetry", "philosophy", "psychology",
            "art", "music", "religion", "politics", "history"
    };
    public static final String GOOGLE_API_URL = "https://www.googleapis.com/books/v1/volumes";
    public static final int TOTAL_BOOKS_TARGET = 100;
    public static final int BOOKS_PER_REQUEST = 10;

}
