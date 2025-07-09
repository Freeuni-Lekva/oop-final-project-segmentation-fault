package com.example.libraryproject.model.entity;

import com.example.libraryproject.model.enums.Role;
import com.example.libraryproject.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users_table")
@Getter
@Setter
@RequiredArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    @NonNull
    private String username;

    @Column(name = "password", nullable = false)
    @NonNull
    private String password;

    @Column(name = "mail", nullable = false)
    @NonNull
    private String mail;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @NonNull
    private Role role = Role.USER;

    @Column(name = "status", nullable = false)
    @NonNull
    private UserStatus status =  UserStatus.ACTIVE;

    @Column(name = "bio")
    @NonNull
    private String bio = "";

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "borrowed_books",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    @NonNull
    private Set<Book> borrowedBooks = new HashSet<>();


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "read_books",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    @NonNull
    private Set<Book> readBooks = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_reviews",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "review_id")
    )
    private Set<Review> reviews = new HashSet<>();



    @Column(name = "review_count")
    @NonNull
    private Long reviewCount = 0L;

    public User() {}

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                ", bio='" + bio + '\'' +
                ", borrowedBooks=" + borrowedBooks +
                ", readBooks=" + readBooks +
                ", reviewCount=" + reviewCount +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return username.equals(user.username) && password.equals(user.password);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

}
