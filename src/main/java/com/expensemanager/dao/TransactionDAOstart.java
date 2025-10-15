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

    private static EntityManager em;
    private static EntityManager em() {return JpaUtil.getEntityManager(); };

    public void save(Transaction transaction) {
        em = em();
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
        em = em();
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
        em = em();
        try {
            return em.find(Transaction.class, transactionId);
        } finally {
            em.close();
        }
    }

    public List<Transaction> findAll() {
        em = em();
        try {
            String jpql = "SELECT t FROM Transaction t ORDER BY t.transactionDate DESC";
            return em.createQuery(jpql, Transaction.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Transaction> findAllByUserId(UUID userId) {
        em = em();
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
        em = em();
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
        em = em();
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
        em = em();
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
        em = em();
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
