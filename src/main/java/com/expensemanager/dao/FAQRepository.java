package com.expensemanager.dao;

import com.expensemanager.model.FAQ;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

import java.util.List;
import java.util.UUID;

public class FAQRepository {
    private static EntityManagerFactory emf;  // Không init static nữa, lazy

    // Method private để lấy emf an toàn (thread-safe)
    private EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            synchronized (FAQRepository.class) {  // Đảm bảo thread-safe
                if (emf == null) {
                    try {
                        emf = Persistence.createEntityManagerFactory("default");
                        System.out.println("lazy init EntityManagerFactory thành công");
                    } catch (Exception e) {
                        System.err.println("Lỗi lazy init EMF " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException("JPA lazy init thất bại", e);
                    }
                }
            }
        }
        return emf;
    }

    // Sử dụng getEntityManager() trong tất cả method
    private EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public List<FAQ> getAll() {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createQuery("SELECT f FROM FAQ f ORDER BY f.createdAt ASC");
            return query.getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public void add(FAQ faq) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(faq);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Lỗi add FAQ", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public void delete(UUID id) {
        EntityManager em = getEntityManager();
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
            throw new RuntimeException("Lỗi delete FAQ", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}