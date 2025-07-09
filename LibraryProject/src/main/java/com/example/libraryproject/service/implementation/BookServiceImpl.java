package com.example.libraryproject.service.implementation;

import com.example.libraryproject.model.dto.BookDTO;
import com.example.libraryproject.model.dto.ReviewDTO;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.ReviewRepository;
import com.example.libraryproject.service.BookService;
import com.example.libraryproject.utilities.Mappers;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);

    public BookDTO getBookDetails(String bookPublicId) {
        logger.info("Getting book details for book id {}", bookPublicId);
        Optional<Book> bookOptional = bookRepository.findByPublicId(bookPublicId);
        if (bookOptional.isEmpty()) {
            throw new IllegalArgumentException("Book not found");
        }
        Book book = bookOptional.get();
        return new BookDTO(
                book.getPublicId(),
                book.getName(),
                book.getDescription(),
                book.getGenre(),
                book.getAuthor(),
                book.getImageUrl(),
                book.getTotalAmount(),
                book.getCurrentAmount(),
                book.getVolume(),
                book.getRating(),
                book.getDate().toString()
        );
    }

    public List<BookDTO> getBooksByGenre(String genre) {
        logger.info("Getting books by genre: {}", genre);
        return bookRepository.findByGenre(genre).stream().map(
                book -> new BookDTO(
                        book.getPublicId(),
                        book.getName(),
                        book.getDescription(),
                        book.getGenre(),
                        book.getAuthor(),
                        book.getImageUrl(),
                        book.getCurrentAmount(),
                        book.getTotalAmount(),
                        book.getVolume(),
                        book.getRating(),
                        book.getDate().toString()
                )
        ).toList();
    }

    public List<BookDTO> getAllBooks() {
        logger.info("Getting all books");
        return bookRepository.findAll().stream().map(
                book -> new BookDTO(
                        book.getPublicId(),
                        book.getName(),
                        book.getDescription(),
                        book.getGenre(),
                        book.getAuthor(),
                        book.getImageUrl(),
                        book.getCurrentAmount(),
                        book.getTotalAmount(),
                        book.getVolume(),
                        book.getRating(),
                        book.getDate().toString()
                )
        ).toList();
    }

    public List<BookDTO> getAvailableBooks() {
        logger.info("Getting all available books");
        return bookRepository.findAll().stream()
                .filter(book -> book.getTotalAmount() > 0)
                .map(
                        book -> new BookDTO(
                                book.getPublicId(),
                                book.getName(),
                                book.getDescription(),
                                book.getGenre(),
                                book.getAuthor(),
                                book.getImageUrl(),
                                book.getCurrentAmount(),
                                book.getTotalAmount(),
                                book.getVolume(),
                                book.getRating(),
                                book.getDate().toString()
                        )
                ).toList();
    }

    public List<ReviewDTO> getReviewsByBook(String bookPublicId) {
        Set<Review> reviews = reviewRepository.findReviewsByBookPublicId(bookPublicId);

        return reviews.stream()
                .map(Mappers::mapReviewToDTO)
                .toList();
    }
}
