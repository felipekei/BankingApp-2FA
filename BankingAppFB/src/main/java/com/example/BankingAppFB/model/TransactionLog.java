package com.example.BankingAppFB.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
public class TransactionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // The type of transaction (deposit / withdrawal / transfer)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
    
    // The amount involved
    @Column(nullable = false)
    private BigDecimal amount;
    
    // The date and time the transaction occurred
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate = LocalDateTime.now();
    
    // The account related to the transaction in deposit/withdrawal
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    // For TRANSFER: The source account (where funds are withdrawn from)
    @ManyToOne
    @JoinColumn(name = "source_account_id")
    private Account sourceAccount;
    
    // For TRANSFER: The destination account (where funds are deposited)
    @ManyToOne
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;

    // New field to store custom transaction messages
    @Column(name = "message", nullable = true, length = 255)
    private String message;

}
