package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.Book;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class BookRepository {

    private final Session session;

    public void save(Book book) {
        Transaction transaction = null;

        try{
            transaction = session.beginTransaction();
            session.persist(book);
            transaction.commit();
        }catch(Exception e) {
            if(transaction != null) transaction.rollback();
            throw e;
        }
    }

    public void update(Book book) {
        Transaction transaction = null;

        try{
            transaction = session.beginTransaction();
            session.merge(book);
            transaction.commit();
        }catch(Exception e) {
            if(transaction != null) transaction.rollback();
            throw e;
        }
    }

    public void delete(Book book) {
        Transaction transaction = null;

        try{
            transaction = session.beginTransaction();
            session.remove(book);
            transaction.commit();
        }catch(Exception e) {
            if(transaction != null) transaction.rollback();
            throw e;
        }
    }

    public Optional<Book> findById(Long id) {
        Book book = session.get(Book.class, id);
        return Optional.ofNullable(book);
    }

    public List<Book> findAll() {
        Query<Book> query = session.createQuery("FROM Book", Book.class);
        return query.getResultList();
    }

    public Optional<Book> findByTitle(String title) {
        Query<Book> query = session.createQuery("FROM Book WHERE name = :title", Book.class);
        query.setParameter("title", title);
        Book book = query.uniqueResult(); // nulls abrunebs tu book objecti ver ipova
        return Optional.ofNullable(book);
    }

    public List<Book> findByAuthor(String author) {
        Query<Book> query = session.createQuery("FROM Book WHERE author = :author", Book.class);
        query.setParameter("author", author);
        return query.getResultList();
    }

    public List<Book> findByGenre(String genre) {
        Query<Book> query = session.createQuery("FROM Book WHERE genre = :genre", Book.class);
        query.setParameter("genre", genre);
        return query.getResultList();
    }
}
