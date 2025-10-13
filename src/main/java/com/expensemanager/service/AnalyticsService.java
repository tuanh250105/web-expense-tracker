package com.expensemanager.service;

import com.expensemanager.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AnalyticsService {

    private final TransactionService transactionService = new TransactionService();

    public List<Transaction> find(UUID userId, String type, LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            String[] typeValues = "all".equalsIgnoreCase(type) || type == null || type.isBlank()
                    ? null
                    : type.split(",");
            return transactionService.filterPanel(
                    userId,
                    from.toLocalDate().toString(),
                    to.toLocalDate().toString(),
                    "", // notes để trống
                    typeValues
            );
        }

        LocalDateTime now = LocalDateTime.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        return transactionService.getAllTransactionsByMonthAndYear(userId, month, year);
    }
}
