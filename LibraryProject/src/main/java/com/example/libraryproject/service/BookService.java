package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.BookDTO;
import com.example.libraryproject.model.dto.ReviewDTO;
import com.example.libraryproject.model.enums.BookSortCriteria;

import java.util.List;

public interface BookService {

    BookDTO getBookDetails(String bookPublicId);

    List<BookDTO> getBooksByGenre(String genre);
    
    List<BookDTO> getBooksByGenre(String genre, BookSortCriteria sortCriteria);

    List<BookDTO> getAllBooks();
    
    List<BookDTO> getAllBooks(BookSortCriteria sortCriteria);

    List<BookDTO> getAvailableBooks();
    
    List<BookDTO> getAvailableBooks(BookSortCriteria sortCriteria);

    List<ReviewDTO> getReviewsByBook(String bookPublicId);

    List<BookDTO> searchBooks(String searchTerm, String sortBy, String availability);
}
