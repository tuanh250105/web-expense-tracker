package com.expensemanager.dao;

import java.util.List;
import java.util.UUID;

import com.expensemanager.model.Account;
import com.expensemanager.model.User;
import com.expensemanager.util.JpaUtil; // ✅ thêm import này

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

/**
 * AccountDAO - Data Access Object cho Account entity
 * ✅ Dùng EntityManagerFactory từ JpaUtil để tránh lỗi JDBC connection
 */
public class AccountDAO {

    // ✅ Dùng chung EntityManagerFactory từ JpaUtil
    private static final EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    // ======================== SAVE ========================
    public void save(Account account) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // Nếu account có user nhưng chưa managed, fetch từ DB
            if (account.getUser() != null && account.getUser().getId() != null && !em.contains(account.getUser())) {
                User managedUser = em.find(User.class, account.getUser().getId());
                if (managedUser != null) {
                    account.setUser(managedUser);
                } else {
                    throw new IllegalArgumentException("User not found with ID: " + account.getUser().getId());
                }
            }

            if (account.getId() == null) {
                em.persist(account);  // thêm mới
            } else {
                em.merge(account);    // cập nhật nếu có ID
            }

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu Account: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    // ======================== FIND ========================
    public Account findById(UUID id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Account.class, id);
        } finally {
            em.close();
        }
    }

    public List<Account> findAllByUser(UUID userId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT a FROM Account a WHERE a.user.id = :userId ORDER BY a.name ASC";
            TypedQuery<Account> query = em.createQuery(jpql, Account.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Account> findAll() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT a FROM Account a ORDER BY a.name ASC";
            return em.createQuery(jpql, Account.class).getResultList();
        } finally {
            em.close();
        }
    }

    // ======================== UPDATE ========================
    public void update(Account account) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(account);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi cập nhật Account: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    // ======================== DELETE ========================
    public void delete(UUID id) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Account account = em.find(Account.class, id);
            if (account != null) {
                em.remove(account);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // ======================== STATISTICS ========================
    public java.math.BigDecimal getTotalBalanceByUser(UUID userId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.user.id = :userId";
            return em.createQuery(jpql, java.math.BigDecimal.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }
}
