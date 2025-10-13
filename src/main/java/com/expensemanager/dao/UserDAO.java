package com.expensemanager.dao;

import com.expensemanager.model.User;
import com.expensemanager.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.UUID;

public class UserDAO {

    private final EntityManager em;

    // Constructor nhận EntityManager
    public UserDAO(EntityManager em) {
        this.em = em;
    }

    // Constructor mặc định (sử dụng JpaUtil)
    public UserDAO() {
        this.em = null;
    }

    private EntityManager getEm() {
        return (em != null) ? em : JpaUtil.getEntityManager();
    }

    private void closeEm(EntityManager e) {
        if (em == null) e.close(); // chỉ close nếu là em do DAO tạo
    }

    public User findById(UUID id) {
        if (id == null) return null;
        EntityManager e = getEm();
        try {
            return e.find(User.class, id);
        } finally {
            closeEm(e);
        }
    }

    public List<User> findAll() {
        EntityManager e = getEm();
        try {
            TypedQuery<User> q = e.createQuery("SELECT u FROM User u ORDER BY u.username", User.class);
            return q.getResultList();
        } finally {
            closeEm(e);
        }
    }

    public List<User> searchByName(String name, UUID currentUserIdToExclude) {
        EntityManager e = getEm();
        try {
            String jpql = "SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(:name) AND u.id != :currentUserId";
            TypedQuery<User> query = e.createQuery(jpql, User.class);
            query.setParameter("name", "%" + name + "%");
            query.setParameter("currentUserId", currentUserIdToExclude);
            query.setMaxResults(5);
            return query.getResultList();
        } finally {
            closeEm(e);
        }
    }
}
