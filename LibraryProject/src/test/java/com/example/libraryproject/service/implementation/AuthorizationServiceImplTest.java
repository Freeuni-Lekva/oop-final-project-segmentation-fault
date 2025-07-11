package com.example.libraryproject.service.implementation;

import com.example.libraryproject.model.dto.LoginRequest;
import com.example.libraryproject.model.dto.LoginResult;
import com.example.libraryproject.model.dto.RegistrationRequest;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.Role;
import com.example.libraryproject.model.enums.UserStatus;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.service.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthorizationServiceImplTest {

    private UserRepository userRepository;
    private AuthorizationServiceImpl authorizationServiceImpl;
    private MailService mailService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        mailService = mock(MailService.class);
        authorizationServiceImpl = new AuthorizationServiceImpl(userRepository, mailService);
    }

    @Test
    void testRegisterUser_Success() {
        RegistrationRequest request = new RegistrationRequest("newuser", "pass123", "froste3110@gmail.com", Role.USER);

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

        authorizationServiceImpl.register(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterBookKeeper_Success() {
        RegistrationRequest request = new RegistrationRequest("keeper", "pass123", "froste3110@gmail.com", Role.BOOKKEEPER);
        when(userRepository.findByUsername("keeper")).thenReturn(Optional.empty());

        authorizationServiceImpl.register(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterUser_DuplicateUsername_ThrowsException() {
        RegistrationRequest request = new RegistrationRequest("existing", "pass123", "froste3110@gmail.com", Role.USER);
        User existingUser = new User();
        existingUser.setUsername("existing");
        when(userRepository.findByUsername("existing")).thenReturn(Optional.of(existingUser));

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
        user.setRole(Role.USER);

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest("user1", "secret");
        LoginResult result = authorizationServiceImpl.login(request);

        assertEquals("user1", result.username());
        assertEquals(Role.USER, result.role());
    }

    @Test
    void testLogin_Success_BookKeeper() {
        String hashedPassword = BCrypt.hashpw("secret", BCrypt.gensalt());
        User user = new User();
        user.setUsername("keeper1");
        user.setPassword(hashedPassword);
        user.setRole(Role.BOOKKEEPER);

        when(userRepository.findByUsername("keeper1")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest("keeper1", "secret");
        LoginResult result = authorizationServiceImpl.login(request);

        assertEquals("keeper1", result.username());
        assertEquals(Role.BOOKKEEPER, result.role());
    }

    @Test
    void testLogin_Failure_WrongPassword() {
        String hashedPassword = BCrypt.hashpw("correct", BCrypt.gensalt());
        User user = new User();
        user.setUsername("user1");
        user.setPassword(hashedPassword);

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest("user1", "wrong");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> authorizationServiceImpl.login(request));
        assertEquals("Username or password is incorrect. Please try again.", ex.getMessage());
    }

    @Test
    void testLogin_Failure_UserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("ghost", "password");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> authorizationServiceImpl.login(request));
        assertEquals("Username or password is incorrect. Please try again.", ex.getMessage());
    }

    @Test
    void testCheckBookkeeper_Success() {
        User bookkeeper = new User();
        bookkeeper.setUsername("activeBookkeeper");
        bookkeeper.setRole(Role.BOOKKEEPER);
        bookkeeper.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByUsernameAndRole("activeBookkeeper", Role.BOOKKEEPER))
                .thenReturn(Optional.of(bookkeeper));

        boolean result = authorizationServiceImpl.checkBookkeeper("activeBookkeeper");

        assertTrue(result);
        verify(userRepository).findByUsernameAndRole("activeBookkeeper", Role.BOOKKEEPER);
    }

    @Test
    void testCheckBookkeeper_Failure() {
        when(userRepository.findByUsernameAndRole("nonexistent", Role.BOOKKEEPER))
                .thenReturn(Optional.empty());

        boolean result = authorizationServiceImpl.checkBookkeeper("nonexistent");

        assertFalse(result);
        verify(userRepository).findByUsernameAndRole("nonexistent", Role.BOOKKEEPER);
    }

    @Test
    void testCheckUser_Success() {
        User user = new User();
        user.setUsername("activeUser");
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByUsernameAndRole("activeUser", Role.USER))
                .thenReturn(Optional.of(user));

        boolean result = authorizationServiceImpl.checkUser("activeUser");

        assertTrue(result);
        verify(userRepository).findByUsernameAndRole("activeUser", Role.USER);
    }

    @Test
    void testCheckUser_Failure() {
        when(userRepository.findByUsernameAndRole("nonexistent", Role.USER))
                .thenReturn(Optional.empty());

        boolean result = authorizationServiceImpl.checkUser("nonexistent");

        assertFalse(result);
        verify(userRepository).findByUsernameAndRole("nonexistent", Role.USER);
    }
}
