package com.expensemanager.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * JpaUtil - tiện ích quản lý EntityManagerFactory duy nhất cho toàn app.
 * Giúp mở / đóng kết nối JPA (Hibernate + PostgreSQL Supabase).
 */
public class JpaUtil {
    // Singleton EntityManagerFactory
    private static final EntityManagerFactory emf = buildFactory();

    /**
     * Khởi tạo EntityManagerFactory từ persistence.xml
     */
    private static EntityManagerFactory buildFactory() {
        try {
            return Persistence.createEntityManagerFactory("BudgetBuddyUnit"); // ✅ đúng tên PU
        } catch (Exception e) {
            System.err.println(" Error building EntityManagerFactory: " + e.getMessage());
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Lấy EntityManager mới (mỗi request nên dùng riêng)
     */
    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /**
     * Đóng toàn bộ EntityManagerFactory khi ứng dụng dừng
     */
    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            try {
                emf.close();
                System.out.println("✅ EntityManagerFactory closed successfully.");
            } catch (Exception e) {
                System.err.println("⚠️ Error closing EntityManagerFactory: " + e.getMessage());
            }
        }
    }
}
