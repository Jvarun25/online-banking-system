package com.bankapp.banking.repository;

import com.bankapp.banking.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Paged history, most recent first - avoids loading a user's entire history at once
    Page<Transaction> findByAccountIdOrderByTimestampDesc(Long accountId, Pageable pageable);

    Optional<Transaction> findByReferenceId(String referenceId);
}
