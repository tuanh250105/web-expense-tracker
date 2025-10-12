package com.expensemanager.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.expensemanager.model.Category;
import com.expensemanager.util.JpaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

/**
 * CategoryDAO - Data Access Object for Category entity
 * Uses manual EntityManager management
 */
public class CategoryDAO {

    public void save(Category category) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (category.getId() == null) {
                em.persist(category);
            } else {
                em.merge(category);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Category> findById(UUID id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Category category = em.find(Category.class, id);
            return Optional.ofNullable(category);
        } finally {
            em.close();
        }
    }
    
    public List<Category> findAllByUser(UUID userId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT c FROM Category c WHERE c.user.id = :userId ORDER BY c.name ASC";
            TypedQuery<Category> query = em.createQuery(jpql, Category.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public void update(Category category) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(category);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Category> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT c FROM Category c ORDER BY c.name ASC";
            return em.createQuery(jpql, Category.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Category> findByType(String type) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT c FROM Category c WHERE c.type = :type ORDER BY c.name ASC";
            TypedQuery<Category> query = em.createQuery(jpql, Category.class);
            query.setParameter("type", type);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Category> findMainCategories() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.name ASC";
            return em.createQuery(jpql, Category.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Category> findSubCategoriesByParentId(UUID parentId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT c FROM Category c WHERE c.parent.id = :parentId ORDER BY c.name ASC";
            TypedQuery<Category> query = em.createQuery(jpql, Category.class);
            query.setParameter("parentId", parentId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Category> findByName(String name) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(:name) ORDER BY c.name ASC";
            TypedQuery<Category> query = em.createQuery(jpql, Category.class);
            query.setParameter("name", "%" + name + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Category> findByUserIdAndName(UUID userId, String name) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT c FROM Category c WHERE c.user.id = :userId AND LOWER(c.name) LIKE LOWER(:name) ORDER BY c.name ASC";
            TypedQuery<Category> query = em.createQuery(jpql, Category.class);
            query.setParameter("userId", userId);
            query.setParameter("name", "%" + name + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Category> findByUserIdAndExactName(UUID userId, String name) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT c FROM Category c WHERE c.user.id = :userId AND c.name = :name";
            TypedQuery<Category> query = em.createQuery(jpql, Category.class);
            query.setParameter("userId", userId);
            query.setParameter("name", name);
            
            List<Category> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    public void delete(Category category) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (em.contains(category)) {
                em.remove(category);
            } else {
                em.remove(em.merge(category));
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
    
    public void delete(UUID id) {
        Optional<Category> category = findById(id);
        if (category.isPresent()) {
            delete(category.get());
        }
    }

    public void deleteById(UUID id) {
        Optional<Category> category = findById(id);
        if (category.isPresent()) {
            delete(category.get());
        }
    }

    public void softDelete(UUID id) {
        Optional<Category> categoryOpt = findById(id);
        if (categoryOpt.isPresent()) {
            delete(categoryOpt.get());
        }
    }

    public long countByUserId(UUID userId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT COUNT(c) FROM Category c WHERE c.user.id = :userId";
            return em.createQuery(jpql, Long.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    public long countByUserIdAndType(UUID userId, String type) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT COUNT(c) FROM Category c WHERE c.user.id = :userId AND c.type = :type";
            return em.createQuery(jpql, Long.class)
                    .setParameter("userId", userId)
                    .setParameter("type", type)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    public List<Category> findTopCategoriesByUsage(UUID userId, int limit) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT c, COUNT(t) as usage_count " +
                         "FROM Category c " +
                         "LEFT JOIN Transaction t ON c.id = t.category.id AND t.account.user.id = :userId " +
                         "WHERE c.user.id = :userId " +
                         "GROUP BY c.id " +
                         "ORDER BY usage_count DESC";
            
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            query.setParameter("userId", userId);
            query.setMaxResults(limit);
            
            List<Object[]> results = query.getResultList();
            return results.stream()
                    .map(result -> (Category) result[0])
                    .toList();
        } finally {
            em.close();
        }
    }
}
