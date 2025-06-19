package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.BookKeeper;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Optional;

@RequiredArgsConstructor
public class BookKeeperRepository {
    private final SessionFactory sessionFactory;

    public void save(BookKeeper bookKeeper) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        session.persist(bookKeeper);

        transaction.commit();
        session.close();
    }

    public void update(BookKeeper bookKeeper) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        session.merge(bookKeeper);

        transaction.commit();
        session.close();
    }

    public void delete(BookKeeper bookKeeper) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        session.remove(bookKeeper);

        transaction.commit();
        session.close();
    }

    public Optional<BookKeeper> findById(Long id) {
        Session session = sessionFactory.openSession();
        Optional<BookKeeper> bookKeeper= Optional.ofNullable(session.find(BookKeeper.class, id));

        session.close();

        return bookKeeper;
    }

    public Optional<BookKeeper> findByUsername(String username) {
        Session session = sessionFactory.openSession();

        Query<BookKeeper> query = session.createQuery(
                "FROM BookKeeper WHERE username = :username", BookKeeper.class);
        BookKeeper result = query.setParameter("username", username).uniqueResult();

        session.close();

        return Optional.ofNullable(result);
    }
}
