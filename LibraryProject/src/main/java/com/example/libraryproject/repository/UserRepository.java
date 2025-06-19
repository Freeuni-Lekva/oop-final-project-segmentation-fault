package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class UserRepository {

    private final SessionFactory sessionFactory;

    public void save(User user) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        session.persist(user);

        tx.commit();
        session.close();
    }

    public void update(User user) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        session.merge(user);

        tx.commit();
        session.close();

    }

    public void delete(User user) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        session.remove(user);

        tx.commit();
        session.close();

    }

    public Optional<User> findById(Long id) {
        Session session = sessionFactory.openSession();

        Optional<User> user = Optional.ofNullable(session.get(User.class, id));

        session.close();

        return user;
    }


    public Set<Book> findBorrowedBooksByUserId(Long userId) {
        Optional<User> user = findById(userId);
        return user.map(value -> Set.copyOf(value.getBorrowedBooks())).orElseGet(Set::of);
    }

    public Set<Book> findReadBooksByUserId(Long userId) {
        Optional<User> user = findById(userId);

        return user.map(value -> Set.copyOf(value.getReadBooks())).orElseGet(HashSet::new);


    }

    public Optional<User> findByUsername(String username) {
        Session session = sessionFactory.openSession();

        User user = session.createQuery(
                        "FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .uniqueResult();

        session.close();

        return Optional.ofNullable(user);
    }

    public Set<User> findAll() {
        Session session = sessionFactory.openSession();

        HashSet<User> users = new HashSet<>(session.createQuery("FROM User", User.class).getResultList());

        session.close();

        return users;
    }

    public void updateAll(Set<User> dueUsers) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        for (User user : dueUsers) {
            session.merge(user);
        }
        tx.commit();
        session.close();
    }
}
