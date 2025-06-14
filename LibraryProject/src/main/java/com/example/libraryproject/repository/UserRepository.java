package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

@RequiredArgsConstructor
public class UserRepository {

    private final Session session;

    public void save(User user) {
        Transaction tx = session.beginTransaction();
        session.persist(user);
        tx.commit();
    }

    public void update(User user) {
        Transaction tx = session.beginTransaction();
        session.merge(user);
        tx.commit();
    }

    public void delete(User user) {
        Transaction tx = session.beginTransaction();
        session.remove(user);
        tx.commit();
    }

    public User findById(Long id) {
        return session.get(User.class, id);
    }

    public List<Review> findReviewsByUserId(Long userId) {
        return session.createQuery(
                        "FROM Review r WHERE r.user.id = :userId", Review.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<Book> findBooksByUserId(Long userId) {
        User user = findById(userId);
        if (user == null) return List.of();
        return List.copyOf(user.getBorrowedBooks());
    }

    public User findByUsername(String username) {
        return session.createQuery(
                        "FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .uniqueResult();
    }

    public List<User> findAll() {
        return session.createQuery("FROM User", User.class).getResultList();
    }
}
