package com.example.BankingAppFB.repository;

import com.example.BankingAppFB.model.Biller;
import com.example.BankingAppFB.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillerRepository extends JpaRepository<Biller, Long> {
    // Custom query to find billers by user
    List<Biller> findByUser(User user);
}