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
import java.time.LocalDateTime;
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
        user.setMail("misha@gmail.com");
        user2 = new User();
        user2.setUsername("kubdari");
        user2.setPassword("kubdari1234");
        user2.setMail("kubdari@gmail.com");

        book1 = new Book("Dzalis_Gamogvidzeba","Dzalis Gamogvidzeba","Politics" , "Mikheil Saakashvili", LocalDate.now(), LocalDateTime.now(), "A thrilling political fiction novel",
                300L, 4L,  555L,4.0, "dzalisGamogvidzeba.jpg" );
        book2 = new Book("100_Years_of_Solitude", "100 Years of Solitude", "Fiction", "Gabriel Garcia Marquez", LocalDate.now(), LocalDateTime.now(), "A classic novel about the Buendia family",
                300L, 4L, 600L, 3.0, "100YearsOfSolitude.jpg");
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
        Review review = new Review();
        review.setPublicId(UUID.randomUUID());
        review.setRating(5);
        review.setComment("Tafliani 9 weli");
        review.setUser(user);
        review.setBook(book1);

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
        assertTrue(found.isEmpty());
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

    @Test
    public void testDeleteAll() {
        Review review1 = new Review();
        review1.setUser(user);
        review1.setPublicId(UUID.randomUUID());
        review1.setBook(book1);
        review1.setRating(4);
        review1.setComment("Great book");

        Review review2 = new Review();
        review2.setUser(user2);
        review2.setPublicId(UUID.randomUUID());
        review2.setBook(book2);
        review2.setRating(3);
        review2.setComment("Average book");

        Review review3 = new Review();
        review3.setUser(user);
        review3.setPublicId(UUID.randomUUID());
        review3.setBook(book2);
        review3.setRating(5);
        review3.setComment("Excellent");

        reviewRepository.save(review1);
        reviewRepository.save(review2);
        reviewRepository.save(review3);

        Set<Review> reviewsToDelete = Set.of(review1, review2);
        reviewRepository.deleteAll(reviewsToDelete);

        Optional<Review> deletedReview1 = reviewRepository.findById(review1.getId());
        Optional<Review> deletedReview2 = reviewRepository.findById(review2.getId());
        Optional<Review> remainingReview = reviewRepository.findById(review3.getId());

        assertTrue(deletedReview1.isEmpty());
        assertTrue(deletedReview2.isEmpty());
        assertTrue(remainingReview.isPresent());
    }

    @Test
    public void testFindReviewsByBookPublicId() {
        Review review1 = new Review();
        review1.setUser(user);
        review1.setPublicId(UUID.randomUUID());
        review1.setBook(book1);
        review1.setRating(5);
        review1.setComment("Amazing book");

        Review review2 = new Review();
        review2.setUser(user2);
        review2.setPublicId(UUID.randomUUID());
        review2.setBook(book1);
        review2.setRating(4);
        review2.setComment("Very good");

        Review review3 = new Review();
        review3.setUser(user);
        review3.setPublicId(UUID.randomUUID());
        review3.setBook(book2);
        review3.setRating(3);
        review3.setComment("Okay book");

        reviewRepository.save(review1);
        reviewRepository.save(review2);
        reviewRepository.save(review3);

        Set<Review> book1Reviews = reviewRepository.findReviewsByBookPublicId(book1.getPublicId());
        Set<Review> book2Reviews = reviewRepository.findReviewsByBookPublicId(book2.getPublicId());

        assertEquals(2, book1Reviews.size());
        assertEquals(1, book2Reviews.size());
        assertTrue(book1Reviews.contains(review1));
        assertTrue(book1Reviews.contains(review2));
        assertTrue(book2Reviews.contains(review3));
        assertFalse(book1Reviews.contains(review3));
    }
}
