package com.example.libraryproject.service;

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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.libraryproject.configuration.ApplicationProperties.RECOMMENDED_COUNT;
import static org.junit.jupiter.api.Assertions.*;

public class BookRecommendationServiceTest {
    private SessionFactory sessionFactory;
    private BookRepository bookRepository;
    private BookRecommendationService recommendationService;
    private UserRepository userRepository;
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
        recommendationService = new BookRecommendationService(bookRepository, userRepository);

        Book book1 = new Book(
                "Shadow_Realms",
                "Shadow Realms",
                "Fantasy",
                "Author A",
                LocalDate.of(2020, 5, 10),
                "A gripping tale of magic and destiny.",
                300L, 1L, 5L, 4L, ""
        );

        Book book2 = new Book(
                "Shadow_Realms_II",
                "Shadow Realms II",
                "Fantasy",
                "Author A",
                LocalDate.of(2021, 6, 15),
                "The saga continues with greater peril.",
                300L, 2L, 3L, 5L, ""
        );

        Book book3 = new Book(
                "Echoes_of_Power",
                "Echoes of Power",
                "Fantasy",
                "Author A",
                LocalDate.of(2019, 4, 20),
                "Ancient secrets resurface to test the realm.",
                300L, 3L, 2L, 3L, ""
        );

        Book book4 = new Book(
                "Rise_of_the_Mage",
                "Rise of the Mage",
                "Fantasy",
                "Author A",
                LocalDate.of(2022, 1, 5),
                "The rise of a legendary sorcerer.",
                300L, 4L, 1L, 5L, ""
        );

        Book book5 = new Book(
                "Fall_of_the_Realm",
                "Fall of the Realm",
                "Fantasy",
                "Author A",
                LocalDate.of(2023, 3, 12),
                "A kingdom on the edge of collapse.",
                300L, 5L, 4L, 2L, ""
        );

        Book book6 = new Book(
                "euf",
                "euf",
                "Fantasy",
                "Author A",
                LocalDate.of(2023, 3, 12),
                "euf.",
                300L, 5L, 4L, 2L, ""
        );
        bookRepository.save(book1);
        bookRepository.save(book2);
        bookRepository.save(book3);
        bookRepository.save(book4);
        bookRepository.save(book5);
        bookRepository.save(book6);

        user = new User("rezi","1234");
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
}
