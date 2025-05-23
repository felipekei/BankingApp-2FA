package com.example.BankingAppFB.service;

import com.example.BankingAppFB.model.*;
import com.example.BankingAppFB.repository.BillerRepository;
import com.example.BankingAppFB.repository.AccountRepository;
import com.example.BankingAppFB.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BillerService {

    private static final Logger logger = LoggerFactory.getLogger(BillerService.class);

    @Autowired
    private BillerRepository billerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountService accountService;
    
    @Autowired
    private EmailService emailService;

    public User findUserByUsername(String username) {
        // Custom logic to retrieve User by username
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void addPayee(User user, BillType billType, String accountNumber, Frequency paymentFrequency, LocalDate nextPaymentDate, AccountType paymentAccountType, BigDecimal paymentAmount) {
        Biller biller = new Biller();
        biller.setUser(user);
        biller.setBillType(billType);
        biller.setAccountNumber(accountNumber);
        biller.setPaymentFrequency(paymentFrequency);
        biller.setNextPaymentDate(nextPaymentDate.atStartOfDay());
        biller.setPaymentAccountType(paymentAccountType);
        biller.setPaymentAmount(paymentAmount);
        billerRepository.save(biller);
    }

    public List<Biller> getBillersForUser(User user) {
        return billerRepository.findByUser(user);
    }

    public void payBiller(User user, Long billerId, Account account, BigDecimal amount) {
        Biller biller = billerRepository.findById(billerId)
                .orElseThrow(() -> new RuntimeException("Biller not found."));
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance for bill payment.");
        }    

        // Deduct payment amount from the account
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        // Log the transaction
        transactionService.logBillPayment(account, amount, biller.getBillType() + " - Account " + biller.getAccountNumber());

        logger.info("Manual payment processed successfully for biller: {}", biller.getAccountNumber());

        // Suspicious payment alert
        if (amount.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            String subject = "Suspicious Bill Payment Alert";
            String body = "Dear " + user.getUsername() + ",\n\n"
                    + "A manual bill payment of $" + amount + " was made from your " + account.getAccountType() + " account to "
                    + biller.getBillType() + " (Account: " + biller.getAccountNumber() + ").\n\n"
                    + "If this was not authorized, please contact support immediately.\n\n"
                    + "Best regards,\nYour Banking App Team";
    
            emailService.sendEmail(user.getEmail(), subject, body);
        }
    
    }


    /**
     * Scheduled method to process recurring bill payments.
     * This method runs daily at midnight and handles:
     * - Automatic payments for due bills.
     * - Updates to the next payment date based on frequency.
     */
    
    @Scheduled(cron = "0 0 0 * * *") // Runs daily at midnight
    public void processScheduledPayments() {

        List<Biller> billers = billerRepository.findAll(); // Fetch all billers
        LocalDateTime now = LocalDateTime.now();

        for (Biller biller : billers) {
            if (isPaymentDue(biller, now)) {
                try {
                    processPaymentForBiller(biller);
                } catch (Exception ex) {
                    logger.error("Error processing payment for biller {}: {}", biller.getAccountNumber(), ex.getMessage());
                }
            }
        }
    }

    /**
     * Helper method to check if a payment is due for a biller.
     *
     * @param biller The biller to check.
     * @param now The current date and time.
     * @return True if the payment is due, false otherwise.
     */
    private boolean isPaymentDue(Biller biller, LocalDateTime now) {
        return biller.getNextPaymentDate() != null && biller.getNextPaymentDate().isBefore(now);
    }

    /**
     * Helper method to process a payment for a specific biller.
     *
     * @param biller The biller for which the payment is being processed.
     */
    private void processPaymentForBiller(Biller biller) {
        User user = biller.getUser(); // Get the user associated with the biller
        BigDecimal paymentAmount = biller.getPaymentAmount(); // Get the dynamic amount
        AccountType accountType = biller.getPaymentAccountType();

        logger.info("Processing payment for biller: {} for user: {}", biller.getAccountNumber(), user.getUsername());

        // Allow the user to select the account type for payment
        Account paymentAccount = accountService.getAccount(user, accountType);

        if (paymentAccount.getBalance().compareTo(paymentAmount) >= 0) {
            // Deduct the payment and log the transaction
            accountService.withdrawMoney(user, paymentAccount.getAccountType(), paymentAmount);

            // Log the transaction as a bill payment
            transactionService.logBillPayment(paymentAccount, paymentAmount,
                    biller.getBillType() + " - Account " + biller.getAccountNumber());

            // Send email alert for payments >= 1000
            if (paymentAmount.compareTo(BigDecimal.valueOf(1000)) >= 0) {
                String subject = "Suspicious Bill Payment Alert";
                String body = "Dear " + user.getUsername() + ",\n\n"
                        + "A bill payment of $" + paymentAmount + " was made from your " + accountType + " account to "
                        + biller.getBillType() + " (Account: " + biller.getAccountNumber() + ").\n\n"
                        + "If this was not authorized, please contact support immediately.\n\n"
                        + "Best regards,\nYour Banking App Team";

                emailService.sendEmail(user.getEmail(), subject, body); // Send email alert
            }

            // Update the next payment date based on frequency
            updateNextPaymentDate(biller);
            billerRepository.save(biller);

            logger.info("Payment processed successfully for biller: {}", biller.getAccountNumber());
        } else {
            logger.error("Insufficient funds for scheduled payment for biller: {}", biller.getAccountNumber());
        }
    }

        /**
     * Updates the next payment date for a biller based on its frequency.
     *
     * @param biller The biller whose next payment date needs to be updated.
     */
    private void updateNextPaymentDate(Biller biller) {
        LocalDateTime nextDate = biller.getNextPaymentDate();
        Frequency frequency = biller.getPaymentFrequency();

        if (frequency == Frequency.WEEKLY) {
            nextDate = nextDate.plusWeeks(1);
        } else if (frequency == Frequency.BIWEEKLY) {
            nextDate = nextDate.plusWeeks(2);
        } else if (frequency == Frequency.MONTHLY) {
            nextDate = nextDate.plusMonths(1);
        }

        biller.setNextPaymentDate(nextDate);
        logger.info("Updated next payment date for biller {} to {}", biller.getAccountNumber(), nextDate);
    }

    /**
     * Sends email reminders for upcoming bill payments.
     * 
     * This method runs daily at 8 AM and checks all billers for payments due
     * within the next three days. For eligible billers, it sends an email
     * reminder to the associated user.
     * 
     * Cron Expression: "0 0 9 * * *" - Executes daily at 9 AM.
     */

    @Scheduled(cron = "0 0 9 * * *") // Runs daily at 9 AM
    public void processReminders() {
        logger.info("Running payment reminders...");
        List<Biller> billers = billerRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Biller biller : billers) {
            if (biller.getNextPaymentDate() != null && biller.getNextPaymentDate().isBefore(now.plusDays(3))) {
                User user = biller.getUser();
                String message = "Upcoming payment for " + biller.getBillType() + " on " + biller.getNextPaymentDate() + ".";

                // Send email notification
                emailService.sendEmail(user.getEmail(), "Upcoming Payment Reminder", message);

                logger.info("Reminder sent for biller: {} to user: {}", biller.getAccountNumber(), user.getUsername());
            }
        }
    }
}




