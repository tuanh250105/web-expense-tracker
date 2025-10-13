package com.expensemanager.repository;

import com.expensemanager.model.PasswordResetToken;
import com.expensemanager.util.JpaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class PasswordResetTokenRepository {
	public void save(PasswordResetToken t) {
			EntityManager em = JpaUtil.em();
			try {
				em.getTransaction().begin();
				em.persist(t);
				em.getTransaction().commit();
			} catch (RuntimeException ex) {
				if (em.getTransaction().isActive()) em.getTransaction().rollback();
				throw ex;
			} finally {
				em.close();
			}
	}

	public PasswordResetToken findValidByEmailAndOtp(String email, String otp) {
			try (EntityManager em = JpaUtil.em()) {
			return em.createQuery(
				"SELECT t FROM PasswordResetToken t WHERE t.email = :email AND t.otp = :otp AND t.expiresAt > CURRENT_TIMESTAMP",
				PasswordResetToken.class)
				.setParameter("email", email.toLowerCase())
				.setParameter("otp", otp)
				.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public void delete(PasswordResetToken t) {
			EntityManager em = JpaUtil.em();
			try {
				em.getTransaction().begin();
				PasswordResetToken managed = em.merge(t);
				em.remove(managed);
				em.getTransaction().commit();
			} catch (RuntimeException ex) {
				if (em.getTransaction().isActive()) em.getTransaction().rollback();
				throw ex;
			} finally {
				em.close();
			}
	}
}