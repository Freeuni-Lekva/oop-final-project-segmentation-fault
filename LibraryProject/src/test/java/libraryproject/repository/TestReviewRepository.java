package libraryproject.repository;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.ReviewRepository;
import com.example.libraryproject.repository.UserRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class TestReviewRepository {

    private SessionFactory sessionFactory;
    private Session session;
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
        session = sessionFactory.openSession();
        reviewRepository = new ReviewRepository(session);
        user = new User();
        user.setUsername("misha");
        user.setPassword("magaria");
        user2 = new User();
        user2.setUsername("kubdari");
        user2.setPassword("kubdari1234");

        book1 = new Book("Dzalis Gamogvidzeba","Politics" , "Mikheil Saakashvili", LocalDate.now(), "A thrilling political fiction novel",
                (long) 4.5, (long) 555, (long) 400);
        book2 = new Book("100 Years of Solitude", "Fiction", "Gabriel Garcia Marquez", LocalDate.now(), "A classic novel about the Buendia family",
                (long) 4.8, (long) 600, (long) 300);
        UserRepository userRepository = new UserRepository(session);
        BookRepository bookRepository = new BookRepository(session);
        userRepository.save(user);
        userRepository.save(user2);
        bookRepository.save(book1);
        bookRepository.save(book2);
    }

    @AfterEach
    public void tearDown() {
        session.close();
        sessionFactory.close();
    }


    @Test
    public void testSaveAndFindById() {
        Review review = new Review(5, "Tafliani 9 weli", user, book1);

        reviewRepository.save(review);

        Review found = reviewRepository.findById(review.getId());
        assertNotNull(found);
        assertEquals("Tafliani 9 weli", found.getComment());
    }

    @Test
    public void testDelete() {
        Review review = new Review();
        review.setUser(user);
        review.setBook(book1);
        review.setRating(5);
        review.setComment("The best book1 ever!");
        reviewRepository.save(review);
        reviewRepository.delete(review);
        Review found = reviewRepository.findById(review.getId());
        assertNull(found);
    }

    @Test
    public void testUpdate() {
        Review review = new Review();
        review.setUser(user);
        review.setBook(book1);
        review.setRating(0);
        review.setComment("Veraferi gavige");
        reviewRepository.save(review);
        review.setRating(5);
        review.setComment("Gavige, magaria");
        reviewRepository.update(review);
        Review found = reviewRepository.findById(review.getId());
        assertNotNull(found);
        assertEquals(5, found.getRating());
        assertEquals("Gavige, magaria", found.getComment());
    }


    @Test
    public void testFindReviewsById() {
        Review review = new Review();
        review.setUser(user);
        review.setBook(book1);
        review.setRating(4);
        review.setComment("Great book1");
        reviewRepository.save(review);
        Review foundReview = reviewRepository.findById(review.getId());
        assertNotNull(foundReview);
        assertEquals("Great book1", foundReview.getComment());
        assertEquals(4, foundReview.getRating());
    }

    @Test
    public void findReviewsByUserBookId() {
        Review review1 = new Review();
        review1.setUser(user);
        review1.setBook(book1);
        review1.setRating(0);
        review1.setComment("Sisulele");

        Review review2 = new Review();
        review2.setUser(user);
        review2.setBook(book2);
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
        review1.setBook(book1);
        review1.setRating(3);
        review1.setComment("Mid book1");

        Review review2 = new Review();
        review2.setUser(user2);
        review2.setBook(book2);
        review2.setRating(4);
        review2.setComment("Good read");

        reviewRepository.save(review1);
        reviewRepository.save(review2);

        Set<Review> allReviews = reviewRepository.findAll();
        assertEquals(2, allReviews.size());
    }


}
