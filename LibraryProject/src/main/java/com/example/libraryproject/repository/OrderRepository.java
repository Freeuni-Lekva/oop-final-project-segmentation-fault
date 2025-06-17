package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.Order;
import com.example.libraryproject.model.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class OrderRepository {

    private final Session session;

    public void save(Order order) {
        Transaction tx = session.beginTransaction();
        session.persist(order);
        tx.commit();
    }

    public void update(Order order) {
        Transaction tx = session.beginTransaction();
        session.merge(order);
        tx.commit();
    }

    public void delete(Order order) {
        Transaction tx = session.beginTransaction();
        session.remove(order);
        tx.commit();
    }

    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(session.get(Order.class, id));
    }

    public Set<Order> findOrdersByUserId(Long userId) {
        return new HashSet<>(session.createQuery(
                "SELECT o FROM Order o WHERE o.user.id = :userId", Order.class)
                .setParameter("userId", userId)
                .getResultList());
    }

    public Set<Order> findOrdersByBookId(Long bookId) {
        return new HashSet<>(session.createQuery(
                "SELECT o FROM Order o WHERE o.book.id = :bookId", Order.class)
                .setParameter("bookId", bookId)
                .getResultList());
    }

    public Set<Order> findAll() {
        return new HashSet<>(session.createQuery("FROM Order", Order.class)
                .getResultList());
    }

    public Optional<Order> findByPublicId(String publicId) {
        return Optional.ofNullable(session.createQuery("SELECT o FROM Order o WHERE o.publicId = :publicId", Order.class)
                .setParameter("publicId", publicId)
                .uniqueResult());
    }

    public Set<Order> findOrdersByStatus(OrderStatus status) {
        return new HashSet<>(session.createQuery(
                "SELECT o FROM Order o WHERE o.status = :status", Order.class)
                .setParameter("status", status)
                .getResultList());
    }
}
