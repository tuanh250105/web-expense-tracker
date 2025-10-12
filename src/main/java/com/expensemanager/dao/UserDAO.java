package com.expensemanager.dao;

import com.expensemanager.model.User;
import com.expensemanager.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.UUID;

public class UserDAO {

    public List<User> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<User> q = em.createQuery("SELECT u FROM User u ORDER BY u.username", User.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public User findById(UUID id) {
        if (id == null) return null;
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }
}
