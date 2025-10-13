package com.expensemanager.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.expensemanager.dao.TransactionDAOstart;
import com.expensemanager.model.Transaction;

/**
 * TransactionService - Business logic cho Transaction
 * Tính toán Top Categories dựa trên Transaction data
 */
public class TransactionServicestart {

    private final TransactionDAOstart transactionDAO;

    public TransactionServicestart() {
        this.transactionDAO = new TransactionDAOstart();
    }

    /**
     * Lấy Top Categories theo tháng/năm
     * @param userId ID của user
     * @param startOfMonth Ngày bắt đầu tháng
     * @param endOfMonth Ngày kết thúc tháng
     * @return List<CategoryStats> - Danh sách thống kê theo category
     */
    public List<CategoryStats> getTopCategoriesByMonth(UUID userId, LocalDateTime startOfMonth, LocalDateTime endOfMonth) {
        // Lấy tất cả transactions trong tháng
        List<Transaction> transactions = transactionDAO.getAllTransactionsByMonthAndYear(userId, startOfMonth, endOfMonth);

        // Group by category và tính tổng
        Map<String, CategoryStats> categoryMap = new HashMap<>();

        for (Transaction t : transactions) {
            String categoryName = t.getCategory().getName();
            String categoryType = t.getCategory().getType();

            CategoryStats stats = categoryMap.getOrDefault(categoryName,
                    new CategoryStats(categoryName, categoryType));

            stats.addAmount(t.getAmount());
            stats.incrementCount();

            categoryMap.put(categoryName, stats);
        }

        // Sắp xếp theo tổng tiền giảm dần
        return categoryMap.values().stream()
                .sorted((a, b) -> Long.compare(b.getTotalAmount(), a.getTotalAmount()))
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả transactions theo tháng
     */
    public List<Transaction> getTransactionsByMonth(UUID userId, LocalDateTime startOfMonth, LocalDateTime endOfMonth) {
        return transactionDAO.getAllTransactionsByMonthAndYear(userId, startOfMonth, endOfMonth);
    }

    /**
     * Thêm transaction mới
     */
    public void addTransaction(Transaction transaction) {
        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDateTime.now());
        }
        transactionDAO.save(transaction);
    }

    /**
     * Xóa transaction
     */
    public void deleteTransaction(UUID transactionId) {
        transactionDAO.delete(transactionId);
    }

    /**
     * Lấy transactions có lọc theo account
     */
    public List<Transaction> getFilteredTransactions(String accountIdFilter) {
        if (accountIdFilter != null && !accountIdFilter.isEmpty()) {
            UUID accountId = UUID.fromString(accountIdFilter);
            return transactionDAO.findByAccountId(accountId);
        }
        return transactionDAO.findAll();
    }

    /**
     * Inner class để chứa thống kê category
     */
    public static class CategoryStats {
        private String categoryName;
        private String categoryType;
        private long totalAmount;
        private int transactionCount;

        public CategoryStats(String categoryName, String categoryType) {
            this.categoryName = categoryName;
            this.categoryType = categoryType;
            this.totalAmount = 0;
            this.transactionCount = 0;
        }

        public void addAmount(BigDecimal amount) {
            if (amount != null) {
                this.totalAmount += amount.longValue();
            }
        }

        public void incrementCount() {
            this.transactionCount++;
        }

        // Getters
        public String getCategoryName() { return categoryName; }
        public String getCategoryType() { return categoryType; }
        public long getTotalAmount() { return totalAmount; }
        public int getTransactionCount() { return transactionCount; }
    }
}
