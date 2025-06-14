package libraryproject.repository;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.repository.UserRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestUserRepository {

    private SessionFactory sessionFactory;
    private Session session;
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
        session = sessionFactory.openSession();
        userRepository = new UserRepository(session);
    }

    @AfterEach
    public void tearDown() {
        session.close();
        sessionFactory.close();
    }

    @Test
    public void testSaveAndFindById() {
        User user = new User();
        user.setUsername("misha");
        user.setPassword("pass");
        user.setBorrowedBooks(new HashSet<>());
        userRepository.save(user);

        User found = userRepository.findById(user.getId());
        assertNotNull(found);
        assertEquals("misha", found.getUsername());
    }

    @Test
    public void testUpdate() {
        User user = new User();
        user.setUsername("original");
        user.setPassword("pass");
        user.setBorrowedBooks(new HashSet<>());
        userRepository.save(user);

        user.setUsername("updated");
        userRepository.update(user);

        User found = userRepository.findById(user.getId());
        assertEquals("updated", found.getUsername());
    }

    @Test
    public void testDelete() {
        User user = new User();
        user.setUsername("toDelete");
        user.setPassword("pass");
        user.setBorrowedBooks(new HashSet<>());
        userRepository.save(user);

        userRepository.delete(user);
        User deleted = userRepository.findById(user.getId());
        assertNull(deleted);
    }

    @Test
    public void testFindByUsername() {
        User user = new User();
        user.setUsername("misha");
        user.setPassword("pass");
        user.setBorrowedBooks(new HashSet<>());
        userRepository.save(user);

        User found = userRepository.findByUsername("misha");
        assertNotNull(found);
        assertEquals(user.getId(), found.getId());
    }

    @Test
    public void testFindAll() {
        User user1 = new User();
        user1.setUsername("misha");
        user1.setPassword("pass");
        user1.setBorrowedBooks(new HashSet<>());

        User user2 = new User();
        user2.setUsername("vano");
        user2.setPassword("pass");
        user2.setBorrowedBooks(new HashSet<>());

        userRepository.save(user1);
        userRepository.save(user2);

        List<User> users = userRepository.findAll();
        assertEquals(2, users.size());
    }

}
