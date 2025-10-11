package com.expensemanager.dao;

import com.expensemanager.model.Category;
import com.expensemanager.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.UUID;

public class CategoryDAO {

    public List<Category> findAllByUser(UUID userId) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        String jpql = "SELECT c FROM Category c " +
                "JOIN FETCH c.user u " +
                "WHERE u.id = :userId " +
                "ORDER BY c.createdAt DESC";
        List<Category> list = em.createQuery(jpql, Category.class)
                .setParameter("userId", userId)
                .getResultList();
        em.close();
        return list;
    }

    public Category findById(UUID id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        Category c = em.find(Category.class, id);
        em.close();
        return c;
    }

    public void save(Category category) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(category);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void update(Category category) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(category);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void delete(UUID id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Category c = em.find(Category.class, id);
            if (c != null) em.remove(c);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
