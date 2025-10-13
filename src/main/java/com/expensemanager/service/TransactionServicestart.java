package com.expensemanager.service;

import com.expensemanager.dao.TransactionDAOstart;
import com.expensemanager.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * TransactionServicestart - Lớp service xử lý nghiệp vụ cho Transaction
 * Gọi DAO và tổng hợp thống kê theo tháng
 */
public class TransactionServicestart {

    private static final Logger LOGGER = Logger.getLogger(TransactionServicestart.class.getName());
    private final TransactionDAOstart dao;

    public TransactionServicestart() {
        this.dao = new TransactionDAOstart();
    }

    /**
     * CategoryStats - DTO chứa thống kê tổng chi tiêu theo danh mục
     */
    public static class CategoryStats {
        private UUID categoryId;
        private String categoryName;
        private String type;
        private BigDecimal totalAmount;

        public CategoryStats(UUID categoryId, String categoryName, String type, BigDecimal totalAmount) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.type = type;
            this.totalAmount = totalAmount;
        }

        public UUID getCategoryId() { return categoryId; }
        public String getCategoryName() { return categoryName; }
        public String getType() { return type; }
        public BigDecimal getTotalAmount() { return totalAmount; }
    }

    /**
     * Lấy danh sách top categories của 1 user trong 1 tháng/năm
     */
    public List<CategoryStats> getTopCategoriesByMonth(UUID userId, LocalDateTime startOfMonth, LocalDateTime endOfMonth) {
        List<Transaction> transactions = dao.getAllTransactionsByMonthAndYear(userId, startOfMonth, endOfMonth);

        // Nhóm theo category và tính tổng
        Map<UUID, BigDecimal> totals = new HashMap<>();
        Map<UUID, String> names = new HashMap<>();
        Map<UUID, String> types = new HashMap<>();

        for (Transaction t : transactions) {
            if (t.getCategory() == null || t.getAmount() == null) continue;

            UUID categoryId = t.getCategory().getId();
            totals.merge(categoryId, t.getAmount(), BigDecimal::add);
            names.put(categoryId, t.getCategory().getName());
            types.put(categoryId, t.getCategory().getType());
        }

        // Chuyển thành list CategoryStats
        return totals.entrySet().stream()
                .map(e -> new CategoryStats(
                        e.getKey(),
                        names.get(e.getKey()),
                        types.get(e.getKey()),
                        e.getValue()
                ))
                .sorted(Comparator.comparing(CategoryStats::getTotalAmount).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Thêm mới một giao dịch
     */
    public void addTransaction(Transaction transaction) {
        try {
            dao.save(transaction);
            LOGGER.log(Level.INFO, "✅ Đã thêm transaction: {0}", transaction.getDescription());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Lỗi khi thêm transaction:", e);
        }
    }

    /**
     * Xóa một giao dịch theo ID
     */
    public void deleteTransaction(UUID transactionId) {
        try {
            dao.delete(transactionId);
            LOGGER.log(Level.INFO, "✅ Đã xóa transaction ID: {0}", transactionId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Lỗi khi xóa transaction:", e);
        }
    }

    /**
     * Lấy danh sách giao dịch đã lọc theo account ID và user ID
     */
    public List<Transaction> getFilteredTransactions(UUID userId, String accountIdFilter) {
        if (accountIdFilter != null && !accountIdFilter.isEmpty()) {
            try {
                UUID accountId = UUID.fromString(accountIdFilter);
                return dao.findByAccountIdAndUserId(userId, accountId);
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "Invalid accountId format: {0}", accountIdFilter);
                return Collections.emptyList(); // Return empty if ID is invalid
            }
        }
        return dao.findAllByUserId(userId);
    }
}
