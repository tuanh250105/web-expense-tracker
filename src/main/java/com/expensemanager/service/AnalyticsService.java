package com.expensemanager.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AnalyticsService {

    private final TransactionService transactionService = new TransactionService();

    public List<Transaction> find(UUID userId, String type, LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            return transactionService.filterPanel(
                    userId,
                    from.toLocalDate().toString(),
                    to.toLocalDate().toString(),
                    "", // notes để trống
                    "all".equalsIgnoreCase(type) ? "" : type
            );
        }

        LocalDateTime now = LocalDateTime.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        return transactionService.getAllTransactionsByMonthAndYear(userId, month, year);
    }
}
