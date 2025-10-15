package com.expensemanager.dao;

import com.expensemanager.model.Budget;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class BudgetDAO {
    private static final EntityManager em = JpaUtil.getEntityManager();

    public Budget findById(UUID id) {
        try {
            return em.find(Budget.class, id);
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    public List<Budget> getAllByUserId(UUID userId) {
        try {
            String jpql = "SELECT b FROM Budget b WHERE b.user.id = :userId ORDER BY b.startDate DESC";
            return em.createQuery(jpql, Budget.class).setParameter("userId", userId).getResultList();
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    public void addBudget(Budget budget) {

        try {
            em.getTransaction().begin();
            em.persist(budget);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Lỗi khi thêm ngân sách", e);
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    public void updateBudget(Budget budget) {

        try {
            em.getTransaction().begin();
            em.merge(budget);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Lỗi khi cập nhật ngân sách", e);
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    public void deleteBudget(Budget budget) {

        try {
            em.getTransaction().begin();
            Budget managedBudget = em.merge(budget);
            em.remove(managedBudget);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Lỗi khi xóa ngân sách", e);
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    public BigDecimal calculateSpent(UUID budgetId) {
        try {
            String sql = "SELECT COALESCE(SUM(t.amount), 0) " +
                    "FROM transactions t " +
                    "WHERE t.category_id = (SELECT b.category_id FROM budgets b WHERE b.id = :budgetId) " +
                    "AND t.type = 'expense' " +
                    "AND t.transaction_date BETWEEN (SELECT b.start_date FROM budgets b WHERE b.id = :budgetId) " +
                    "AND (SELECT b.end_date FROM budgets b WHERE b.id = :budgetId)";
            return (BigDecimal) em.createNativeQuery(sql).setParameter("budgetId", budgetId).getSingleResult();
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    @SuppressWarnings("unchecked")
    public List<BigDecimal> getDailySpent(UUID budgetId) {
        try {
            String sql = "SELECT COALESCE(SUM(t.amount), 0) FROM transactions t " +
                    "WHERE t.category_id = (SELECT b.category_id FROM budgets b WHERE b.id = :budgetId) " +
                    "AND t.type = 'expense' " +
                    "AND t.transaction_date BETWEEN (SELECT b.start_date FROM budgets b WHERE b.id = :budgetId) " +
                    "AND (SELECT b.end_date FROM budgets b WHERE b.id = :budgetId) " +
                    "GROUP BY t.transaction_date ORDER BY t.transaction_date";
            return em.createNativeQuery(sql).setParameter("budgetId", budgetId).getResultList();
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    @SuppressWarnings("unchecked")
    public List<LocalDate> getDailyDates(UUID budgetId) {

        try {
            String sql = "SELECT t.transaction_date FROM transactions t " +
                    "WHERE t.category_id = (SELECT b.category_id FROM budgets b WHERE b.id = :budgetId) " +
                    "AND t.type = 'expense' " +
                    "AND t.transaction_date BETWEEN (SELECT b.start_date FROM budgets b WHERE b.id = :budgetId) " +
                    "AND (SELECT b.end_date FROM budgets b WHERE b.id = :budgetId) " +
                    "GROUP BY t.transaction_date ORDER BY t.transaction_date";
            return em.createNativeQuery(sql).setParameter("budgetId", budgetId).getResultList();
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    public List<Category> getAllByUserIdForCategories(UUID userId) {

        try {
            String jpql = "SELECT c FROM Category c WHERE c.user.id = :userId AND c.type = 'expense' ORDER BY c.name";
            return em.createQuery(jpql, Category.class).setParameter("userId", userId).getResultList();
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    public List<Budget> getHistoricalBudgets(UUID userId, UUID categoryId) {

        try {
            String jpql = "SELECT b FROM Budget b WHERE b.user.id = :userId AND b.category.id = :categoryId " +
                    "AND b.endDate < CURRENT_DATE ORDER BY b.startDate DESC";
            return em.createQuery(jpql, Budget.class)
                    .setParameter("userId", userId)
                    .setParameter("categoryId", categoryId)
                    .getResultList();
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    // Thêm method mới để lấy list Transaction cho budget mà không sửa logic gốc
    public List<Transaction> getTransactionsForBudget(UUID budgetId) {

        try {
            String jpql = "SELECT t FROM Transaction t WHERE t.category.id = (SELECT b.category.id FROM Budget b WHERE b.id = :budgetId) " +
                    "AND t.type = 'expense' " +
                    "AND t.transactionDate BETWEEN (SELECT b.startDate FROM Budget b WHERE b.id = :budgetId) " +
                    "AND (SELECT b.endDate FROM Budget b WHERE b.id = :budgetId) " +
                    "ORDER BY t.transactionDate DESC";
            return em.createQuery(jpql, Transaction.class)
                    .setParameter("budgetId", budgetId)
                    .getResultList();
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }
}