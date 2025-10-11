package com.expensemanager.dao;

import com.expensemanager.model.RewardPoints;
import com.expensemanager.model.RewardPrize;
import com.expensemanager.model.RewardSpin;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class RewardDAO {

    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory("BudgetBuddyUnit"); // ✅ fix typo

    private EntityManager em() {
        return EMF.createEntityManager();
    }

    // ======= ĐIỂM NGƯỜI DÙNG =======
    public int getUserScore(UUID userId) {
        EntityManager em = em();
        try {
            RewardPoints rp = em.find(RewardPoints.class, userId);
            return rp == null ? 0 : rp.getPoints();
        } finally {
            em.close();
        }
    }

    public int addPoints(UUID userId, int delta) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            RewardPoints rp = em.find(RewardPoints.class, userId);
            if (rp == null) {
                rp = new RewardPoints();
                rp.setUserId(userId);
                rp.setPoints(Math.max(delta, 0));
                rp.setUpdatedAt(OffsetDateTime.now());
                em.persist(rp);
            } else {
                rp.setPoints(rp.getPoints() + delta);
                rp.setUpdatedAt(OffsetDateTime.now());
                em.merge(rp);
            }
            tx.commit();
            return rp.getPoints();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public boolean trySpendPoints(UUID userId, int cost) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            RewardPoints rp = em.find(RewardPoints.class, userId, LockModeType.PESSIMISTIC_WRITE);
            int cur = rp.getPoints();
            if (cur < cost) {
                tx.rollback();
                return false;
            }
            if (rp == null) {
                rp = new RewardPoints();
                rp.setUserId(userId);
            }
            rp.setPoints(cur - cost);
            rp.setUpdatedAt(OffsetDateTime.now());
            em.merge(rp);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // ======= PHẦN THƯỞNG =======
    public List<RewardPrize> getActivePrizes() {
        EntityManager em = em();
        try {
            return em.createQuery(
                    "SELECT p FROM RewardPrize p WHERE p.active = true ORDER BY p.weight DESC",
                    RewardPrize.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public RewardSpin saveSpin(UUID userId, String prizeCode, String prizeLabel, int pointsSpent) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            RewardSpin s = new RewardSpin();
            s.setUserId(userId);
            s.setPrizeCode(prizeCode);
            s.setPrizeLabel(prizeLabel);
            s.setPointsSpent(pointsSpent);
            s.setCreatedAt(OffsetDateTime.now());
            em.persist(s);
            tx.commit();
            return s;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<RewardSpin> recentSpins(UUID userId, int limit) {
        EntityManager em = em();
        try {
            return em.createQuery(
                            "SELECT s FROM RewardSpin s WHERE s.userId = :uid ORDER BY s.createdAt DESC",
                            RewardSpin.class
                    )
                    .setParameter("uid", userId)
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // ======= LIÊN KẾT VỚI BUDGET (không cần model Budget.java) =======

    /** Số ngân sách đã đạt mục tiêu */
    public int countAchievedBudgets(UUID userId) {
        EntityManager em = em();
        try {
            Query q = em.createNativeQuery("""
                SELECT COUNT(*) 
                FROM budgets b
                WHERE b.user_id = ?1
                  AND b.end_date < now()::date
                  AND COALESCE(b.spent_amount,0) <= COALESCE(b.limit_amount,0)
            """);
            q.setParameter(1, userId);
            Number n = (Number) q.getSingleResult();
            return n == null ? 0 : n.intValue();
        } finally {
            em.close();
        }
    }

    /** Đã nhận bao nhiêu lượt (mỗi lượt = +perBudget điểm) */
    public int countBudgetClaims(UUID userId, int perBudget) {
        EntityManager em = em();
        try {
            Query q = em.createNativeQuery("""
                SELECT COALESCE(SUM(CASE WHEN points_spent < 0 THEN -points_spent ELSE 0 END), 0)
                FROM reward_spins
                WHERE user_id = ?1
                  AND prize_code IS NULL
                  AND prize_label = 'BUDGET_AWARD'
            """);
            q.setParameter(1, userId);
            Number pts = (Number) q.getSingleResult();
            int totalAwardedPoints = pts == null ? 0 : pts.intValue();
            return totalAwardedPoints / Math.max(perBudget, 1);
        } finally {
            em.close();
        }
    }

    /** Nhận 1 lượt (+perBudget điểm). Trả về điểm vừa cộng (0 hoặc perBudget). */
    public int claimOneBudgetAward(UUID userId, int perBudget) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // còn bao nhiêu lượt có thể nhận
            int achieved = countAchievedBudgets(userId);
            int claimed = countBudgetClaims(userId, perBudget);
            int remain = Math.max(achieved - claimed, 0);
            if (remain <= 0) {
                tx.commit();
                return 0;
            }

            // + perBudget điểm
            RewardPoints rp = em.find(RewardPoints.class, userId, LockModeType.PESSIMISTIC_WRITE);
            if (rp == null) {
                rp = new RewardPoints();
                rp.setUserId(userId);
                rp.setPoints(0);
                em.persist(rp);
            }
            rp.setPoints( rp.getPoints() + perBudget);
            rp.setUpdatedAt(OffsetDateTime.now());
            em.merge(rp);

            // log 1 lượt đã nhận vào reward_spins
            RewardSpin s = new RewardSpin();
            s.setUserId(userId);
            s.setPrizeCode(null);              // NULL để không dính FK
            s.setPrizeLabel("BUDGET_AWARD");   // flag để nhận diện
            s.setPointsSpent(-perBudget);      // âm = cộng điểm
            s.setCreatedAt(OffsetDateTime.now());
            em.persist(s);

            tx.commit();
            return perBudget;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
