package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.Review;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.HashSet;
import java.util.Set;


@RequiredArgsConstructor
public class ReviewRepository {

    private final Session session;

    public void save(Review review) {
        Transaction tx = session.beginTransaction();
        session.persist(review);
        tx.commit();
    }

    public void update(Review review) {
        Transaction tx = session.beginTransaction();
        session.merge(review);
        tx.commit();
    }

    public void delete(Review review) {
        Transaction tx = session.beginTransaction();
        session.remove(review);
        tx.commit();
    }

    public Review findById(Long id) {
        return session.get(Review.class, id);
    }

    public Set<Review> findReviewsByUserId(Long userId) {
        return new HashSet<>(session.createQuery(
                        "SELECT r FROM Review r WHERE r.user.id = :userId", Review.class)
                .setParameter("userId", userId)
                .getResultList());
    }

    public Set<Review> findReviewsByBookId(Long bookId) {
        return new HashSet<>(session.createQuery(
                        "SELECT r FROM Review r WHERE r.book.id = :bookId", Review.class)
                .setParameter("bookId", bookId)
                .getResultList());
    }

    public Set<Review> findAll() {
        return new HashSet<>(session.createQuery("FROM Review", Review.class)
                .getResultList());
    }



}
