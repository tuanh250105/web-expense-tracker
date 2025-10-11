package com.expensemanager.service;

import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.model.Transaction;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;


public class TransactionService {
    private TransactionDAO transactionDAO = new TransactionDAO();

    public List<Transaction> getAllTransactionsByMonthAndYear(UUID userId, int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay(); // Ví dụ: 2025-10-01 00:00:00
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay(); // Sẽ là 2025-11-01 00:00:00

        return transactionDAO.getAllTransactionsByMonthAndYear(userId, startOfMonth, endOfMonth);
    }

//    public List<Transaction> filter(UUID userId, String fromDate, String toDate, String notes, String type) {
//        return transactionDAO.filter(userId, fromDate, toDate, notes, type);
//    }
}
