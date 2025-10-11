package com.expensemanager.dao;

import com.expensemanager.model.Account;
import com.expensemanager.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.UUID;

public class AccountDAO {

    private static final EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();

    public List<Account> getAllAccountsByUser(UUID userId) {
        EntityManager em = emf.createEntityManager();
        try {
            // Cập nhật câu truy vấn để lọc theo user_id
            return em.createQuery("SELECT a FROM Account a WHERE a.user.id = :userId", Account.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Account findById(UUID id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Account.class, id);
        } finally {
            em.close();
        }
    }

    public void saveOrUpdate(Account account) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        try {
            if (account.getId() == null) {
                em.persist(account);
            } else {
                em.merge(account);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Lỗi khi lưu account: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void delete(UUID id, UUID userId) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        try {
            Account account = em.find(Account.class, id);
            // Chỉ xóa nếu tài khoản tồn tại và thuộc về đúng người dùng
            if (account != null && account.getUser() != null && userId.equals(account.getUser().getId())) {
                em.remove(account);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Lỗi khi xóa account: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
