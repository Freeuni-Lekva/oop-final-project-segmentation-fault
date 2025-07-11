package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.example.libraryproject.utils.MockDataForTests.createTestBook;
import static org.junit.jupiter.api.Assertions.*;

public class TestBookRepository {

    private SessionFactory sessionFactory;
    private BookRepository bookRepository;

    @BeforeEach
    public void setUp() {
        Configuration configuration = new Configuration()
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
                .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.show_sql", "true")
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Book.class)
                .addAnnotatedClass(Review.class);

        sessionFactory = configuration.buildSessionFactory();
        bookRepository = new BookRepository(sessionFactory);
    }

    @AfterEach
    public void tearDown() {
        sessionFactory.close();
    }

    @Test
    @DisplayName("Should save a new book successfully")
    void testSaveBook() {
        Book book = createTestBook("Wuthering Heights", "Emily Bronte", "Classic", 5.0, 10L, 10L,"wutheringHeights.jpg");
        assertDoesNotThrow(() -> bookRepository.save(book));
        assertNotNull(book.getId());
        Optional<Book> savedBook = bookRepository.findById(book.getId());
        assertTrue(savedBook.isPresent());
        assertEquals("Wuthering Heights", savedBook.get().getName());
        assertEquals("Emily Bronte", savedBook.get().getAuthor());
        assertEquals("Classic", savedBook.get().getGenre());
        assertEquals(5.0, savedBook.get().getRating());
        assertEquals(10L, savedBook.get().getTotalAmount());
        assertEquals(10L, savedBook.get().getCurrentAmount());
    }

    @Test
    @DisplayName("Should handle exception during save and rollback transaction")
    void testSaveWithException() {
        Book invalidBook = new Book();

        assertThrows(Exception.class, () -> bookRepository.save(invalidBook));
    }

    @Test
    @DisplayName("Should find book by ID")
    void testFindById() {
        Book book = createTestBook("The Count of Monte Cristo", "Alexandre Dumas", "Historical", 5.0, 10L, 20L,"countOfMonteCristo.jpg");
        bookRepository.save(book);
        Optional<Book> foundBook = bookRepository.findById(book.getId());
        assertTrue(foundBook.isPresent());
        assertEquals("The Count of Monte Cristo", foundBook.get().getName());
        assertEquals("Alexandre Dumas", foundBook.get().getAuthor());
    }

    @Test
    @DisplayName("Should return empty Optional when book ID not found")
    void testFindByIdNotFound() {
        Optional<Book> foundBook = bookRepository.findById(999L);
        assertFalse(foundBook.isPresent());
    }

    @Test
    @DisplayName("Should find book by title")
    void testFindByTitle() {
        Book book = createTestBook("White Nights", "Fyodor Dostoevsky", "Romantic fiction", 5.0, 2L, 10L,"whiteNights.jpg");
        bookRepository.save(book);
        Optional<Book> foundBook = bookRepository.findByTitle("White Nights");

        assertTrue(foundBook.isPresent());
        assertEquals("White Nights", foundBook.get().getName());
        assertEquals("Fyodor Dostoevsky", foundBook.get().getAuthor());
    }

    @Test
    @DisplayName("Should return empty Optional when title not found")
    void testFindByTitleNotFound() {
        Optional<Book> foundBook = bookRepository.findByTitle("Non-existent Book");
        assertFalse(foundBook.isPresent());
    }

    @Test
    @DisplayName("Should find books by author")
    void testFindByAuthor() {
        Book book1 = createTestBook("The Hobbit", "J.R.R. Tolkien", "Fantasy", 5.0, 3L, 5L,"theHobbit.jpg");
        Book book2 = createTestBook("The Lord of the Rings", "J.R.R. Tolkien", "Fantasy", 5.0, 2L, 5L,"theLordOfTheRings.jpg");
        Book book3 = createTestBook("The Chronicles of Narnia", "C.S. Lewis", "Fantasy", 4.0, 4L, 5L,"theChroniclesOfNarnia.jpg");

        bookRepository.save(book1);
        bookRepository.save(book2);
        bookRepository.save(book3);

        List<Book> tolkienBooks = bookRepository.findByAuthor("J.R.R. Tolkien");
        assertEquals(2, tolkienBooks.size());
        assertTrue(tolkienBooks.stream().anyMatch(book -> book.getName().equals("The Hobbit")));
        assertTrue(tolkienBooks.stream().anyMatch(book -> book.getName().equals("The Lord of the Rings")));
    }

    @Test
    @DisplayName("Should find books by genre")
    void testFindByGenre() {
        Book book1 = createTestBook("The Murder of Roger Ackroyd", "Agatha Christie", "Mystery", 5.0, 5L, 5L,"theMurderOfRogerAckroyd.jph");
        Book book2 = createTestBook("The Golden Compass", "Philip Pullman", "Fantasy", 5.0, 10L, 5L,"theGoldernCompass.jpg");

        bookRepository.save(book1);
        bookRepository.save(book2);

        List<Book> fantasyBooks = bookRepository.findByGenre("Fantasy");
        assertEquals(1, fantasyBooks.size());
        assertEquals("The Golden Compass", fantasyBooks.getFirst().getName());
    }

    @Test
    @DisplayName("Should find all books")
    void testFindAll() {
        Book book1 = createTestBook("Book One", "Author One", "Genre One", 3.0, 1L, 5L,"bookone.jpg");
        Book book2 = createTestBook("Book Two", "Author Two", "Genre Two", 4.0, 2L, 5L,"booktwo.jpg");
        Book book3 = createTestBook("Book Three", "Author Three", "Genre Three", 5.0, 3L, 5L,"bookthree.jpg");

        bookRepository.save(book1);
        bookRepository.save(book2);
        bookRepository.save(book3);
        List<Book> allBooks = bookRepository.findAll();
        assertEquals(3, allBooks.size());
    }

    @Test
    @DisplayName("Should update book successfully")
    void testUpdateBook() {
        Book book = createTestBook("Original Title", "Original Author", "Original Genre", 3.0, 5L, 5L,"original.jpg");
        bookRepository.save(book);
        book.setName("Updated Title");
        book.setAuthor("Updated Author");
        book.setRating(5.0);
        assertDoesNotThrow(() -> bookRepository.update(book));

        Optional<Book> updatedBook = bookRepository.findById(book.getId());
        assertTrue(updatedBook.isPresent());
        assertEquals("Updated Title", updatedBook.get().getName());
        assertEquals("Updated Author", updatedBook.get().getAuthor());
        assertEquals(5.0, updatedBook.get().getRating());
    }

    @Test
    @DisplayName("Should delete book successfully")
    void testDeleteBook() {
        Book book = createTestBook("Book to Delete", "Author", "Genre", 3.0, 1L, 5L,"deleteBook.jpg");
        bookRepository.save(book);
        Long bookId = book.getId();

        assertTrue(bookRepository.findById(bookId).isPresent());
        assertDoesNotThrow(() -> bookRepository.delete(book));

        Optional<Book> deletedBook = bookRepository.findById(bookId);
        assertFalse(deletedBook.isPresent());
    }

    @Test
    @DisplayName("Should handle exception during delete and rollback transaction")
    void testDeleteWithException() {
        Book detachedBook = createTestBook("Book", "Author", "Genre", 3.0, 1L, 5L,"detachedBook.jpg");

        try {
            java.lang.reflect.Field idField = Book.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(detachedBook, 999L);
        } catch (Exception e) {
            fail("Failed to set up test");
        }
        assertThrows(Exception.class, () -> bookRepository.delete(detachedBook));
    }

    @Test
    @DisplayName("Should return empty list when no books found by author")
    void testFindByAuthorNoResults() {
        List<Book> books = bookRepository.findByAuthor("Non-existent Author");
        assertTrue(books.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when no books found by genre")
    void testFindByGenreNoResults() {
        List<Book> books = bookRepository.findByGenre("Non-existent Genre");
        assertTrue(books.isEmpty());
    }

    @Test
    @DisplayName("Should handle null parameters gracefully")
    void testNullParameters() {
        Optional<Book> bookByTitle = bookRepository.findByTitle(null);
        assertFalse(bookByTitle.isPresent());

        List<Book> booksByAuthor = bookRepository.findByAuthor(null);
        assertTrue(booksByAuthor.isEmpty());

        List<Book> booksByGenre = bookRepository.findByGenre(null);
        assertTrue(booksByGenre.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty string parameters")
    void testEmptyStringParameters() {
        Optional<Book> bookByTitle = bookRepository.findByTitle("");
        assertFalse(bookByTitle.isPresent());

        List<Book> booksByAuthor = bookRepository.findByAuthor("");
        assertTrue(booksByAuthor.isEmpty());

        List<Book> booksByGenre = bookRepository.findByGenre("");
        assertTrue(booksByGenre.isEmpty());
    }

    @Test
    @DisplayName("Should find books with case sensitive search")
    void testCaseSensitiveSearch() {
        Book book = createTestBook("Test Book", "Test Author", "Test Genre", 3.0, 1L, 5L,"testBook.jpg");
        bookRepository.save(book);

        Optional<Book> foundBook = bookRepository.findByTitle("test book");
        assertFalse(foundBook.isPresent());

        List<Book> authorBooks = bookRepository.findByAuthor("test author");
        assertTrue(authorBooks.isEmpty());

        List<Book> genreBooks = bookRepository.findByGenre("test genre");
        assertTrue(genreBooks.isEmpty());
    }

    @Test
    @DisplayName("Should handle special characters in search")
    void testSpecialCharactersInSearch() {
        Book book = createTestBook("Book's Title", "O'Connor", "Sci-Fi", 4.0, 2L, 5L,"bookTitle.jpg");
        bookRepository.save(book);

        Optional<Book> foundBook = bookRepository.findByTitle("Book's Title");
        assertTrue(foundBook.isPresent());

        List<Book> authorBooks = bookRepository.findByAuthor("O'Connor");
        assertEquals(1, authorBooks.size());

        List<Book> genreBooks = bookRepository.findByGenre("Sci-Fi");
        assertEquals(1, genreBooks.size());
    }

    @Test
    @DisplayName("Should handle empty list in saveAll")
    void testSaveAllEmptyList() {
        List<Book> emptyList = List.of();
        assertDoesNotThrow(() -> bookRepository.saveAll(emptyList));

        List<Book> allBooks = bookRepository.findAll();
        assertEquals(0, allBooks.size());
    }

    @Test
    @DisplayName("Should find books by authors and genres")
    void testFindByAuthorsAndGenres() {
        Book book1 = createTestBook("Fantasy Adventure", "J.R.R. Tolkien", "Fantasy", 5.0, 3L, 5L, "fantasy1.jpg");
        Book book2 = createTestBook("Mystery Novel", "Agatha Christie", "Mystery", 4.0, 2L, 3L, "mystery1.jpg");
        Book book3 = createTestBook("Another Fantasy", "Brandon Sanders", "Fantasy", 5.0, 4L, 6L, "fantasy2.jpg");
        Book book4 = createTestBook("Science Fiction", "Isaac Asimov", "Sci-Fi", 4.0, 2L, 4L, "scifi1.jpg");

        bookRepository.save(book1);
        bookRepository.save(book2);
        bookRepository.save(book3);
        bookRepository.save(book4);

        Set<String> authors = Set.of("J.R.R. Tolkien", "Agatha Christie");
        Set<String> genres = Set.of("Fantasy", "Mystery");
        Set<Book> readBooks = Set.of();

        List<Book> result = bookRepository.findByAuthorsAndGenres(authors, genres, readBooks);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(book -> book.getName().equals("Fantasy Adventure")));
        assertTrue(result.stream().anyMatch(book -> book.getName().equals("Mystery Novel")));
    }

    @Test
    @DisplayName("Should exclude read books from findByAuthorsAndGenres results")
    void testFindByAuthorsAndGenresExcludeReadBooks() {
        Book book1 = createTestBook("Read Fantasy", "J.R.R. Tolkien", "Fantasy", 5.0, 3L, 5L, "readFantasy.jpg");
        Book book2 = createTestBook("Unread Fantasy", "J.R.R. Tolkien", "Fantasy", 4.0, 2L, 3L, "unreadFantasy.jpg");

        bookRepository.save(book1);
        bookRepository.save(book2);

        Set<String> authors = Set.of("J.R.R. Tolkien");
        Set<String> genres = Set.of("Fantasy");
        Set<Book> readBooks = Set.of(book1);

        List<Book> result = bookRepository.findByAuthorsAndGenres(authors, genres, readBooks);
        assertEquals(1, result.size());
        assertEquals("Unread Fantasy", result.getFirst().getName());
    }

    @Test
    @DisplayName("Should find all books when empty authors and genres sets")
    void testFindByAuthorsAndGenresEmptyCriteria() {
        Book book1 = createTestBook("Book One", "Author One", "Genre One", 3.0, 1L, 5L, "book1.jpg");
        Book book2 = createTestBook("Book Two", "Author Two", "Genre Two", 4.0, 2L, 5L, "book2.jpg");

        bookRepository.save(book1);
        bookRepository.save(book2);

        Set<String> authors = Set.of();
        Set<String> genres = Set.of();
        Set<Book> readBooks = Set.of();

        List<Book> result = bookRepository.findByAuthorsAndGenres(authors, genres, readBooks);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should return empty list when all books are read")
    void testFindByAuthorsAndGenresAllBooksRead() {
        Book book1 = createTestBook("Read Book One", "Author One", "Genre One", 3.0, 1L, 5L, "readBook1.jpg");
        Book book2 = createTestBook("Read Book Two", "Author Two", "Genre Two", 4.0, 2L, 5L, "readBook2.jpg");

        bookRepository.save(book1);
        bookRepository.save(book2);

        Set<String> authors = Set.of();
        Set<String> genres = Set.of();
        Set<Book> readBooks = Set.of(book1, book2);

        List<Book> result = bookRepository.findByAuthorsAndGenres(authors, genres, readBooks);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find book by public ID")
    void testFindByPublicId() {
        Book book = createTestBook("Test Book for Public ID", "Test Author", "Test Genre", 4.0, 3L, 5L, "testPublicId.jpg");
        bookRepository.save(book);

        Optional<Book> foundBook = bookRepository.findByPublicId(book.getPublicId());
        assertTrue(foundBook.isPresent());
        assertEquals("Test Book for Public ID", foundBook.get().getName());
        assertEquals("Test Author", foundBook.get().getAuthor());
    }

    @Test
    @DisplayName("Should return empty Optional when public ID not found")
    void testFindByPublicIdNotFound() {
        Optional<Book> foundBook = bookRepository.findByPublicId("non-existent-public-id");
        assertFalse(foundBook.isPresent());
    }

    @Test
    @DisplayName("Should handle null public ID")
    void testFindByPublicIdNull() {
        Optional<Book> foundBook = bookRepository.findByPublicId(null);
        assertFalse(foundBook.isPresent());
    }

    @Test
    @DisplayName("Should handle empty string public ID")
    void testFindByPublicIdEmpty() {
        Optional<Book> foundBook = bookRepository.findByPublicId("");
        assertFalse(foundBook.isPresent());
    }

    @Test
    @DisplayName("Should search books by title containing search term")
    void testSearchByTitle() {
        Book book1 = createTestBook("The Great Adventure", "Author One", "Adventure", 4.0, 3L, 5L, "adventure1.jpg");
        Book book2 = createTestBook("Great Expectations", "Charles Dickens", "Classic", 5.0, 2L, 4L, "expectations.jpg");
        Book book3 = createTestBook("Small Adventures", "Author Two", "Adventure", 3.0, 1L, 3L, "smallAdv.jpg");

        bookRepository.save(book1);
        bookRepository.save(book2);
        bookRepository.save(book3);

        List<Book> result = bookRepository.searchByTitle("Great");
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(book -> book.getName().equals("The Great Adventure")));
        assertTrue(result.stream().anyMatch(book -> book.getName().equals("Great Expectations")));
    }

    @Test
    @DisplayName("Should search books by title case insensitive")
    void testSearchByTitleCaseInsensitive() {
        Book book = createTestBook("BEST Title", "Author", "Genre", 4.0, 3L, 5L, "uppercase.jpg");
        bookRepository.save(book);

        List<Book> result1 = bookRepository.searchByTitle("best");
        assertEquals(1, result1.size());
        assertEquals("BEST Title", result1.getFirst().getName());

        List<Book> result2 = bookRepository.searchByTitle("TITLE");
        assertEquals(1, result2.size());
        assertEquals("BEST Title", result2.getFirst().getName());
    }

    @Test
    @DisplayName("Should return empty list when no titles match search term")
    void testSearchByTitleNoResults() {
        Book book = createTestBook("Existing Book", "Author", "Genre", 4.0, 3L, 5L, "existing.jpg");
        bookRepository.save(book);

        List<Book> result = bookRepository.searchByTitle("NonExistent");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle null search term in title search")
    void testSearchByTitleNullTerm() {
        Book book = createTestBook("Test Book", "Author", "Genre", 4.0, 3L, 5L, "test.jpg");
        bookRepository.save(book);

        List<Book> result = bookRepository.searchByTitle(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty search term in title search")
    void testSearchByTitleEmptyTerm() {
        Book book1 = createTestBook("Book One", "Author One", "Genre", 4.0, 3L, 5L, "book1.jpg");
        Book book2 = createTestBook("Book Two", "Author Two", "Genre", 3.0, 2L, 4L, "book2.jpg");
        
        bookRepository.save(book1);
        bookRepository.save(book2);

        List<Book> result = bookRepository.searchByTitle("");
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should search books by author containing search term")
    void testSearchByAuthor() {
        Book book1 = createTestBook("Book One", "Stephen King", "Horror", 4.0, 3L, 5L, "book1.jpg");
        Book book2 = createTestBook("Book Two", "Stephen Hawking", "Science", 5.0, 2L, 4L, "book2.jpg");
        Book book3 = createTestBook("Book Three", "John Smith", "Fiction", 3.0, 1L, 3L, "book3.jpg");

        bookRepository.save(book1);
        bookRepository.save(book2);
        bookRepository.save(book3);

        List<Book> result = bookRepository.searchByAuthor("Stephen");
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(book -> book.getAuthor().equals("Stephen King")));
        assertTrue(result.stream().anyMatch(book -> book.getAuthor().equals("Stephen Hawking")));
    }

    @Test
    @DisplayName("Should search books by author case insensitive")
    void testSearchByAuthorCaseInsensitive() {
        Book book = createTestBook("Test Book", "J.K. ROWLING", "Fantasy", 5.0, 4L, 6L, "testBook.jpg");
        bookRepository.save(book);

        List<Book> result1 = bookRepository.searchByAuthor("rowling");
        assertEquals(1, result1.size());
        assertEquals("J.K. ROWLING", result1.getFirst().getAuthor());

        List<Book> result2 = bookRepository.searchByAuthor("j.k.");
        assertEquals(1, result2.size());
        assertEquals("J.K. ROWLING", result2.getFirst().getAuthor());
    }

    @Test
    @DisplayName("Should return empty list when no authors match search term")
    void testSearchByAuthorNoResults() {
        Book book = createTestBook("Test Book", "Existing Author", "Genre", 4.0, 3L, 5L, "existing.jpg");
        bookRepository.save(book);

        List<Book> result = bookRepository.searchByAuthor("NonExistent");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle null search term in author search")
    void testSearchByAuthorNullTerm() {
        Book book = createTestBook("Test Book", "Test Author", "Genre", 4.0, 3L, 5L, "test.jpg");
        bookRepository.save(book);

        List<Book> result = bookRepository.searchByAuthor(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty search term in author search")
    void testSearchByAuthorEmptyTerm() {
        Book book1 = createTestBook("Book One", "Author One", "Genre", 4.0, 3L, 5L, "book1.jpg");
        Book book2 = createTestBook("Book Two", "Author Two", "Genre", 3.0, 2L, 4L, "book2.jpg");
        
        bookRepository.save(book1);
        bookRepository.save(book2);

        List<Book> result = bookRepository.searchByAuthor("");
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should find partial matches in author search")
    void testSearchByAuthorPartialMatch() {
        Book book = createTestBook("Test Book", "John David Smith", "Genre", 4.0, 3L, 5L, "test.jpg");
        bookRepository.save(book);

        List<Book> result1 = bookRepository.searchByAuthor("David");
        assertEquals(1, result1.size());
        assertEquals("John David Smith", result1.getFirst().getAuthor());

        List<Book> result2 = bookRepository.searchByAuthor("Smith");
        assertEquals(1, result2.size());
        assertEquals("John David Smith", result2.getFirst().getAuthor());
    }
}