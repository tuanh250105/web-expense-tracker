package com.expensemanager.repository;

import com.expensemanager.model.User;
import com.expensemanager.util.JpaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class UserRepository {
  public User findByEmail(String email) {
    try (EntityManager em = JpaUtil.em()) {
      return em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
        .setParameter("email", email.toLowerCase())
        .getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

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

  public boolean existsByEmail(String email) {
    try (EntityManager em = JpaUtil.em()) {
      Long count = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
        .setParameter("email", email.toLowerCase())
        .getSingleResult();
      return count > 0;
    }
  }

  public boolean existsByUsername(String username) {
    try (EntityManager em = JpaUtil.em()) {
      Long count = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
        .setParameter("username", username)
        .getSingleResult();
      return count > 0;
    }
  }
}
