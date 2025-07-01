package com.example.libraryproject.service.implementation;

import com.example.libraryproject.model.dto.GoogleBooksResponse;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.List;

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

        doReturn(createFakeBooks()).when(service).fetchBooksFromGenre(anyString(), anyInt());
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


}
