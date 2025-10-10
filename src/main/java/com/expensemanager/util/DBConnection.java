package com.expensemanager.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // URL KHÔNG chứa username, password
    static String url = "jdbc:postgresql://db.bgbaeuehewyxditjnncg.supabase.co:5432/postgres";
    static String user = "postgres";
    static String password = "web_database@pass"; // mật khẩu thật của bạn

    static {
        try {
            Class.forName("org.postgresql.Driver"); // Load driver PostgreSQL
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Hàm lấy connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    // Hàm main để test kết nối
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Kết nối database thành công!");
            } else {
                System.out.println("❌ Kết nối thất bại.");
            }
        } catch (SQLException e) {
            System.out.println("Lỗi khi kết nối: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
