package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.LoginRequest;
import com.example.libraryproject.model.dto.RegistrationRequest;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.BookKeeper;
import com.example.libraryproject.model.entity.User;
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

    public void login(LoginRequest request) {
        String username = request.username();
        String password = request.password();

        Optional<BookKeeper> optionalBookKeeper = bookKeeperRepository.findByUsername(request.username());
        Optional<User> optionalUser = userRepository.findByUsername(request.username());

        if (optionalUser.isEmpty() && optionalBookKeeper.isEmpty()) {
            logger.info("Login attempt with non-existing username: {}", username);
            throw new IllegalArgumentException("This username does not exist");
        }

        String storedPassword;
        if (optionalBookKeeper.isPresent()){
            BookKeeper bookKeeper = optionalBookKeeper.get();
            storedPassword = bookKeeper.getPassword();
        } else {
            User user = optionalUser.get();
            storedPassword = user.getPassword();
        }

        if (!BCrypt.checkpw(password, storedPassword)) {
            logger.info("Incorrect password for user {}", username);
            throw new IllegalArgumentException("Incorrect password");
        }

    }

}
