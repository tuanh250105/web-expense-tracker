package com.expensemanager.service;

import com.expensemanager.model.Transaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;

public class AnalyticsService {
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("BudgetBuddyUnit");

    public List<Transaction> find(String type, LocalDateTime from, LocalDateTime to) {
        EntityManager em = emf.createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT DISTINCT t FROM Transaction t " +
                            "LEFT JOIN FETCH t.category " +
                            "LEFT JOIN FETCH t.account " +
                            "WHERE 1=1 "
            );

            if (!"all".equalsIgnoreCase(type)) jpql.append("AND t.type = :type ");
            if (from != null) jpql.append("AND t.transactionDate >= :from ");
            if (to != null) jpql.append("AND t.transactionDate <= :to ");

            var query = em.createQuery(jpql.toString(), Transaction.class);
            if (!"all".equalsIgnoreCase(type)) query.setParameter("type", type);
            if (from != null) query.setParameter("from", from);
            if (to != null) query.setParameter("to", to);

            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
