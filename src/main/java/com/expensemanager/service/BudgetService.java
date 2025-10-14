package com.expensemanager.service;

import com.expensemanager.dao.BudgetDAO;
import com.expensemanager.model.Budget;
import com.expensemanager.model.Category;
import com.expensemanager.model.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class BudgetService {
    private BudgetDAO budgetDAO = new BudgetDAO();

    public List<Budget> getAllByUserId(UUID userId) {
        return budgetDAO.getAllByUserId(userId);
    }

    public void addBudget(String categoryId, String limitAmount, String startDate, String endDate, UUID userId) {
        Budget b = new Budget();
        User user = new User();
        user.setId(userId);
        b.setUser(user);

        Category category = new Category(); // Giả sử có CategoryService để findById, ở đây đơn giản hóa
        category.setId(UUID.fromString(categoryId));
        b.setCategory(category);

        b.setLimitAmount(new BigDecimal(limitAmount));
        b.setStartDate(LocalDate.parse(startDate));
        b.setEndDate(LocalDate.parse(endDate));

        budgetDAO.addBudget(b);
    }

    public void updateBudget(String id, String categoryId, String limitAmount, String startDate, String endDate) {
        Budget b = budgetDAO.findById(UUID.fromString(id));
        if (b == null) {
            throw new IllegalArgumentException("Ngân sách không tồn tại");
        }

        Category category = new Category();
        category.setId(UUID.fromString(categoryId));
        b.setCategory(category);

        b.setLimitAmount(new BigDecimal(limitAmount));
        b.setStartDate(LocalDate.parse(startDate));
        b.setEndDate(LocalDate.parse(endDate));

        budgetDAO.updateBudget(b);
    }

    public void deleteBudget(String id) {
        Budget b = budgetDAO.findById(UUID.fromString(id));
        if (b == null) {
            throw new IllegalArgumentException("Ngân sách không hợp lệ");
        }
        budgetDAO.deleteBudget(b);
    }

    public Budget findById(UUID id) {
        return budgetDAO.findById(id);
    }

    public BigDecimal calculateSpent(UUID budgetId) {
        return budgetDAO.calculateSpent(budgetId);
    }

    public BigDecimal calculateSpentPercentage(UUID budgetId) {
        try {
            Budget budget = findById(budgetId);
            BigDecimal spent = calculateSpent(budgetId);
            if (spent.compareTo(BigDecimal.ZERO) == 0 || budget.getLimitAmount().compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            BigDecimal percentage = spent
                    .divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            if (percentage.compareTo(new BigDecimal("100")) > 0) {
                return new BigDecimal("100");
            }
            return percentage.setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    public void updateSpentAmount(UUID budgetId) {
        Budget budget = findById(budgetId);
        if (budget != null) {
            BigDecimal spent = calculateSpent(budgetId);
            budget.setSpentAmount(spent);
            updateBudget(budget.getId().toString(), budget.getCategory().getId().toString(),
                    budget.getLimitAmount().toString(), budget.getStartDate().toString(), budget.getEndDate().toString());
        }
    }

    public List<Budget> getHistoricalBudgets(UUID userId, UUID categoryId) {
        return budgetDAO.getHistoricalBudgets(userId, categoryId);
    }
}