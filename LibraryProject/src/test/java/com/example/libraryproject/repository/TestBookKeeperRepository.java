package com.example.libraryproject.repository;
import com.example.libraryproject.model.entity.BookKeeper;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestBookKeeperRepository {

    private SessionFactory sessionFactory;
    private BookKeeperRepository bookKeeperRepository;

    @BeforeEach
    public void setUp() {
        Configuration configuration = new Configuration()
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
                .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.show_sql", "true")
                .addAnnotatedClass(BookKeeper.class);

        sessionFactory = configuration.buildSessionFactory();
        bookKeeperRepository  = new BookKeeperRepository(sessionFactory);
    }

    @AfterEach
    public void tearDown() {
        sessionFactory.close();
    }

    @Test
    public void testSaveAndDeleteBookKeeper() {
        BookKeeper bookKeeper = new BookKeeper();
        bookKeeper.setUsername("luka");
        bookKeeper.setPassword("paroli123");
        bookKeeperRepository.save(bookKeeper);

        assertEquals("luka", bookKeeper.getUsername());
        bookKeeperRepository.delete(bookKeeper);
        assertTrue(bookKeeperRepository.findById(bookKeeper.getId()).isEmpty());
    }

    @Test
    public void testUpdateBookKeeper() {
        BookKeeper bookKeeper = new BookKeeper();
        bookKeeper.setUsername("luka");
        bookKeeper.setPassword("paroli123");
        bookKeeperRepository.save(bookKeeper);
        assertEquals("luka", bookKeeper.getUsername());
        bookKeeper.setUsername("maka");
        bookKeeperRepository.update(bookKeeper);
        assertNotNull(bookKeeperRepository.findByUsername("maka"));
    }

    @Test
    public void testEverything() {
        BookKeeper bookKeeper1 = new BookKeeper();
        bookKeeper1.setUsername("luka");
        bookKeeper1.setPassword("paroli123");
        bookKeeperRepository.save(bookKeeper1);

        BookKeeper bookKeeper2 = new BookKeeper();
        bookKeeper2.setUsername("maka");
        bookKeeper2.setPassword("paroli123");
        bookKeeperRepository.save(bookKeeper2);

        assertEquals("luka", bookKeeper1.getUsername());
        assertEquals("maka", bookKeeper2.getUsername());
        bookKeeperRepository.delete(bookKeeper1);
        assertTrue(bookKeeperRepository.findByUsername("luka").isEmpty());
        assertTrue(bookKeeperRepository.findByUsername("maka").isPresent());
    }

}
