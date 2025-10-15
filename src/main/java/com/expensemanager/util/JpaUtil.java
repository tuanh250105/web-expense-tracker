package com.expensemanager.util;

import java.util.HashMap;
import java.util.Map;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * JpaUtil - tiện ích khởi tạo EntityManagerFactory dùng cho Hibernate +
 * Supabase (PostgreSQL).
 * Hỗ trợ lấy cấu hình từ file .env (qua biến môi trường).
 * Tự động thêm các option an toàn cho PgBouncer pooler.
 */
public class JpaUtil {

    private static final EntityManagerFactory emf;

    static {
        EntityManagerFactory tempEmf = null;
        try {
            System.out.println("──────────────────────────────────────────────");
            System.out.println("🔗 [JpaUtil] Đang khởi tạo EntityManagerFactory...");

            // Lấy từ .env, fallback sang System.getenv nếu không có
            String url = System.getenv("DB_URL");
            String user = System.getenv("DB_USER");
            String pass = System.getenv("DB_PASS");

            // Fallback: đọc từ system property nếu env không có
            if (url == null) url = System.getProperty("DB_URL");
            if (user == null) user = System.getProperty("DB_USER");
            if (pass == null) pass = System.getProperty("DB_PASS");

            // 2️⃣ Gộp vào Map thuộc tính JPA
            Map<String, Object> props = new HashMap<>();
            if (url != null)
                props.put("jakarta.persistence.jdbc.url", appendSafeUrlOptions(url));
            if (user != null)
                props.put("jakarta.persistence.jdbc.user", user);
            if (pass != null)
                props.put("jakarta.persistence.jdbc.password", pass);

            // 3️⃣ Các thiết lập Hibernate bổ sung (tối ưu cho Supabase)
            props.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
            props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            props.put("hibernate.hbm2ddl.auto", "update");
            props.put("hibernate.show_sql", "true");
            props.put("hibernate.format_sql", "true");

            // ⚡ QUAN TRỌNG: Cấu hình Connection Pool
            // Sử dụng built-in connection pool của Hibernate (không cần thư viện ngoài)
            props.put("hibernate.connection.pool_size", "5");  // Tối đa 5 connections


            // Release mode - trả connection về pool sau mỗi statement
            props.put("hibernate.connection.release_mode", "after_transaction");

            // Auto-commit
            props.put("hibernate.connection.autocommit", "false");

            // Connection timeout và validation
            props.put("hibernate.c3p0.timeout", "300");  // 5 phút timeout
            props.put("hibernate.c3p0.idle_test_period", "60");  // Test mỗi 60 giây
            props.put("hibernate.c3p0.max_statements", "0");  // Tắt prepared statement cache

            // Validation query
            //props.put("hibernate.connection.provider_class", "org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl");

            // 4️⃣ Tên persistence-unit (phải trùng trong persistence.xml)
            String persistenceUnitName = "default";
            System.out.println("DEBUG >>> DB_URL=" + url);
            System.out.println("DEBUG >>> DB_USER=" + user);
            System.out.println("DEBUG >>> DB_PASS=" + (pass != null ? "********" : "null"));

            // 5️⃣ Tạo EntityManagerFactory
            tempEmf = Persistence.createEntityManagerFactory(persistenceUnitName, props);

            System.out.println("✅ [JpaUtil] Khởi tạo EntityManagerFactory thành công!");
            System.out.println("   DB_URL  = " + (url != null ? url.replaceAll(":[^:@]+@", ":****@") : "null"));
            System.out.println("   DB_USER = " + user);
            System.out.println("   Max Connection Pool Size = 5");
            System.out.println("──────────────────────────────────────────────");

        } catch (Exception e) {
            System.err.println("❌ [JpaUtil] Lỗi khi khởi tạo EntityManagerFactory: " + e.getMessage());
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
        emf = tempEmf;
    }

    /**
     * Thêm option "prepareThreshold=0" để tránh lỗi Supabase pooler (prepared
     * statement conflict).
     */
    private static String appendSafeUrlOptions(String url) {
        if (!url.contains("prepareThreshold")) {
            String separator = url.contains("?") ? "&" : "?";
            url += separator + "sslmode=require&prepareThreshold=0";
        }
        return url;
    }

    /** Lấy EntityManager mới. */
    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /** Lấy EntityManagerFactory. */
    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public static EntityManager em() {
        return emf.createEntityManager();
    }

    /** Đóng EntityManagerFactory khi tắt ứng dụng. */
    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            System.out.println("✅ [JpaUtil] Đã đóng EntityManagerFactory.");
        }
    }
}