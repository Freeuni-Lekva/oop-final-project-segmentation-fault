package com.example.libraryproject.integration;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.dto.BookDTO;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.service.BookRecommendationService;
import com.example.libraryproject.service.implementation.BookRecommendationServiceImpl;
import com.example.libraryproject.servlet.BookRecommendationServlet;
import com.example.libraryproject.utils.MockDataForTests;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookRecommendationServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private ServletContext servletContext;

    @Mock
    private ServletConfig servletConfig;

    private BookRecommendationServlet servlet;
    private BookRecommendationService bookRecommendationService;
    private UserRepository userRepository;
    private BookRepository bookRepository;
    private SessionFactory sessionFactory;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Create a test session factory
        sessionFactory = new Configuration()
                .configure("hibernate-test.cfg.xml")
                .buildSessionFactory();

        // Initialize repositories with the session factory
        userRepository = new UserRepository(sessionFactory);
        bookRepository = new BookRepository(sessionFactory);

        // Create the recommendation service
        bookRecommendationService = new BookRecommendationServiceImpl(bookRepository, userRepository);

        // Create the servlet
        servlet = new BookRecommendationServlet();

        // Mock servlet configuration
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        servlet.init(servletConfig);

        // Setup mocks
        when(request.getSession(false)).thenReturn(session);
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(ApplicationProperties.get("attribute.book-recommendation-service")))
                .thenReturn(bookRecommendationService);
    }

    @AfterEach
    void tearDown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }

    private <T> T executeInTransaction(SessionFactory sessionFactory, TransactionCallback<T> callback) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            T result = callback.execute(session);

            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Transaction failed", e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @FunctionalInterface
    private interface TransactionCallback<T> {
        T execute(Session session);
    }

    @Test
    void testSuccessfulBookRecommendation() throws Exception {
        // Prepare test data in a transaction
        User testUser = executeInTransaction(sessionFactory, session -> {
            // First, create and persist the user
            User user = MockDataForTests.createTestUser();
            session.persist(user);

            // Create books
            Book book1 = MockDataForTests.createTestBook("Fantasy Adventure", "Epic Author", "Fantasy", 4.5, 10L, 5L, "fantasy.jpg");
            Book book2 = MockDataForTests.createTestBook("Mystery Novel", "Detective Writer", "Mystery", 4.0, 8L, 3L, "mystery.jpg");
            Book book3 = MockDataForTests.createTestBook("Sci-Fi Epic", "Sci-Fi Author", "Science Fiction", 4.2, 12L, 6L, "scifi.jpg");

            session.persist(book1);
            session.persist(book2);
            session.persist(book3);

            // Add books to user's read books
            Set<Book> readBooks = new HashSet<>();
            readBooks.add(book1);
            readBooks.add(book2);

            // Create reviews with different ratings
            // Ensure reviews are created with the persisted user
            Review review1 = new Review();
            review1.setPublicId(UUID.randomUUID());  // Set UUID
            review1.setUser(user);
            review1.setBook(book1);
            review1.setRating(5);
            review1.setComment("Great fantasy book!");

            Review review2 = new Review();
            review2.setPublicId(UUID.randomUUID());  // Set UUID
            review2.setUser(user);
            review2.setBook(book2);
            review2.setRating(4);
            review2.setComment("Interesting mystery");

            // Persist reviews
            session.persist(review1);
            session.persist(review2);

            // Update user with read books and reviews
            user.setReadBooks(readBooks);
            Set<Review> reviews = new HashSet<>();
            reviews.add(review1);
            reviews.add(review2);
            user.setReviews(reviews);

            // Merge the updated user
            session.merge(user);

            return user;
        });

        // Prepare servlet request
        when(session.getAttribute("username")).thenReturn(testUser.getUsername());

        // Prepare response writer
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // Call the servlet
        servlet.doGet(request, response);

        // Verify response
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setStatus(HttpServletResponse.SC_OK);

        // Parse the response
        String responseContent = stringWriter.toString();
        ObjectMapper objectMapper = new ObjectMapper();
        Set<BookDTO> recommendedBooks = objectMapper.readValue(
                responseContent,
                objectMapper.getTypeFactory().constructCollectionType(Set.class, BookDTO.class)
        );

        // Assertions
        assertNotNull(recommendedBooks);
        assertTrue(recommendedBooks.size() <= 25); // Based on RECOMMENDED_COUNT
    }

    @Test
    void testRecommendationForUserWithNoBooks() throws Exception {
        // Prepare test data in a transaction
        User testUser = executeInTransaction(sessionFactory, session -> {
            User user = MockDataForTests.createTestUser();
            session.persist(user);
            return user;
        });

        // Prepare servlet request
        when(session.getAttribute("username")).thenReturn(testUser.getUsername());

        // Prepare response writer
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // Call the servlet
        servlet.doGet(request, response);

        // Verify response
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setStatus(HttpServletResponse.SC_OK);

        // Parse the response
        String responseContent = stringWriter.toString();
        ObjectMapper objectMapper = new ObjectMapper();
        Set<BookDTO> recommendedBooks = objectMapper.readValue(
                responseContent,
                objectMapper.getTypeFactory().constructCollectionType(Set.class, BookDTO.class)
        );

        // Assertions
        assertNotNull(recommendedBooks);
        assertTrue(recommendedBooks.isEmpty());
    }

    @Test
    void testRecommendationForNonExistentUser() throws Exception {
        // Prepare servlet request with non-existent username
        when(session.getAttribute("username")).thenReturn("non_existent_user");

        // Prepare response writer
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // Call the servlet and expect an internal server error
        servlet.doGet(request, response);

        // Verify response
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        // Parse the error response
        String responseContent = stringWriter.toString();
        assertTrue(responseContent.contains("User not found"));
    }
}