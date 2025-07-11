package com.example.libraryproject.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "account_activation_table")
public class AccountActivation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true)
    @NonNull
    private UUID token = UUID.randomUUID();

    @Column(name = "email", nullable = false)
    @NonNull
    private String email;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NonNull
    private User user;

    @Column(name = "expiration_date", nullable = false)
    @NonNull
    private LocalDateTime expirationDate = LocalDateTime.now().plusDays(1);

    @Column(name = "activated", nullable = false)
    @NonNull
    private boolean activated = false;

}
