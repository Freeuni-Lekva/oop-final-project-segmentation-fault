package com.example.libraryproject.utilities;

import com.example.libraryproject.model.dto.BookDTO;
import com.example.libraryproject.model.dto.GoogleBooksResponse;
import com.example.libraryproject.model.dto.RegistrationRequest;
import com.example.libraryproject.model.dto.UserDTO;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.BookKeeper;
import com.example.libraryproject.model.entity.User;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.libraryproject.configuration.ApplicationProperties.DEFAULT_RATING;

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
        String safeTitle = googleBooksResponse.title().replaceAll("[^a-zA-Z0-9.\\-]", "_");
        book.setPublicId(safeTitle);
        book.setName(googleBooksResponse.title());
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
        long initialCopies = ThreadLocalRandom.current().nextLong(2, 16);
        book.setTotalAmount(initialCopies);
        book.setCurrentAmount(initialCopies);
        book.setRating((long) DEFAULT_RATING);

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

    public static UserDTO convertUser(User user) {
        return new UserDTO(user.getUsername(), user.getStatus().toString());
    }
    public static BookDTO mapBookToDTO(Book book) {
        return new BookDTO(
                book.getName(),
                book.getDescription(),
                book.getGenre(),
                book.getAuthor(),
                book.getImageUrl(),
                book.getTotalAmount(),
                book.getCurrentAmount(),
                book.getVolume(),
                book.getRating()
        );
    }
}
