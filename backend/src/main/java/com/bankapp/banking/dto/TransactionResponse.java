package com.bankapp.banking.dto;

import com.bankapp.banking.enums.TransactionStatus;
import com.bankapp.banking.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String referenceId;
    private String accountNumber;
    private String relatedAccountNumber;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private TransactionStatus status;
    private String description;
    private LocalDateTime timestamp;
}
