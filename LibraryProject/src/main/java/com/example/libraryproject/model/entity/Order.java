package com.example.libraryproject.model.entity;

import com.example.libraryproject.model.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;
import java.util.UUID;


@RequiredArgsConstructor
@Getter
@Setter
@Table(name = "orders_table")
@Entity
@Builder
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate = LocalDateTime.now();

    @Column(name = "borrow_date")
    private LocalDateTime borrowDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Column(name = "requested_duration")
    @NonNull
    private Long requestedDurationInDays;

    @Column(name = "status", length = 50, nullable = false)
    @NonNull
    private OrderStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NonNull
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    @NonNull
    private Book book;

    public Order() {}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Order order = (Order) obj;
        return publicId.equals(order.publicId);
    }


    @Override
    public int hashCode() {
        return publicId.hashCode();
    }
}
