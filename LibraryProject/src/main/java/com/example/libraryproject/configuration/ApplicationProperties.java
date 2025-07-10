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
            "romance", "mystery", "fantasy", "fiction",
            "horror", "adventure", "comedy", "crime", "memoir", "poetry",
            "philosophy", "psychology", "art", "religion", "politics",
            "history", "classics"
    };
    public static final String GOOGLE_API_URL = "https://www.googleapis.com/books/v1/volumes";
    public static final int BOOKS_PER_REQUEST = 40;
    public static final int GOOGLE_BOOKS_API_MAX_PAGE = 150;

    public static final int DEFAULT_RATING = 3;
    public static final int RECOMMENDED_COUNT = 25;
    public static final int TOP_AUTHORS_COUNT = 3;
    public static final int TOP_GENRE_COUNT = 3;

    public static final int SCHEDULER_UPDATE_INTERVAL_HRS = 1;
    public static final int STALE_ORDER_TIMEOUT_HRS = 48;

    public static final String EMAIL_ADDRESS = "ooplibrarymanagement@gmail.com";
    public static final String EMAIL_PASSWORD = "zpux dgjo uept afdn";
    public static final String SMTP_HOST = "smtp.googlemail.com";
    public static final int SMTP_PORT = 465;

    public static final int SCHEDULER_BOOK_REMINDER_INTERVAL_HRS = 24;
}
