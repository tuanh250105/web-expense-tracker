package com.expensemanager.controller;

import com.expensemanager.model.Transaction;
import com.expensemanager.service.AnalyticsService;
import com.expensemanager.util.GsonFactory;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name = "AnalyticsController", urlPatterns = {"/analytics", "/api/analytics"})
public class AnalyticsController extends HttpServlet {
    private static final Gson GSON = GsonFactory.create();
    private final AnalyticsService service = new AnalyticsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        // ✅ Nếu truy cập giao diện /analytics → load layout.jsp
        if ("/analytics".equals(req.getServletPath())) {
            req.setAttribute("view", "/views/analytics.jsp");
            req.getRequestDispatcher("/layout/layout.jsp").forward(req, resp);
            return;
        }

        // ✅ Còn lại là API /api/analytics
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        System.out.println("👉 AnalyticsController triggered");

        String type = param(req, "type", "all");
        LocalDateTime from = parse(req.getParameter("from"));
        LocalDateTime to = parse(req.getParameter("to"));

        Map<String, Object> result = new LinkedHashMap<>();

        try {
            List<Transaction> list = service.find(type, from, to);
            System.out.println("✅ Query done, size = " + list.size());

            if (list.isEmpty()) {
                result.put("message", "No data found");
            } else {
                double income = list.stream()
                        .filter(t -> "income".equalsIgnoreCase(t.getType()))
                        .mapToDouble(Transaction::getAmount)
                        .sum();

                double expense = list.stream()
                        .filter(t -> "expense".equalsIgnoreCase(t.getType()))
                        .mapToDouble(Transaction::getAmount)
                        .sum();

                double balance = income - expense;

                Map<String, Double> grouped = list.stream()
                        .collect(Collectors.groupingBy(
                                t -> {
                                    if (t.getCategory() != null && t.getCategory().getName() != null) {
                                        return t.getCategory().getName();
                                    }
                                    return "Không xác định";
                                },
                                Collectors.summingDouble(Transaction::getAmount)
                        ));

                List<Map<String, Object>> topCategory = grouped.entrySet().stream()
                        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                        .limit(10)
                        .map(e -> {
                            Map<String, Object> map = new LinkedHashMap<>();
                            map.put("categoryName", e.getKey());
                            map.put("total", e.getValue());
                            return map;
                        })
                        .collect(Collectors.toList());

                List<Map<String, Object>> safeList = list.stream().map(t -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", t.getId());
                    item.put("type", t.getType());
                    item.put("amount", t.getAmount());
                    item.put("note", t.getNote());
                    item.put("date", t.getTransactionDate());
                    item.put("category", (t.getCategory() != null) ? t.getCategory().getName() : null);
                    return item;
                }).collect(Collectors.toList());

                result.put("summary", Map.of(
                        "income", income,
                        "expense", expense,
                        "balance", balance
                ));
                result.put("topCategory", topCategory);
                result.put("raw", safeList);
            }

            String json = GSON.toJson(result);
            try (PrintWriter out = resp.getWriter()) {
                out.write(json);
                out.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            String errJson = "{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}";
            resp.getWriter().write(errJson);
            resp.getWriter().flush();
        }
    }

    private String param(HttpServletRequest req, String k, String def) {
        String v = req.getParameter(k);
        return (v == null || v.isEmpty()) ? def : v;
    }

    private LocalDateTime parse(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return LocalDateTime.parse(s + "T00:00:00");
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
