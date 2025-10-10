package com.expensemanager.dao;

import com.expensemanager.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AccountDAO {

    public Map<String, Object> getBalanceHistory(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        try {
            UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid userId format: " + userId, e);
        }

        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();

        String sql = "SELECT name, balance FROM accounts WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(userId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    labels.add(rs.getString("name"));
                    data.add(rs.getDouble("balance"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching balance history: " + e.getMessage(), e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("data", data);
        return result;
    }

}