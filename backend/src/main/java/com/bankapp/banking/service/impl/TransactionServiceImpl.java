package com.bankapp.banking.service.impl;

import com.bankapp.banking.dto.TransactionResponse;
import com.bankapp.banking.entity.Account;
import com.bankapp.banking.entity.Transaction;
import com.bankapp.banking.exception.ResourceNotFoundException;
import com.bankapp.banking.exception.UnauthorizedAccessException;
import com.bankapp.banking.repository.AccountRepository;
import com.bankapp.banking.repository.TransactionRepository;
import com.bankapp.banking.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    public Page<TransactionResponse> getHistory(String username, String accountNumber, Pageable pageable) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));

        if (!account.getOwner().getUsername().equals(username)) {
            throw new UnauthorizedAccessException("You do not have access to this account's history");
        }

        Page<Transaction> page = transactionRepository.findByAccountIdOrderByTimestampDesc(account.getId(), pageable);
        return page.map(this::toResponse);
    }

    private TransactionResponse toResponse(Transaction txn) {
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
