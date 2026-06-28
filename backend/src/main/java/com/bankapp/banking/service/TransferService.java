package com.bankapp.banking.service;

import com.bankapp.banking.dto.TransferRequest;
import com.bankapp.banking.dto.TransactionResponse;

public interface TransferService {
    TransactionResponse transfer(String username, TransferRequest request);
}
