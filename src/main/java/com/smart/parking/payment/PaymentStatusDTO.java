package com.smart.parking.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentStatusDTO {
    private String status;
    private BigDecimal amount;
    private String transactionId;
    private LocalDateTime processedAt;

    public PaymentStatusDTO(String status, BigDecimal amount, String transactionId, LocalDateTime processedAt) {
        this.status = status;
        this.amount = amount;
        this.transactionId = transactionId;
        this.processedAt = processedAt;
    }

    public String getStatus() { return status; }
    public BigDecimal getAmount() { return amount; }
    public String getTransactionId() { return transactionId; }
    public LocalDateTime getProcessedAt() { return processedAt; }
}
