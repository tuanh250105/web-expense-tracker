package com.expensemanager.service;

import com.expensemanager.dao.RewardDAO;
import com.expensemanager.model.RewardPrize;
import com.expensemanager.model.RewardSpin;

import java.util.*;

public class RewardService {

    private final RewardDAO dao = new RewardDAO();
    private static final int COST_SPIN = 20;
    private static final int REWARD_PER_BUDGET = 5;

    /** 🔹 Điểm hiện tại của user */
    public int getUserScore(UUID userId) {
        return dao.getUserScore(userId);
    }

    /** 🔹 Danh sách phần thưởng đang hoạt động */
    public List<RewardPrize> getActivePrizes() {
        return dao.getActivePrizes();
    }

    /** 🔹 Lịch sử quay gần đây (map gọn để trả JSON) */
    public List<Map<String, Object>> getRecentSpins(UUID userId, int limit) {
        var list = dao.recentSpins(userId, limit);
        List<Map<String, Object>> mapped = new ArrayList<>();
        for (RewardSpin s : list) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("prizeLabel", s.getPrizeLabel());
            map.put("prizeCode", s.getPrizeCode());
            map.put("pointsSpent", s.getPointsSpent());
            map.put("createdAt", s.getCreatedAt());
            mapped.add(map);
        }
        return mapped;
    }

    /** 🔹 Quay thưởng (trừ điểm, chọn random, lưu lịch sử) */
    public Map<String, Object> spin(UUID userId) {
        if (!dao.trySpendPoints(userId, COST_SPIN)) {
            return Map.of("error", "not_enough_points");
        }

        var prizes = dao.getActivePrizes();
        if (prizes.isEmpty()) {
            return Map.of("error", "no_prize");
        }

        RewardPrize pick = prizes.get(new Random().nextInt(prizes.size()));
        dao.saveSpin(userId, pick.getCode(), pick.getLabel(), COST_SPIN);

        if ("EXTRA".equalsIgnoreCase(pick.getCode())) {
            dao.addPoints(userId, COST_SPIN);
        }

        return Map.of(
                "prizeCode", pick.getCode(),
                "prizeLabel", pick.getLabel(),
                "spent", COST_SPIN
        );
    }

    /** 🔹 Nhận 1 lần thưởng ngân sách */
    public Map<String, Object> claimOne(UUID userId) {
        int added = dao.claimOneBudgetAward(userId, REWARD_PER_BUDGET);
        int points = dao.getUserScore(userId);
        int achieved = dao.countAchievedBudgets(userId);
        int claimed = dao.countBudgetClaims(userId, REWARD_PER_BUDGET);
        return Map.of(
                "added", added,
                "points", points,
                "remaining", Math.max(achieved - claimed, 0)
        );
    }

    /** 🔹 Đếm ngân sách đạt mục tiêu */
    public int countAchievedBudgets(UUID userId) {
        return dao.countAchievedBudgets(userId);
    }

    /** 🔹 Đếm số lần claim ngân sách */
    public int countBudgetClaims(UUID userId) {
        return dao.countBudgetClaims(userId, REWARD_PER_BUDGET);
    }
}
