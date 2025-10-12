package com.expensemanager.controller;

import com.expensemanager.dao.RewardDAO;
import com.expensemanager.model.RewardPrize;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@WebServlet(name = "RewardsController", urlPatterns = {"/rewards", "/api/rewards/*"})
public class RewardsController extends HttpServlet {

    private static final Gson GSON = new Gson();
    private final RewardDAO dao = new RewardDAO();
    private static final int COST_SPIN = 20;
    private static final int REWARD_PER_BUDGET = 5;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        if ("/rewards".equals(req.getServletPath())) {
            req.setAttribute("view", "/views/rewards.jsp");
            req.getRequestDispatcher("/layout/layout.jsp").forward(req, resp);
            return;
        }

        resp.setContentType("application/json;charset=UTF-8");
        String path = Optional.ofNullable(req.getPathInfo()).orElse("/");
        UUID uid = resolveUserId(req);

        switch (path) {
            case "/points" -> write(resp, Map.of("points", dao.getUserScore(uid)));
            case "/recent" -> {
                int limit = parseInt(req.getParameter("limit"), 5);
                var list = dao.recentSpins(uid, limit).stream().map(s -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("prizeLabel", s.getPrizeLabel());
                    map.put("prizeCode", s.getPrizeCode());
                    map.put("pointsSpent", s.getPointsSpent());
                    map.put("createdAt", s.getCreatedAt().toString());
                    return map;
                }).collect(Collectors.toList());
                write(resp, list);
            }
            case "/claimable" -> {
                int achieved = dao.countAchievedBudgets(uid);
                int claimed = dao.countBudgetClaims(uid, REWARD_PER_BUDGET);
                write(resp, Map.of("remaining", Math.max(achieved - claimed, 0)));
            }
            default -> {
                var prizes = dao.getActivePrizes().stream()
                        .map(p -> Map.of("code", p.getCode(), "label", p.getLabel()))
                        .collect(Collectors.toList());
                write(resp, Map.of("points", dao.getUserScore(uid), "prizes", prizes));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");
        String path = Optional.ofNullable(req.getPathInfo()).orElse("/");
        UUID uid = resolveUserId(req);

        switch (path) {
            case "/claim-one" -> {
                int added = dao.claimOneBudgetAward(uid, REWARD_PER_BUDGET);
                int points = dao.getUserScore(uid);
                int achieved = dao.countAchievedBudgets(uid);
                int claimed = dao.countBudgetClaims(uid, REWARD_PER_BUDGET);
                write(resp, Map.of(
                        "added", added,
                        "points", points,
                        "remaining", Math.max(achieved - claimed, 0)
                ));
            }
            case "/spin" -> handleSpin(uid, resp);
            default -> resp.sendError(404);
        }
    }

    private void handleSpin(UUID uid, HttpServletResponse resp) throws IOException {
        if (!dao.trySpendPoints(uid, COST_SPIN)) {
            resp.setStatus(400);
            write(resp, Map.of("error", "not_enough_points"));
            return;
        }

        var prizes = dao.getActivePrizes();
        if (prizes.isEmpty()) {
            write(resp, Map.of("error", "no_prize"));
            return;
        }

        RewardPrize pick = prizes.get(new Random().nextInt(prizes.size()));
        dao.saveSpin(uid, pick.getCode(), pick.getLabel(), COST_SPIN);

        if ("EXTRA".equalsIgnoreCase(pick.getCode())) {
            dao.addPoints(uid, COST_SPIN);
        }

        write(resp, Map.of(
                "prizeCode", pick.getCode(),
                "prizeLabel", pick.getLabel(),
                "spent", COST_SPIN
        ));

    }

    private void write(HttpServletResponse resp, Object obj) throws IOException {
        try (PrintWriter out = resp.getWriter()) {
            out.write(GSON.toJson(obj));
        }
    }

    private UUID resolveUserId(HttpServletRequest req) {
        // Ưu tiên lấy từ session
        HttpSession session = req.getSession(false);
        UUID uid = null;

        if (session != null && session.getAttribute("userId") != null) {
            Object val = session.getAttribute("userId");
            if (val instanceof UUID u) uid = u;
            else if (val instanceof String s && !s.isBlank()) {
                try { uid = UUID.fromString(s); } catch (Exception ignored) {}
            }
        }

        // Fallback nếu chưa login hoặc chưa có session
        if (uid == null) {
            try {
                uid = UUID.fromString(req.getParameter("userId"));
            } catch (Exception e) {
                uid = UUID.fromString("67b78d51-4eec-491c-bbf0-30e982def9e0");
            }
        }

        return uid;
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s); }
        catch (Exception e) { return def; }
    }
}
