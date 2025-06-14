package com.example.libraryproject.utilities;

import com.example.libraryproject.model.dto.RegistrationRequest;
import com.example.libraryproject.model.dto.UserDTO;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.BookKeeper;
import com.example.libraryproject.model.entity.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashSet;

public class Mappers {

    public static User mapRequestToUser(RegistrationRequest userRequest) {
        User user = new User();
        String hashedPassword = BCrypt.hashpw(userRequest.password(), BCrypt.gensalt());
        user.setUsername(userRequest.username());
        user.setPassword(hashedPassword);
        user.setBorrowedBooks(new HashSet<Book>());
        user.setReadBooks(new HashSet<Book>());
        user.setReviewCount(0L);
        return user;
    }
    public static BookKeeper mapRequestToBookKeeper(RegistrationRequest bookKeeperRequest) {
        BookKeeper bookKeeper = new BookKeeper();
        String hashedPassword = BCrypt.hashpw(bookKeeperRequest.password(), BCrypt.gensalt());
        bookKeeper.setUsername(bookKeeperRequest.username());
        bookKeeper.setPassword(hashedPassword);
        return bookKeeper;
    }
}
