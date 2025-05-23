package com.example.BankingAppFB.repository;

import com.example.BankingAppFB.model.TransactionLog;
import com.example.BankingAppFB.model.User;
import com.example.BankingAppFB.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page; // For pagination support
import org.springframework.data.domain.Pageable; // For pagination settings
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<TransactionLog, Long> {
    // Retrieve all transactions for a user across different accounts

    @Query("SELECT t FROM TransactionLog t " +
        "LEFT JOIN t.account a " +
        "LEFT JOIN t.sourceAccount sa " +
        "LEFT JOIN t.destinationAccount da " +
        "WHERE a.user = :user OR sa.user = :user OR da.user = :user")
    List<TransactionLog> findByAccountUser(User user);

    // Retrieve filtered transactions for a user with pagination

    @Query("SELECT t FROM TransactionLog t " +
        "LEFT JOIN t.account a " +
        "LEFT JOIN t.sourceAccount sa " +
        "LEFT JOIN t.destinationAccount da " +
        "WHERE (a.user = :user OR sa.user = :user OR da.user = :user) " +
        "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
        "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
        "AND (:type IS NULL OR t.type = :type)")
        Page<TransactionLog> findByFilters(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("type") TransactionType type,
            Pageable pageable);
    
}