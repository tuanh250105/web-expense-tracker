package com.expensemanager.controller;

import com.expensemanager.model.Budget;
import com.expensemanager.model.Category;
import com.expensemanager.model.User;
import com.expensemanager.service.BudgetService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@WebServlet("/budget")
public class BudgetController extends HttpServlet {
    private final BudgetService budgetService = new BudgetService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/views/auth/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        UUID userId = user.getId();

        String action = request.getParameter("action");
        String budgetId = request.getParameter("budgetId");
        String filterPeriod = request.getParameter("filterPeriod");

        try {
            if ("edit".equals(action) && budgetId != null) {
                Budget editBudget = budgetService.findById(UUID.fromString(budgetId));
                if (editBudget != null) {
                    request.setAttribute("editBudget", editBudget);
                } else {
                    session.setAttribute("errorMessage", "Không tìm thấy ngân sách");
                }
            }

            List<Budget> budgets;
            if (filterPeriod != null && !filterPeriod.equals("ALL")) {
                budgets = budgetService.getBudgetsByPeriod(userId, filterPeriod);
            } else {
                budgets = budgetService.getAllByUserId(userId);
            }

            if (!budgets.isEmpty()) {
                for (Budget b : budgets) {
                    budgetService.updateSpentAmount(b.getId());
                }
            }

            Map<String, List<Budget>> groupedBudgets = budgetService.groupByPeriodType(budgets);

            request.setAttribute("budgets", budgets);
            request.setAttribute("groupedBudgets", groupedBudgets);
            request.setAttribute("budgetService", budgetService);
            request.setAttribute("currentFilter", filterPeriod != null ? filterPeriod : "ALL");

            List<Category> categories = budgetService.getAllCategoriesByUserId(userId);
            request.setAttribute("categories", categories);

            long activeBudgets = budgets.stream().filter(Budget::isActive).count();
            long exceededBudgets = budgets.stream().filter(b -> budgetService.isOverBudget(b.getId())).count();

            request.setAttribute("activeBudgets", activeBudgets);
            request.setAttribute("exceededBudgets", exceededBudgets);

            // Set view cho layout
            request.setAttribute("view", "/views/budget.jsp");

            // Forward đến layout.jsp
            request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Lỗi khi tải ngân sách: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/budget");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/views/auth/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        String action = request.getParameter("action");
        try {
            if ("add".equals(action)) {
                String categoryId = request.getParameter("categoryId");
                String limitAmount = request.getParameter("limitAmount");
                String startDate = request.getParameter("startDate");
                String endDate = request.getParameter("endDate");
                budgetService.addBudget(categoryId, limitAmount, startDate, endDate, user.getId());
                session.setAttribute("successMessage", "Thêm ngân sách thành công");
            } else if ("update".equals(action)) {
                String id = request.getParameter("id");
                String categoryId = request.getParameter("categoryId");
                String limitAmount = request.getParameter("limitAmount");
                String startDate = request.getParameter("startDate");
                String endDate = request.getParameter("endDate");
                budgetService.updateBudget(id, categoryId, limitAmount, startDate, endDate);
                session.setAttribute("successMessage", "Cập nhật ngân sách thành công");
            } else if ("delete".equals(action)) {
                String id = request.getParameter("id");
                budgetService.deleteBudget(id);
                session.setAttribute("successMessage", "Xóa ngân sách thành công");
            }
            response.sendRedirect(request.getContextPath() + "/budget");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Lỗi: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/budget");
        }
    }
}