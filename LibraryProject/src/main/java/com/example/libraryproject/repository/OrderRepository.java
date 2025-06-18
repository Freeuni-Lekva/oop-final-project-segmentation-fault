package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.Order;
import com.example.libraryproject.model.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.example.libraryproject.configuration.ApplicationProperties.STALE_ORDER_TIMEOUT_HRS;


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

    public void deleteAll(Set<Order> staleOrders) {
        Transaction tx = session.beginTransaction();
        for (Order order : staleOrders) {
            session.remove(order);
        }
        tx.commit();
    }

    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(session.get(Order.class, id));
    }

    public Set<Order> findOrdersByUserId(Long userId) {
        return Set.copyOf(session.createQuery(
                        "SELECT o FROM Order o WHERE o.user.id = :userId", Order.class)
                .setParameter("userId", userId)
                .getResultList());
    }

    public Set<Order> findOrdersByBookId(Long bookId) {
        return Set.copyOf(session.createQuery(
                        "SELECT o FROM Order o WHERE o.book.id = :bookId", Order.class)
                .setParameter("bookId", bookId)
                .getResultList());
    }

    public Set<Order> findDueOrders() {
        return Set.copyOf(session.createQuery(
                        "SELECT o FROM Order o WHERE o.borrowDate > o.dueDate AND o.status = :status", Order.class)
                .setParameter("status", OrderStatus.BORROWED).
                getResultList());
    }


    public Set<Order> findAll() {
        return Set.copyOf(session.createQuery("FROM Order", Order.class)
                .getResultList());
    }

    public Optional<Order> findByPublicId(String publicId) {
        return Optional.ofNullable(session.createQuery("SELECT o FROM Order o WHERE o.publicId = :publicId", Order.class)
                .setParameter("publicId", UUID.fromString(publicId))
                .uniqueResult());
    }

    public Set<Order> findOrdersByStatus(OrderStatus status) {
        return Set.copyOf(session.createQuery(
                        "SELECT o FROM Order o WHERE o.status = :status", Order.class)
                .setParameter("status", status)
                .getResultList());
    }

    public Set<Order> findStaleOrders() {
        LocalDateTime staleDate = LocalDateTime.now().minusHours(STALE_ORDER_TIMEOUT_HRS);
        return Set.copyOf(session.createQuery(
                        "SELECT o FROM Order o WHERE o.status = :status AND o.createDate <= :staleDate", Order.class)
                .setParameter("status", OrderStatus.RESERVED).setParameter("staleDate", staleDate)
                .getResultList());
    }


}
