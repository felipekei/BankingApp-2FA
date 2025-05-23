package com.example.BankingAppFB.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Table(name = "billers")
@Data
public class Biller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING) // Map the enum as a string in the database
    @Column(name = "bill_type", nullable = false)
    private BillType billType;

    @Column(name = "account_number", nullable = false, length = 6)
    @Size(min = 6, max = 6, message = "Account number must be exactly 6 digits.")
    private String accountNumber;

    @Enumerated(EnumType.STRING) // Frequency (e.g., ONCE, WEEKLY, BIWEEKLY, MONTHLY)
    @Column(name = "payment_frequency", nullable = true)
    private Frequency paymentFrequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_account_type", nullable = true)
    private AccountType paymentAccountType;

    @Column(name = "next_payment_date", nullable = true)
    private LocalDateTime nextPaymentDate;

    @Column(name = "payment_amount", nullable = false)
    private BigDecimal paymentAmount;
}

