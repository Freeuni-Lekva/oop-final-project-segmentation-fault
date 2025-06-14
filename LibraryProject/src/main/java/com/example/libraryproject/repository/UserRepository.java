package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;

import java.util.List;

@RequiredArgsConstructor
public class UserRepository {

    private final Session session;

    public void save(User user) {
    }

    public void update(User user) {
    }

    public void delete(User user) {
    }

    public User findById(Long id) {
        return null;
    }
    public List<Review> findReviewsByUserId(Long userId) {
        return null;
    }
    public List<Book> findBooksByUserId(Long userId) {
        return null;
    }
    public User findByUsername(String username) {
        return null;
    }

    public List<User> findAll() {
        return null;
    }
}
