package com.expensemanager.util;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * JpaUtil - tiện ích khởi tạo EntityManagerFactory dùng cho Hibernate + Supabase (PostgreSQL).
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

            // 1️⃣ Lấy thông tin cấu hình từ biến môi trường (Tomcat đọc từ system env)
            String url = System.getenv("DB_URL");
            String user = System.getenv("DB_USER");
            String pass = System.getenv("DB_PASS");

            // 2️⃣ Gộp vào Map thuộc tính JPA
            Map<String, Object> props = new HashMap<>();
            if (url != null) props.put("jakarta.persistence.jdbc.url", appendSafeUrlOptions(url));
            if (user != null) props.put("jakarta.persistence.jdbc.user", user);
            if (pass != null) props.put("jakarta.persistence.jdbc.password", pass);

            // 3️⃣ Các thiết lập Hibernate bổ sung (tối ưu cho Supabase)
            props.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
            props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            props.put("hibernate.hbm2ddl.auto", "update");
            props.put("hibernate.show_sql", "true");
            props.put("hibernate.format_sql", "true");

            // 🔒 Ngăn lỗi “prepared statement already exists” trên Supabase Pooler
            props.put("hibernate.hikari.dataSource.cachePrepStmts", "false");
            props.put("hibernate.hikari.dataSource.prepStmtCacheSize", "0");
            props.put("hibernate.hikari.dataSource.useServerPrepStmts", "false");

            props.put("hibernate.connection.provider_class",
              "org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl");

            // 4️⃣ Tên persistence-unit (phải trùng trong persistence.xml)
            String persistenceUnitName ="default";

            // 5️⃣ Tạo EntityManagerFactory
            if (props.isEmpty()) {
                tempEmf = Persistence.createEntityManagerFactory(persistenceUnitName);
            } else {
                tempEmf = Persistence.createEntityManagerFactory(persistenceUnitName, props);
            }

            System.out.println("✅ [JpaUtil] Khởi tạo EntityManagerFactory thành công!");
            System.out.println("   DB_URL  = " + url);
            System.out.println("   DB_USER = " + user);
            System.out.println("──────────────────────────────────────────────");

        } catch (Exception e) {
            System.err.println("❌ [JpaUtil] Lỗi khi khởi tạo EntityManagerFactory: " + e.getMessage());
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
        emf = tempEmf;
    }

    /**
     * Thêm option “prepareThreshold=0” để tránh lỗi Supabase pooler (prepared statement conflict).
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

    public static EntityManager em() { return emf.createEntityManager(); }

    /** Đóng EntityManagerFactory khi tắt ứng dụng. */
    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            System.out.println("✅ [JpaUtil] Đã đóng EntityManagerFactory.");
        }
    }

}