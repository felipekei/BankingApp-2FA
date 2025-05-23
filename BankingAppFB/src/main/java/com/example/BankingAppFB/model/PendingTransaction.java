package com.example.BankingAppFB.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "pending_transactions")
@Data 
public class PendingTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    public PendingTransaction() {}

    public PendingTransaction(User user, AccountType accountType, BigDecimal amount) {
        this.user = user;
        this.accountType = accountType;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }

    // Enum for TransactionStatus
    public enum TransactionStatus {
        PENDING,
        COMPLETED,
        FAILED,
        CANCELED
    }
}
