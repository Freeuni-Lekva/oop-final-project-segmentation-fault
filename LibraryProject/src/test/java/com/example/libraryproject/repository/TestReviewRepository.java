package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


public class TestReviewRepository {

    private SessionFactory sessionFactory;
    private ReviewRepository reviewRepository;
    private Book book1;
    private User user;
    private User user2;
    private Book book2;


    @BeforeEach
    public void setUp() {
        Configuration configuration = new Configuration()
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
                .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.show_sql", "true")
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Book.class)
                .addAnnotatedClass(Review.class);

        sessionFactory = configuration.buildSessionFactory();
        reviewRepository = new ReviewRepository(sessionFactory);
        user = new User();
        user.setUsername("misha");
        user.setPassword("magaria");
        user2 = new User();
        user2.setUsername("kubdari");
        user2.setPassword("kubdari1234");

        book1 = new Book("Dzalis_Gamogvidzeba","Dzalis Gamogvidzeba","Politics" , "Mikheil Saakashvili", LocalDate.now(), "A thrilling political fiction novel",
                300L, 4L,  555L,400L, "dzalisGamogvidzeba.jpg" );
        book2 = new Book("100_Years_of_Solitude", "100 Years of Solitude", "Fiction", "Gabriel Garcia Marquez", LocalDate.now(), "A classic novel about the Buendia family",
                300L, 4L, 600L, 300L, "100YearsOfSolitude.jpg");
        UserRepository userRepository = new UserRepository(sessionFactory);
        BookRepository bookRepository = new BookRepository(sessionFactory);
        userRepository.save(user);
        userRepository.save(user2);
        bookRepository.save(book1);
        bookRepository.save(book2);
    }

    @AfterEach
    public void tearDown() {
        sessionFactory.close();
    }


    @Test
    public void testSaveAndFindById() {
        Review review = new Review(UUID.randomUUID(),5, "Tafliani 9 weli", user, book1);

        reviewRepository.save(review);

        Optional<Review> foundOptional = reviewRepository.findById(review.getId());
        assertTrue(foundOptional.isPresent());
        Review found = foundOptional.get();
        assertEquals("Tafliani 9 weli", found.getComment());
    }

    @Test
    public void testDelete() {
        Review review = new Review();
        review.setUser(user);
        review.setPublicId(UUID.randomUUID());
        review.setBook(book1);
        review.setRating(5);
        review.setComment("The best book1 ever!");
        reviewRepository.save(review);
        reviewRepository.delete(review);
        Optional<Review> found = reviewRepository.findById(review.getId());
        assertFalse(found.isPresent());
    }

    @Test
    public void testUpdate() {
        Review review = new Review();
        review.setUser(user);
        review.setPublicId(UUID.randomUUID());
        review.setBook(book1);
        review.setRating(0);
        review.setComment("Veraferi gavige");
        reviewRepository.save(review);
        review.setRating(5);
        review.setComment("Gavige, magaria");
        reviewRepository.update(review);
        Optional<Review> foundOptional = reviewRepository.findById(review.getId());
        assertTrue(foundOptional.isPresent());
        Review found = foundOptional.get();
        assertEquals(5, found.getRating());
        assertEquals("Gavige, magaria", found.getComment());
    }


    @Test
    public void testFindReviewsById() {
        Review review = new Review();
        review.setUser(user);
        review.setPublicId(UUID.randomUUID());
        review.setBook(book1);
        review.setRating(4);
        review.setComment("Great book1");
        reviewRepository.save(review);
        Optional<Review> foundReviewOptional = reviewRepository.findById(review.getId());
        assertTrue(foundReviewOptional.isPresent());
        Review foundReview = foundReviewOptional.get();
        assertEquals("Great book1", foundReview.getComment());
        assertEquals(4, foundReview.getRating());
    }

    @Test
    public void findReviewsByUserBookId() {
        Review review1 = new Review();
        review1.setUser(user);
        review1.setPublicId(UUID.randomUUID());
        review1.setBook(book1);
        review1.setRating(0);
        review1.setComment("Sisulele");

        Review review2 = new Review();
        review2.setUser(user);
        review2.setBook(book2);
        review2.setPublicId(UUID.randomUUID());
        review2.setRating(5);
        review2.setComment("Magaria, omi minda");

        reviewRepository.save(review1);
        reviewRepository.save(review2);

        Set<Review> reviewsByUserId = reviewRepository.findReviewsByUserId(user.getId());
        Set<Review> reviewsByBookId = reviewRepository.findReviewsByBookId(book1.getId());

        assertEquals(2, reviewsByUserId.size());
        assertEquals(1, reviewsByBookId.size());

        assertTrue(reviewsByUserId.contains(review1));
        assertTrue(reviewsByUserId.contains(review2));
        assertTrue(reviewsByBookId.contains(review1));
    }

    @Test
    public void testFindAll() {

        Review review1 = new Review();
        review1.setUser(user);
        review1.setPublicId(UUID.randomUUID());
        review1.setBook(book1);
        review1.setRating(3);
        review1.setComment("Mid book1");

        Review review2 = new Review();
        review2.setUser(user2);
        review2.setPublicId(UUID.randomUUID());
        review2.setBook(book2);
        review2.setRating(4);
        review2.setComment("Good read");

        reviewRepository.save(review1);
        reviewRepository.save(review2);

        Set<Review> allReviews = reviewRepository.findAll();
        assertEquals(2, allReviews.size());
    }


}
