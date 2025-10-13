package com.expensemanager.dao;

import com.expensemanager.model.Budget;
import com.expensemanager.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BudgetDAO {

    public Long upsert(Budget b) {
        String sql = "INSERT INTO budgets (user_id, period_type, period_start, period_end, category_id, limit_amount, spent_amount, note) " +
                "VALUES (?, ?, ?, ?, ?, ?, COALESCE(?, 0), ?) " +
                "ON CONFLICT (user_id, period_start, period_end, COALESCE(category_id, -1)) DO UPDATE SET " +
                "limit_amount = EXCLUDED.limit_amount, note = EXCLUDED.note RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, b.getUserId());
            ps.setString(2, b.getPeriodType());
            ps.setTimestamp(3, Timestamp.valueOf(b.getStartDate()));
            ps.setTimestamp(4, Timestamp.valueOf(b.getEndDate()));
            if (b.getCategoryId() == null) ps.setNull(5, Types.BIGINT); else ps.setLong(5, b.getCategoryId());
            ps.setBigDecimal(6, b.getLimitAmount());
            ps.setBigDecimal(7, b.getSpentAmount() == null ? BigDecimal.ZERO : b.getSpentAmount());
            ps.setString(8, b.getNote());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<Budget> findByUser(Long userId, String periodType, Long categoryId) {
        StringBuilder sb = new StringBuilder("SELECT id, user_id, period_type, period_start, period_end, category_id, limit_amount, spent_amount, note, created_at FROM budgets WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);
        if (periodType != null && !periodType.isEmpty()) {
            sb.append(" AND period_type = ?");
            params.add(periodType);
        }
        if (categoryId != null) {
            sb.append(" AND category_id = ?");
            params.add(categoryId);
        }
        sb.append(" ORDER BY period_start DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Budget> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Budget findById(Long id) {
        String sql = "SELECT id, user_id, period_type, period_start, period_end, category_id, limit_amount, spent_amount, note, created_at FROM budgets WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return null;
    }

    public void updateSpent(Long id, BigDecimal spent) {
        String sql = "UPDATE budgets SET spent_amount = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, spent);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public List<DailySpent> getDailySeries(Long budgetId) {
        String sql = "SELECT tx_date as date, SUM(amount) as spent FROM (" +
                "  SELECT DATE(transaction_date) as tx_date, CASE WHEN type = 'expense' THEN amount ELSE 0 END as amount " +
                "  FROM transactions t WHERE t.user_id = ? AND DATE(t.transaction_date) BETWEEN ? AND ?" +
                ") s GROUP BY tx_date ORDER BY tx_date";
        Budget b = findById(budgetId);
        if (b == null) return List.of();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, b.getUserId());
            ps.setTimestamp(2, Timestamp.valueOf(b.getStartDate()));
            ps.setTimestamp(3, Timestamp.valueOf(b.getEndDate()));
            try (ResultSet rs = ps.executeQuery()) {
                List<DailySpent> list = new ArrayList<>();
                while (rs.next()) {
                    DailySpent d = new DailySpent();
                    d.date = rs.getDate("date").toLocalDate();
                    d.spent = rs.getBigDecimal("spent");
                    list.add(d);
                }
                return list;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public BigDecimal calcTotalSpent(Long budgetId) {
        Budget b = findById(budgetId);
        if (b == null) return BigDecimal.ZERO;
        String sql = "SELECT COALESCE(SUM(CASE WHEN type = 'expense' THEN amount ELSE 0 END),0) AS total " +
                "FROM transactions t WHERE t.user_id = ? AND DATE(t.transaction_date) BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, b.getUserId());
            ps.setTimestamp(2, Timestamp.valueOf(b.getStartDate()));
            ps.setTimestamp(3, Timestamp.valueOf(b.getEndDate()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("total");
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return BigDecimal.ZERO;
    }

    private Budget map(ResultSet rs) throws SQLException {
        Budget b = new Budget();
        b.setId(rs.getLong("id"));
        b.setUserId(rs.getLong("user_id"));
        b.setPeriodType(rs.getString("period_type"));
        b.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
        b.setEndDate(rs.getTimestamp("end_date").toLocalDateTime());
        long cat = rs.getLong("category_id");
        b.setCategoryId(rs.wasNull() ? null : cat);
        b.setLimitAmount(rs.getBigDecimal("limit_amount"));
        b.setSpentAmount(rs.getBigDecimal("spent_amount"));
        b.setNote(rs.getString("note"));
        return b;
    }

    public static class DailySpent {
        public LocalDate date;
        public BigDecimal spent;
    }
}


