package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.BookDTO;

import java.util.List;

public interface BookService {

    BookDTO getBookDetails(String bookPublicId);

    List<BookDTO> getBooksByGenre(String genre);

    List<BookDTO> getAllBooks();

    List<BookDTO> getAvailableBooks();
}
