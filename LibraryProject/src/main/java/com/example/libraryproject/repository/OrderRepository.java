package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.Order;
import com.example.libraryproject.model.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.example.libraryproject.configuration.ApplicationProperties.STALE_ORDER_TIMEOUT_HRS;


@RequiredArgsConstructor
public class OrderRepository {

    private final SessionFactory sessionFactory;

    public void save(Order order) {

        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.persist(order);
            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void update(Order order) {

        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.merge(order);
            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void delete(Order order) {

        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.remove(order);
            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void deleteAll(Set<Order> staleOrders) {

        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            for (Order order : staleOrders) {
                session.remove(order);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }

    }

    public Optional<Order> findById(Long id) {

        Session session = sessionFactory.openSession();

        Optional<Order> order = Optional.ofNullable(session.get(Order.class, id));

        session.close();

        return order;
    }

    public Set<Order> findOrdersByUserId(Long userId) {

        Session session = sessionFactory.openSession();

        Set<Order> orders = Set.copyOf(session.createQuery(
                        "SELECT o FROM Order o WHERE o.user.id = :userId", Order.class)
                .setParameter("userId", userId)
                .getResultList());

        session.close();

        return orders;

    }

    public Set<Order> findOrdersByBookId(Long bookId) {

        Session session = sessionFactory.openSession();

        Set<Order> orders = Set.copyOf(session.createQuery(
                        "SELECT o FROM Order o WHERE o.book.id = :bookId", Order.class)
                .setParameter("bookId", bookId)
                .getResultList());

        session.close();

        return orders;
    }

    public Set<Order> findDueOrders() {

        Session session = sessionFactory.openSession();

        Set<Order> orders = Set.copyOf(session.createQuery(
                        "SELECT o FROM Order o WHERE o.borrowDate > o.dueDate AND o.status = :status", Order.class)
                .setParameter("status", OrderStatus.BORROWED).
                getResultList());

        session.close();

        return orders;
    }


    public Set<Order> findAll() {

        Session session = sessionFactory.openSession();

        Set<Order> orders = Set.copyOf(session.createQuery("FROM Order", Order.class)
                .getResultList());

        session.close();

        return orders;
    }

    public Optional<Order> findByPublicId(String publicId) {

        Session session = sessionFactory.openSession();

        Optional<Order> order = Optional.ofNullable(session.createQuery(
                        "SELECT o FROM Order o WHERE o.publicId = :publicId", Order.class)
                .setParameter("publicId", UUID.fromString(publicId))
                .uniqueResult());

        session.close();

        return order;
    }

    public Set<Order> findOrdersByStatus(OrderStatus status) {

        Session session = sessionFactory.openSession();

        Set<Order> orders = Set.copyOf(session.createQuery(
                        "SELECT o FROM Order o WHERE o.status = :status", Order.class)
                .setParameter("status", status)
                .getResultList());

        session.close();

        return orders;

    }

    public Set<Order> findOrdersByUsername(String username) {
        Session session = sessionFactory.openSession();

        Set<Order> orders = Set.copyOf(session.createQuery(
                        "SELECT o FROM Order o WHERE LOWER(o.user.username) LIKE LOWER(:username)", Order.class)
                .setParameter("username", "%" + username + "%")
                .getResultList());

        session.close();

        return orders;
    }

    public Set<Order> findActiveOrders() {
        Session session = sessionFactory.openSession();

        Set<Order> orders = Set.copyOf(session.createQuery(
                        "SELECT o FROM Order o WHERE o.status IN (:reserved, :borrowed)", Order.class)
                .setParameter("reserved", OrderStatus.RESERVED)
                .setParameter("borrowed", OrderStatus.BORROWED)
                .getResultList());

        session.close();

        return orders;
    }

    public Set<Order> findOverdueOrders() {
        Session session = sessionFactory.openSession();

        LocalDateTime now = LocalDateTime.now();
        Set<Order> orders = Set.copyOf(session.createQuery(
                        "SELECT o FROM Order o WHERE o.dueDate < :now", Order.class)
                .setParameter("now", now)
                .getResultList());

        session.close();

        return orders;
    }

    public Set<Order> findStaleOrders() {

        Session session = sessionFactory.openSession();

        LocalDateTime staleDate = LocalDateTime.now().minusHours(STALE_ORDER_TIMEOUT_HRS);

        Set<Order> orders = Set.copyOf(session.createQuery(
                        "SELECT o FROM Order o " +
                                "WHERE o.status = :status AND o.createDate <= :staleDate", Order.class)
                .setParameter("status", OrderStatus.RESERVED)
                .setParameter("staleDate", staleDate)
                .getResultList());

        session.close();

        return orders;

    }

    public boolean hasReservation(Long userId, Long bookId) {

        Session session = sessionFactory.openSession();

        Optional<Order> orderOptional = Optional.ofNullable(session.createQuery(
                        "SELECT o FROM Order o " +
                                "WHERE o.user.id = :userId AND o.book.id = :bookId " +
                                "AND o.status = :status", Order.class)
                .setParameter("userId", userId)
                .setParameter("bookId", bookId)
                .setParameter("status", OrderStatus.RESERVED)
                .uniqueResult());

        session.close();
        return orderOptional.isPresent();
    }
}
