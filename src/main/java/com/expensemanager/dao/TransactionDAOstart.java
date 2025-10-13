package com.expensemanager.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.expensemanager.model.Account;
import com.expensemanager.model.Transaction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

/**
 * TransactionDAOstart - Data Access Object cho Transaction entity
 * Dùng static EntityManagerFactory, không dùng JpaUtil
 */
public class TransactionDAOstart {

    // Static EMF, tạo 1 lần cho toàn bộ ứng dụng
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    // Đóng EMF khi app shutdown
    public static void closeFactory() {
        if (emf.isOpen()) {
            emf.close();
        }
    }

    // ======================== SAVE ========================
    public void save(Transaction transaction) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (transaction.getId() == null) {
                em.persist(transaction);   // Thêm mới
            } else {
                em.merge(transaction);     // Cập nhật nếu có ID
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // ======================== DELETE ========================
    public void delete(UUID transactionId) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Transaction transaction = em.find(Transaction.class, transactionId);
            if (transaction != null) {
                if (em.contains(transaction)) {
                    em.remove(transaction);
                } else {
                    em.remove(em.merge(transaction));
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // ======================== FIND ========================
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

    public List<Transaction> getAllTransactionsByMonthAndYear(UUID userId, LocalDateTime startOfMonth, LocalDateTime endOfMonth) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT t FROM Transaction t " +
                    "JOIN FETCH t.category c " +
                    "JOIN FETCH t.account a " +
                    "WHERE a.user.id = :userId " +
                    "AND t.transactionDate >= :startOfMonth " +
                    "AND t.transactionDate < :endOfMonth " +
                    "ORDER BY t.transactionDate DESC";
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
