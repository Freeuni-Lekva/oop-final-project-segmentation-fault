package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.BookDTO;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    public BookDTO getBookDetails(String bookPublicId) {
        logger.info("Getting book details for book id {}", bookPublicId);
        Optional<Book> bookOptional = bookRepository.findByPublicId(bookPublicId);
        if (bookOptional.isEmpty()) {
            throw new IllegalArgumentException("Book not found");
        }
        Book book = bookOptional.get();
        return new BookDTO(
                book.getName(),
                book.getDescription(),
                book.getGenre(),
                book.getAuthor(),
                book.getImageUrl(),
                book.getCopiesInLib(),
                book.getAmountInLib(),
                book.getVolume(),
                book.getRating()
        );
    }

    public List<BookDTO> getBooksByGenre(String genre) {
        logger.info("Getting books by genre: {}", genre);
        return bookRepository.findByGenre(genre).stream().map(
                book -> new BookDTO(
                        book.getName(),
                        book.getDescription(),
                        book.getGenre(),
                        book.getAuthor(),
                        book.getImageUrl(),
                        book.getCopiesInLib(),
                        book.getAmountInLib(),
                        book.getVolume(),
                        book.getRating()
                )
        ).toList();
    }

    public List<BookDTO> getAllBooks() {
        logger.info("Getting all books");
        return bookRepository.findAll().stream().map(
                book -> new BookDTO(
                        book.getName(),
                        book.getDescription(),
                        book.getGenre(),
                        book.getAuthor(),
                        book.getImageUrl(),
                        book.getCopiesInLib(),
                        book.getAmountInLib(),
                        book.getVolume(),
                        book.getRating()
                )
        ).toList();
    }

    public List<BookDTO> getAvailableBooks() {
        logger.info("Getting all available books");
        return bookRepository.findAll().stream()
                .filter(book -> book.getAmountInLib() > 0)
                .map(
                        book -> new BookDTO(
                                book.getName(),
                                book.getDescription(),
                                book.getGenre(),
                                book.getAuthor(),
                                book.getImageUrl(),
                                book.getCopiesInLib(),
                                book.getAmountInLib(),
                                book.getVolume(),
                                book.getRating()
                        )
                ).toList();
    }
}
