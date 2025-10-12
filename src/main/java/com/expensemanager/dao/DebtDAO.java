package com.expensemanager.dao;

import com.expensemanager.model.Debt;
import com.expensemanager.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class DebtDAO {

    public List<Debt> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Debt> q = em.createQuery("SELECT d FROM Debt d ORDER BY d.dueDate NULLS LAST", Debt.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Debt> findAllByUser(UUID userId) {
        if (userId == null) return findAll();
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Debt> q = em.createQuery("SELECT d FROM Debt d WHERE d.userId = :uid ORDER BY d.dueDate NULLS LAST", Debt.class);
            q.setParameter("uid", userId);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Debt findById(UUID id) {
        if (id == null) return null;
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Debt.class, id);
        } finally {
            em.close();
        }
    }

    public Debt saveOrUpdate(Debt debt) {
        if (debt == null) return null;
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (debt.getId() == null) {
                em.persist(debt);
            } else {
                debt = em.merge(debt);
            }
            em.getTransaction().commit();
            return debt;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public boolean delete(UUID id) {
        if (id == null) return false;
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Debt d = em.find(Debt.class, id);
            if (d != null) em.remove(d);
            em.getTransaction().commit();
            return d != null;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public boolean markAsPaid(UUID id) {
        if (id == null) return false;
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Debt d = em.find(Debt.class, id);
            if (d == null) {
                em.getTransaction().commit();
                return false;
            }
            d.setStatus(Debt.STATUS_PAID);
            em.merge(d);
            em.getTransaction().commit();
            return true;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public List<Debt> findOverdue() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Debt> q = em.createQuery(
                    "SELECT d FROM Debt d WHERE d.dueDate < :today AND (d.status IS NULL OR d.status <> :paid) ORDER BY d.dueDate",
                    Debt.class);
            q.setParameter("today", LocalDate.now());
            q.setParameter("paid", Debt.STATUS_PAID);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Debt> findOverdueByUser(UUID userId) {
        if (userId == null) return findOverdue();
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Debt> q = em.createQuery(
                    "SELECT d FROM Debt d WHERE d.userId = :uid AND d.dueDate < :today AND (d.status IS NULL OR d.status <> :paid) ORDER BY d.dueDate",
                    Debt.class);
            q.setParameter("uid", userId);
            q.setParameter("today", LocalDate.now());
            q.setParameter("paid", Debt.STATUS_PAID);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Debt> findNearDue(int daysThreshold) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            LocalDate today = LocalDate.now();
            LocalDate max = today.plusDays(daysThreshold);
            TypedQuery<Debt> q = em.createQuery(
                    "SELECT d FROM Debt d WHERE d.dueDate IS NOT NULL AND d.dueDate >= :today AND d.dueDate <= :max AND (d.status IS NULL OR d.status <> :paid) ORDER BY d.dueDate",
                    Debt.class);
            q.setParameter("today", today);
            q.setParameter("max", max);
            q.setParameter("paid", Debt.STATUS_PAID);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Debt> findNearDueByUser(UUID userId, int daysThreshold) {
        if (userId == null) return findNearDue(daysThreshold);
        EntityManager em = JPAUtil.getEntityManager();
        try {
            LocalDate today = LocalDate.now();
            LocalDate max = today.plusDays(daysThreshold);
            TypedQuery<Debt> q = em.createQuery(
                    "SELECT d FROM Debt d WHERE d.userId = :uid AND d.dueDate IS NOT NULL AND d.dueDate >= :today AND d.dueDate <= :max AND (d.status IS NULL OR d.status <> :paid) ORDER BY d.dueDate",
                    Debt.class);
            q.setParameter("uid", userId);
            q.setParameter("today", today);
            q.setParameter("max", max);
            q.setParameter("paid", Debt.STATUS_PAID);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public BigDecimal getTotalUnpaid() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<BigDecimal> q = em.createQuery(
                    "SELECT COALESCE(SUM(d.amount), 0) FROM Debt d WHERE d.status IS NULL OR d.status <> :paid",
                    BigDecimal.class);
            q.setParameter("paid", Debt.STATUS_PAID);
            return q.getSingleResult();
        } finally {
            em.close();
        }
    }

    public BigDecimal getTotalUnpaidByUser(UUID userId) {
        if (userId == null) return getTotalUnpaid();
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<BigDecimal> q = em.createQuery(
                    "SELECT COALESCE(SUM(d.amount), 0) FROM Debt d WHERE d.userId = :uid AND (d.status IS NULL OR d.status <> :paid)",
                    BigDecimal.class);
            q.setParameter("uid", userId);
            q.setParameter("paid", Debt.STATUS_PAID);
            return q.getSingleResult();
        } finally {
            em.close();
        }
    }

    public BigDecimal getTotalOverdue() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<BigDecimal> q = em.createQuery(
                    "SELECT COALESCE(SUM(d.amount), 0) FROM Debt d WHERE d.dueDate < :today AND (d.status IS NULL OR d.status <> :paid)",
                    BigDecimal.class);
            q.setParameter("today", LocalDate.now());
            q.setParameter("paid", Debt.STATUS_PAID);
            return q.getSingleResult();
        } finally {
            em.close();
        }
    }

    public BigDecimal getTotalOverdueByUser(UUID userId) {
        if (userId == null) return getTotalOverdue();
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<BigDecimal> q = em.createQuery(
                    "SELECT COALESCE(SUM(d.amount), 0) FROM Debt d WHERE d.userId = :uid AND d.dueDate < :today AND (d.status IS NULL OR d.status <> :paid)",
                    BigDecimal.class);
            q.setParameter("uid", userId);
            q.setParameter("today", LocalDate.now());
            q.setParameter("paid", Debt.STATUS_PAID);
            return q.getSingleResult();
        } finally {
            em.close();
        }
    }
}
