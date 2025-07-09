package com.example.libraryproject.utilities;

import com.example.libraryproject.model.dto.*;
import com.example.libraryproject.model.dto.BookDTO;
import com.example.libraryproject.model.dto.GoogleBooksResponse;
import com.example.libraryproject.model.dto.OrderDTO;
import com.example.libraryproject.model.dto.RegistrationRequest;
import com.example.libraryproject.model.dto.UserDTO;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Order;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import java.util.stream.Collectors;

public class Mappers {

    public static User mapRequestToUser(RegistrationRequest userRequest) {
        User user = new User();
        String hashedPassword = BCrypt.hashpw(userRequest.password(), BCrypt.gensalt());
        user.setUsername(userRequest.username());
        user.setPassword(hashedPassword);
        user.setRole(userRequest.role());
        user.setBorrowedBooks(new HashSet<>());
        user.setReadBooks(new HashSet<>());
        user.setReviewCount(0L);
        return user;
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
        
        // Set default values to prevent null constraint violations
        // These can be overridden by the service layer if needed
        book.setTotalAmount(1L);
        book.setCurrentAmount(1L);
        book.setRating(0.0);

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

    public static UserDTO convertUserToDTO(User user) {
        List<ReviewDTO> reviewDTOs = user.getReviews().stream()
                .map(review -> new ReviewDTO(
                        user.getUsername(),
                        review.getBook().getName(),
                        review.getBook().getAuthor(),
                        review.getRating(),
                        review.getComment()))
                .collect(Collectors.toList());

        List<BookDTO> currentlyReadingDTOs = user.getBorrowedBooks().stream()
                .map(book -> new BookDTO(
                        book.getPublicId() != null ? book.getPublicId() : "",
                        book.getName() != null ? book.getName() : "Unknown Book",
                        book.getDescription() != null ? book.getDescription() : "",
                        book.getGenre() != null ? book.getGenre() : "Unknown",
                        book.getAuthor() != null ? book.getAuthor() : "Unknown Author",
                        book.getImageUrl() != null ? book.getImageUrl() : "",
                        book.getTotalAmount() != null ? book.getTotalAmount() : 0L,
                        book.getCurrentAmount() != null ? book.getCurrentAmount() : 0L,
                        book.getVolume() != null ? book.getVolume() : 0L,
                        book.getRating() != null ? book.getRating() : 0.0,
                        book.getDate() != null ? book.getDate().toString() : ""))
                .collect(Collectors.toList());

        return new UserDTO(
                user.getUsername(),
                user.getBio(),
                user.getReadBooks().size(),
                reviewDTOs.size(),
                reviewDTOs,
                currentlyReadingDTOs,
                user.getStatus().name()
        );
    }
    public static BookDTO mapBookToDTO(Book book) {
        return new BookDTO(
                book.getPublicId() != null ? book.getPublicId() : "",
                book.getName() != null ? book.getName() : "Unknown Title",
                book.getDescription() != null ? book.getDescription() : "",
                book.getGenre() != null ? book.getGenre() : "Unknown",
                book.getAuthor() != null ? book.getAuthor() : "Unknown Author",
                book.getImageUrl() != null ? book.getImageUrl() : "",
                book.getTotalAmount() != null ? book.getTotalAmount() : 0L,
                book.getCurrentAmount() != null ? book.getCurrentAmount() : 0L,
                book.getVolume() != null ? book.getVolume() : 0L,
                book.getRating() != null ? book.getRating() : 0L,
                book.getDate() != null ? book.getDate().toString() : ""
        );
    }

    public static ReviewDTO mapReviewToDTO(Review r) {
        String bookTitle = r.getBook() != null && r.getBook().getName() != null
                ? r.getBook().getName() : "Unknown Title";

        String author = r.getBook() != null && r.getBook().getAuthor() != null
                ? r.getBook().getAuthor() : "Unknown Author";

        String comment = r.getComment() != null ? r.getComment() : "";
        
        String username = r.getUser() != null && r.getUser().getUsername() != null
                ? r.getUser().getUsername() : "Anonymous";

        return new ReviewDTO(
                username,
                bookTitle,
                author,
                r.getRating(),
                comment
        );
    }

    public static OrderDTO mapOrderToDTO(Order order) {
        if (order == null) {
            return null;
        }

        String orderPublicId = order.getPublicId() != null ? order.getPublicId().toString() : "";
        String username = order.getUser() != null && order.getUser().getUsername() != null
                ? order.getUser().getUsername() : "Unknown";
        String bookTitle = order.getBook() != null && order.getBook().getName() != null
                ? order.getBook().getName() : "Unknown Book";
        String bookPublicId = order.getBook() != null && order.getBook().getPublicId() != null
                ? order.getBook().getPublicId() : "";
        String status = order.getStatus() != null ? order.getStatus().toString() : "UNKNOWN";

        String reservedDate = order.getCreateDate() != null ? order.getCreateDate().toString() : "";
        String dueDate = order.getDueDate() != null ? order.getDueDate().toString() : "";
        String borrowedDate = order.getBorrowDate() != null ? order.getBorrowDate().toString() : "";
        String returnDate = order.getReturnDate() != null ? order.getReturnDate().toString() : "";

        boolean isOverdue = order.getDueDate() != null && LocalDateTime.now().isAfter(order.getDueDate());

        return new OrderDTO(
                orderPublicId,
                username,
                bookTitle,
                bookPublicId,
                status,
                reservedDate,
                dueDate,
                borrowedDate,
                returnDate,
                isOverdue
        );
    }
}
