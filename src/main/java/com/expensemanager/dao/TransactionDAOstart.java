package com.expensemanager.dao;

import com.expensemanager.model.Account;
import com.expensemanager.model.Transaction;
import com.expensemanager.util.JpaUtil;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * TransactionDAOstart - Data Access Object cho Transaction entity
 * Đồng bộ với persistence.xml (persistence-unit name="default")
 * Dùng static EntityManagerFactory khởi tạo 1 lần cho toàn app
 */
public class TransactionDAOstart {

    private static EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();

    static {
        EntityManagerFactory tmp = null;
        try {
            System.out.println("🚀 Initializing EntityManagerFactory...");

            // Programmatically read environment variables
            Map<String, String> properties = new HashMap<>();
            String dbUrl = System.getenv("DB_URL");
            String dbUser = System.getenv("DB_USER");
            String dbPass = System.getenv("DB_PASS");

            if (dbUrl == null || dbUser == null || dbPass == null) {
                throw new IllegalStateException("Database environment variables (DB_URL, DB_USER, DB_PASS) are not set.");
            }

            properties.put("jakarta.persistence.jdbc.url", dbUrl);
            properties.put("jakarta.persistence.jdbc.user", dbUser);
            properties.put("jakarta.persistence.jdbc.password", dbPass);

            tmp = JpaUtil.getEntityManagerFactory();
            System.out.println("✅ EntityManagerFactory initialized successfully!");
        } catch (Exception e) {
            System.err.println("❌ Critical Error initializing EntityManagerFactory:");
            e.printStackTrace();
            // Re-throw the exception to make the root cause visible
            throw new ExceptionInInitializerError(e);
        }
        emf = tmp;
    }

    private EntityManager getEntityManager() {
        if (emf == null) {
            // This should ideally not be reached if ExceptionInInitializerError is thrown above
            throw new IllegalStateException("EntityManagerFactory is not initialized. Check server logs for root cause.");
        }
        return emf.createEntityManager();
    }

    public static void closeFactory() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    public void save(Transaction transaction) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (transaction.getId() == null) {
                em.persist(transaction);
            } else {
                em.merge(transaction);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void delete(UUID transactionId) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Transaction transaction = em.find(Transaction.class, transactionId);
            if (transaction != null) {
                em.remove(em.contains(transaction) ? transaction : em.merge(transaction));
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public Transaction findById(UUID transactionId) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Transaction.class, transactionId);
        } finally {
            em.close();
        }
    }

    public List<Transaction> findAll() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT t FROM Transaction t ORDER BY t.transactionDate DESC";
            return em.createQuery(jpql, Transaction.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Transaction> findAllByUserId(UUID userId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT t FROM Transaction t JOIN t.account a WHERE a.user.id = :userId ORDER BY t.transactionDate DESC";
            TypedQuery<Transaction> query = em.createQuery(jpql, Transaction.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Transaction> findByAccountId(UUID accountId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT t FROM Transaction t WHERE t.account.id = :accountId ORDER BY t.transactionDate DESC";
            TypedQuery<Transaction> query = em.createQuery(jpql, Transaction.class);
            query.setParameter("accountId", accountId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Transaction> findByAccountIdAndUserId(UUID userId, UUID accountId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT t FROM Transaction t WHERE t.account.user.id = :userId AND t.account.id = :accountId ORDER BY t.transactionDate DESC";
            TypedQuery<Transaction> query = em.createQuery(jpql, Transaction.class);
            query.setParameter("userId", userId);
            query.setParameter("accountId", accountId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Transaction> getAllTransactionsByMonthAndYear(UUID userId,
                                                              LocalDateTime startOfMonth,
                                                              LocalDateTime endOfMonth) {
        EntityManager em = getEntityManager();
        try {
            String jpql = """
                    SELECT t FROM Transaction t
                    JOIN FETCH t.category c
                    JOIN FETCH t.account a
                    WHERE a.user.id = :userId
                    AND t.transactionDate >= :startOfMonth
                    AND t.transactionDate < :endOfMonth
                    ORDER BY t.transactionDate DESC
                    """;
            TypedQuery<Transaction> query = em.createQuery(jpql, Transaction.class);
            query.setParameter("userId", userId);
            query.setParameter("startOfMonth", startOfMonth);
            query.setParameter("endOfMonth", endOfMonth);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Account> getAllAccountByUserId(UUID userId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT a FROM Account a WHERE a.user.id = :userId";
            TypedQuery<Account> query = em.createQuery(jpql, Account.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
