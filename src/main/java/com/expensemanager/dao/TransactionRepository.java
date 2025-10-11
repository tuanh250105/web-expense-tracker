package com.expensemanager.dao;

import com.expensemanager.model.Transaction;
import com.expensemanager.shared.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.OffsetDateTime;
import java.util.List;

public class TransactionRepository {

    public List<Transaction> find(String type, java.time.LocalDateTime from, java.time.LocalDateTime to) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder sb = new StringBuilder("SELECT t FROM Transaction t WHERE 1=1 ");
            if (!"all".equalsIgnoreCase(type)) {
                sb.append("AND t.type = :type ");
            }
            if (from != null) {
                sb.append("AND t.transactionDate >= :from ");
            }
            if (to != null) {
                sb.append("AND t.transactionDate <= :to ");
            }

            TypedQuery<Transaction> q = em.createQuery(sb.toString(), Transaction.class);
            if (!"all".equalsIgnoreCase(type)) q.setParameter("type", type);
            if (from != null) q.setParameter("from", from);
            if (to != null) q.setParameter("to", to);

            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
