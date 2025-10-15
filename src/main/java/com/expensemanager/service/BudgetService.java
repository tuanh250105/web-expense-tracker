package com.expensemanager.service;

import com.expensemanager.dao.BudgetDAO;
import com.expensemanager.model.Budget;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.model.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class BudgetService {
    private final BudgetDAO budgetDAO = new BudgetDAO();

    public List<Budget> getAllByUserId(UUID userId) {
        return budgetDAO.getAllByUserId(userId);
    }

    public List<Category> getAllCategoriesByUserId(UUID userId) {
        return budgetDAO.getAllByUserIdForCategories(userId);
    }

    public void addBudget(String categoryId, String limitAmount, String startDate, String endDate, UUID userId) {
        Budget budget = new Budget();
        User user = new User();
        user.setId(userId);
        budget.setUser(user);

        Category category = new Category();
        category.setId(UUID.fromString(categoryId));
        budget.setCategory(category);

        budget.setLimitAmount(new BigDecimal(limitAmount));
        budget.setStartDate(LocalDate.parse(startDate));
        budget.setEndDate(LocalDate.parse(endDate));

        budgetDAO.addBudget(budget);
    }

    public void updateBudget(String id, String categoryId, String limitAmount, String startDate, String endDate) {
        Budget budget = budgetDAO.findById(UUID.fromString(id));
        if (budget == null) {
            throw new IllegalArgumentException("Ngân sách không tồn tại");
        }

        Category category = new Category();
        category.setId(UUID.fromString(categoryId));
        budget.setCategory(category);

        budget.setLimitAmount(new BigDecimal(limitAmount));
        budget.setStartDate(LocalDate.parse(startDate));
        budget.setEndDate(LocalDate.parse(endDate));

        budgetDAO.updateBudget(budget);
    }

    public void deleteBudget(String id) {
        Budget budget = budgetDAO.findById(UUID.fromString(id));
        if (budget == null) {
            throw new IllegalArgumentException("Ngân sách không hợp lệ");
        }
        budgetDAO.deleteBudget(budget);
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
            BigDecimal percentage = spent.divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            return percentage.compareTo(BigDecimal.valueOf(100)) > 0 ? BigDecimal.valueOf(100) : percentage.setScale(2, RoundingMode.HALF_UP);
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
            budgetDAO.updateBudget(budget);
        }
    }

    public List<Budget> getBudgetsByPeriod(UUID userId, String period) {
        List<Budget> all = getAllByUserId(userId);
        return all.stream().filter(b -> b.getPeriodType().equalsIgnoreCase(period)).collect(Collectors.toList());
    }

    public Map<String, List<Budget>> groupByPeriodType(List<Budget> budgets) {
        return budgets.stream().collect(Collectors.groupingBy(Budget::getPeriodType));
    }

    public BigDecimal getMinDailySpending(UUID budgetId) {
        List<BigDecimal> daily = getDailySpent(budgetId);
        return daily.isEmpty() ? BigDecimal.ZERO : daily.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    public BigDecimal getMaxDailySpending(UUID budgetId) {
        List<BigDecimal> daily = getDailySpent(budgetId);
        return daily.isEmpty() ? BigDecimal.ZERO : daily.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    public BigDecimal getAverageDailySpending(UUID budgetId) {
        List<BigDecimal> daily = getDailySpent(budgetId);
        if (daily.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = daily.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(daily.size()), RoundingMode.HALF_UP);
    }

    public BigDecimal calculateRemaining(UUID budgetId) {
        Budget budget = findById(budgetId);
        if (budget == null) return BigDecimal.ZERO;
        return budget.getLimitAmount().subtract(budget.getSpentAmount());
    }

    public String getBudgetStatus(UUID budgetId) {
        BigDecimal percentage = calculateSpentPercentage(budgetId);
        if (percentage.compareTo(BigDecimal.valueOf(100)) > 0) return "OVER";
        if (percentage.compareTo(BigDecimal.valueOf(80)) > 0) return "WARNING";
        return "UNDER";
    }

    public boolean isOverBudget(UUID budgetId) {
        return calculateSpentPercentage(budgetId).compareTo(BigDecimal.valueOf(100)) > 0;
    }

    public List<Budget> getHistoricalBudgets(UUID userId, UUID categoryId) {
        return budgetDAO.getHistoricalBudgets(userId, categoryId);
    }

    public List<BigDecimal> getDailySpent(UUID budgetId) {
        return budgetDAO.getDailySpent(budgetId);
    }

    public List<LocalDate> getDailyDates(UUID budgetId) {
        return budgetDAO.getDailyDates(budgetId);
    }

    // Thêm method mới để lấy transactions mà không sửa logic gốc
    public List<Transaction> getTransactionsForBudget(UUID budgetId) {
        return budgetDAO.getTransactionsForBudget(budgetId);
    }
}