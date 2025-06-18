package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.Book;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    public List<Book> findByAuthorsAndGenres(Set<String> authors, Set<String> genres, Set<Book> readBooks){
        String hql = "FROM Book b WHERE " +
                "(:authorsSize = 0 OR b.author IN :authors) AND " +
                "(:genresSize = 0 OR b.genre IN :genres) AND " +
                "(b NOT IN :readBooks)";
        Query<Book> query = session.createQuery(hql, Book.class);

        query.setParameter("authors", authors);
        query.setParameter("genres", genres);
        query.setParameter("readBooks", readBooks);

        query.setParameter("authorsSize", authors.size());
        query.setParameter("genresSize", genres.size());

        return query.getResultList();
    }

    public List<Book> findAll() {
        Query<Book> query = session.createQuery("FROM Book", Book.class);
        return query.getResultList();
    }
    public Optional<Book> findByPublicId(String publicId) {
        Query<Book> query = session.createQuery("FROM Book WHERE publicId = :publicId", Book.class);
        query.setParameter("publicId", publicId);
        return Optional.ofNullable(query.uniqueResult());
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

    public void saveAll(List<Book> books) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            for (Book book : books) {
                session.persist(book);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }
}
