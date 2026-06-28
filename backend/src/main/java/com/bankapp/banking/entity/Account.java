package com.bankapp.banking.entity;

import com.bankapp.banking.enums.AccountStatus;
import com.bankapp.banking.enums.AccountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account entity - represents a bank account owned by a User.
 *
 * IMPORTANT - concurrency safety:
 * The @Version field enables JPA/Hibernate's OPTIMISTIC LOCKING.
 * Every UPDATE checks the version number in the WHERE clause:
 *   UPDATE accounts SET balance = ?, version = version + 1 WHERE id = ? AND version = ?
 * If two requests read the same account and both try to update it, the second
 * write fails with an OptimisticLockException because the version no longer matches.
 * The TransactionService catches this and retries - this is what prevents
 * "double spend" / lost-update race conditions when two transfers hit the same
 * account at the same time (the classic interview question for this project).
 *
 * BigDecimal is used for money instead of double/float to avoid floating-point
 * rounding errors - never use float/double for currency.
 */
@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    /**
     * Optimistic locking version - Hibernate manages this automatically.
     * Do NOT set this manually anywhere in business logic.
     */
    @Version
    private Long version;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
