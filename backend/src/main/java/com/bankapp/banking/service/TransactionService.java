package com.bankapp.banking.service;

import com.bankapp.banking.dto.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    Page<TransactionResponse> getHistory(String username, String accountNumber, Pageable pageable);
}
