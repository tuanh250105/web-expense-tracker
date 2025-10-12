package com.expensemanager.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;


public class JpaUtil {
    // Singleton EntityManagerFactory
    private static final EntityManagerFactory emf = buildFactory();


    private static EntityManagerFactory buildFactory() {
        try {
            return Persistence.createEntityManagerFactory("BudgetBuddyUnit");
        } catch (Exception e) {
            System.err.println(" Error building EntityManagerFactory: " + e.getMessage());
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }


    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            try {
                emf.close();
                System.out.println(" EntityManagerFactory closed successfully.");
            } catch (Exception e) {
                System.err.println("️ Error closing EntityManagerFactory: " + e.getMessage());
            }
        }
    }
}
