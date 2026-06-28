package com.bankapp.banking.service.impl;

import com.bankapp.banking.dto.AccountResponse;
import com.bankapp.banking.dto.CreateAccountRequest;
import com.bankapp.banking.dto.DepositWithdrawRequest;
import com.bankapp.banking.dto.TransactionResponse;
import com.bankapp.banking.entity.Account;
import com.bankapp.banking.entity.Transaction;
import com.bankapp.banking.entity.User;
import com.bankapp.banking.enums.AccountStatus;
import com.bankapp.banking.enums.TransactionStatus;
import com.bankapp.banking.enums.TransactionType;
import com.bankapp.banking.exception.AccountNotActiveException;
import com.bankapp.banking.exception.InsufficientFundsException;
import com.bankapp.banking.exception.ResourceNotFoundException;
import com.bankapp.banking.exception.UnauthorizedAccessException;
import com.bankapp.banking.repository.AccountRepository;
import com.bankapp.banking.repository.TransactionRepository;
import com.bankapp.banking.repository.UserRepository;
import com.bankapp.banking.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Self-injected proxy reference.
     *
     * Why this is needed: Spring's @Transactional works by wrapping the bean
     * in a proxy. If a method on this class calls another method on "this"
     * directly (doDeposit(...) instead of self.doDeposit(...)), that call
     * bypasses the proxy entirely, so @Transactional on doDeposit/doWithdraw
     * would silently do nothing. Injecting the proxy back into itself
     * (constructor injection can't be used here since it would create a
     * circular dependency at bean-creation time, hence field injection +
     * @Lazy) lets deposit()/withdraw() invoke self.doDeposit(...) and get a
     * real transactional boundary on every retry attempt.
     */
    @Lazy
    @org.springframework.beans.factory.annotation.Autowired
    private AccountServiceImpl self;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_RETRIES = 3; // for optimistic-lock contention

    @Override
    @Transactional
    public AccountResponse createAccount(String username, CreateAccountRequest request) {
        User user = getUserOrThrow(username);

        Account account = Account.builder()
                .accountNumber(generateUniqueAccountNumber())
                .owner(user)
                .accountType(request.getAccountType())
                .balance(request.getInitialDeposit() == null ? BigDecimal.ZERO : request.getInitialDeposit())
                .status(AccountStatus.ACTIVE)
                .build();

        Account saved = accountRepository.save(account);

        // If they funded it on creation, log that as an opening-deposit transaction too
        if (saved.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            recordTransaction(saved, null, TransactionType.DEPOSIT, saved.getBalance(), saved.getBalance(),
                    "Opening deposit");
        }

        return toAccountResponse(saved);
    }

    @Override
    public List<AccountResponse> getMyAccounts(String username) {
        User user = getUserOrThrow(username);
        return accountRepository.findAllByOwnerId(user.getId()).stream()
                .map(this::toAccountResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AccountResponse getAccountByNumber(String username, String accountNumber) {
        Account account = getAccountOrThrow(accountNumber);
        assertOwnership(account, username);
        return toAccountResponse(account);
    }

    @Override
    public TransactionResponse deposit(String username, DepositWithdrawRequest request) {
        // Calling self.doDeposit(...) (the injected proxy) - NOT this.doDeposit(...) -
        // so @Transactional on doDeposit actually applies on every retry attempt.
        return executeWithOptimisticRetry(() -> self.doDeposit(username, request));
    }

    @Transactional
    public TransactionResponse doDeposit(String username, DepositWithdrawRequest request) {
        Account account = getAccountOrThrow(request.getAccountNumber());
        assertOwnership(account, username);
        assertActive(account);

        account.setBalance(account.getBalance().add(request.getAmount()));
        Account updated = accountRepository.save(account); // version check happens at flush/commit

        Transaction txn = recordTransaction(
                updated, null, TransactionType.DEPOSIT, request.getAmount(), updated.getBalance(),
                request.getDescription() != null ? request.getDescription() : "Deposit"
        );
        return toTransactionResponse(txn);
    }

    @Override
    public TransactionResponse withdraw(String username, DepositWithdrawRequest request) {
        return executeWithOptimisticRetry(() -> self.doWithdraw(username, request));
    }

    @Transactional
    public TransactionResponse doWithdraw(String username, DepositWithdrawRequest request) {
        Account account = getAccountOrThrow(request.getAccountNumber());
        assertOwnership(account, username);
        assertActive(account);

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient balance. Available: " + account.getBalance());
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        Account updated = accountRepository.save(account); // version check happens at flush/commit

        Transaction txn = recordTransaction(
                updated, null, TransactionType.WITHDRAWAL, request.getAmount(), updated.getBalance(),
                request.getDescription() != null ? request.getDescription() : "Withdrawal"
        );
        return toTransactionResponse(txn);
    }

    @Override
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::toAccountResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountResponse freezeAccount(String accountNumber) {
        Account account = getAccountOrThrow(accountNumber);
        account.setStatus(AccountStatus.FROZEN);
        return toAccountResponse(accountRepository.save(account));
    }

    @Override
    @Transactional
    public AccountResponse unfreezeAccount(String accountNumber) {
        Account account = getAccountOrThrow(accountNumber);
        account.setStatus(AccountStatus.ACTIVE);
        return toAccountResponse(accountRepository.save(account));
    }

    // ---------- helpers ----------

    /**
     * Wraps a deposit/withdraw operation with a small retry loop.
     *
     * Why this exists: doDeposit()/doWithdraw() are each a single
     * @Transactional unit (balance update + ledger row, commit or rollback
     * together). If Hibernate throws an OptimisticLockingFailureException at
     * commit time - because another concurrent request updated the same
     * Account row first and bumped its @Version - that whole transaction
     * rolls back cleanly with nothing partially applied. This loop then
     * simply retries the operation from scratch (fresh read, fresh version),
     * rather than failing the user's request outright on the first
     * collision. This is the "optimistic locking + retry" pattern - a
     * standard answer to "how do you prevent race conditions on balance
     * updates" in interviews.
     */
    private <T> T executeWithOptimisticRetry(java.util.function.Supplier<T> action) {
        int attempts = 0;
        while (true) {
            try {
                return action.get();
            } catch (OptimisticLockingFailureException ex) {
                attempts++;
                if (attempts >= MAX_RETRIES) {
                    throw ex; // bubble up -> GlobalExceptionHandler returns 409 Conflict
                }
                // brief backoff before retrying
                try {
                    Thread.sleep(50L * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private Transaction recordTransaction(
            Account account, String relatedAccountNumber, TransactionType type,
            BigDecimal amount, BigDecimal balanceAfter, String description) {
        Transaction txn = Transaction.builder()
                .referenceId(UUID.randomUUID().toString())
                .account(account)
                .relatedAccountNumber(relatedAccountNumber)
                .type(type)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .status(TransactionStatus.SUCCESS)
                .description(description)
                .build();
        return transactionRepository.save(txn);
    }

    private String generateUniqueAccountNumber() {
        String candidate;
        do {
            // 12-digit numeric account number, e.g. "483920175643"
            candidate = String.format("%012d", Math.abs(RANDOM.nextLong() % 1_000_000_000_000L));
        } while (accountRepository.existsByAccountNumber(candidate));
        return candidate;
    }

    private User getUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private Account getAccountOrThrow(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
    }

    private void assertOwnership(Account account, String username) {
        if (!account.getOwner().getUsername().equals(username)) {
            throw new UnauthorizedAccessException("You do not have access to this account");
        }
    }

    private void assertActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "Account " + account.getAccountNumber() + " is " + account.getStatus() + " and cannot be used");
        }
    }

    private AccountResponse toAccountResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .ownerUsername(account.getOwner() != null ? account.getOwner().getUsername() : null)
                .build();
    }

    private TransactionResponse toTransactionResponse(Transaction txn) {
        return TransactionResponse.builder()
                .referenceId(txn.getReferenceId())
                .accountNumber(txn.getAccount().getAccountNumber())
                .relatedAccountNumber(txn.getRelatedAccountNumber())
                .type(txn.getType())
                .amount(txn.getAmount())
                .balanceAfter(txn.getBalanceAfter())
                .status(txn.getStatus())
                .description(txn.getDescription())
                .timestamp(txn.getTimestamp())
                .build();
    }
}
