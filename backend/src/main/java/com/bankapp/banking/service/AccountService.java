package com.bankapp.banking.service;

import com.bankapp.banking.dto.AccountResponse;
import com.bankapp.banking.dto.CreateAccountRequest;
import com.bankapp.banking.dto.DepositWithdrawRequest;
import com.bankapp.banking.dto.TransactionResponse;

import java.util.List;

public interface AccountService {

    AccountResponse createAccount(String username, CreateAccountRequest request);

    List<AccountResponse> getMyAccounts(String username);

    AccountResponse getAccountByNumber(String username, String accountNumber);

    TransactionResponse deposit(String username, DepositWithdrawRequest request);

    TransactionResponse withdraw(String username, DepositWithdrawRequest request);

    // Admin-only
    List<AccountResponse> getAllAccounts();

    AccountResponse freezeAccount(String accountNumber);

    AccountResponse unfreezeAccount(String accountNumber);
}
