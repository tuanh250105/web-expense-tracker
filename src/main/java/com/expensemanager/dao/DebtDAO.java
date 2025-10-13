package com.expensemanager.dao;

import com.expensemanager.model.Debt;
import com.expensemanager.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class DebtDAO {

    private final EntityManager em;

    // Constructor nhận EntityManager
    public DebtDAO(EntityManager em) {
        this.em = em;
    }

    // Constructor mặc định (tạo EntityManager từ JpaUtil)
    public DebtDAO() {
        this.em = JpaUtil.getEntityManager();
    }

    private EntityManager getEm() {
        return em;
    }

    public List<Debt> findAll() {
        TypedQuery<Debt> q = em.createQuery("SELECT d FROM Debt d ORDER BY d.dueDate NULLS LAST", Debt.class);
        return q.getResultList();
    }

    public List<Debt> findAllByUser(UUID userId) {
        if (userId == null) return findAll();
        TypedQuery<Debt> q = em.createQuery(
                "SELECT d FROM Debt d WHERE d.userId = :uid ORDER BY d.dueDate NULLS LAST", Debt.class);
        q.setParameter("uid", userId);
        return q.getResultList();
    }

    public Debt findById(UUID id) {
        if (id == null) return null;
        return em.find(Debt.class, id);
    }

    public Debt saveOrUpdate(Debt debt) {
        if (debt == null) return null;
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
        }
    }

    public boolean delete(UUID id) {
        if (id == null) return false;
        try {
            em.getTransaction().begin();
            Debt d = em.find(Debt.class, id);
            if (d != null) em.remove(d);
            em.getTransaction().commit();
            return d != null;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        }
    }

    public boolean markAsPaid(UUID id) {
        if (id == null) return false;
        try {
            em.getTransaction().begin();
            Debt d = em.find(Debt.class, id);
            if (d == null) return false;
            d.setStatus(Debt.STATUS_PAID);
            em.merge(d);
            em.getTransaction().commit();
            return true;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        }
    }

    public BigDecimal getTotalOverdueByUser(UUID userId) {
        if (userId == null) return BigDecimal.ZERO;
        try {
            TypedQuery<BigDecimal> query = em.createQuery(
                    "SELECT COALESCE(SUM(d.amount), 0) FROM Debt d " +
                            "WHERE d.userId = :userId AND (d.status = 'OVERDUE' OR d.dueDate < CURRENT_DATE)",
                    BigDecimal.class
            );
            query.setParameter("userId", userId);
            return query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    // Ví dụ thêm method getTotalUnpaidByUser
    public BigDecimal getTotalUnpaidByUser(UUID userId) {
        if (userId == null) return BigDecimal.ZERO;
        try {
            TypedQuery<BigDecimal> query = em.createQuery(
                    "SELECT COALESCE(SUM(d.amount), 0) FROM Debt d " +
                            "WHERE d.userId = :userId AND d.status != 'PAID'",
                    BigDecimal.class
            );
            query.setParameter("userId", userId);
            return query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }
    // Tìm các khoản nợ quá hạn
    public List<Debt> findOverdue() {
        try {
            TypedQuery<Debt> query = em.createQuery(
                    "SELECT d FROM Debt d WHERE d.status = 'OVERDUE' OR d.dueDate < CURRENT_DATE ORDER BY d.dueDate",
                    Debt.class
            );
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<Debt> findOverdueByUser(UUID userId) {
        if (userId == null) return findOverdue();
        try {
            TypedQuery<Debt> query = em.createQuery(
                    "SELECT d FROM Debt d WHERE d.userId = :userId AND (d.status = 'OVERDUE' OR d.dueDate < CURRENT_DATE) ORDER BY d.dueDate",
                    Debt.class
            );
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // Tìm các khoản nợ sắp đến hạn
    public List<Debt> findNearDue(int daysThreshold) {
        try {
            TypedQuery<Debt> query = em.createQuery(
                    "SELECT d FROM Debt d WHERE d.status != 'PAID' AND d.dueDate BETWEEN CURRENT_DATE AND CURRENT_DATE + :days ORDER BY d.dueDate",
                    Debt.class
            );
            query.setParameter("days", daysThreshold);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<Debt> findNearDueByUser(UUID userId, int daysThreshold) {
        if (userId == null) return findNearDue(daysThreshold);
        try {
            TypedQuery<Debt> query = em.createQuery(
                    "SELECT d FROM Debt d WHERE d.userId = :userId AND d.status != 'PAID' AND d.dueDate BETWEEN CURRENT_DATE AND CURRENT_DATE + :days ORDER BY d.dueDate",
                    Debt.class
            );
            query.setParameter("userId", userId);
            query.setParameter("days", daysThreshold);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // Tổng số tiền nợ chưa trả
    public BigDecimal getTotalUnpaid() {
        try {
            TypedQuery<BigDecimal> query = em.createQuery(
                    "SELECT COALESCE(SUM(d.amount), 0) FROM Debt d WHERE d.status != 'PAID'",
                    BigDecimal.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    // Tổng số tiền nợ quá hạn
    public BigDecimal getTotalOverdue() {
        try {
            TypedQuery<BigDecimal> query = em.createQuery(
                    "SELECT COALESCE(SUM(d.amount), 0) FROM Debt d WHERE d.status = 'OVERDUE' OR d.dueDate < CURRENT_DATE",
                    BigDecimal.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    // Có thể thêm các hàm khác như findOverdue, findNearDue, getTotalOverdue, getTotalUnpaid
    // theo mẫu tương tự, đảm bảo luôn dùng COALESCE và handle exception
}
