package com.expensemanager.controller;

import com.expensemanager.dao.RewardDAO;
import com.expensemanager.model.RewardPrize;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@WebServlet(name = "RewardsController", urlPatterns = {"/api/rewards/*"})
public class RewardsController extends HttpServlet {

    private static final Gson GSON = new Gson();
    private final RewardDAO dao = new RewardDAO(); // ← dùng DB thật

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getPathInfo() == null ? "" : req.getPathInfo();

        try (PrintWriter out = resp.getWriter()) {
            switch (path) {
                case "/points" -> { // ?userId=UUID
                    UUID uid = uuid(req.getParameter("userId"));
                    int points = dao.getUserScore(uid);
                    out.print(GSON.toJson(Map.of("userId", uid.toString(), "points", points)));
                }
                case "/prizes" -> {
                    var prizes = dao.getActivePrizes();
                    out.print(GSON.toJson(prizes));
                }
                case "/recent" -> { // ?userId=UUID&limit=10
                    UUID uid = uuid(req.getParameter("userId"));
                    int limit = parseInt(req.getParameter("limit"), 10);
                    out.print(GSON.toJson(dao.recentSpins(uid, limit)));
                }
                // -------------------- GET --------------------
                case "/claimable" -> { // ?userId=UUID
                    UUID uid = uuid(req.getParameter("userId"));
                    final int per = 5;
                    RewardDAO d = new RewardDAO();
                    int achieved = d.countAchievedBudgets(uid);
                    int claimed  = d.countBudgetClaims(uid, per);
                    int remaining = Math.max(achieved - claimed, 0);
                    out.print(GSON.toJson(Map.of(
                            "userId", uid.toString(),
                            "perBudget", per,
                            "achieved", achieved,
                            "claimed", claimed,
                            "remaining", remaining
                    )));
                }

// -------------------- POST --------------------
                case "/claim-one" -> { // ?userId=UUID
                    UUID uid = uuid(req.getParameter("userId"));
                    final int per = 5;
                    int added = dao.claimOneBudgetAward(uid, per); // 0 hoặc 5
                    int points = dao.getUserScore(uid);
                    int achieved = dao.countAchievedBudgets(uid);
                    int claimed  = dao.countBudgetClaims(uid, per);
                    int remaining = Math.max(achieved - claimed, 0);
                    out.print(GSON.toJson(Map.of(
                            "userId", uid.toString(),
                            "added", added,
                            "points", points,
                            "remaining", remaining
                    )));
                }


                default -> {
                    resp.setStatus(404);
                    out.print(GSON.toJson(Map.of("error", "Not found")));
                }
            }
        } catch (IllegalArgumentException e) {
            resp.setStatus(400);
            resp.getWriter().print(GSON.toJson(Map.of("error", e.getMessage())));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getPathInfo() == null ? "" : req.getPathInfo();

        try (PrintWriter out = resp.getWriter()) {
            switch (path) {
                case "/calc" -> {
                    // khi nối business thật có thể thay bằng tính điểm theo Budgets
                    UUID uid = uuid(req.getParameter("userId"));
                    int newPoints = dao.addPoints(uid, 5);
                    out.print(GSON.toJson(Map.of(
                            "userId", uid.toString(),
                            "added", 5,
                            "points", newPoints
                    )));
                }
                case "/spin" -> {
                    UUID uid = uuid(req.getParameter("userId"));
                    final int cost = 20;

                    if (!dao.trySpendPoints(uid, cost)) {
                        resp.setStatus(400);
                        out.print(GSON.toJson(Map.of("error", "NOT_ENOUGH_POINTS")));
                        return;
                    }

                    var prize = pickWeighted(dao.getActivePrizes());
                    var spin = dao.saveSpin(uid, prize.getCode(), prize.getLabel(), cost);

                    out.print(GSON.toJson(Map.of(
                            "userId", uid.toString(),
                            "prizeCode", prize.getCode(),
                            "prizeLabel", prize.getLabel(),
                            "spent", cost,
                            "spinId", spin.getId()
                    )));
                }
                default -> {
                    resp.setStatus(404);
                    out.print(GSON.toJson(Map.of("error", "Not found")));
                }
            }
        } catch (IllegalArgumentException e) {
            resp.setStatus(400);
            resp.getWriter().print(GSON.toJson(Map.of("error", e.getMessage())));
        }
    }

    // helpers
    private static UUID uuid(String s) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException("Missing userId");
        return UUID.fromString(s);
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    private static RewardPrize pickWeighted(List<RewardPrize> prizes) {
        if (prizes == null || prizes.isEmpty()) throw new IllegalArgumentException("No active prizes");

        var active = prizes.stream()
                .filter(p -> p.getWeight() != null && p.getWeight() > 0)
                .collect(Collectors.toList());

        if (active.isEmpty()) {
            return prizes.get(ThreadLocalRandom.current().nextInt(prizes.size()));
        }

        int total = active.stream().mapToInt(RewardPrize::getWeight).sum();
        int r = ThreadLocalRandom.current().nextInt(total) + 1;
        int acc = 0;
        for (RewardPrize p : active) {
            acc += p.getWeight();
            if (r <= acc) return p;
        }
        return active.get(active.size() - 1);
    }
}
