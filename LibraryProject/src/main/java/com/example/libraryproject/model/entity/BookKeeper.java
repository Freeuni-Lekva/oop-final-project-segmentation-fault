package com.example.libraryproject.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bookkeepers_table")
@Getter
@Setter
@RequiredArgsConstructor
public class BookKeeper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BookKeeper bookKeeper = (BookKeeper) obj;
        return username.equals(bookKeeper.username) && password.equals(bookKeeper.password);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

}
