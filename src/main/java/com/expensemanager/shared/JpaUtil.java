package com.expensemanager.shared;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {
    private static final EntityManagerFactory emf = buildFactory();

    private static EntityManagerFactory buildFactory() {
        try {
            return Persistence.createEntityManagerFactory("BudgetBuddyUnit");
        } catch (Exception e) {
            System.err.println("❌ Error building EntityManagerFactory: " + e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
