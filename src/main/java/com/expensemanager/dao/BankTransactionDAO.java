package com.expensemanager.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.expensemanager.model.Transaction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

/**
 * BankTransactionDAO - Data Access Object for bank transactions
 * Uses Transaction entity which maps to transactions table in Supabase
 */
public class BankTransactionDAO {
    
    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void save(Transaction transaction) {
        if (transaction.getId() == null) {
            em.persist(transaction);
        } else {
            em.merge(transaction);
        }
    }

    @Transactional
    public Transaction findById(UUID id) {
        return em.find(Transaction.class, id);
    }

    @Transactional
    public List<Transaction> findByAccountId(UUID accountId) {
        String jpql = "SELECT t FROM Transaction t " +
                     "WHERE t.accountId = :accountId " +
                     "ORDER BY t.transactionDate DESC";
        
        return em.createQuery(jpql, Transaction.class)
                .setParameter("accountId", accountId)
                .getResultList();
    }

    @Transactional
    public List<Transaction> findByAccountIdAndType(UUID accountId, String type) {
        String jpql = "SELECT t FROM Transaction t " +
                     "WHERE t.accountId = :accountId " +
                     "AND t.type = :type " +
                     "ORDER BY t.transactionDate DESC";
        
        return em.createQuery(jpql, Transaction.class)
                .setParameter("accountId", accountId)
                .setParameter("type", type)
                .getResultList();
    }

    @Transactional
    public void delete(Transaction transaction) {
        if (em.contains(transaction)) {
            em.remove(transaction);
        } else {
            em.remove(em.merge(transaction));
        }
    }

    @Transactional
    public void deleteById(UUID id) {
        Transaction transaction = findById(id);
        if (transaction != null) {
            delete(transaction);
        }
    }
}
