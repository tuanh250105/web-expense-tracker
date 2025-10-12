package com.expensemanager.dao;

import com.expensemanager.model.Transaction;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TransactionDAO {
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("BudgetBuddyUnit");

    // Lấy danh sách giao dịch theo user, loại và khoảng thời gian
    public List<Transaction> find(UUID userId, String type, LocalDateTime from, LocalDateTime to) {
        EntityManager em = emf.createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT DISTINCT t FROM Transaction t " +
                            "LEFT JOIN FETCH t.category " +
                            "LEFT JOIN FETCH t.account a " +
                            "WHERE a.user.id = :uid "
            );

            if (!"all".equalsIgnoreCase(type)) jpql.append("AND t.type = :type ");
            if (from != null) jpql.append("AND t.transactionDate >= :from ");
            if (to != null) jpql.append("AND t.transactionDate <= :to ");
            jpql.append("ORDER BY t.transactionDate ASC");

            var q = em.createQuery(jpql.toString(), Transaction.class);
            q.setParameter("uid", userId);
            if (!"all".equalsIgnoreCase(type)) q.setParameter("type", type);
            if (from != null) q.setParameter("from", from);
            if (to != null) q.setParameter("to", to);

            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
