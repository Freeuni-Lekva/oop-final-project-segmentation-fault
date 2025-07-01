package com.example.libraryproject.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "reviews_table")
@RequiredArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @Column(name = "rating", nullable = false)
    @NonNull
    private int rating;

    @Column(name = "comment", length = 1000)
    @NonNull
    private String comment;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NonNull
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    @NonNull
    private Book book;

    public Review() {}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Review review = (Review) obj;
        return publicId.equals(review.publicId);
    }

    @Override
    public int hashCode() {
        return publicId.hashCode();
    }

}
