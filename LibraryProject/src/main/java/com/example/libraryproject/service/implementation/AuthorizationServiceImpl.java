package com.example.libraryproject.service.implementation;

import com.example.libraryproject.model.dto.LoginRequest;
import com.example.libraryproject.model.dto.LoginResult;
import com.example.libraryproject.model.dto.RegistrationRequest;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.Role;
import com.example.libraryproject.model.enums.UserStatus;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.service.AuthorizationService;
import com.example.libraryproject.service.MailService;
import com.example.libraryproject.utilities.Mappers;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationServiceImpl.class);
    private final UserRepository userRepository;
    private final MailService mailService;

    public User register(RegistrationRequest request) {
        logger.info("Attempting to register user: {} with role: {}", request.username(), request.role());
        Optional<User> existingUser = userRepository.findByUsername(request.username());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("This username already exists");
        }
        Optional<User> existingUserWithMail = userRepository.findByMail(request.mail());
        if (existingUserWithMail.isPresent()) {
            throw new IllegalArgumentException("Account with this mail already exists");
        }
        User user = Mappers.mapRequestToUser(request);
        try {
            mailService.sendEmail(
                    List.of(user.getMail()),
                    "Library Registration",
                    "Welcome to the Library, " + user.getUsername() + "! Your registration was successful."
            );
        } catch (Exception e) {
            logger.error("Failed to send registration email to {}: {}", user.getMail(), e.getMessage());
        }
        userRepository.save(user);
        logger.info("User with username {} and role {} registered successfully", request.username(), request.role());
        return user;
    }

    public LoginResult login(LoginRequest request) {
        String username = request.username();
        String password = request.password();

        Optional<User> optionalUser = userRepository.findByUsername(username);

        String errorMsg = "Username or password is incorrect. Please try again.";
        if (optionalUser.isEmpty()) {
            logger.warn("Login failed: user {} not found", username);
            throw new IllegalArgumentException(errorMsg);
        }

        User user = optionalUser.get();
        if (!BCrypt.checkpw(password, user.getPassword())) {
            logger.warn("Login failed: incorrect password for user {}", username);
            throw new IllegalArgumentException(errorMsg);
        }
        
        // Check if user account is activated
        if (user.getStatus() == UserStatus.INACTIVE) {
            logger.warn("Login failed: user {} account is not activated", username);
            throw new IllegalArgumentException("Your account is not activated yet. Please check your email for the activation link.");
        }
        
        if (user.getStatus() == UserStatus.BANNED) {
            logger.warn("Login failed: user {} account is banned", username);
            throw new IllegalArgumentException("Your account has been banned. Please contact support.");
        }
        
        if (user.getStatus() == UserStatus.CLOSED) {
            logger.warn("Login failed: user {} account is closed", username);
            throw new IllegalArgumentException("Your account has been closed. Please contact support.");
        }
        
        return new LoginResult(username, user.getRole());
    }

    public boolean checkBookkeeper(String username) {
        Optional<User> optionalUser = userRepository.findByUsernameAndRole(username, Role.BOOKKEEPER);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            logger.info("BOOKKEEPER user details - Role: {}, Status: {}", user.getRole(), user.getStatus());
            return user.getStatus() == UserStatus.ACTIVE;
        }

        logger.warn("BOOKKEEPER not found for username: {}", username);
        return false;
    }

    public boolean checkUser(String username) {
        Optional<User> optionalUser = userRepository.findByUsernameAndRole(username, Role.USER);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            logger.info("USER user details - Role: {}, Status: {}", user.getRole(), user.getStatus());
            return user.getStatus() == UserStatus.ACTIVE || user.getStatus() == UserStatus.BANNED;
        }

        logger.warn("USER not found for username: {}", username);
        return false;
    }

}
