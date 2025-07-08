package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.BookDTO;
import com.example.libraryproject.model.dto.ReviewDTO;

import java.util.List;

public interface BookService {

    BookDTO getBookDetails(String bookPublicId);

    List<BookDTO> getBooksByGenre(String genre);

    List<BookDTO> getAllBooks();

    List<BookDTO> getAvailableBooks();

    List<ReviewDTO> getReviewsByBook(String bookPublicId);
}
