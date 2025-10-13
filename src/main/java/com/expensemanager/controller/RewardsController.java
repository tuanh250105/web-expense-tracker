package com.expensemanager.controller;

import com.expensemanager.service.RewardService;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.UUID;

@WebServlet(name = "RewardsController", urlPatterns = {"/rewards", "/api/rewards/*"})
public class RewardsController extends HttpServlet {

    private static final Gson GSON = new Gson();
    private final RewardService service = new RewardService();

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
                    write(resp, Map.of(
                            "points", service.getUserScore(uid),
                            "prizes", prizes
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

    private UUID resolveUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        UUID uid = null;

        if (session != null && session.getAttribute("userId") != null) {
            Object val = session.getAttribute("userId");
            if (val instanceof UUID u) uid = u;
            else if (val instanceof String s && !s.isBlank()) {
                try { uid = UUID.fromString(s); } catch (Exception ignored) {}
            }
        }

        if (uid == null) {
            try {
                uid = UUID.fromString(req.getParameter("userId"));
            } catch (Exception e) {
                // fallback: user test
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
