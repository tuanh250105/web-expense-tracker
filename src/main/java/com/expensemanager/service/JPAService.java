package com.expensemanager.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * JPA Manager Service (Singleton)
 * Quản lý EntityManagerFactory và EntityManager, tương thích Tomcat + Docker.
 */
public class JPAService {

    private static final String PERSISTENCE_UNIT = "default"; // 👈 Giữ nguyên theo persistence.xml
    private static JPAService instance;
    private EntityManagerFactory entityManagerFactory;

    private JPAService() {
        try {
            System.out.println("🔗 [JPAService] Initializing EntityManagerFactory...");

            // 1️⃣ Đọc biến môi trường từ Docker (.env)
            String dbUrl = System.getenv("DB_URL");
            String dbUser = System.getenv("DB_USER");
            String dbPass = System.getenv("DB_PASS");

            if (dbUrl == null || dbUser == null) {
                System.err.println("⚠️ [JPAService] Missing DB environment variables! Check .env or docker-compose.yml");
            }

            // 2️⃣ Truyền cấu hình động vào Hibernate
            Map<String, String> props = new HashMap<>();
            String url = System.getenv("DB_URL");
            String user = System.getenv("DB_USER");
            String pass = System.getenv("DB_PASS");
            if (url != null) props.put("jakarta.persistence.jdbc.url", url);
            if (user != null) props.put("jakarta.persistence.jdbc.user", user);
            if (pass != null) props.put("jakarta.persistence.jdbc.password", pass);

            // Hibernate configuration
            props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            props.put("hibernate.hbm2ddl.auto", "update");
            props.put("hibernate.show_sql", "true");
            props.put("hibernate.format_sql", "true");

            // 🔑 Fix lỗi: Unknown ConnectionProvider
            props.put("hibernate.connection.provider_class",
                    "org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl");

            // 3️⃣ Khởi tạo EntityManagerFactory
            this.entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, props);
            System.out.println("✅ [JPAService] EntityManagerFactory initialized successfully!");

        } catch (Exception e) {
            System.err.println("❌ [JPAService] Failed to initialize JPA: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize JPA", e);
        }
    }

    /**
     * Singleton instance
     */
    public static synchronized JPAService getInstance() {
        if (instance == null) {
            instance = new JPAService();
        }
        return instance;
    }

    /**
     * Create new EntityManager
     */
    public EntityManager getEntityManager() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            throw new IllegalStateException("EntityManagerFactory is not available");
        }
        return entityManagerFactory.createEntityManager();
    }

    /**
     * Shutdown JPA
     */
    public void shutdown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            System.out.println("🛑 [JPAService] EntityManagerFactory closed.");
        }
    }

    /**
     * Check if JPA ready
     */
    public boolean isReady() {
        return entityManagerFactory != null && entityManagerFactory.isOpen();
    }
}
