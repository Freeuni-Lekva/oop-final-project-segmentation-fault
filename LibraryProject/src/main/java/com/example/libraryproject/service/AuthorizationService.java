package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.LoginRequest;
import com.example.libraryproject.model.dto.LoginResult;
import com.example.libraryproject.model.dto.RegistrationRequest;
import com.example.libraryproject.model.entity.BookKeeper;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.UserStatus;
import com.example.libraryproject.repository.BookKeeperRepository;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.utilities.Mappers;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@RequiredArgsConstructor
public class AuthorizationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationService.class);
    private final UserRepository userRepository;
    private final BookKeeperRepository bookKeeperRepository;

    public void register(RegistrationRequest request) {
        switch (request.role()) {
            case BOOKKEEPER -> registerBookKeeper(request);
            case USER -> registerUser(request);
        }
    }

    private void registerBookKeeper(RegistrationRequest request) {
        Optional<BookKeeper> optionalBookKeeper = bookKeeperRepository.findByUsername(request.username());
        Optional<User> optionalUser = userRepository.findByUsername(request.username());
        if (optionalUser.isPresent() || optionalBookKeeper.isPresent()) {
            throw new IllegalArgumentException("This username already exists");
        }
        BookKeeper bookKeeper = Mappers.mapRequestToBookKeeper(request);
        bookKeeperRepository.save(bookKeeper);
        logger.info("BookKeeper with username {} registered successfully", request.username());
    }

    private void registerUser(RegistrationRequest request) {
        Optional<BookKeeper> optionalBookKeeper = bookKeeperRepository.findByUsername(request.username());
        Optional<User> optionalUser = userRepository.findByUsername(request.username());
        if (optionalUser.isPresent() || optionalBookKeeper.isPresent()) {
            throw new IllegalArgumentException("This username already exists");
        }
        User user = Mappers.mapRequestToUser(request);
        userRepository.save(user);
        logger.info("User with username {} registered successfully", request.username());
    }

    public LoginResult login(LoginRequest request) {
        String username = request.username();
        String password = request.password();

        Optional<BookKeeper> optionalBookKeeper = bookKeeperRepository.findByUsername(username);
        Optional<User> optionalUser = userRepository.findByUsername(username);

        String errorMsg = "Username or password is incorrect. Please try again.";
        if (optionalBookKeeper.isEmpty() && optionalUser.isEmpty()) {
            throw new IllegalArgumentException(errorMsg);
        }
        if (optionalBookKeeper.isPresent()) {
            BookKeeper bookKeeper = optionalBookKeeper.get();
            if (!BCrypt.checkpw(password, bookKeeper.getPassword())) {
                throw new IllegalArgumentException(errorMsg);
            }
            return new LoginResult(username, com.example.libraryproject.model.enums.Role.BOOKKEEPER);
        } else {
            User user = optionalUser.get();
            if (!BCrypt.checkpw(password, user.getPassword())) {
                throw new IllegalArgumentException(errorMsg);
            }
            return new LoginResult(username, com.example.libraryproject.model.enums.Role.USER);
        }
    }

    public boolean checkBookkeeper(String username) {

        Optional<BookKeeper> optionalBookKeeper = bookKeeperRepository.findByUsername(username);

        return optionalBookKeeper.isPresent();
    }

    public boolean checkUser(String username) {

        Optional<User> optionalUser = userRepository.findByUsername(username);

        return optionalUser.isPresent() && optionalUser.get().getStatus() == UserStatus.ACTIVE;
    }

}
