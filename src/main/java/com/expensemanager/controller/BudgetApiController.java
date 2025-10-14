package com.expensemanager.controller;
import com.expensemanager.dao.BudgetDAO;
import com.expensemanager.model.Budget;
import com.expensemanager.model.Transaction;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@WebServlet("/api/budget/*")
public class BudgetApiController extends HttpServlet {
    private BudgetDAO budgetDAO = new BudgetDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UUID userId = (UUID) session.getAttribute("user");
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Vui lòng đăng nhập\"}");
            return;
        }

        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            if ("/daily".equals(pathInfo)) {
                String budgetId = request.getParameter("budgetId");
                List<LocalDate> dates = budgetDAO.getDailyDates(UUID.fromString(budgetId));
                List<BigDecimal> amounts = budgetDAO.getDailySpent(UUID.fromString(budgetId));

                Map<String, Object> data = new HashMap<>();
                data.put("dates", dates.stream().map(LocalDate::toString).collect(Collectors.toList()));
                data.put("amounts", amounts);

                response.getWriter().write(gson.toJson(data));
            } else if ("/history".equals(pathInfo)) {
                String categoryId = request.getParameter("categoryId");
                List<Budget> budgets = budgetDAO.getHistoricalBudgets(userId, UUID.fromString(categoryId));

                List<Map<String, Object>> data = budgets.stream().map(b -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("startDate", b.getStartDate().toString());
                    map.put("endDate", b.getEndDate().toString());
                    map.put("spentAmount", b.getSpentAmount().doubleValue());
                    map.put("limitAmount", b.getLimitAmount().doubleValue());
                    return map;
                }).collect(Collectors.toList());

                response.getWriter().write(gson.toJson(data));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"API không tồn tại\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Lỗi server: " + e.getMessage() + "\"}");
        }
    }
}