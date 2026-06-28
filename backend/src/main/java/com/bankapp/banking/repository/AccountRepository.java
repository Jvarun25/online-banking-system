package com.bankapp.banking.repository;

import com.bankapp.banking.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findAllByOwnerId(Long ownerId);

    boolean existsByAccountNumber(String accountNumber);

    /**
     * PESSIMISTIC_WRITE lock variant.
     *
     * The default locking strategy used by TransferService is OPTIMISTIC
     * (via the @Version field on Account) - good when transfer collisions on
     * the same account are rare, since it avoids holding a DB row lock and is
     * cheaper under low contention.
     *
     * This pessimistic alternative is here to show the other classic approach:
     * it issues `SELECT ... FOR UPDATE`, which makes any other transaction
     * trying to read/lock the same row simply BLOCK and wait until this
     * transaction commits or rolls back, instead of racing and failing fast.
     * Worth mentioning in interviews as a tradeoff: optimistic = fail-and-retry,
     * pessimistic = block-and-wait. Pessimistic is generally preferred when
     * contention on the same account is expected to be frequent.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);
}
