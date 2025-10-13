package com.expensemanager.dao;

import com.expensemanager.model.FAQ;
import com.expensemanager.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.List;
import java.util.UUID;

public class FAQRepository {

    public List<FAQ> getAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Query query = em.createQuery("SELECT f FROM FAQ f ORDER BY f.createdAt ASC", FAQ.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void add(FAQ faq) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(faq);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Lỗi khi thêm FAQ", e);
        } finally {
            em.close();
        }
    }

    public void delete(UUID id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            FAQ faq = em.find(FAQ.class, id);
            if (faq != null) {
                em.remove(faq);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Lỗi khi xóa FAQ", e);
        } finally {
            em.close();
        }
    }

    public FAQ getById(UUID id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(FAQ.class, id);
        } finally {
            em.close();
        }
    }
}
