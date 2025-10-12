package com.expensemanager.service;

import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.model.Transaction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AnalyticsService {

    private final TransactionDAO dao = new TransactionDAO();

    public List<Transaction> find(UUID userId, String type, LocalDateTime from, LocalDateTime to) {
        // Nếu có from/to, gọi filter()
        if (from != null && to != null) {
            return dao.filter(
                    userId,
                    from.toLocalDate().toString(),
                    to.toLocalDate().toString(),
                    null,
                    "all".equalsIgnoreCase(type) ? null : type
            );
        }
        // Nếu không có from/to, lấy dữ liệu trong tháng hiện tại
        LocalDateTime start = LocalDateTime.now().withDayOfMonth(1);
        LocalDateTime end = start.plusMonths(1);
        return dao.getAllTransactionsByMonthAndYear(userId, start, end);
    }
}
