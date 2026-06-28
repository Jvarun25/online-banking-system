package com.bankapp.banking.controller;

import com.bankapp.banking.dto.AccountResponse;
import com.bankapp.banking.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only endpoints.
 *
 * Defense in depth - two layers enforce ROLE_ADMIN here:
 *   1. SecurityConfig: .requestMatchers("/api/admin/**").hasRole("ADMIN")
 *      (URL-pattern based, checked by the filter chain before the request
 *      even reaches this controller)
 *   2. @PreAuthorize on each method (method-level, evaluated by Spring AOP
 *      right before the method body runs)
 * Belt-and-suspenders: even if a URL pattern in SecurityConfig were ever
 * mistyped or refactored, the method-level check still protects the action.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AccountService accountService;

    @GetMapping("/accounts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @PatchMapping("/accounts/{accountNumber}/freeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> freezeAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.freezeAccount(accountNumber));
    }

    @PatchMapping("/accounts/{accountNumber}/unfreeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> unfreezeAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.unfreezeAccount(accountNumber));
    }
}
