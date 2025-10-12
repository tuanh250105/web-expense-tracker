package com.expensemanager.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Kết nối tới Supabase Postgres (JDBC URL phải bắt đầu bằng jdbc:)
    private static final String DB_URL = "jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:6543/postgres?user=postgres.bgbaeuehewyxditjnncg&password=web_database@pass";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "web_database@pass"; // thay bằng password Supabase thực tế

    static {
        try {
            Class.forName("org.postgresql.Driver"); // load driver
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
}
