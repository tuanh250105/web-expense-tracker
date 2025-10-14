package com.expensemanager.controller;

import com.expensemanager.model.User;
import com.expensemanager.service.RewardService;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;

@WebServlet(name = "RewardsController", urlPatterns = {"/rewards", "/api/rewards/*"})
public class RewardsController extends HttpServlet {

    private static final Gson GSON = new Gson();
    private final RewardService service = new RewardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        // UI page
        if ("/rewards".equals(req.getServletPath())) {
            UUID uid = resolveUserId(req);
            String userId = uid.toString();
            req.setAttribute("view", "/views/rewards.jsp");
            req.setAttribute("userId", userId);
            req.getRequestDispatcher("/layout/layout.jsp").forward(req, resp);
            return;
        }

        resp.setContentType("application/json;charset=UTF-8");
        String path = Optional.ofNullable(req.getPathInfo()).orElse("/");
        UUID uid = resolveUserId(req);

        try {
            switch (path) {
                case "/points" -> write(resp, Map.of("points", service.getUserScore(uid)));

                case "/recent" -> {
                    int limit = parseInt(req.getParameter("limit"), 5);
                    write(resp, service.getRecentSpins(uid, limit));
                }

                case "/claimable" -> {
                    int achieved = service.countAchievedBudgets(uid);
                    int claimed = service.countBudgetClaims(uid);
                    write(resp, Map.of("remaining", Math.max(achieved - claimed, 0)));
                }

                default -> {
                    var prizes = service.getActivePrizes().stream()
                            .map(p -> Map.of("code", p.getCode(), "label", p.getLabel()))
                            .toList();

                    var recentSpins = service.getRecentSpins(uid, 10); // 🔹 lấy lịch sử 10 lượt gần nhất

                    write(resp, Map.of(
                            "points", service.getUserScore(uid),
                            "prizes", prizes,
                            "recent", recentSpins  // ✅ thêm trường recent vào JSON
                    ));
                }


            }
        } catch (Exception e) {
            resp.setStatus(500);
            write(resp, Map.of("error", e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");
        String path = Optional.ofNullable(req.getPathInfo()).orElse("/");
        UUID uid = resolveUserId(req);

        try {
            switch (path) {
                case "/claim-one" -> write(resp, service.claimOne(uid));

                case "/spin" -> {
                    var result = service.spin(uid);
                    if (result.containsKey("error")) {
                        resp.setStatus(400);
                    }
                    write(resp, result);
                }

                default -> resp.sendError(404);
            }
        } catch (Exception e) {
            resp.setStatus(500);
            write(resp, Map.of("error", e.getMessage()));
        }
    }

    // ===== Helper =====
    private void write(HttpServletResponse resp, Object obj) throws IOException {
        try (PrintWriter out = resp.getWriter()) {
            out.write(GSON.toJson(obj));
        }
    }

    private int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * Lấy userId theo thứ tự ưu tiên:
     * 1) session "user" (User object) -> id
     * 2) session "userId" (UUID/String)
     * 3) query param "userId" (UUID)
     * 4) Fallback test id (dùng cho localhost/demo)
     */
    private UUID resolveUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        User user = (User) session.getAttribute("user");
        UUID userId = user.getId();

        return userId;
    }
}

