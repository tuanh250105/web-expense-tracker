package com.expensemanager.dao;

import com.expensemanager.model.Transaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TransactionDAO {

    // Tạo EntityManagerFactory 1 lần cho toàn bộ ứng dụng
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("default"); // Tên persistence-unit trong persistence.xml

    public List<Transaction> getAllTransactionsByMonthAndYear(UUID userId, LocalDateTime startOfMonth, LocalDateTime endOfMonth) {
        EntityManager em = emf.createEntityManager();
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
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT t FROM Account t WHERE t.user.id = :userId";
            return em.createQuery(jpql, Account.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
