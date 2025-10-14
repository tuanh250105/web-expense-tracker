package com.expensemanager.controller;

import com.expensemanager.model.Budget;
import com.expensemanager.model.Category;
import com.expensemanager.model.User;
import com.expensemanager.service.BudgetService;
import com.expensemanager.dao.BudgetDAO; // Thay CategoryDAO bằng BudgetDAO
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet("/budget")
public class BudgetController extends HttpServlet {
    private final BudgetService budgetService = new BudgetService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/views/auth/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/views/auth/login.jsp");
            return;
        }
        UUID userId = user.getId();

        String action = request.getParameter("action");
        String budgetId = request.getParameter("budgetId");

        try {
            if ("edit".equals(action) && budgetId != null) {
                Budget editBudget = budgetService.findById(UUID.fromString(budgetId));
                if (editBudget != null) {
                    request.setAttribute("editBudget", editBudget);
                } else {
                    request.setAttribute("errorMessage", "Không tìm thấy ngân sách");
                }
            }

            // Lấy danh sách ngân sách
            List<Budget> budgets = budgetService.getAllByUserId(userId);
            // Cập nhật spent_amount cho tất cả budgets
            for (Budget b : budgets) {
                budgetService.updateSpentAmount(b.getId());
            }
            request.setAttribute("budgets", budgets);
            request.setAttribute("budgetService", budgetService);

            // Lấy danh sách danh mục từ BudgetDAO
            BudgetDAO budgetDAO = new BudgetDAO();
            List<Category> categories = budgetDAO.getAllByUserIdForCategories(userId); // Sử dụng BudgetDAO
            request.setAttribute("categories", categories);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Lỗi khi tải danh sách ngân sách: " + e.getMessage());
        }

        request.setAttribute("view", "/views/budget.jsp");
        request.setAttribute("pageCss", "budgets.css");
        request.setAttribute("pageJs", "budget.js");
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/views/auth/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/views/auth/login.jsp");
            return;
        }
        UUID userId = user.getId();

        String action = request.getParameter("action");

        try {
            if ("add".equals(action)) {
                String categoryId = request.getParameter("categoryId");
                String limitAmount = request.getParameter("limitAmount");
                String startDate = request.getParameter("startDate");
                String endDate = request.getParameter("endDate");

                budgetService.addBudget(categoryId, limitAmount, startDate, endDate, userId);
                session.setAttribute("successMessage", "Thêm ngân sách thành công!");

            } else if ("update".equals(action)) {
                String id = request.getParameter("id");
                String categoryId = request.getParameter("categoryId");
                String limitAmount = request.getParameter("limitAmount");
                String startDate = request.getParameter("startDate");
                String endDate = request.getParameter("endDate");

                budgetService.updateBudget(id, categoryId, limitAmount, startDate, endDate);
                session.setAttribute("successMessage", "Cập nhật ngân sách thành công!");

            } else if ("delete".equals(action)) {
                String id = request.getParameter("id");
                budgetService.deleteBudget(id);
                session.setAttribute("successMessage", "Xóa ngân sách thành công!");
            }

        } catch (IllegalArgumentException e) {
            session.setAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/budget");
    }
}