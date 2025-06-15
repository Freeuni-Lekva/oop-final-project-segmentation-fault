package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.BookKeeper;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

@RequiredArgsConstructor
public class BookKeeperRepository {
    private final Session session;

    public void save(BookKeeper bookKeeper) {
        Transaction transaction = session.beginTransaction();
        session.persist(bookKeeper);
        transaction.commit();
    }

    public void update(BookKeeper bookKeeper) {
        Transaction transaction = session.beginTransaction();
        session.merge(bookKeeper);
        transaction.commit();
    }

    public void delete(BookKeeper bookKeeper) {
        Transaction transaction = session.beginTransaction();
        session.remove(bookKeeper);
        transaction.commit();
    }

    public BookKeeper findById(Long id) {
        return session.find(BookKeeper.class, id);
    }
    
    public BookKeeper findByUsername(String username) {
        Query<BookKeeper> query = session.createQuery("FROM BookKeeper WHERE username = :username", BookKeeper.class);
        return query.setParameter("username", username).uniqueResult();
    }
}
