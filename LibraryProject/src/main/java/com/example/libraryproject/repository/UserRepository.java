package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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


    public Set<Book> findBorrowedBooksByUserId(Long userId) {
        User user = findById(userId);
        if (user == null) return Set.of();
        return Set.copyOf(user.getBorrowedBooks());
    }

    public Set<Book> findReadBooksByUserId(Long userId) {
        User user = findById(userId);
        if (user == null) return new HashSet<>();
        return Set.copyOf(user.getReadBooks());
    }

    public Optional<User> findByUsername(String username) {
        User user = session.createQuery(
                        "FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .uniqueResult();
        return Optional.ofNullable(user);
    }

    public Set<User> findAll() {
        return new HashSet<>(session.createQuery("FROM User", User.class).getResultList());
    }

    public void updateAll(Set<User> dueUsers) {
        Transaction tx = session.beginTransaction();
        for (User user : dueUsers) {
            session.merge(user);
        }
        tx.commit();
    }
}
