package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestUserRepository {

    private SessionFactory sessionFactory;
    private UserRepository userRepository;

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
        userRepository = new UserRepository(sessionFactory);
    }

    @AfterEach
    public void tearDown() {
        sessionFactory.close();
    }

    @Test
    public void testSaveAndFindById() {
        User user = new User();
        user.setUsername("misha");
        user.setPassword("pass");
        userRepository.save(user);

        Optional<User> found = userRepository.findById(user.getId());
        assertTrue(found.isPresent());
        assertEquals("misha", found.get().getUsername());
    }

    @Test
    public void testUpdate() {
        User user = new User();
        user.setUsername("original");
        user.setPassword("pass");
        userRepository.save(user);

        user.setUsername("updated");
        userRepository.update(user);

        Optional<User> found = userRepository.findById(user.getId());

        assertTrue(found.isPresent());
        assertEquals("updated", found.get().getUsername());
    }

    @Test
    public void testDelete() {
        User user = new User();
        user.setUsername("toDelete");
        user.setPassword("pass");
        userRepository.save(user);

        userRepository.delete(user);
        Optional<User> deleted = userRepository.findById(user.getId());

        assertTrue(deleted.isEmpty());

    }

    @Test
    public void testFindByUsername() {
        User user = new User();
        user.setUsername("misha");
        user.setPassword("pass");
        userRepository.save(user);

        Optional<User> foundOptional = userRepository.findByUsername("misha");
        assertTrue(foundOptional.isPresent());
        User found = foundOptional.get();
        assertNotNull(found);
        assertEquals(user.getId(), found.getId());
    }

    @Test
    public void testFindAll() {
        User user1 = new User();
        user1.setUsername("misha");
        user1.setPassword("pass");

        User user2 = new User();
        user2.setUsername("vano");
        user2.setPassword("pass");

        userRepository.save(user1);
        userRepository.save(user2);

        Set<User> users = userRepository.findAll();
        assertEquals(2, users.size());
    }

    @Test
    public void testFindBorrowedReadBooksByUserId() {
        Book book1 = new Book("100_Years_of_Solitude","100 Years of Solitude", "Fiction", "Gabriel Garcia Marquez", LocalDate.now(),
                "A classic novel about the Buendia family",
                4L, 600L, 300L, "100YearsOfSolitude.jpg");

        Book book2 = new Book("Dzalis_Gamogvidzeba","Dzalis Gamogvidzeba", "Politics", "Mikheil Saakashvili",
                LocalDate.now(), "A thrilling political fiction novel", 4L, 555L, 400L, "dzalisGamogvidzeba.jpg");


        User user = new User();
        user.setUsername("kubdari");
        user.setPassword("kubdari1234");
        Set<Book> borrowed = new HashSet<>();
        borrowed.add(book1);
        borrowed.add(book2);
        user.setBorrowedBooks(borrowed);

        userRepository.save(user);

        Set<Book> foundBooks = userRepository.findBorrowedBooksByUserId(user.getId());

        assertEquals(2, foundBooks.size());
        assertTrue(foundBooks.contains(book1));
        assertTrue(foundBooks.contains(book2));

        user.setReadBooks(borrowed);
        user.setBorrowedBooks(new HashSet<>());

        userRepository.update(user);

        Set<Book> readBooks = userRepository.findReadBooksByUserId(user.getId());
        Set<Book> borrowedBooks = userRepository.findBorrowedBooksByUserId(user.getId());

        assertEquals(2, readBooks.size());
        assertEquals(0, borrowedBooks.size());
    }


}
