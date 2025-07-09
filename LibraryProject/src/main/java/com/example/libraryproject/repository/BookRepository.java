package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.Book;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class BookRepository {

    private final SessionFactory sessionFactory;

    public void save(Book book) {
        Transaction transaction = null;
        Session session = sessionFactory.openSession();
        try{
            transaction = session.beginTransaction();
            session.persist(book);
            transaction.commit();
            session.close();
        }catch(Exception e) {
            if(transaction != null) transaction.rollback();
            throw e;
        }
    }

    public void update(Book book) {
        Transaction transaction = null;
        Session session = sessionFactory.openSession();

        try{
            transaction = session.beginTransaction();
            session.merge(book);
            transaction.commit();
            session.close();

        }catch(Exception e) {
            if(transaction != null) transaction.rollback();
            throw e;
        }
    }

    public void delete(Book book) {
        Transaction transaction = null;
        Session session = sessionFactory.openSession();
        try{
            transaction = session.beginTransaction();
            session.remove(book);
            transaction.commit();
            session.close();

        }catch(Exception e) {
            if(transaction != null) transaction.rollback();
            throw e;
        }
    }

    public Optional<Book> findById(Long id) {
        Session session = sessionFactory.openSession();

        Book book = session.get(Book.class, id);

        session.close();

        return Optional.ofNullable(book);
    }

    public List<Book> findByAuthorsAndGenres(Set<String> authors, Set<String> genres, Set<Book> readBooks){
        Session session = sessionFactory.openSession();

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

        List<Book> result = query.getResultList();

        session.close();

        return result;
    }

    public List<Book> findAll() {
        Session session = sessionFactory.openSession();

        Query<Book> query = session.createQuery("FROM Book", Book.class);

        List<Book> books = query.getResultList();

        session.close();

        return books;
    }
    public Optional<Book> findByPublicId(String publicId) {
        Session session = sessionFactory.openSession();

        Query<Book> query = session.createQuery("FROM Book WHERE publicId = :publicId", Book.class);
        query.setParameter("publicId", publicId);

        Optional<Book> result = Optional.ofNullable(query.uniqueResult());

        session.close();

        return result;
    }

    public Optional<Book> findByTitle(String title) {
        Session session = sessionFactory.openSession();

        Query<Book> query = session.createQuery("FROM Book WHERE name = :title", Book.class);
        query.setParameter("title", title);
        Book book = query.uniqueResult();

        session.close();

        return Optional.ofNullable(book);
    }

    public List<Book> findByAuthor(String author) {
        Session session = sessionFactory.openSession();

        Query<Book> query = session.createQuery("FROM Book WHERE author = :author", Book.class);
        query.setParameter("author", author);

        List<Book> books = query.getResultList();
        session.close();
        return books;
    }

    public List<Book> findByGenre(String genre) {
        Session session = sessionFactory.openSession();

        Query<Book> query = session.createQuery("FROM Book WHERE genre = :genre", Book.class);
        query.setParameter("genre", genre);

        List<Book> books = query.getResultList();

        session.close();
        return books;
    }

    public void saveAll(List<Book> books) {
        Session session = sessionFactory.openSession();

        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            for (Book book : books) {
                session.persist(book);
            }
            transaction.commit();
            session.close();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    public List<Book> searchByTitle(String searchTerm) {
        Session session = sessionFactory.openSession();

        Query<Book> query = session.createQuery(
                "FROM Book WHERE LOWER(name) LIKE LOWER(:searchTerm)", Book.class);
        query.setParameter("searchTerm", "%" + searchTerm + "%");

        List<Book> results = query.getResultList();
        session.close();
        return results;
    }

    public List<Book> searchByAuthor(String searchTerm) {
        Session session = sessionFactory.openSession();

        Query<Book> query = session.createQuery(
                "FROM Book WHERE LOWER(author) LIKE LOWER(:searchTerm)", Book.class);
        query.setParameter("searchTerm", "%" + searchTerm + "%");

        List<Book> results = query.getResultList();
        session.close();
        return results;
    }
}
