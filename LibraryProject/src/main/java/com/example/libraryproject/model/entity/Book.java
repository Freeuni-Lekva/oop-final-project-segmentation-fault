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
    @Column(name = "public_id", unique = true, nullable = false)
    private String publicId;

    @NonNull
    @Column(name = "name", unique = true, nullable = false)
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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id")
    private List<Review> reviews = new ArrayList<>();

    @NonNull
    @Column(name = "amount_in_library")
    private Long totalAmount;

    @NonNull
    @Column(name = "copies_in_library")
    private Long currentAmount;

    @NonNull
    @Column(name = "rating")
    private Long rating;

    @NonNull
    @Column(name = "image_url")
    private String imageUrl;

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
                ", total amount=" + totalAmount +
                ", current amount=" + currentAmount +
                ", rating=" + rating +
                '}';
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Book book = (Book) obj;
        return publicId.equalsIgnoreCase(book.publicId);
    }
}
