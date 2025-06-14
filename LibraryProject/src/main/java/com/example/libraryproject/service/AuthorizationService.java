package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.RegistrationRequest;
import com.example.libraryproject.model.entity.BookKeeper;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.repository.BookKeeperRepository;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.utilities.Mappers;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;


@RequiredArgsConstructor
public class AuthorizationService {

    public static final String ATTRIBUTE_NAME = "AuthorizationService";

    private final UserRepository userRepository;
    private final BookKeeperRepository bookKeeperRepository;

    public void register(RegistrationRequest request) {
        switch (request.role()) {
            case BOOKKEEPER -> registerBookKeeper(request);
            case USER -> registerUser(request);
        }
    }

    private void registerBookKeeper(RegistrationRequest request) {
        BookKeeper bookKeeper = bookKeeperRepository.findByUsername(request.username());
        if (bookKeeper != null) {
            throw new IllegalArgumentException("BookKeeper with this username already exists");
        }
        bookKeeper = Mappers.mapRequestToBookKeeper(request);
        bookKeeperRepository.save(bookKeeper);
    }

    private void registerUser(RegistrationRequest request) {
        User user = userRepository.findByUsername(request.username());
        if (user != null) {
            throw new IllegalArgumentException("User with this username already exists");
        }
        user = Mappers.mapRequestToUser(request);
        userRepository.save(user);
    }

    public void login(RegistrationRequest request) {
        switch (request.role()) {
            case BOOKKEEPER -> loginBookKeeper(request);
            case USER -> loginUser(request);
        }
    }

    private void loginBookKeeper(RegistrationRequest request) {
        String hashedPassword = BCrypt.hashpw(request.password(), BCrypt.gensalt());
        BookKeeper keeper = bookKeeperRepository.findByUsername(request.username());
        if (keeper == null)
            throw new IllegalArgumentException("BookKeeper with this username does not exist");
        if (!keeper.getPassword().equals(hashedPassword)) {
            throw new IllegalArgumentException("Incorrect password for BookKeeper");
        }
    }

    private void loginUser(RegistrationRequest request) {
        String hashedPassword = BCrypt.hashpw(request.password(), BCrypt.gensalt());
        User user = userRepository.findByUsername(request.username());
        if (user == null)
            throw new IllegalArgumentException("User with this username does not exist");
        if (!user.getPassword().equals(hashedPassword)) {
            throw new IllegalArgumentException("Incorrect password for User");
        }

    }
}
