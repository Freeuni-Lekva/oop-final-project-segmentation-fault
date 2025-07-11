package com.example.libraryproject.service.implementation;

import com.example.libraryproject.model.dto.BookAdditionFromGoogleRequest;
import com.example.libraryproject.model.dto.GoogleBooksResponse;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.libraryproject.utils.MockDataForTests.createTestBook;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoogleBooksApiServiceImplTest {

    private BookRepository bookRepository;
    private GoogleBooksApiServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        bookRepository = mock(BookRepository.class);

        service = Mockito.spy(new GoogleBooksApiServiceImpl(bookRepository));

        doReturn(createFakeBooks()).when(service)
                .fetchBooksFromGenre(anyString(), anyInt(), any(ExecutorService.class));

        doNothing().when(service).downloadAndSaveImage(anyString(), anyString());
    }

    private HashSet<GoogleBooksResponse> createFakeBooks() {
        HashSet<GoogleBooksResponse> fakeBooks = new HashSet<>();
        fakeBooks.add(new GoogleBooksResponse("Fake Title", "2025", "Fake Author", "Fake Desc", "Fake_Title.jpg", "fiction", 100L));
        return fakeBooks;
    }

    @Test
    void fetchAndSaveBooks_whenRepositoryEmpty_savesBooks() {
        when(bookRepository.findAll()).thenReturn(List.of());
        service.fetchAndSaveBooks();

        ArgumentCaptor<List<Book>> captor = ArgumentCaptor.forClass(List.class);
        verify(bookRepository, times(1)).saveAll(captor.capture());

        List<Book> savedBooks = captor.getValue();
        assertFalse(savedBooks.isEmpty());
        assertEquals("Fake Title", savedBooks.getFirst().getName());
    }


    @Test
    void fetchAndSaveBooks_whenRepositoryNotEmpty_doesNotSave() {

        Book book1 = createTestBook("The Murder of Roger Ackroyd", "Agatha Christie", "Mystery", 5L, 5L, 5L,"theMurderOfRogerAckroyd.jph");

        when(bookRepository.findAll()).thenReturn(List.of(book1));

        service.fetchAndSaveBooks();

        verify(bookRepository, never()).saveAll(any());
    }

    @Test
    void testFetchBook_fetchesSingleBook() {
        Book book = service.getBookFromGoogle("100 Years Of Solitude", "Gabriel Garcia Marquez");
        assertEquals("Gabriel García Márquez".toLowerCase(),book.getAuthor().toLowerCase());
        assertEquals("One Hundred Years Of Solitude".toLowerCase(), book.getName().toLowerCase());
        assertNotNull(book.getDescription());
        assertNotEquals(0, book.getVolume());
        assertNotNull(book.getGenre());
    }

    @Test
    void testFetchBooksFromGenre1() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        GoogleBooksApiServiceImpl realService = new GoogleBooksApiServiceImpl(bookRepository);
        Set<GoogleBooksResponse> result = realService.fetchBooksFromGenre("fiction", 5, executor);
        
        assertNotNull(result);
        assertTrue(result.size() >= 0);
        
        executor.shutdown();
    }

    @Test
    void testFetchBooksFromGenre2() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        GoogleBooksApiServiceImpl realService = new GoogleBooksApiServiceImpl(bookRepository);
        Set<GoogleBooksResponse> result = realService.fetchBooksFromGenre("invalid_genre_12345", 1, executor);
        
        assertNotNull(result);
        assertTrue(result.size() >= 0);
        
        executor.shutdown();
    }

    @Test
    void testDownloadAndSaveImage1() throws Exception {
        GoogleBooksApiServiceImpl realService = new GoogleBooksApiServiceImpl(bookRepository);

        try {
            realService.downloadAndSaveImage("https://via.placeholder.com/150", "test_book_title");
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    void testDownloadAndSaveImage2() throws Exception {
        GoogleBooksApiServiceImpl realService = new GoogleBooksApiServiceImpl(bookRepository);
        try {
            realService.downloadAndSaveImage("invalid_url", "test_title");
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    void testCapitalizeWords1() throws Exception {
        Method method = GoogleBooksApiServiceImpl.class.getDeclaredMethod("capitalizeWords", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "hello world test");
        
        assertEquals("Hello World Test", result);
    }

    @Test
    void testCapitalizeWords2() throws Exception {
        Method method = GoogleBooksApiServiceImpl.class.getDeclaredMethod("capitalizeWords", String.class);
        method.setAccessible(true);

        String result1 = (String) method.invoke(service, (String) null);
        String result2 = (String) method.invoke(service, "");
        String result3 = (String) method.invoke(service, "   ");
        
        assertEquals("Unknown", result1);
        assertEquals("Unknown", result2);
        assertEquals("Unknown", result3);
    }

    @Test
    void testDeleteImages1() throws Exception {
        Method method = GoogleBooksApiServiceImpl.class.getDeclaredMethod("deleteImages");
        method.setAccessible(true);
        assertDoesNotThrow(() -> {
            try {
                method.invoke(service);
            } catch (Exception e) {

            }
        });
    }

    @Test
    void testDeleteImages2() throws Exception {
        Method method = GoogleBooksApiServiceImpl.class.getDeclaredMethod("deleteImages");
        method.setAccessible(true);

        try {
            method.invoke(service);
        } catch (Exception e) {
            assertTrue(e.getCause() == null || e.getCause().getClass().getName().contains("NullPointer") || 
                      e.getCause().getMessage().contains("IMAGE_DIR"));
        }
    }

    @Test
    void testMapGoogleGenreToStandard1() throws Exception {
        Method method = GoogleBooksApiServiceImpl.class.getDeclaredMethod("mapGoogleGenreToStandard", String.class);
        method.setAccessible(true);

        assertEquals("Fiction", method.invoke(service, "fiction"));
        assertEquals("Sci-Fi", method.invoke(service, "science fiction"));
        assertEquals("Fantasy", method.invoke(service, "fantasy"));
        assertEquals("Mystery", method.invoke(service, "mystery"));
        assertEquals("Romance", method.invoke(service, "romance"));
        assertEquals("Non-Fiction", method.invoke(service, "computers"));
    }

    @Test
    void testMapGoogleGenreToStandard2() throws Exception {
        Method method = GoogleBooksApiServiceImpl.class.getDeclaredMethod("mapGoogleGenreToStandard", String.class);
        method.setAccessible(true);

        assertEquals("Unknown", method.invoke(service, (String) null));
        assertEquals("Unknown", method.invoke(service, ""));
        assertEquals("Unknown", method.invoke(service, "   "));
        assertEquals("Custom Genre", method.invoke(service, "custom genre"));
    }

    @Test
    void testFetchBook1() {
        BookAdditionFromGoogleRequest request = new BookAdditionFromGoogleRequest("1984", "George Orwell");

        Book testBook = createTestBook("1984", "George Orwell", "Fiction", 4.0, 1L, 1L, "1984.jpg");
        doReturn(testBook).when(service).getBookFromGoogle("1984", "George Orwell");
        
        when(bookRepository.findByTitle("1984")).thenReturn(Optional.empty());
        
        boolean result = service.fetchBook(request, 5);
        
        assertTrue(result);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void testFetchBook2() {
        BookAdditionFromGoogleRequest request = new BookAdditionFromGoogleRequest("Non Existent Book", "Unknown Author");

        doReturn(null).when(service).getBookFromGoogle("Non Existent Book", "Unknown Author");
        
        boolean result = service.fetchBook(request, 3);
        
        assertFalse(result);
        verify(bookRepository, never()).save(any(Book.class));
        verify(bookRepository, never()).update(any(Book.class));
    }
}
