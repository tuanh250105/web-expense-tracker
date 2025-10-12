package com.expensemanager.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {

    private static final EntityManagerFactory emf;

    static {
        try {
            emf = Persistence.createEntityManagerFactory("BudgetBuddyUnit");
            System.out.println("✅ Đã kết nối Supabase PostgreSQL thành công!");
        } catch (Exception ex) {
            System.err.println("❌ Lỗi khi kết nối Supabase: " + ex.getMessage());
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf.isOpen()) emf.close();
    }
}
