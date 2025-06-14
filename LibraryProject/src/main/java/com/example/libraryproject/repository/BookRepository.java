package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.Book;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;

import java.util.List;

@RequiredArgsConstructor
public class BookRepository {

    private final Session session;

    public void save(Book book) {

    }
    public void update(Book book) {

    }
    public void delete(Book book) {

    }
    public Book findById(Long id) {
        //TODO
        return null;
    }
    public List<Book> findAll() {
        //TODO
        return null;
    }
    public Book findByTitle(String title) {
        //TODO
        return null;
    }
    public List<Book> findByAuthor(String author) {
        //TODO
        return null;
    }
    public List<Book> findByGenre(String genre) {
        //TODO
        return null;
    }
}
