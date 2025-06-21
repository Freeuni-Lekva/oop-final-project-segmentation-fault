package com.example.libraryproject.configuration;

public class ApplicationProperties {

    public static final String GOOGLE_BOOKS_API_ATTRIBUTE_NAME = "google_api_service";
    public static final String BOOKKEEPER_SERVICE_ATTRIBUTE_NAME = "BookKeeperService";
    public static final String AUTHORIZATION_SERVICE_ATTRIBUTE_NAME = "AuthorizationService";
    public static final String BOOK_RECOMMENDATION_SERVICE_ATTRIBUTE_NAME = "BookRecommendationService";
    public static final String BOOK_SERVICE_ATTRIBUTE_NAME = "BookService";
    public static final String USER_SERVICE_ATTRIBUTE_NAME = "UserService";
    public static final String SCHEDULER_SERVICE_ATTRIBUTE_NAME = "SchedulerService";
    public static final String OBJECT_MAPPER_ATTRIBUTE_NAME = "ObjectMapper";

    public static final String[] GOOGLE_BOOKS_GENRES = {
            "romance", "mystery", "fantasy", "thriller", "science-fiction",
            "horror", "adventure", "drama", "comedy",
            "biography", "memoir", "poetry", "philosophy", "psychology",
            "art", "music", "religion", "politics", "history"
    };
    public static final String GOOGLE_API_URL = "https://www.googleapis.com/books/v1/volumes";
    public static final int TOTAL_BOOKS_TARGET = 100;
    public static final int BOOKS_PER_REQUEST = 10;
    public static final String IMAGE_DIR = System.getProperty("user.dir") + "/src/main/webapp/images/";


    public static final int DEFAULT_RATING = 3;
    public static final int RECOMMENDED_COUNT = 3;
    public static final int TOP_AUTHORS_COUNT = 3;
    public static final int TOP_GENRE_COUNT = 3;

    public static final int SCHEDULER_UPDATE_INTERVAL_HRS = 1;
    public static final int STALE_ORDER_TIMEOUT_HRS = 48;
}
