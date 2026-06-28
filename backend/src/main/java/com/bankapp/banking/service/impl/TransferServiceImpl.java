package com.bankapp.banking.service.impl;

import com.bankapp.banking.dto.TransactionResponse;
import com.bankapp.banking.dto.TransferRequest;
import com.bankapp.banking.entity.Account;
import com.bankapp.banking.entity.Transaction;
import com.bankapp.banking.enums.AccountStatus;
import com.bankapp.banking.enums.TransactionStatus;
import com.bankapp.banking.enums.TransactionType;
import com.bankapp.banking.exception.AccountNotActiveException;
import com.bankapp.banking.exception.InsufficientFundsException;
import com.bankapp.banking.exception.ResourceNotFoundException;
import com.bankapp.banking.exception.UnauthorizedAccessException;
import com.bankapp.banking.repository.AccountRepository;
import com.bankapp.banking.repository.TransactionRepository;
import com.bankapp.banking.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Handles fund transfers between two accounts.
 *
 * ============================ WHY THIS IS THE HARD PART ============================
 * A transfer must be ATOMIC: either both the debit (sender) and credit
 * (receiver) happen, or NEITHER does. If the app crashes or throws after
 * debiting the sender but before crediting the receiver, money would simply
 * vanish. This is THE classic banking-system interview question.
 *
 * How this is solved here:
 *
 * 1. ATOMICITY -> @Transactional on transfer(). Spring wraps the whole
 *    method in a single DB transaction. If ANY exception is thrown anywhere
 *    inside (insufficient funds, account frozen, a lock conflict, etc.),
 *    Spring automatically issues a ROLLBACK and none of the partial changes
 *    (debit, credit, or the two ledger rows) are persisted. The client either
 *    sees a fully-succeeded transfer or an error with nothing changed.
 *
 * 2. PESSIMISTIC LOCKING -> we use findByAccountNumberForUpdate(), which
 *    issues "SELECT ... FOR UPDATE" under the hood. This takes a row-level
 *    lock in the database for the duration of the transaction, so a second,
 *    concurrent transfer touching the SAME account has to wait until this
 *    transaction commits/rolls back before it can even read the balance.
 *    This rules out the "lost update" race condition where two simultaneous
 *    transfers both read balance=100, both subtract 80, and both commit -
 *    leaving balance=20 instead of the correct -60 (rejected) or 20 (correct,
 *    if only one should have succeeded).
 *
 * 3. DEADLOCK PREVENTION -> if Transfer A locks (Acc1 then Acc2) while
 *    Transfer B simultaneously locks (Acc2 then Acc1), they can deadlock,
 *    each waiting on the other. We avoid this by always acquiring locks in a
 *    FIXED, DETERMINISTIC ORDER (sorted by account number) regardless of
 *    which account is "from" and which is "to". Now every concurrent transfer
 *    between the same two accounts requests locks in the same order, so they
 *    simply queue up rather than deadlock.
 *
 * 4. ISOLATION LEVEL -> READ_COMMITTED is sufficient here because the
 *    pessimistic row lock is doing the heavy lifting; we don't rely on
 *    SERIALIZABLE isolation, which would hurt throughput unnecessarily.
 * =====================================================================================
 */
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse transfer(String username, TransferRequest request) {

        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        // --- Deterministic lock ordering to prevent deadlocks ---
        String first = request.getFromAccountNumber().compareTo(request.getToAccountNumber()) < 0
                ? request.getFromAccountNumber() : request.getToAccountNumber();
        String second = first.equals(request.getFromAccountNumber())
                ? request.getToAccountNumber() : request.getFromAccountNumber();

        Account firstLocked = accountRepository.findByAccountNumberForUpdate(first)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + first));
        Account secondLocked = accountRepository.findByAccountNumberForUpdate(second)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + second));

        // Now re-resolve which locked entity is actually "from" and which is "to"
        Account fromAccount = firstLocked.getAccountNumber().equals(request.getFromAccountNumber())
                ? firstLocked : secondLocked;
        Account toAccount = fromAccount == firstLocked ? secondLocked : firstLocked;

        // --- Authorization: caller must own the source account ---
        if (!fromAccount.getOwner().getUsername().equals(username)) {
            throw new UnauthorizedAccessException("You do not have access to the source account");
        }

        // --- Business rule checks ---
        if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("Source account is " + fromAccount.getStatus());
        }
        if (toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("Destination account is " + toAccount.getStatus());
        }
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient balance. Available: " + fromAccount.getBalance());
        }

        // --- The actual money movement (both happen, or neither does) ---
        BigDecimal amount = request.getAmount();
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // --- Double-entry ledger: one row per account, same reference ties them together ---
        String referenceId = UUID.randomUUID().toString();

        Transaction debitLeg = Transaction.builder()
                .referenceId(referenceId)
                .account(fromAccount)
                .relatedAccountNumber(toAccount.getAccountNumber())
                .type(TransactionType.TRANSFER_OUT)
                .amount(amount)
                .balanceAfter(fromAccount.getBalance())
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription() != null ? request.getDescription() : "Transfer to " + toAccount.getAccountNumber())
                .build();
        transactionRepository.save(debitLeg);

        Transaction creditLeg = Transaction.builder()
                .referenceId(referenceId)
                .account(toAccount)
                .relatedAccountNumber(fromAccount.getAccountNumber())
                .type(TransactionType.TRANSFER_IN)
                .amount(amount)
                .balanceAfter(toAccount.getBalance())
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription() != null ? request.getDescription() : "Transfer from " + fromAccount.getAccountNumber())
                .build();
        transactionRepository.save(creditLeg);

        return TransactionResponse.builder()
                .referenceId(referenceId)
                .accountNumber(fromAccount.getAccountNumber())
                .relatedAccountNumber(toAccount.getAccountNumber())
                .type(TransactionType.TRANSFER_OUT)
                .amount(amount)
                .balanceAfter(fromAccount.getBalance())
                .status(TransactionStatus.SUCCESS)
                .description(debitLeg.getDescription())
                .timestamp(debitLeg.getTimestamp())
                .build();
    }
}
