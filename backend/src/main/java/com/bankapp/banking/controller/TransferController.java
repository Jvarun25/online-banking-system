package com.bankapp.banking.controller;

import com.bankapp.banking.dto.TransactionResponse;
import com.bankapp.banking.dto.TransferRequest;
import com.bankapp.banking.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransactionResponse> transfer(
            Authentication authentication,
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transferService.transfer(authentication.getName(), request));
    }
}
