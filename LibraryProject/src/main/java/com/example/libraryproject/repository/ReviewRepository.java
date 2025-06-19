package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.Review;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@RequiredArgsConstructor
public class ReviewRepository {

    private final SessionFactory sessionFactory;

    public void save(Review review) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        session.persist(review);

        tx.commit();
        session.close();
    }

    public void update(Review review) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        session.merge(review);

        tx.commit();
        session.close();
    }

    public void delete(Review review) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        session.remove(review);

        tx.commit();
        session.close();
    }

    public Optional<Review> findById(Long id) {
        Session session = sessionFactory.openSession();

        Optional<Review> review = Optional.ofNullable(session.get(Review.class, id));

        session.close();

        return review;
    }

    public Set<Review> findReviewsByUserId(Long userId) {
        Session session = sessionFactory.openSession();

        HashSet<Review> reviews = new HashSet<>(session.createQuery(
                        "SELECT r FROM Review r WHERE r.user.id = :userId", Review.class)
                .setParameter("userId", userId)
                .getResultList());

        session.close();

        return reviews;
    }

    public Set<Review> findReviewsByBookId(Long bookId) {
        Session session = sessionFactory.openSession();

        HashSet<Review> reviews = new HashSet<>(session.createQuery(
                        "SELECT r FROM Review r WHERE r.book.id = :bookId", Review.class)
                .setParameter("bookId", bookId)
                .getResultList());

        session.close();

        return reviews;
    }

    public Set<Review> findAll() {
        Session session = sessionFactory.openSession();

        HashSet<Review> reviews = new HashSet<>(session.createQuery("FROM Review", Review.class)
                .getResultList());

        session.close();

        return reviews;
    }

}
