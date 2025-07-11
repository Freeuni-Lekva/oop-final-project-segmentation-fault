package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.AccountActivation;
import com.example.libraryproject.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class AccountActivationRepository {
    
    private final SessionFactory sessionFactory;

    public void save(AccountActivation accountActivation) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.persist(accountActivation);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to save account activation", e);
        } finally {
            session.close();
        }
    }

    public void update(AccountActivation accountActivation) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.merge(accountActivation);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to update account activation", e);
        } finally {
            session.close();
        }
    }

    public void delete(AccountActivation accountActivation) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.remove(accountActivation);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to delete account activation", e);
        } finally {
            session.close();
        }
    }

    public Optional<AccountActivation> findById(Long id) {
        Session session = sessionFactory.openSession();
        
        AccountActivation accountActivation = session.get(AccountActivation.class, id);
        
        session.close();
        
        return Optional.ofNullable(accountActivation);
    }

    public Optional<AccountActivation> findByToken(UUID token) {
        Session session = sessionFactory.openSession();

        Query<AccountActivation> query = session.createQuery(
                "FROM AccountActivation a WHERE a.token = :token", AccountActivation.class);
        query.setParameter("token", token);

        AccountActivation result = query.uniqueResult();
        
        session.close();
        
        return Optional.ofNullable(result);
    }

    public Optional<AccountActivation> findByEmail(String email) {
        Session session = sessionFactory.openSession();

        Query<AccountActivation> query = session.createQuery(
                "FROM AccountActivation a WHERE a.email = :email AND a.activated = false", AccountActivation.class);
        query.setParameter("email", email);

        AccountActivation result = query.uniqueResult();
        
        session.close();
        
        return Optional.ofNullable(result);
    }

    public Optional<AccountActivation> findByUser(User user) {
        Session session = sessionFactory.openSession();

        Query<AccountActivation> query = session.createQuery(
                "FROM AccountActivation a WHERE a.user = :user AND a.activated = false", AccountActivation.class);
        query.setParameter("user", user);

        AccountActivation result = query.uniqueResult();
        
        session.close();
        
        return Optional.ofNullable(result);
    }

    public Optional<AccountActivation> findByTokenAndNotExpired(UUID token) {
        Session session = sessionFactory.openSession();

        Query<AccountActivation> query = session.createQuery(
                "FROM AccountActivation a WHERE a.token = :token AND a.expirationDate > :now AND a.activated = false", 
                AccountActivation.class);
        query.setParameter("token", token);
        query.setParameter("now", LocalDateTime.now());

        AccountActivation result = query.uniqueResult();
        
        session.close();
        
        return Optional.ofNullable(result);
    }

    public List<AccountActivation> findExpiredActivations() {
        Session session = sessionFactory.openSession();

        Query<AccountActivation> query = session.createQuery(
                "FROM AccountActivation a WHERE a.expirationDate <= :now AND a.activated = false", 
                AccountActivation.class);
        query.setParameter("now", LocalDateTime.now());

        List<AccountActivation> results = query.getResultList();
        
        session.close();
        
        return results;
    }

    public void deleteExpiredActivations() {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            
            Query<?> query = session.createQuery(
                    "DELETE FROM AccountActivation a WHERE a.expirationDate <= :now AND a.activated = false");
            query.setParameter("now", LocalDateTime.now());
            
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to delete expired activations", e);
        } finally {
            session.close();
        }
    }

    public List<AccountActivation> findAll() {
        Session session = sessionFactory.openSession();

        Query<AccountActivation> query = session.createQuery("FROM AccountActivation", AccountActivation.class);
        List<AccountActivation> results = query.getResultList();

        session.close();

        return results;
    }
}
