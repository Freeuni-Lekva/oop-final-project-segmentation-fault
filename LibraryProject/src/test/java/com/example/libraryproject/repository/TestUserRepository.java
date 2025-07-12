package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.Role;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        user.setMail("misha@gmail.com");
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
        user.setMail("misha@gmail.com");
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
        user.setMail("misha@gmail.com");
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
        user.setMail("misha@gmail.com");
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
        user1.setMail("misha@gmail.com");

        User user2 = new User();
        user2.setUsername("vano");
        user2.setPassword("pass");
        user2.setMail("misha2@gmail.com");
        userRepository.save(user1);
        userRepository.save(user2);

        Set<User> users = userRepository.findAll();
        assertEquals(2, users.size());
    }

    @Test
    public void testFindBorrowedReadBooksByUserId() {
        Book book1 = new Book("100_Years_of_Solitude","100 Years of Solitude", "Fiction", "Gabriel Garcia Marquez", LocalDate.now(),
                LocalDateTime.now(), "A classic novel about the Buendia family",
                300L, 4L, 600L, 300.0, "100YearsOfSolitude.jpg");

        Book book2 = new Book("Dzalis_Gamogvidzeba","Dzalis Gamogvidzeba", "Politics", "Mikheil Saakashvili",
                LocalDate.now(), LocalDateTime.now(), "A thrilling political fiction novel", 300L, 4L, 555L, 400.0, "dzalisGamogvidzeba.jpg");


        User user = new User();
        user.setUsername("kubdari");
        user.setPassword("kubdari1234");
        Set<Book> borrowed = new HashSet<>();
        borrowed.add(book1);
        borrowed.add(book2);
        user.setBorrowedBooks(borrowed);
        user.setMail("misha@gmail.com");
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

    @Test
    public void testUpdateAll() {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("pass1");
        user1.setMail("user1@gmail.com");
        user1.setRole(Role.USER);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("pass2");
        user2.setMail("user2@gmail.com");
        user2.setRole(Role.USER);

        User user3 = new User();
        user3.setUsername("user3");
        user3.setPassword("pass3");
        user3.setMail("user3@gmail.com");
        user3.setRole(Role.BOOKKEEPER);

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        user1.setBio("Updated bio 1");
        user2.setBio("Updated bio 2");
        user3.setBio("Updated bio 3");

        Set<User> usersToUpdate = Set.of(user1, user2);
        userRepository.updateAll(usersToUpdate);

        Optional<User> updatedUser1 = userRepository.findById(user1.getId());
        Optional<User> updatedUser2 = userRepository.findById(user2.getId());
        Optional<User> notUpdatedUser3 = userRepository.findById(user3.getId());

        assertTrue(updatedUser1.isPresent());
        assertTrue(updatedUser2.isPresent());
        assertTrue(notUpdatedUser3.isPresent());

        assertEquals("Updated bio 1", updatedUser1.get().getBio());
        assertEquals("Updated bio 2", updatedUser2.get().getBio());
        assertEquals("", notUpdatedUser3.get().getBio());
    }

    @Test
    public void testFindByMail() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpass");
        user.setMail("test@example.com");
        user.setRole(Role.USER);
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByMail("test@example.com");
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        assertEquals("test@example.com", foundUser.get().getMail());

        Optional<User> notFoundUser = userRepository.findByMail("nonexistent@example.com");
        assertFalse(notFoundUser.isPresent());

        Optional<User> nullMailUser = userRepository.findByMail(null);
        assertFalse(nullMailUser.isPresent());
    }

    @Test
    public void testFindByUsernameAndRole() {
        User userWithUserRole = new User();
        userWithUserRole.setUsername("regularuser");
        userWithUserRole.setPassword("userpass");
        userWithUserRole.setMail("user@example.com");
        userWithUserRole.setRole(Role.USER);

        User userWithBookkeeperRole = new User();
        userWithBookkeeperRole.setUsername("bookkeeper");
        userWithBookkeeperRole.setPassword("bookpass");
        userWithBookkeeperRole.setMail("bookkeeper@example.com");
        userWithBookkeeperRole.setRole(Role.BOOKKEEPER);

        User anotherUserWithBookkeeperRole = new User();
        anotherUserWithBookkeeperRole.setUsername("anotherbookkeeper");
        anotherUserWithBookkeeperRole.setPassword("pass");
        anotherUserWithBookkeeperRole.setMail("another@example.com");
        anotherUserWithBookkeeperRole.setRole(Role.BOOKKEEPER);

        userRepository.save(userWithUserRole);
        userRepository.save(userWithBookkeeperRole);
        userRepository.save(anotherUserWithBookkeeperRole);

        Optional<User> foundUser = userRepository.findByUsernameAndRole("regularuser", Role.USER);
        assertTrue(foundUser.isPresent());
        assertEquals("regularuser", foundUser.get().getUsername());
        assertEquals(Role.USER, foundUser.get().getRole());

        Optional<User> foundBookkeeper = userRepository.findByUsernameAndRole("bookkeeper", Role.BOOKKEEPER);
        assertTrue(foundBookkeeper.isPresent());
        assertEquals("bookkeeper", foundBookkeeper.get().getUsername());
        assertEquals(Role.BOOKKEEPER, foundBookkeeper.get().getRole());

        Optional<User> wrongRole = userRepository.findByUsernameAndRole("regularuser", Role.BOOKKEEPER);
        assertFalse(wrongRole.isPresent());

        Optional<User> notFound = userRepository.findByUsernameAndRole("nonexistent", Role.USER);
        assertFalse(notFound.isPresent());
    }

    @Test
    public void testFindByRole() {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("pass1");
        user1.setMail("user1@example.com");
        user1.setRole(Role.USER);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("pass2");
        user2.setMail("user2@example.com");
        user2.setRole(Role.USER);

        User bookkeeper1 = new User();
        bookkeeper1.setUsername("bookkeeper1");
        bookkeeper1.setPassword("bookpass1");
        bookkeeper1.setMail("bookkeeper1@example.com");
        bookkeeper1.setRole(Role.BOOKKEEPER);

        User bookkeeper2 = new User();
        bookkeeper2.setUsername("bookkeeper2");
        bookkeeper2.setPassword("bookpass2");
        bookkeeper2.setMail("bookkeeper2@example.com");
        bookkeeper2.setRole(Role.BOOKKEEPER);

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(bookkeeper1);
        userRepository.save(bookkeeper2);

        Set<User> users = userRepository.findByRole(Role.USER);
        assertEquals(2, users.size());
        assertTrue(users.stream().allMatch(user -> user.getRole() == Role.USER));
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));

        Set<User> bookkeepers = userRepository.findByRole(Role.BOOKKEEPER);
        assertEquals(2, bookkeepers.size());
        assertTrue(bookkeepers.stream().allMatch(user -> user.getRole() == Role.BOOKKEEPER));
        assertTrue(bookkeepers.contains(bookkeeper1));
        assertTrue(bookkeepers.contains(bookkeeper2));

        assertFalse(users.contains(bookkeeper1));
        assertFalse(bookkeepers.contains(user1));
    }
}
