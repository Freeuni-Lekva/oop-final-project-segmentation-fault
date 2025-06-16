package com.example.libraryproject.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books_table")
@Getter @Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    @NonNull
    @Column(name = "genre", nullable = false, length = 100)
    private String genre;

    @NonNull
    @Column(name = "author", nullable = false, length = 100)
    private String author;

    @NonNull
    @Column(name = "publication_date")
    private LocalDate date;

    @Lob
    @NonNull
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NonNull
    @Column(name = "volume")
    private Long volume;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private List<Review> reviews = new ArrayList<>();

    @NonNull
    @Column(name = "amount_in_library")
    private Long amountInLib;

    @NonNull
    @Column(name = "rating")
    private Long rating;

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", genre='" + genre + '\'' +
                ", author='" + author + '\'' +
                ", date=" + date +
                ", volume=" + volume +
                ", reviews=" + reviews +
                ", amountInLib=" + amountInLib +
                ", rating=" + rating +
                '}';
    }
    
    @NonNull
    @Column(name = "image_url", length = 255)
    private String imageUrl;
}

