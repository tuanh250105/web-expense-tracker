package com.expensemanager.service;

import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.model.Account;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;


public class TransactionService {
    private TransactionDAO transactionDAO = new TransactionDAO();

    //Dư; Schelduled_Transaction
    public boolean hasTransactionNearDue(UUID categoryId, BigDecimal amount, String type, Timestamp dueDate, int daysBefore, UUID userId) {
        LocalDateTime due = dueDate.toLocalDateTime();
        LocalDateTime start = due.minusDays(daysBefore);
        LocalDateTime end = due;

        return transactionDAO.hasTransactionNearDue(categoryId, amount, type, start, end, userId);
    }
}