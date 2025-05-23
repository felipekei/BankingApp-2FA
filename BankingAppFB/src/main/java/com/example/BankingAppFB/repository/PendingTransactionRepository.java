package com.example.BankingAppFB.repository;

import com.example.BankingAppFB.model.PendingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PendingTransactionRepository extends JpaRepository<PendingTransaction, Long> {

    // This query will return the first pending transaction for a user, based on username
    Optional<PendingTransaction> findFirstByUser_UsernameAndStatus(String username, PendingTransaction.TransactionStatus status);


    // This query will return the first pending transaction for a user, if available
    @Query("SELECT pt FROM PendingTransaction pt WHERE pt.user.id = :userId AND pt.status = 'PENDING' ORDER BY pt.createdAt ASC")
    Optional<PendingTransaction> findFirstPendingTransactionByUserId(@Param("userId") Long userId);

}

