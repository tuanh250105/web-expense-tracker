package com.expensemanager.repository;

import com.expensemanager.model.User;
import com.expensemanager.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class UserRepository {

    // Tìm user theo email
    public User findByEmail(String email) {
        try (EntityManager em = JpaUtil.em()) {
            return em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email.toLowerCase())
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    // Lưu hoặc cập nhật user
    public User save(User u) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            User managed;
            if (u.getId() == null) {
                em.persist(u);
                managed = u;
            } else {
                managed = em.merge(u);
            }
            em.getTransaction().commit();
            return managed;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    // Kiểm tra email đã tồn tại chưa
    public boolean existsByEmail(String email) {
        try (EntityManager em = JpaUtil.em()) {
            Long count = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                    .setParameter("email", email.toLowerCase())
                    .getSingleResult();
            return count > 0;
        }
    }

    // Kiểm tra username đã tồn tại chưa
    public boolean existsByUsername(String username) {
        try (EntityManager em = JpaUtil.em()) {
            Long count = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return count > 0;
        }
    }

    // Đếm tất cả user
    public long countAllUsers() {
        try (EntityManager em = JpaUtil.em()) {
            return em.createQuery("SELECT COUNT(u) FROM User u", Long.class)
                    .getSingleResult();
        }
    }

    // Đếm user tạo hôm nay
    public long countNewUsersToday() {
    try (EntityManager em = JpaUtil.em()) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        java.time.Instant startOfDayInstant = startOfDay.atZone(java.time.ZoneId.systemDefault()).toInstant();
        return em.createQuery("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startOfDay", Long.class)
            .setParameter("startOfDay", startOfDayInstant)
            .getSingleResult();
    }
    }

    // Đếm user tạo tuần này
    public long countNewUsersThisWeek() {
    try (EntityManager em = JpaUtil.em()) {
        LocalDateTime startOfWeek = LocalDateTime.now()
            .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            .toLocalDate().atStartOfDay();
        java.time.Instant startOfWeekInstant = startOfWeek.atZone(java.time.ZoneId.systemDefault()).toInstant();
        return em.createQuery("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startOfWeek", Long.class)
            .setParameter("startOfWeek", startOfWeekInstant)
            .getSingleResult();
    }
    }

    // Lấy thống kê user theo thời gian (ví dụ: số user theo ngày)
    public List<UserStat> getUserStatsByPeriod() {
        try (EntityManager em = JpaUtil.em()) {
            return em.createQuery(
                            "SELECT NEW com.expensemanager.repository.UserStat(FUNCTION('DATE', u.createdAt), COUNT(u)) " +
                                    "FROM User u GROUP BY FUNCTION('DATE', u.createdAt) ORDER BY FUNCTION('DATE', u.createdAt)",
                            UserStat.class)
                    .getResultList();
        }
    }

    // Lấy tất cả user
    public List<User> findAll() {
        try (EntityManager em = JpaUtil.em()) {
            return em.createQuery("SELECT u FROM User u ORDER BY u.username", User.class)
                    .getResultList();
        }
    }
}
