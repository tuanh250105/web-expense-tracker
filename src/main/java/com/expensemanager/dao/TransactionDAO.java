package com.expensemanager.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.expensemanager.model.Account;
import com.expensemanager.model.Transaction;
import com.expensemanager.util.JpaUtil;

import jakarta.persistence.EntityManager;

public class TransactionDAO {

    public List<Transaction> getAllTransactionsByMonthAndYear(UUID userId, LocalDateTime startOfMonth, LocalDateTime endOfMonth) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT t FROM Transaction t " +
                    "JOIN FETCH t.category c " +
                    "JOIN FETCH t.account a " +
                    "WHERE a.user.id = :userId " +
                    "AND t.transactionDate >= :startOfMonth " +
                    "AND t.transactionDate < :endOfMonth " +
                    "ORDER BY t.transactionDate DESC";

            return em.createQuery(jpql, Transaction.class)
                    .setParameter("userId", userId)
                    .setParameter("startOfMonth", startOfMonth)
                    .setParameter("endOfMonth", endOfMonth)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Account> getAllAccountByUserId(UUID userId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT t FROM Account t WHERE t.user.id = :userId";
            return em.createQuery(jpql, Account.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
    
    public void save(Transaction transaction) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (transaction.getId() == null) {
                em.persist(transaction);
            } else {
                em.merge(transaction);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
    
    public void delete(UUID transactionId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Transaction transaction = em.find(Transaction.class, transactionId);
            if (transaction != null) {
                em.remove(transaction);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
    
    public List<Transaction> findByAccountId(UUID accountId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT t FROM Transaction t WHERE t.account.id = :accountId ORDER BY t.transactionDate DESC";
            return em.createQuery(jpql, Transaction.class)
                    .setParameter("accountId", accountId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Transaction> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT t FROM Transaction t ORDER BY t.transactionDate DESC";
            return em.createQuery(jpql, Transaction.class).getResultList();
        } finally {
            em.close();
        }
    }
}
