package com.expensemanager.repository;

import com.expensemanager.model.User;
import com.expensemanager.util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class UserRepository {
  public java.util.List<User> findAll() {
    try (jakarta.persistence.EntityManager em = JPAUtil.em()) {
      return em.createQuery("SELECT u FROM User u ORDER BY u.createdAt DESC", User.class).getResultList();
    }
  }
  public long countAllUsers() {
    try (EntityManager em = JPAUtil.em()) {
      return em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
    }
  }

  public long countNewUsersToday() {
    try (EntityManager em = JPAUtil.em()) {
      return em.createQuery("SELECT COUNT(u) FROM User u WHERE u.createdAt >= CURRENT_DATE", Long.class).getSingleResult();
    }
  }

  public long countNewUsersThisWeek() {
    try (EntityManager em = JPAUtil.em()) {
      // Lấy ngày đầu tuần (thứ 2) và cuối tuần (chủ nhật)
      java.time.LocalDate today = java.time.LocalDate.now();
      java.time.DayOfWeek dow = today.getDayOfWeek();
      java.time.LocalDate startOfWeek = today.minusDays(dow.getValue() - 1);
      java.time.LocalDate endOfWeek = startOfWeek.plusDays(6);
      java.time.Instant start = startOfWeek.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
      java.time.Instant end = endOfWeek.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
      return em.createQuery("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :start AND u.createdAt < :end", Long.class)
        .setParameter("start", start)
        .setParameter("end", end)
        .getSingleResult();
    }
  }

  public java.util.List<UserStat> getUserStatsByPeriod() {
    try (EntityManager em = JPAUtil.em()) {
      // Lấy mốc thời gian 14 ngày gần nhất bằng Java
      java.time.LocalDate today = java.time.LocalDate.now();
      java.time.LocalDate startDate = today.minusDays(13);
      java.time.Instant start = startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
      return em.createQuery(
        "SELECT NEW com.expensemanager.repository.UserStat(FUNCTION('TO_CHAR', u.createdAt, 'YYYY-MM-DD'), COUNT(u)) " +
        "FROM User u WHERE u.createdAt >= :start GROUP BY FUNCTION('TO_CHAR', u.createdAt, 'YYYY-MM-DD') ORDER BY 1 DESC",
        UserStat.class
      ).setParameter("start", start).getResultList();
    }
  }
  public User findByEmail(String email) {
    try (EntityManager em = JPAUtil.em()) {
      return em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
        .setParameter("email", email.toLowerCase())
        .getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  public User save(User u) {
    EntityManager em = JPAUtil.em();
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

  public boolean existsByEmail(String email) {
    try (EntityManager em = JPAUtil.em()) {
      Long count = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
        .setParameter("email", email.toLowerCase())
        .getSingleResult();
      return count > 0;
    }
  }

  public boolean existsByUsername(String username) {
    try (EntityManager em = JPAUtil.em()) {
      Long count = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
        .setParameter("username", username)
        .getSingleResult();
      return count > 0;
    }
  }
}
