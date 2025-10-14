package com.expensemanager.dao;

import com.expensemanager.model.Budget;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BudgetDAO {
    private static final EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();

    public Budget findById(UUID id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Budget.class, id);
        } finally {
            em.close();
        }
    }

    public List<Budget> getAllByUserId(UUID userId) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT b FROM Budget b WHERE b.user.id = :userId ORDER BY b.startDate DESC";
            return em.createQuery(jpql, Budget.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void addBudget(Budget budget) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(budget);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Lỗi khi thêm ngân sách", e);
        } finally {
            em.close();
        }
    }

    public void updateBudget(Budget budget) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(budget);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Lỗi khi cập nhật ngân sách", e);
        } finally {
            em.close();
        }
    }

    public void deleteBudget(Budget budget) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Budget managedBudget = em.merge(budget);
            em.remove(managedBudget);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Lỗi khi xóa ngân sách", e);
        } finally {
            em.close();
        }
    }

    public BigDecimal calculateSpent(UUID budgetId) {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT COALESCE(SUM(t.amount), 0) " +
                    "FROM transactions t " +
                    "WHERE t.category_id = (SELECT b.category_id FROM budgets b WHERE b.id = ?1) " +
                    "AND t.type = 'expense' " +
                    "AND t.transaction_date BETWEEN (SELECT b.start_date FROM budgets b WHERE b.id = ?1) " +
                    "AND (SELECT b.end_date FROM budgets b WHERE b.id = ?1)";

            Object result = em.createNativeQuery(sql)
                    .setParameter(1, budgetId)
                    .getSingleResult();

            if (result instanceof BigDecimal) {
                return (BigDecimal) result;
            } else if (result instanceof Number) {
                return new BigDecimal(result.toString());
            }
            return BigDecimal.ZERO;
        } catch (NoResultException e) {
            return BigDecimal.ZERO;
        } finally {
            em.close();
        }
    }

    public List<Budget> getHistoricalBudgets(UUID userId, UUID categoryId) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT b FROM Budget b WHERE b.user.id = :userId AND b.category.id = :categoryId " +
                    "AND b.endDate < CURRENT_DATE ORDER BY b.endDate DESC";
            return em.createQuery(jpql, Budget.class)
                    .setParameter("userId", userId)
                    .setParameter("categoryId", categoryId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Transaction> getTransactionsForBudget(UUID budgetId) {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT t.* FROM transactions t " +
                    "WHERE t.category_id = (SELECT b.category_id FROM budgets b WHERE b.id = ?1) " +
                    "AND t.type = 'expense' " +
                    "AND t.transaction_date BETWEEN (SELECT b.start_date FROM budgets b WHERE b.id = ?1) " +
                    "AND (SELECT b.end_date FROM budgets b WHERE b.id = ?1)";

            @SuppressWarnings("unchecked")
            List<Transaction> transactions = em.createNativeQuery(sql, Transaction.class)
                    .setParameter(1, budgetId)
                    .getResultList();
            return transactions;
        } finally {
            em.close();
        }
    }

    public List<BigDecimal> getDailySpent(UUID budgetId) {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT COALESCE(SUM(t.amount), 0) FROM transactions t " +
                    "WHERE t.category_id = (SELECT b.category_id FROM budgets b WHERE b.id = ?1) " +
                    "AND t.type = 'expense' " +
                    "AND t.transaction_date BETWEEN (SELECT b.start_date FROM budgets b WHERE b.id = ?1) " +
                    "AND (SELECT b.end_date FROM budgets b WHERE b.id = ?1) " +
                    "GROUP BY t.transaction_date ORDER BY t.transaction_date";

            @SuppressWarnings("unchecked")
            List<BigDecimal> dailyAmounts = em.createNativeQuery(sql)
                    .setParameter(1, budgetId)
                    .getResultList();
            return dailyAmounts != null ? dailyAmounts : new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public List<LocalDate> getDailyDates(UUID budgetId) {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT t.transaction_date FROM transactions t " +
                    "WHERE t.category_id = (SELECT b.category_id FROM budgets b WHERE b.id = ?1) " +
                    "AND t.type = 'expense' " +
                    "AND t.transaction_date BETWEEN (SELECT b.start_date FROM budgets b WHERE b.id = ?1) " +
                    "AND (SELECT b.end_date FROM budgets b WHERE b.id = ?1) " +
                    "GROUP BY t.transaction_date ORDER BY t.transaction_date";

            @SuppressWarnings("unchecked")
            List<LocalDate> dates = em.createNativeQuery(sql)
                    .setParameter(1, budgetId)
                    .getResultList();
            return dates != null ? dates : new ArrayList<>();
        } finally {
            em.close();
        }
    }

    // Thêm phương thức để lấy danh mục thuộc người dùng
    public List<Category> getAllByUserIdForCategories(UUID userId) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT c FROM Category c WHERE c.user.id = :userId AND c.type = 'expense' ORDER BY c.name";
            return em.createQuery(jpql, Category.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}