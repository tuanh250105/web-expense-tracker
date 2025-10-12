package com.expensemanager.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.expensemanager.model.Transaction;

/**
 * Service Interface cho các nghiệp vụ cốt lõi của Bank Transaction.
 * Sử dụng Transaction entity mapping với bảng transactions
 */
public interface BankTransactionService {

    // --- CRUD Operations ---
    Optional<Transaction> getBankTransactionById(UUID transactionId);
    Transaction createBankTransaction(Transaction transaction);
    Transaction updateBankTransaction(UUID transactionId, Transaction transactionDetails);
    void deleteBankTransaction(UUID transactionId);

    // --- Core Query Operations ---
    List<Transaction> getBankTransactionsByAccountId(UUID accountId);
    List<Transaction> getBankTransactionsPaginated(UUID accountId, int page, int size);
    List<Transaction> findByDateRange(UUID accountId, LocalDateTime startDate, LocalDateTime endDate);
    List<Transaction> searchByKeyword(UUID accountId, String keyword);

    // --- Categorization Logic ---
    List<Transaction> getUncategorizedTransactions(UUID accountId);
    
    /**
     * Gắn một Transaction với một Category.
     */
    Transaction linkToCategory(UUID transactionId, Integer categoryId);

    /**
     * Tạo một Transaction mới với category.
     */
    Transaction createTransactionWithCategory(Transaction transaction, Integer categoryId);

    void markAsProcessed(UUID transactionId);

    void markAsUnprocessed(UUID transactionId);
}