package com.expensemanager.controller;

import com.expensemanager.model.Budget;
import com.expensemanager.service.BudgetService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/budgets", "/budgets/*"})
public class BudgetController extends HttpServlet {
    private final BudgetService budgetService = new BudgetService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        Long userId = getUserIdAsLong(session.getAttribute("user_id"));

        String path = request.getPathInfo();
        if (path != null && path.matches("/^\\/\\d+$/") ) {
            // detail JSON series
            Long budgetId = Long.valueOf(path.substring(1));
            Map<String, Object> series = budgetService.getBudgetDailySeries(budgetId);
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.write(toJson(series));
            out.flush();
            return;
        }

        String periodType = request.getParameter("period");
        String category = request.getParameter("categoryId");
        Long cat = (category == null || category.isEmpty()) ? null : Long.valueOf(category);
        List<Budget> budgets = budgetService.listBudgets(userId, periodType, cat);
        request.setAttribute("budgets", budgets);
        request.setAttribute("view", "/views/budget.jsp");
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        Long userId = getUserIdAsLong(session.getAttribute("user_id"));

        String action = request.getParameter("action");
        if ("create".equalsIgnoreCase(action) || "update".equalsIgnoreCase(action)) {
            String periodType = request.getParameter("periodType");
            LocalDate start = LocalDate.parse(request.getParameter("periodStart"));
            LocalDate end = LocalDate.parse(request.getParameter("periodEnd"));
            String category = request.getParameter("categoryId");
            Long cat = (category == null || category.isEmpty()) ? null : Long.valueOf(category);
            BigDecimal limit = new BigDecimal(request.getParameter("limitAmount"));
            String note = request.getParameter("note");
            budgetService.createOrUpdateBudget(userId, periodType, start, end, cat, limit, note);
            response.sendRedirect(request.getContextPath() + "/budgets");
        } else {
            response.sendError(400, "Unsupported action");
        }
    }

    private Long getUserIdAsLong(Object sessionVal) {
        if (sessionVal instanceof Long) return (Long) sessionVal;
        if (sessionVal instanceof String) return Long.valueOf((String) sessionVal);
        // fallback: if existing app uses UUID, we can hash to long for this module or map elsewhere.
        return 0L;
    }

    private String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int i = 0;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("\"").append(e.getKey()).append("\":");
            Object v = e.getValue();
            if (v instanceof String[]) {
                String[] arr = (String[]) v;
                sb.append("[");
                for (int j = 0; j < arr.length; j++) {
                    if (j > 0) sb.append(",");
                    sb.append("\"").append(arr[j]).append("\"");
                }
                sb.append("]");
            } else if (v instanceof Object[]) {
                Object[] arr = (Object[]) v;
                sb.append("[");
                for (int j = 0; j < arr.length; j++) {
                    if (j > 0) sb.append(",");
                    sb.append(String.valueOf(arr[j]));
                }
                sb.append("]");
            } else {
                sb.append("\"").append(String.valueOf(v)).append("\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}


