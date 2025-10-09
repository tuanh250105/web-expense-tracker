package com.expensemanager.dao;

import com.expensemanager.model.Transaction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;


public class TransactionDAO {
    @PersistenceContext
    private EntityManager em;

    @Transactional
    public List<Transaction> getAllTransactionsByMonthAndYear(int userId, int month, int year) {
        String jpql = "SELECT t FROM Transaction t " +
                "JOIN FETCH t.category c " +
                "JOIN FETCH t.account a " +
                "WHERE a.user.id = :userId " +
                "AND FUNCTION('YEAR', t.transactionDate) = :year " +
                "AND FUNCTION('MONTH', t.transactionDate) = :month " +
                "ORDER BY t.transactionDate DESC";

        return em.createQuery(jpql, Transaction.class)
                .setParameter("userId", userId)
                .setParameter("year", year)
                .setParameter("month", month)
                .getResultList();
    }
}


