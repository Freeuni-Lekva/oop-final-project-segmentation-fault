package com.example.libraryproject.service;

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

public class AuthorizationServiceTest {

    private AuthorizationService authorizationService;
    private UserRepository userRepository;
    private BookKeeperRepository bookKeeperRepository;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        bookKeeperRepository = mock(BookKeeperRepository.class);
        authorizationService = new AuthorizationService(userRepository, bookKeeperRepository);
    }

    @Test
    void testRegisterUser_Success() {
        RegistrationRequest request = new RegistrationRequest("newuser", "pass123", Role.USER);

        when(userRepository.findByUsername("newuser")).thenReturn(null);
        when(bookKeeperRepository.findByUsername("newuser")).thenReturn(null);

        authorizationService.register(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterBookKeeper_Success() {
        RegistrationRequest request = new RegistrationRequest("keeper", "pass123", Role.BOOKKEEPER);
        when(userRepository.findByUsername("keeper")).thenReturn(null);
        when(bookKeeperRepository.findByUsername("keeper")).thenReturn(null);

        authorizationService.register(request);

        verify(bookKeeperRepository).save(any(BookKeeper.class));
    }

    @Test
    void testRegisterUser_DuplicateUsername_ThrowsException() {
        RegistrationRequest request = new RegistrationRequest("existing", "pass123", Role.USER);
        when(userRepository.findByUsername("existing")).thenReturn(Optional.of(new User()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authorizationService.register(request));

        assertEquals("This username already exists", ex.getMessage());
    }

    @Test
    void testLogin_Success_User() {
        String hashedPassword = BCrypt.hashpw("secret", BCrypt.gensalt());
        User user = new User();
        user.setUsername("user1");
        user.setPassword(hashedPassword);

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(bookKeeperRepository.findByUsername("user1")).thenReturn(null);

        LoginRequest request = new LoginRequest("user1", "secret");

        assertDoesNotThrow(() -> authorizationService.login(request));
    }

    @Test
    void testLogin_Failure_WrongPassword() {
        String hashedPassword = BCrypt.hashpw("correct", BCrypt.gensalt());
        User user = new User();
        user.setUsername("user1");
        user.setPassword(hashedPassword);

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(bookKeeperRepository.findByUsername("user1")).thenReturn(null);

        LoginRequest request = new LoginRequest("user1", "wrong");

        assertThrows(IllegalArgumentException.class, () -> authorizationService.login(request));
    }

    @Test
    void testLogin_Failure_UserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(null);
        when(bookKeeperRepository.findByUsername("ghost")).thenReturn(null);

        LoginRequest request = new LoginRequest("ghost", "any");

        assertThrows(IllegalArgumentException.class, () -> authorizationService.login(request));
    }
}
