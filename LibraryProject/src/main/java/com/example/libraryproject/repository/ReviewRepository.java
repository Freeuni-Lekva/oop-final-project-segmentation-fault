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
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.persist(review);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to save review", e);
        }
    }

    public void update(Review review) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.merge(review);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to update review", e);
        }
    }

    public void delete(Review review) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            if (review != null) {
                session.remove(review);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to delete review", e);
        }
    }

    public void deleteAll(Set<Review> reviews) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            for (Review review : reviews) {
                if (review != null) {
                    session.remove(review);
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to delete all reviews", e);
        }
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

    public Set<Review> findReviewsByBookPublicId(String bookPublicId) {
        Session session = sessionFactory.openSession();

        Set<Review> reviews = new HashSet<>(session.createQuery(
                        "SELECT r FROM Review r WHERE r.book.publicId = :bookPublicId", Review.class)
                .setParameter("bookPublicId", bookPublicId)
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
