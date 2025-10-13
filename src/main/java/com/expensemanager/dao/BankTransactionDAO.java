package com.expensemanager.dao;

import java.util.List;
import java.util.UUID;

import com.expensemanager.model.Transaction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

/**
 * BankTransactionDAO - Data Access Object cho Transaction entity
 * Dùng static EntityManagerFactory, không dùng JpaUtil hay @PersistenceContext
 */
public class BankTransactionDAO {

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

    // ======================== FIND ========================
    public Transaction findById(UUID id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Transaction.class, id);
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
            String jpql = "SELECT t FROM Transaction t " +
                    "WHERE t.account.id = :accountId " +
                    "ORDER BY t.transactionDate DESC";
            TypedQuery<Transaction> query = em.createQuery(jpql, Transaction.class);
            query.setParameter("accountId", accountId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Transaction> findByAccountIdAndType(UUID accountId, String type) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT t FROM Transaction t " +
                    "WHERE t.account.id = :accountId AND t.type = :type " +
                    "ORDER BY t.transactionDate DESC";
            TypedQuery<Transaction> query = em.createQuery(jpql, Transaction.class);
            query.setParameter("accountId", accountId);
            query.setParameter("type", type);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // ======================== DELETE ========================
    public void delete(Transaction transaction) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (em.contains(transaction)) {
                em.remove(transaction);
            } else {
                em.remove(em.merge(transaction));
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteById(UUID id) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Transaction transaction = em.find(Transaction.class, id);
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
}
