package com.expensemanager.service;

import com.expensemanager.model.Transaction;

import java.time.LocalDateTime;
import java.util.*;

public class AnalyticsService {

    private final TransactionService transactionService = new TransactionService();

    /**
     * Lấy danh sách giao dịch của user để phân tích.
     * Nếu có from/to → lọc theo khoảng ngày.
     * Nếu không → lấy theo tháng hiện tại.
     */
    public List<Transaction> findTransactions(UUID userId, String type, LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            String[] types = "all".equalsIgnoreCase(type) ? null : new String[]{type};
            String categoryId = null;
            String notes = null;
            return transactionService.filterPanel(
                    userId,
                    from.toLocalDate().toString(),
                    to.toLocalDate().toString(),
                    notes,
                    types,
                    categoryId
            );
        }

        LocalDateTime now = LocalDateTime.now();
        return transactionService.getAllTransactionsByMonthAndYear(userId, now.getMonthValue(), now.getYear());
    }

    /**
     * Gọi các hàm tiện ích từ TransactionService để xây dựng kết quả analytics.
     */
    public Map<String, Object> buildAnalytics(UUID userId, String type, LocalDateTime from, LocalDateTime to, String group, int topN) {
        List<Transaction> list = findTransactions(userId, type, from, to);

        if (list == null || list.isEmpty()) {
            return Map.of("message", "No data found");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", transactionService.calculateSummary(list));
        result.put("grouped", transactionService.groupTransactionsByTime(list, group));
        result.put("topCategory", transactionService.groupTransactionsByCategory(list, topN));

        // Raw data cho frontend
        List<Map<String, Object>> raw = new ArrayList<>();
        for (Transaction t : list) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", t.getId());
            item.put("type", t.getType());
            item.put("amount", t.getAmount());
            item.put("note", t.getNote());
            item.put("date", t.getTransactionDate());
            item.put("category", (t.getCategory() != null) ? t.getCategory().getName() : null);
            raw.add(item);
        }
        result.put("raw", raw);

        return result;
    }
}
