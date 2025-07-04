package com.example.libraryproject.service.implementation;

import com.example.libraryproject.model.dto.LoginRequest;
import com.example.libraryproject.model.dto.RegistrationRequest;
import com.example.libraryproject.model.entity.BookKeeper;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.Role;
import com.example.libraryproject.repository.BookKeeperRepository;
import com.example.libraryproject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthorizationServiceImplTest {

    private AuthorizationServiceImpl authorizationServiceImpl;
    private UserRepository userRepository;
    private BookKeeperRepository bookKeeperRepository;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        bookKeeperRepository = mock(BookKeeperRepository.class);
        authorizationServiceImpl = new AuthorizationServiceImpl(userRepository, bookKeeperRepository);
    }

    @Test
    void testRegisterUser_Success() {
        RegistrationRequest request = new RegistrationRequest("newuser", "pass123", Role.USER);

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(bookKeeperRepository.findByUsername("newuser")).thenReturn(Optional.empty());

        authorizationServiceImpl.register(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterBookKeeper_Success() {
        RegistrationRequest request = new RegistrationRequest("keeper", "pass123", Role.BOOKKEEPER);
        when(userRepository.findByUsername("keeper")).thenReturn(Optional.empty());
        when(bookKeeperRepository.findByUsername("keeper")).thenReturn(Optional.empty());

        authorizationServiceImpl.register(request);

        verify(bookKeeperRepository).save(any(BookKeeper.class));
    }

    @Test
    void testRegisterUser_DuplicateUsername_ThrowsException() {
        RegistrationRequest request = new RegistrationRequest("existing", "pass123", Role.USER);
        when(userRepository.findByUsername("existing")).thenReturn(Optional.of(new User()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authorizationServiceImpl.register(request));

        assertEquals("This username already exists", ex.getMessage());
    }

    @Test
    void testLogin_Success_User() {
        String hashedPassword = BCrypt.hashpw("secret", BCrypt.gensalt());
        User user = new User();
        user.setUsername("user1");
        user.setPassword(hashedPassword);

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(bookKeeperRepository.findByUsername("user1")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("user1", "secret");

        assertDoesNotThrow(() -> authorizationServiceImpl.login(request));
    }

    @Test
    void testLogin_Failure_WrongPassword() {
        String hashedPassword = BCrypt.hashpw("correct", BCrypt.gensalt());
        User user = new User();
        user.setUsername("user1");
        user.setPassword(hashedPassword);

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(bookKeeperRepository.findByUsername("user1")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("user1", "wrong");

        assertThrows(IllegalArgumentException.class, () -> authorizationServiceImpl.login(request));
    }

    @Test
    void testLogin_Failure_UserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        when(bookKeeperRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("ghost", "any");

        assertThrows(IllegalArgumentException.class, () -> authorizationServiceImpl.login(request));
    }
}
