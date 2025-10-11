package com.expensemanager.dao;

import com.expensemanager.model.Transaction;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

public class TransactionRepository {

    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory("BudgetBuddyUnit");

    private EntityManager em() { return EMF.createEntityManager(); }

    /** type: "all" | "income" | "expense"; from/to: OffsetDateTime có thể null */
    public List<Transaction> find(String type, OffsetDateTime from, OffsetDateTime to) {
        LocalDateTime f = (from == null) ? LocalDateTime.of(1000, 1, 1, 0, 0) : from.toLocalDateTime();
        LocalDateTime t = (to == null) ? LocalDateTime.of(3000, 12, 31, 23, 59) : to.toLocalDateTime();

        EntityManager em = em();
        try {
            String jpql = """
            SELECT t FROM Transaction t
            WHERE t.transactionDate BETWEEN :f AND :t
              AND (:k = 'all' OR LOWER(t.type) = :k)
            ORDER BY t.transactionDate ASC
        """;
            return em.createQuery(jpql, Transaction.class)
                    .setParameter("f", f)
                    .setParameter("t", t)
                    .setParameter("k", type == null ? "all" : type.toLowerCase())
                    .getResultList();
        } finally {
            em.close();
        }
    }

}
