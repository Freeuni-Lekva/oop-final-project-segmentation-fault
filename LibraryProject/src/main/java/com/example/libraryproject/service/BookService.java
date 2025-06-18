package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.BookDTO;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    public BookDTO getBookDetails(String bookPublicId) {
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
                book.getVolume(),
                book.getRating()
        );
    }

    public List<BookDTO> getBooksByGenre(String genre) {
        return bookRepository.findByGenre(genre).stream().map(
                book -> new BookDTO(
                        book.getName(),
                        book.getDescription(),
                        book.getGenre(),
                        book.getAuthor(),
                        book.getImageUrl(),
                        book.getVolume(),
                        book.getRating()
                )
        ).toList();
    }

    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll().stream().map(
                book -> new BookDTO(
                        book.getName(),
                        book.getDescription(),
                        book.getGenre(),
                        book.getAuthor(),
                        book.getImageUrl(),
                        book.getVolume(),
                        book.getRating()
                )
        ).toList();
    }

    public List<BookDTO> getAvailableBooks() {
        return bookRepository.findAll().stream()
                .filter(book -> book.getAmountInLib() > 0)
                .map(
                        book -> new BookDTO(
                                book.getName(),
                                book.getDescription(),
                                book.getGenre(),
                                book.getAuthor(),
                                book.getImageUrl(),
                                book.getVolume(),
                                book.getRating()
                        )
                ).toList();
    }
}
