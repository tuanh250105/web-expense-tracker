package com.expensemanager.dao;

import com.expensemanager.model.Point;
import com.expensemanager.util.DatabaseConnection;

import java.sql.*;

public class PointDAO {
    public Point getOrCreate(Long userId) {
        String upsert = "INSERT INTO points(user_id, score) VALUES(?, 0) ON CONFLICT (user_id) DO NOTHING";
        try (Connection c = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(upsert)) {
                ps.setLong(1, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT id, user_id, score FROM points WHERE user_id=?")) {
                ps.setLong(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Point p = new Point();
                        p.setId(rs.getLong("id"));
                        p.setUserId(rs.getLong("user_id"));
                        p.setScore(rs.getInt("score"));
                        return p;
                    }
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return null;
    }

    public void addPoints(Long userId, int delta) {
        String sql = "INSERT INTO points(user_id, score) VALUES(?, ?) ON CONFLICT (user_id) DO UPDATE SET score = points.score + EXCLUDED.score, updated_at = NOW()";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, delta);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public boolean redeem(Long userId, int amount) {
        String sql = "UPDATE points SET score = score - ?, updated_at = NOW() WHERE user_id = ? AND score >= ?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, amount);
            ps.setLong(2, userId);
            ps.setInt(3, amount);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}


