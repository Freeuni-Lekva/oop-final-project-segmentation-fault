package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.Book;
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


    public List<Book> findBorrowedBooksByUserId(Long userId) {
        User user = findById(userId);
        if (user == null) return List.of();
        return List.copyOf(user.getBorrowedBooks());
    }

    public List<Book> findReadBooksByUserId(Long userId) {
        User user = findById(userId);
        if (user == null) return List.of();
        return List.copyOf(user.getReadBooks());
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
