package com.expensemanager.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * JPA Manager Service
 * Singleton class để quản lý EntityManagerFactory và EntityManager
 */
public class JPAService {

    private static final String PERSISTENCE_UNIT = "BudgetBuddyUnit";
    private static JPAService instance;
    private EntityManagerFactory entityManagerFactory;

    private JPAService() {
        try {
            this.entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
        } catch (Exception e) {
            System.err.println("Failed to create EntityManagerFactory: " + e.getMessage());
            throw new RuntimeException("Failed to initialize JPA", e);
        }
    }

    /**
     * Get singleton instance
     */
    public static synchronized JPAService getInstance() {
        if (instance == null) {
            instance = new JPAService();
        }
        return instance;
    }

    /**
     * Get new EntityManager
     */
    public EntityManager getEntityManager() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            throw new IllegalStateException("EntityManagerFactory is not available");
        }
        return entityManagerFactory.createEntityManager();
    }

    /**
     * Close EntityManagerFactory
     */
    public void shutdown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    /**
     * Check if JPA is ready
     */
    public boolean isReady() {
        return entityManagerFactory != null && entityManagerFactory.isOpen();
    }
}