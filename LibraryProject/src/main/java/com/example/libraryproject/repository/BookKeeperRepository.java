package com.example.libraryproject.repository;

import com.example.libraryproject.model.entity.BookKeeper;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;

@RequiredArgsConstructor
public class BookKeeperRepository {

    private final Session session;

    public void save(BookKeeper bookKeeper) {

    }

    public void update(BookKeeper bookKeeper) {

    }

    public void delete(BookKeeper bookKeeper) {

    }

    public BookKeeper findById(Long id) {
        //TODO
        return null;
    }
    
    public BookKeeper findByUsername(String username) {
        //TODO
        return null;
    }
}
