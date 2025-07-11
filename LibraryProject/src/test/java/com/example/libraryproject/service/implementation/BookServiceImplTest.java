package com.example.libraryproject.service.implementation;

import com.example.libraryproject.model.dto.BookDTO;
import com.example.libraryproject.model.dto.ReviewDTO;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.BookSortCriteria;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static com.example.libraryproject.utils.MockDataForTests.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookServiceImplTest {

    private BookRepository bookRepository;
    private ReviewRepository reviewRepository;
    private BookServiceImpl bookService;



    @BeforeEach
    void setUp() {
        bookRepository = mock(BookRepository.class);
        reviewRepository = mock(ReviewRepository.class);
        bookService = new BookServiceImpl(bookRepository, reviewRepository);
    }

    @Test
    void test1() {
        String bookPublicId = "test-book-id";
        Book book = createTestBook(bookPublicId, "Test Book", "Test Author", "Fiction");

        when(bookRepository.findByPublicId(bookPublicId)).thenReturn(Optional.of(book));

        BookDTO result = bookService.getBookDetails(bookPublicId);

        assertNotNull(result);
        assertEquals(bookPublicId, result.publicId());
        assertEquals("Test Book", result.name());
        assertEquals("Test Author", result.author());
        assertEquals("Fiction", result.genre());
        verify(bookRepository).findByPublicId(bookPublicId);
    }

    @Test
    void test2() {
        String bookPublicId = "non-existent-id";

        when(bookRepository.findByPublicId(bookPublicId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> bookService.getBookDetails(bookPublicId));
        assertEquals("Book not found", ex.getMessage());
        verify(bookRepository).findByPublicId(bookPublicId);
    }

    @Test
    void test3() {
        String genre = "Fantasy";
        List<Book> books = List.of(
            createTestBook("book1", "Fantasy Book 1", "Author 1", genre),
            createTestBook("book2", "Fantasy Book 2", "Author 2", genre)
        );

        when(bookRepository.findByGenre(genre)).thenReturn(books);

        List<BookDTO> result = bookService.getBooksByGenre(genre);

        assertEquals(2, result.size());
        assertEquals("Fantasy Book 1", result.get(0).name());
        assertEquals("Fantasy Book 2", result.get(1).name());
        verify(bookRepository).findByGenre(genre);
    }

    @Test
    void test4() {
        String genre = "Horror";

        when(bookRepository.findByGenre(genre)).thenReturn(List.of());

        List<BookDTO> result = bookService.getBooksByGenre(genre);

        assertTrue(result.isEmpty());
        verify(bookRepository).findByGenre(genre);
    }

    @Test
    void test5() {
        String genre = "Sci-Fi";
        List<Book> books = List.of(
            createTestBook("book1", "Zebra Book", "Author Z", genre),
            createTestBook("book2", "Alpha Book", "Author A", genre)
        );

        when(bookRepository.findByGenre(genre)).thenReturn(books);

        List<BookDTO> result = bookService.getBooksByGenre(genre, BookSortCriteria.TITLE);

        assertEquals(2, result.size());
        assertEquals("Alpha Book", result.get(0).name());
        assertEquals("Zebra Book", result.get(1).name());
        verify(bookRepository).findByGenre(genre);
    }

    @Test
    void test6() {
        String genre = "Mystery";
        List<Book> books = List.of(
            createTestBook("book1", "Book 1", "Author B", genre),
            createTestBook("book2", "Book 2", "Author A", genre)
        );

        when(bookRepository.findByGenre(genre)).thenReturn(books);

        List<BookDTO> result = bookService.getBooksByGenre(genre, BookSortCriteria.AUTHOR);

        assertEquals(2, result.size());
        assertEquals("Author A", result.get(0).author());
        assertEquals("Author B", result.get(1).author());
        verify(bookRepository).findByGenre(genre);
    }

    @Test
    void test7() {
        List<Book> books = List.of(
            createTestBook("book1", "Book 1", "Author 1", "Genre 1"),
            createTestBook("book2", "Book 2", "Author 2", "Genre 2"),
            createTestBook("book3", "Book 3", "Author 3", "Genre 3")
        );

        when(bookRepository.findAll()).thenReturn(books);

        List<BookDTO> result = bookService.getAllBooks();

        assertEquals(3, result.size());
        assertEquals("Book 1", result.get(0).name());
        assertEquals("Book 2", result.get(1).name());
        assertEquals("Book 3", result.get(2).name());
        verify(bookRepository).findAll();
    }

    @Test
    void test8() {
        when(bookRepository.findAll()).thenReturn(List.of());

        List<BookDTO> result = bookService.getAllBooks();

        assertTrue(result.isEmpty());
        verify(bookRepository).findAll();
    }

    @Test
    void test9() {
        Book book1 = createTestBook("book1", "Book C", "Author 1", "Genre 1");
        book1.setRating(3.0);
        Book book2 = createTestBook("book2", "Book A", "Author 2", "Genre 2");
        book2.setRating(5.0);
        List<Book> books = List.of(book1, book2);

        when(bookRepository.findAll()).thenReturn(books);

        List<BookDTO> result = bookService.getAllBooks(BookSortCriteria.RATING);

        assertEquals(2, result.size());
        assertEquals(5.0, result.get(0).rating());
        assertEquals(3.0, result.get(1).rating());
        verify(bookRepository).findAll();
    }

    @Test
    void test10() {
        Book book1 = createTestBook("book1", "Book 1", "Author 1", "Genre 1");
        book1.setCurrentAmount(1L);
        Book book2 = createTestBook("book2", "Book 2", "Author 2", "Genre 2");
        book2.setCurrentAmount(5L);
        List<Book> books = List.of(book1, book2);

        when(bookRepository.findAll()).thenReturn(books);

        List<BookDTO> result = bookService.getAllBooks(BookSortCriteria.AVAILABLE);

        assertEquals(2, result.size());
        assertEquals(5L, result.get(0).currentAmount());
        assertEquals(1L, result.get(1).currentAmount());
        verify(bookRepository).findAll();
    }

    @Test
    void test11() {
        Book availableBook = createTestBook("book1", "Available Book", "Author 1", "Genre 1");
        availableBook.setTotalAmount(5L);
        Book unavailableBook = createTestBook("book2", "Unavailable Book", "Author 2", "Genre 2");
        unavailableBook.setTotalAmount(0L);

        List<Book> books = List.of(availableBook, unavailableBook);

        when(bookRepository.findAll()).thenReturn(books);

        List<BookDTO> result = bookService.getAvailableBooks();

        assertEquals(1, result.size());
        assertEquals("Available Book", result.get(0).name());
        verify(bookRepository).findAll();
    }

    @Test
    void test12() {
        Book book1 = createTestBook("book1", "Book 1", "Author 1", "Genre 1");
        book1.setTotalAmount(0L);
        Book book2 = createTestBook("book2", "Book 2", "Author 2", "Genre 2");
        book2.setTotalAmount(0L);

        List<Book> books = List.of(book1, book2);

        when(bookRepository.findAll()).thenReturn(books);

        List<BookDTO> result = bookService.getAvailableBooks();

        assertTrue(result.isEmpty());
        verify(bookRepository).findAll();
    }

    @Test
    void test13() {
        Book book1 = createTestBook("book1", "Vaime Available", "Author 1", "Genre 1");
        book1.setTotalAmount(3L);
        Book book2 = createTestBook("book2", "Uxx Available", "Author 2", "Genre 2");
        book2.setTotalAmount(2L);

        List<Book> books = List.of(book1, book2);

        when(bookRepository.findAll()).thenReturn(books);

        List<BookDTO> result = bookService.getAvailableBooks(BookSortCriteria.TITLE);

        assertEquals(2, result.size());
        assertEquals("Uxx Available", result.get(0).name());
        assertEquals("Vaime Available", result.get(1).name());
        verify(bookRepository).findAll();
    }

    @Test
    void test14() {
        Book book1 = createTestBook("book1", "Book 1", "Author 1", "Genre 1");
        book1.setTotalAmount(1L);
        book1.setDate(LocalDate.of(2004, 1, 1));
        Book book2 = createTestBook("book2", "Book 2", "Author 2", "Genre 2");
        book2.setTotalAmount(2L);
        book2.setDate(LocalDate.of(2023, 1, 1));

        List<Book> books = List.of(book1, book2);

        when(bookRepository.findAll()).thenReturn(books);

        List<BookDTO> result = bookService.getAvailableBooks(BookSortCriteria.DATE);

        assertEquals(2, result.size());
        assertEquals("2023-01-01", result.get(0).date());
        assertEquals("2004-01-01", result.get(1).date());
        verify(bookRepository).findAll();
    }

    @Test
    void test15() {
        String bookPublicId = "book-with-reviews";
        Set<Review> reviews = Set.of(
            createTestReview("user1", 5, "Great book!"),
            createTestReview("user2", 4, "Good read")
        );

        when(reviewRepository.findReviewsByBookPublicId(bookPublicId)).thenReturn(reviews);

        List<ReviewDTO> result = bookService.getReviewsByBook(bookPublicId);

        assertEquals(2, result.size());
        verify(reviewRepository).findReviewsByBookPublicId(bookPublicId);
    }

    @Test
    void test16() {
        String bookPublicId = "book-no-reviews";

        when(reviewRepository.findReviewsByBookPublicId(bookPublicId)).thenReturn(Set.of());

        List<ReviewDTO> result = bookService.getReviewsByBook(bookPublicId);

        assertTrue(result.isEmpty());
        verify(reviewRepository).findReviewsByBookPublicId(bookPublicId);
    }

    @Test
    void test17() {
        String searchTerm = "fantasy";
        List<Book> titleResults = List.of(createTestBook("book1", "Fantasy Adventure", "Author 1", "Fantasy"));
        List<Book> authorResults = List.of(createTestBook("book2", "Epic Tale", "Fantasy Author", "Adventure"));

        when(bookRepository.searchByTitle(searchTerm)).thenReturn(titleResults);
        when(bookRepository.searchByAuthor(searchTerm)).thenReturn(authorResults);

        List<BookDTO> result = bookService.searchBooks(searchTerm, "title", "all");

        assertEquals(2, result.size());
        verify(bookRepository).searchByTitle(searchTerm);
        verify(bookRepository).searchByAuthor(searchTerm);
    }

    @Test
    void test18() {
        String searchTerm = "mystery";
        Book availableBook = createTestBook("book1", "Mystery Novel", "Author 1", "Mystery");
        availableBook.setCurrentAmount(3L);
        Book unavailableBook = createTestBook("book2", "Mystery Story", "Author 2", "Mystery");
        unavailableBook.setCurrentAmount(0L);

        List<Book> titleResults = List.of(availableBook, unavailableBook);
        List<Book> authorResults = List.of();

        when(bookRepository.searchByTitle(searchTerm)).thenReturn(titleResults);
        when(bookRepository.searchByAuthor(searchTerm)).thenReturn(authorResults);

        List<BookDTO> result = bookService.searchBooks(searchTerm, "rating", "available");

        assertEquals(1, result.size());
        assertEquals("Mystery Novel", result.get(0).name());
        verify(bookRepository).searchByTitle(searchTerm);
        verify(bookRepository).searchByAuthor(searchTerm);
    }
}
