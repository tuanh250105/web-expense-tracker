package com.expensemanager.dao;

import com.expensemanager.model.Account;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public class TransactionDAO {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("default");

    //Dư; Schelduled_Transaction
    public boolean hasTransactionNearDue(UUID categoryId, BigDecimal amount, String type, LocalDateTime start, LocalDateTime end, UUID userId) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT COUNT(t) > 0 FROM Transaction t " +
                    "JOIN t.account a " +
                    "WHERE t.category.id = :categoryId " +
                    "AND t.amount = :amount " +
                    "AND LOWER(t.type) = LOWER(:type) " +
                    "AND t.transactionDate BETWEEN :start AND :end " +
                    "AND a.user.id = :userId";
            return (boolean) em.createQuery(jpql)
                    .setParameter("categoryId", categoryId)
                    .setParameter("amount", amount)
                    .setParameter("type", type)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .setParameter("userId", userId)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }
}