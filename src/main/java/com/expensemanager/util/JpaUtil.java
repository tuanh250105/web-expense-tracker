package com.expensemanager.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {
    private static EntityManagerFactory entityManagerFactory;
    
    static {
        try {
            // Sử dụng persistence unit name đúng từ persistence.xml
            entityManagerFactory = Persistence.createEntityManagerFactory("BudgetBuddyUnit");
            System.out.println("✅ JpaUtil: EntityManagerFactory initialized successfully!");
        } catch (Exception e) {
            System.err.println("❌ JpaUtil: Initial EntityManagerFactory creation failed: " + e);
            throw new ExceptionInInitializerError(e);
        }
    }
    
    public static EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }
    
    public static EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }
    
    public static void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            System.out.println("✅ JpaUtil: EntityManagerFactory closed!");
        }
    }
}