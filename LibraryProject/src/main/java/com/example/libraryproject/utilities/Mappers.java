package com.example.libraryproject.utilities;

import com.example.libraryproject.model.dto.GoogleBooksResponse;
import com.example.libraryproject.model.dto.RegistrationRequest;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.BookKeeper;
import com.example.libraryproject.model.entity.User;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Random;

public class Mappers {

    public static User mapRequestToUser(RegistrationRequest userRequest) {
        User user = new User();
        String hashedPassword = BCrypt.hashpw(userRequest.password(), BCrypt.gensalt());
        user.setUsername(userRequest.username());
        user.setPassword(hashedPassword);
        user.setBorrowedBooks(new HashSet<>());
        user.setReadBooks(new HashSet<>());
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

    public static Book mapGoogleBookToBook(GoogleBooksResponse googleBooksResponse) {
        Book book = new Book();

        book.setName(googleBooksResponse.title() != null ? googleBooksResponse.title() : "Unknown Title");
        book.setAuthor(googleBooksResponse.author() != null ? googleBooksResponse.author() : "Unknown Author");
        book.setDescription(googleBooksResponse.description() != null ? googleBooksResponse.description() : "No description available");
        book.setImageUrl(googleBooksResponse.thumbnailUrl());
        if (googleBooksResponse.publishedDate() != null && !googleBooksResponse.publishedDate().isEmpty()) {
            try {
                LocalDate parsedDate = getLocalDate(googleBooksResponse);

                book.setDate(parsedDate);
            } catch (Exception e) {
                book.setDate(LocalDate.now());
            }
        } else {
            book.setDate(LocalDate.now());
        }

        book.setGenre(googleBooksResponse.genre());
        book.setVolume(googleBooksResponse.volume());
        Random random = new Random();
        book.setAmountInLib(random.nextLong(2,15));
        book.setRating(0L);

        return book;
    }

    private static LocalDate getLocalDate(GoogleBooksResponse googleBooksResponse) {
        String dateStr = googleBooksResponse.publishedDate();
        LocalDate parsedDate;

        if (dateStr.length() == 4) {
            parsedDate = LocalDate.of(Integer.parseInt(dateStr), 1, 1);
        } else if (dateStr.length() == 7) {
            String[] parts = dateStr.split("-");
            parsedDate = LocalDate.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), 1);
        } else {
            parsedDate = LocalDate.parse(dateStr);
        }
        return parsedDate;
    }
}
