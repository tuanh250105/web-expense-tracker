package com.expensemanager.service;

import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AnalyticsService {
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public List<Transaction> find(UUID userId, String type, LocalDateTime from, LocalDateTime to) {
        return transactionDAO.find(userId, type, from, to);
    }
}

