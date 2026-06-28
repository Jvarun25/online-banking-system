package com.bankapp.banking.controller;

import com.bankapp.banking.dto.AccountResponse;
import com.bankapp.banking.dto.CreateAccountRequest;
import com.bankapp.banking.dto.DepositWithdrawRequest;
import com.bankapp.banking.dto.TransactionResponse;
import com.bankapp.banking.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * All endpoints here require a valid JWT (enforced globally in SecurityConfig
 * via .anyRequest().authenticated()). The currently logged-in username is
 * pulled from the SecurityContext (populated by JwtAuthenticationFilter) via
 * the injected Authentication parameter - Spring MVC resolves this
 * automatically for any controller method, no extra config needed.
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            Authentication authentication,
            @Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getMyAccounts(Authentication authentication) {
        return ResponseEntity.ok(accountService.getMyAccounts(authentication.getName()));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(
            Authentication authentication,
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(authentication.getName(), accountNumber));
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            Authentication authentication,
            @Valid @RequestBody DepositWithdrawRequest request) {
        return ResponseEntity.ok(accountService.deposit(authentication.getName(), request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            Authentication authentication,
            @Valid @RequestBody DepositWithdrawRequest request) {
        return ResponseEntity.ok(accountService.withdraw(authentication.getName(), request));
    }
}
