package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.AccountActivation;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.UserStatus;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class TestAccountActivationRepository {

    private SessionFactory sessionFactory;
    private AccountActivationRepository accountActivationRepository;
    private UserRepository userRepository;
    private User user;

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
                .addAnnotatedClass(Review.class)
                .addAnnotatedClass(AccountActivation.class);

        sessionFactory = configuration.buildSessionFactory();
        accountActivationRepository = new AccountActivationRepository(sessionFactory);
        userRepository = new UserRepository(sessionFactory);

        user = new User();
        user.setUsername("activation_test");
        user.setPassword("secure123");
        user.setMail("activation@example.com");
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    @AfterEach
    public void tearDown() {
        sessionFactory.close();
    }

    @Test
    public void testSaveAndFindById() {
        AccountActivation activation = new AccountActivation();
        activation.setToken(UUID.randomUUID());
        activation.setEmail(user.getMail());
        activation.setUser(user);
        activation.setExpirationDate(LocalDateTime.now().plusDays(1));
        activation.setActivated(false);

        accountActivationRepository.save(activation);

        Optional<AccountActivation> found = accountActivationRepository.findById(activation.getId());
        assertTrue(found.isPresent());
        assertEquals(user.getMail(), found.get().getEmail());
        assertFalse(found.get().isActivated());
    }

    @Test
    public void testUpdateActivationStatus() {
        AccountActivation activation = new AccountActivation();
        activation.setToken(UUID.randomUUID());
        activation.setEmail(user.getMail());
        activation.setUser(user);
        activation.setExpirationDate(LocalDateTime.now().plusDays(1));
        activation.setActivated(false);

        accountActivationRepository.save(activation);

        activation.setActivated(true);
        accountActivationRepository.update(activation);

        Optional<AccountActivation> updated = accountActivationRepository.findById(activation.getId());
        assertTrue(updated.isPresent());
        assertTrue(updated.get().isActivated());
    }

    @Test
    public void testDeleteActivation() {
        AccountActivation activation = new AccountActivation();
        activation.setToken(UUID.randomUUID());
        activation.setEmail(user.getMail());
        activation.setUser(user);
        activation.setExpirationDate(LocalDateTime.now().plusDays(1));
        activation.setActivated(false);

        accountActivationRepository.save(activation);
        accountActivationRepository.delete(activation);

        Optional<AccountActivation> found = accountActivationRepository.findById(activation.getId());
        assertTrue(found.isEmpty());
    }

    @Test
    public void testFindByToken() {
        UUID token = UUID.randomUUID();
        AccountActivation activation = new AccountActivation();
        activation.setToken(token);
        activation.setEmail(user.getMail());
        activation.setUser(user);
        activation.setExpirationDate(LocalDateTime.now().plusDays(1));
        activation.setActivated(false);

        accountActivationRepository.save(activation);

        Optional<AccountActivation> found = accountActivationRepository.findByToken(token);
        assertTrue(found.isPresent());
        assertEquals(token, found.get().getToken());
    }

    @Test
    public void testFindByToken_NotFound() {
        UUID nonExistentToken = UUID.randomUUID();
        Optional<AccountActivation> found = accountActivationRepository.findByToken(nonExistentToken);
        assertTrue(found.isEmpty());
    }

    @Test
    public void testFindByEmail() {
        String email = "test.email@example.com";
        AccountActivation activation = new AccountActivation();
        activation.setToken(UUID.randomUUID());
        activation.setEmail(email);
        activation.setUser(user);
        activation.setExpirationDate(LocalDateTime.now().plusDays(1));
        activation.setActivated(false);

        accountActivationRepository.save(activation);

        Optional<AccountActivation> found = accountActivationRepository.findByEmail(email);
        assertTrue(found.isPresent());
        assertEquals(email, found.get().getEmail());
    }

    @Test
    public void testFindByEmail_NotFound() {
        String nonExistentEmail = "nonexistent@example.com";
        Optional<AccountActivation> found = accountActivationRepository.findByEmail(nonExistentEmail);
        assertTrue(found.isEmpty());
    }

    @Test
    public void testFindByEmail_ActivatedNotReturned() {
        String email = "activated.email@example.com";
        AccountActivation activation = new AccountActivation();
        activation.setToken(UUID.randomUUID());
        activation.setEmail(email);
        activation.setUser(user);
        activation.setExpirationDate(LocalDateTime.now().plusDays(1));
        activation.setActivated(true);

        accountActivationRepository.save(activation);

        Optional<AccountActivation> found = accountActivationRepository.findByEmail(email);
        assertTrue(found.isEmpty());
    }

    @Test
    public void testFindByUser() {
        AccountActivation activation = new AccountActivation();
        activation.setToken(UUID.randomUUID());
        activation.setEmail(user.getMail());
        activation.setUser(user);
        activation.setExpirationDate(LocalDateTime.now().plusDays(1));
        activation.setActivated(false);

        accountActivationRepository.save(activation);

        Optional<AccountActivation> found = accountActivationRepository.findByUser(user);
        assertTrue(found.isPresent());
        assertEquals(user.getId(), found.get().getUser().getId());
    }

    @Test
    public void testFindByUser_NotFound() {
        User anotherUser = new User();
        anotherUser.setUsername("another_user");
        anotherUser.setPassword("password");
        anotherUser.setMail("another@example.com");
        userRepository.save(anotherUser);

        Optional<AccountActivation> found = accountActivationRepository.findByUser(anotherUser);
        assertTrue(found.isEmpty());
    }

    @Test
    public void testFindByUser_ActivatedNotReturned() {
        AccountActivation activation = new AccountActivation();
        activation.setToken(UUID.randomUUID());
        activation.setEmail(user.getMail());
        activation.setUser(user);
        activation.setExpirationDate(LocalDateTime.now().plusDays(1));
        activation.setActivated(true);

        accountActivationRepository.save(activation);

        Optional<AccountActivation> found = accountActivationRepository.findByUser(user);
        assertTrue(found.isEmpty());
    }

    @Test
    public void testFindByTokenAndNotExpired() {
        UUID token = UUID.randomUUID();
        AccountActivation activation = new AccountActivation();
        activation.setToken(token);
        activation.setEmail(user.getMail());
        activation.setUser(user);
        activation.setExpirationDate(LocalDateTime.now().plusDays(1));
        activation.setActivated(false);

        accountActivationRepository.save(activation);

        Optional<AccountActivation> found = accountActivationRepository.findByTokenAndNotExpired(token);
        assertTrue(found.isPresent());
        assertEquals(token, found.get().getToken());
    }

    @Test
    public void testFindByTokenAndNotExpired_Expired() {
        UUID token = UUID.randomUUID();
        AccountActivation activation = new AccountActivation();
        activation.setToken(token);
        activation.setEmail(user.getMail());
        activation.setUser(user);
        activation.setExpirationDate(LocalDateTime.now().minusDays(1));
        activation.setActivated(false);

        accountActivationRepository.save(activation);

        Optional<AccountActivation> found = accountActivationRepository.findByTokenAndNotExpired(token);
        assertTrue(found.isEmpty());
    }

    @Test
    public void testFindByTokenAndNotExpired_Activated() {
        UUID token = UUID.randomUUID();
        AccountActivation activation = new AccountActivation();
        activation.setToken(token);
        activation.setEmail(user.getMail());
        activation.setUser(user);
        activation.setExpirationDate(LocalDateTime.now().plusDays(1));
        activation.setActivated(true);

        accountActivationRepository.save(activation);

        Optional<AccountActivation> found = accountActivationRepository.findByTokenAndNotExpired(token);
        assertTrue(found.isEmpty());
    }

    @Test
    public void testFindExpiredActivations() {
        AccountActivation expiredActivation = new AccountActivation();
        expiredActivation.setToken(UUID.randomUUID());
        expiredActivation.setEmail("expired@example.com");
        expiredActivation.setUser(user);
        expiredActivation.setExpirationDate(LocalDateTime.now().minusDays(1));
        expiredActivation.setActivated(false);
        accountActivationRepository.save(expiredActivation);

        AccountActivation validActivation = new AccountActivation();
        validActivation.setToken(UUID.randomUUID());
        validActivation.setEmail("valid@example.com");
        validActivation.setUser(user);
        validActivation.setExpirationDate(LocalDateTime.now().plusDays(1));
        validActivation.setActivated(false);
        accountActivationRepository.save(validActivation);

        List<AccountActivation> expiredActivations = accountActivationRepository.findExpiredActivations();
        assertEquals(1, expiredActivations.size());
        assertEquals("expired@example.com", expiredActivations.get(0).getEmail());
    }

    @Test
    public void testDeleteExpiredActivations() {
        AccountActivation expiredActivation = new AccountActivation();
        expiredActivation.setToken(UUID.randomUUID());
        expiredActivation.setEmail("expired@example.com");
        expiredActivation.setUser(user);
        expiredActivation.setExpirationDate(LocalDateTime.now().minusDays(1));
        expiredActivation.setActivated(false);
        accountActivationRepository.save(expiredActivation);

        AccountActivation validActivation = new AccountActivation();
        validActivation.setToken(UUID.randomUUID());
        validActivation.setEmail("valid@example.com");
        validActivation.setUser(user);
        validActivation.setExpirationDate(LocalDateTime.now().plusDays(1));
        validActivation.setActivated(false);
        accountActivationRepository.save(validActivation);

        accountActivationRepository.deleteExpiredActivations();

        Optional<AccountActivation> foundExpired = accountActivationRepository.findById(expiredActivation.getId());
        assertTrue(foundExpired.isEmpty());

        Optional<AccountActivation> foundValid = accountActivationRepository.findById(validActivation.getId());
        assertTrue(foundValid.isPresent());
    }

    @Test
    public void testFindAll() {
        AccountActivation activation1 = new AccountActivation();
        activation1.setToken(UUID.randomUUID());
        activation1.setEmail("user1@example.com");
        activation1.setUser(user);
        activation1.setExpirationDate(LocalDateTime.now().plusDays(1));
        activation1.setActivated(false);
        accountActivationRepository.save(activation1);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password2");
        user2.setMail("user2@example.com");
        userRepository.save(user2);

        AccountActivation activation2 = new AccountActivation();
        activation2.setToken(UUID.randomUUID());
        activation2.setEmail("user2@example.com");
        activation2.setUser(user2);
        activation2.setExpirationDate(LocalDateTime.now().plusDays(1));
        activation2.setActivated(true);
        accountActivationRepository.save(activation2);

        List<AccountActivation> allActivations = accountActivationRepository.findAll();
        assertEquals(2, allActivations.size());
    }

    @Test
    public void testFindAll_EmptyRepository() {
        List<AccountActivation> existingActivations = accountActivationRepository.findAll();
        for (AccountActivation activation : existingActivations) {
            accountActivationRepository.delete(activation);
        }

        List<AccountActivation> allActivations = accountActivationRepository.findAll();
        assertTrue(allActivations.isEmpty());
    }

    @Test
    public void testSave_ExceptionHandling() {
        SessionFactory mockSessionFactory = Mockito.mock(SessionFactory.class);
        Session mockSession = Mockito.mock(Session.class);
        Transaction mockTransaction = Mockito.mock(Transaction.class);
        
        when(mockSessionFactory.openSession()).thenReturn(mockSession);
        when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        doThrow(new RuntimeException("Database connection failed")).when(mockSession).persist(any(AccountActivation.class));
        
        AccountActivationRepository repository = new AccountActivationRepository(mockSessionFactory);
        AccountActivation activation = new AccountActivation();
        activation.setToken(UUID.randomUUID());
        activation.setEmail("exception@example.com");
        activation.setUser(user);
        
        assertThrows(RuntimeException.class, () -> repository.save(activation));
        verify(mockTransaction).rollback();
        verify(mockSession).close();
    }

    @Test
    public void testUpdate_ExceptionHandling() {
        SessionFactory mockSessionFactory = Mockito.mock(SessionFactory.class);
        Session mockSession = Mockito.mock(Session.class);
        Transaction mockTransaction = Mockito.mock(Transaction.class);
        
        when(mockSessionFactory.openSession()).thenReturn(mockSession);
        when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        doThrow(new RuntimeException("Update failed")).when(mockSession).merge(any(AccountActivation.class));
        
        AccountActivationRepository repository = new AccountActivationRepository(mockSessionFactory);
        AccountActivation activation = new AccountActivation();
        activation.setId(1L);
        activation.setToken(UUID.randomUUID());
        activation.setEmail("update_exception@example.com");
        activation.setUser(user);
        
        assertThrows(RuntimeException.class, () -> repository.update(activation));
        verify(mockTransaction).rollback();
        verify(mockSession).close();
    }

    @Test
    public void testDelete_ExceptionHandling() {
        SessionFactory mockSessionFactory = Mockito.mock(SessionFactory.class);
        Session mockSession = Mockito.mock(Session.class);
        Transaction mockTransaction = Mockito.mock(Transaction.class);
        
        when(mockSessionFactory.openSession()).thenReturn(mockSession);
        when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        doThrow(new RuntimeException("Delete failed")).when(mockSession).remove(any(AccountActivation.class));
        
        AccountActivationRepository repository = new AccountActivationRepository(mockSessionFactory);
        AccountActivation activation = new AccountActivation();
        activation.setId(1L);
        activation.setToken(UUID.randomUUID());
        activation.setEmail("delete_exception@example.com");
        activation.setUser(user);
        
        assertThrows(RuntimeException.class, () -> repository.delete(activation));
        verify(mockTransaction).rollback();
        verify(mockSession).close();
    }

    @Test
    public void testDeleteExpiredActivations_ExceptionHandling() {
        SessionFactory mockSessionFactory = Mockito.mock(SessionFactory.class);
        Session mockSession = Mockito.mock(Session.class);
        Transaction mockTransaction = Mockito.mock(Transaction.class);
        
        when(mockSessionFactory.openSession()).thenReturn(mockSession);
        when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        when(mockSession.createQuery(anyString())).thenThrow(new RuntimeException("Query execution failed"));
        
        AccountActivationRepository repository = new AccountActivationRepository(mockSessionFactory);
        
        assertThrows(RuntimeException.class, () -> repository.deleteExpiredActivations());
        verify(mockTransaction).rollback();
        verify(mockSession).close();
    }
}
