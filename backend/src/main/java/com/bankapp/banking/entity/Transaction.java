package com.bankapp.banking.entity;

import com.bankapp.banking.enums.TransactionStatus;
import com.bankapp.banking.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction entity - an immutable ledger record.
 *
 * Every deposit, withdrawal, and transfer leg creates a Transaction row.
 * A single fund transfer creates TWO rows (TRANSFER_OUT on sender's account,
 * TRANSFER_IN on receiver's account) so each account's history is self-contained
 * and the rows are never updated after creation - this mirrors how real
 * double-entry ledgers work and is a common interview talking point.
 */
@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String referenceId; // e.g. UUID, shown to the user as a receipt number

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account; // the account this ledger row belongs to

    @Column(name = "related_account_number", length = 20)
    private String relatedAccountNumber; // the other party in a transfer, if any

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAfter; // snapshot of balance right after this txn

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(length = 255)
    private String description;

    @Column(updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}
