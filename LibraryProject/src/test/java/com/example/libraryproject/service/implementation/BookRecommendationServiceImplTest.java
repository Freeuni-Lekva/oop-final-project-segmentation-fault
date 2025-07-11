package com.example.libraryproject.service.implementation;

import com.example.libraryproject.model.dto.BookDTO;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.UserRepository;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.*;

import static com.example.libraryproject.configuration.ApplicationProperties.RECOMMENDED_COUNT;
import static org.junit.jupiter.api.Assertions.*;

public class BookRecommendationServiceImplTest {
    private SessionFactory sessionFactory;
    private BookRepository bookRepository;
    private BookRecommendationServiceImpl recommendationService;
    private Set<BookDTO> recommendedBooks;

    private User user;

    @BeforeEach
    public void setUp() {
        Configuration configuration = new Configuration()
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
                .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.show_sql", "true")
                .addAnnotatedClass(Book.class)
                .addAnnotatedClass(Review.class)
                .addAnnotatedClass(User.class);

        sessionFactory = configuration.buildSessionFactory();

        bookRepository = new BookRepository(sessionFactory);
        UserRepository userRepository = new UserRepository(sessionFactory);
        recommendationService = new BookRecommendationServiceImpl(bookRepository, userRepository);

        Book book1 = new Book(
                "Shadow_Realms",
                "Shadow Realms",
                "Fantasy",
                "Author A",
                LocalDate.of(2020, 5, 10),
                "A gripping tale of magic and destiny.",
                300L, 1L, 5L, 4.0, ""
        );

        Book book2 = new Book(
                "Shadow_Realms_II",
                "Shadow Realms II",
                "Fantasy",
                "Author A",
                LocalDate.of(2021, 6, 15),
                "The saga continues with greater peril.",
                300L, 2L, 3L, 5.0, ""
        );

        Book book3 = new Book(
                "Echoes_of_Power",
                "Echoes of Power",
                "Fantasy",
                "Author A",
                LocalDate.of(2019, 4, 20),
                "Ancient secrets resurface to test the realm.",
                300L, 3L, 2L, 3.0, ""
        );

        Book book4 = new Book(
                "Rise_of_the_Mage",
                "Rise of the Mage",
                "Fantasy",
                "Author A",
                LocalDate.of(2022, 1, 5),
                "The rise of a legendary sorcerer.",
                300L, 4L, 1L, 5.0, ""
        );

        Book book5 = new Book(
                "Fall_of_the_Realm",
                "Fall of the Realm",
                "Fantasy",
                "Author A",
                LocalDate.of(2023, 3, 12),
                "A kingdom on the edge of collapse.",
                300L, 5L, 4L, 2.0, ""
        );

        Book book6 = new Book(
                "euf",
                "euf",
                "Fantasy",
                "Author A",
                LocalDate.of(2023, 3, 12),
                "euf.",
                300L, 5L, 4L, 2.0, ""
        );
        bookRepository.save(book1);
        bookRepository.save(book2);
        bookRepository.save(book3);
        bookRepository.save(book4);
        bookRepository.save(book5);
        bookRepository.save(book6);

        user = new User("rezi","1234", "froste3110@gmail.com");
        userRepository.save(user);

        Set<Book> readBook = new HashSet<>();
        readBook.add(book6);
        user.setReadBooks(readBook);

        recommendedBooks = recommendationService.recommendBooks(user.getUsername());
    }

    @AfterEach
    public void tearDown() {
      sessionFactory.close();
    }

    @Test
    public void test1(){
        assertTrue(recommendedBooks.size() <= RECOMMENDED_COUNT);
    }

    @Test
    public void test2() {
        List<BookDTO> bookList = new ArrayList<>(recommendedBooks);
        Set<BookDTO> uniqueBooks = new HashSet<>(bookList);
        assertEquals(bookList.size(), uniqueBooks.size(), "Duplicate books found in recommendation set");
    }

    @Test
    public void test3() {
        assertTrue(recommendedBooks.stream().noneMatch(user.getReadBooks()::contains));
    }

    @Test
    public void testApplyCoefficients() throws Exception {
        Set<String> topAuthorNames = Set.of("Author A", "Author B");
        Set<String> topGenreNames = Set.of("Fantasy", "Science Fiction");
        
        Map<String, Double> authorScores = new HashMap<>();
        authorScores.put("Author A", 6.0);
        authorScores.put("Author B", 3.0);
        
        Map<String, Double> genreScores = new HashMap<>();
        genreScores.put("Fantasy", 4.0);
        genreScores.put("Science Fiction", 2.0);

        List<Book> candidateBooks = new ArrayList<>();
        candidateBooks.add(new Book("book1", "Book 1", "Fantasy", "Author A", LocalDate.now(), "Description 1", 300L, 1L, 5L, 4.0, "book1.jpg"));
        candidateBooks.add(new Book("book2", "Book 2", "Fantasy", "Author B", LocalDate.now(), "Description 2", 300L, 1L, 5L, 4.0, "book2.jpg"));
        candidateBooks.add(new Book("book3", "Book 3", "Science Fiction", "Author A", LocalDate.now(), "Description 3", 300L, 1L, 5L, 4.0, "book3.jpg"));
        candidateBooks.add(new Book("book4", "Book 4", "Science Fiction", "Author B", LocalDate.now(), "Description 4", 300L, 1L, 5L, 4.0, "book4.jpg"));

        Method applyCoefficientsMethod = BookRecommendationServiceImpl.class.getDeclaredMethod(
            "applyCoefficients", 
            Set.class, Set.class, Map.class, Map.class, List.class
        );
        applyCoefficientsMethod.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Book> result = (List<Book>) applyCoefficientsMethod.invoke(
            recommendationService, 
            topAuthorNames, topGenreNames, authorScores, genreScores, candidateBooks
        );

        assertNotNull(result);
        assertTrue(result.size() <= RECOMMENDED_COUNT);
        assertTrue(result.size() <= candidateBooks.size());

        for (Book book : result) {
            assertTrue(candidateBooks.contains(book), "Result contains book not in candidate list");
        }

        Set<Book> uniqueBooks = new HashSet<>(result);
        assertEquals(result.size(), uniqueBooks.size(), "Result contains duplicate books");
    }
}
