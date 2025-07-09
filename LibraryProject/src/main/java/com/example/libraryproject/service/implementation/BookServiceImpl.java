package com.example.libraryproject.service.implementation;

import com.example.libraryproject.model.dto.BookDTO;
import com.example.libraryproject.model.dto.ReviewDTO;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.enums.BookSortCriteria;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.ReviewRepository;
import com.example.libraryproject.service.BookService;
import com.example.libraryproject.utilities.Mappers;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                        book.getTotalAmount(),
                        book.getCurrentAmount(),
                        book.getVolume(),
                        book.getRating(),
                        book.getDate().toString()
                )
        ).toList();
    }
    
    public List<BookDTO> getBooksByGenre(String genre, BookSortCriteria sortCriteria) {
        logger.info("Getting books by genre: {} with sorting: {}", genre, sortCriteria);
        Stream<Book> bookStream = bookRepository.findByGenre(genre).stream();
        return sortBooks(bookStream, sortCriteria);
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
                        book.getTotalAmount(),
                        book.getCurrentAmount(),
                        book.getVolume(),
                        book.getRating(),
                        book.getDate().toString()
                )
        ).toList();
    }
    
    public List<BookDTO> getAllBooks(BookSortCriteria sortCriteria) {
        logger.info("Getting all books with sorting: {}", sortCriteria);
        Stream<Book> bookStream = bookRepository.findAll().stream();
        return sortBooks(bookStream, sortCriteria);
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
                                book.getTotalAmount(),
                                book.getCurrentAmount(),
                                book.getVolume(),
                                book.getRating(),
                                book.getDate().toString()
                        )
                ).toList();
    }
    
    public List<BookDTO> getAvailableBooks(BookSortCriteria sortCriteria) {
        logger.info("Getting all available books with sorting: {}", sortCriteria);
        Stream<Book> bookStream = bookRepository.findAll().stream()
                .filter(book -> book.getTotalAmount() > 0);
        return sortBooks(bookStream, sortCriteria);
    }

    public List<ReviewDTO> getReviewsByBook(String bookPublicId) {
        Set<Review> reviews = reviewRepository.findReviewsByBookPublicId(bookPublicId);

        return reviews.stream()
                .map(Mappers::mapReviewToDTO)
                .toList();
    }
    
    /**
     * Helper method to sort books based on the given criteria
     */
    private List<BookDTO> sortBooks(Stream<Book> bookStream, BookSortCriteria sortCriteria) {
        Comparator<Book> comparator = switch (sortCriteria) {
            case TITLE -> Comparator.comparing(book ->
                    book.getName().toLowerCase()
            );
            case AUTHOR -> Comparator.comparing(book ->
                    book.getAuthor().toLowerCase()
            );
            case AVAILABLE -> Comparator.<Book>comparingLong(Book::getCurrentAmount).reversed(); // Most available first
            default -> Comparator.<Book>comparingDouble(Book::getRating).reversed(); // Highest rating first
        };

        return bookStream
                .sorted(comparator)
                .map(book -> new BookDTO(
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
                ))
                .toList();
    }

    public List<BookDTO> searchBooks(String searchTerm, String sortBy, String availability) {
        logger.info("Searching books containing: {}, sorting by: {}, availability: {}", searchTerm, sortBy, availability);

        List<Book> books = bookRepository.searchByTitle(searchTerm);

        Stream<Book> bookStream = books.stream();
        if ("available".equalsIgnoreCase(availability)) {
            bookStream = bookStream.filter(book -> book.getCurrentAmount() > 0);
        }

        BookSortCriteria sortCriteria = mapSortByToEnum(sortBy);
        return sortBooks(bookStream, sortCriteria);
    }

    /**
     * Helper method to map string sort parameter to BookSortCriteria enum
     */
    private BookSortCriteria mapSortByToEnum(String sortBy) {
        if (sortBy == null) {
            return BookSortCriteria.RATING; // default
        }

        return switch (sortBy.toLowerCase()) {
            case "title" -> BookSortCriteria.TITLE;
            case "author" -> BookSortCriteria.AUTHOR;
            case "available" -> BookSortCriteria.AVAILABLE;
            default -> BookSortCriteria.RATING;
        };
    }

}
