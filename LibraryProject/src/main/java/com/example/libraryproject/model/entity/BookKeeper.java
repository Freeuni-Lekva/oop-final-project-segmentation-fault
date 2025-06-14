package com.example.libraryproject.model.entity;

import jakarta.persistence.*;

@Entity
public class BookKeeper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;
}
