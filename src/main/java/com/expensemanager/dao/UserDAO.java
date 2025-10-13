package com.expensemanager.dao;
import com.expensemanager.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;

public class UserDAO {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");
    private final EntityManager em;

    public UserDAO(EntityManager em) {
        this.em = em;
    }

    public User findById(UUID id) {
        return em.find(User.class, id);
    }

    public List<User> searchByName(String name, UUID currentUserIdToExclude) {
        String jpql = "SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(:name) AND u.id != :currentUserId";
        TypedQuery<User> query = em.createQuery(jpql, User.class);
        query.setParameter("name", "%" + name + "%");
        query.setParameter("currentUserId", currentUserIdToExclude);
        query.setMaxResults(5);
        return query.getResultList();
    }

    public List<User> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<User> q = em.createQuery("SELECT u FROM User u ORDER BY u.username", User.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
