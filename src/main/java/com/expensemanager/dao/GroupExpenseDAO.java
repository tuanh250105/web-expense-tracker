package com.expensemanager.dao;

import com.expensemanager.entity.GroupExpense;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class GroupExpenseDAO {

    @PersistenceContext
    private EntityManager em;

    public List<GroupExpense> findAll() {
        TypedQuery<GroupExpense> query = em.createQuery("SELECT g FROM GroupExpense g", GroupExpense.class);
        return query.getResultList();
    }

    public List<Object[]> getTotalByUser() {
        TypedQuery<Object[]> query = em.createQuery(
                "SELECT g.userId, SUM(g.amount) FROM GroupExpense g GROUP BY g.userId",
                Object[].class
        );
        return query.getResultList();
    }
}
