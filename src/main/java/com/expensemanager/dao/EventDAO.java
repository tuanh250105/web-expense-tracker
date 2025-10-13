package com.expensemanager.dao;

import com.expensemanager.model.Event;
import com.expensemanager.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {
    public Long create(Event e) {
        String sql = "INSERT INTO events(user_id, name, goal_amount, start_date, end_date, status) VALUES(?,?,?,?,?,COALESCE(?, 'ACTIVE')) RETURNING id";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, e.getUserId());
            ps.setString(2, e.getName());
            if (e.getGoalAmount() == null) ps.setNull(3, Types.NUMERIC); else ps.setBigDecimal(3, e.getGoalAmount());
            if (e.getStartDate() == null) ps.setNull(4, Types.DATE); else ps.setDate(4, Date.valueOf(e.getStartDate()));
            if (e.getEndDate() == null) ps.setNull(5, Types.DATE); else ps.setDate(5, Date.valueOf(e.getEndDate()));
            ps.setString(6, e.getStatus());
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getLong(1); }
        } catch (SQLException ex) { throw new RuntimeException(ex); }
        return null;
    }

    public void archive(Long eventId) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE events SET status='ARCHIVED' WHERE id=?")) {
            ps.setLong(1, eventId);
            ps.executeUpdate();
        } catch (SQLException ex) { throw new RuntimeException(ex); }
    }

    public List<Event> listByUser(Long userId) {
        String sql = "SELECT id, user_id, name, goal_amount, start_date, end_date, status FROM events WHERE user_id=? ORDER BY created_at DESC";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Event> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException ex) { throw new RuntimeException(ex); }
    }

    public void attachTransaction(Long eventId, Long transactionId) {
        String sql = "INSERT INTO event_transactions(event_id, transaction_id) VALUES(?, ?) ON CONFLICT DO NOTHING";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ps.setLong(2, transactionId);
            ps.executeUpdate();
        } catch (SQLException ex) { throw new RuntimeException(ex); }
    }

    public List<Daily> getEventSeries(Long eventId, Long userId) {
        String sql = "SELECT DATE(t.transaction_date) d, SUM(CASE WHEN t.type='expense' THEN t.amount ELSE 0 END) s " +
                "FROM event_transactions et JOIN transactions t ON et.transaction_id=t.id " +
                "WHERE et.event_id=? GROUP BY DATE(t.transaction_date) ORDER BY DATE(t.transaction_date)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Daily> list = new ArrayList<>();
                while (rs.next()) {
                    Daily d = new Daily();
                    d.date = rs.getDate(1).toLocalDate();
                    d.spent = rs.getBigDecimal(2);
                    list.add(d);
                }
                return list;
            }
        } catch (SQLException ex) { throw new RuntimeException(ex); }
    }

    private Event map(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setId(rs.getLong("id"));
        e.setUserId(rs.getLong("user_id"));
        e.setName(rs.getString("name"));
        e.setGoalAmount(rs.getBigDecimal("goal_amount"));
        Date sd = rs.getDate("start_date");
        e.setStartDate(sd == null ? null : sd.toLocalDate());
        Date ed = rs.getDate("end_date");
        e.setEndDate(ed == null ? null : ed.toLocalDate());
        e.setStatus(rs.getString("status"));
        return e;
    }

    public static class Daily {
        public LocalDate date;
        public java.math.BigDecimal spent;
    }
}


