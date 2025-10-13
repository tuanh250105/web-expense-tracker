package com.expensemanager.service;

import com.expensemanager.dao.BudgetDAO;
import com.expensemanager.model.Budget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetService {
    private final BudgetDAO budgetDAO = new BudgetDAO();

    public Long createOrUpdateBudget(Long userId, String periodType, LocalDate start, LocalDate end, Long categoryId, BigDecimal limitAmount, String note) {
        Budget b = new Budget();
        b.setUserId(userId);
        b.setPeriodType(periodType);
        b.setStartDate(start.atStartOfDay());
        b.setEndDate(end.atStartOfDay());
        b.setCategoryId(categoryId);
        b.setLimitAmount(limitAmount);
        b.setNote(note);
        b.setSpentAmount(BigDecimal.ZERO);
        Long id = budgetDAO.upsert(b);
        recalcSpent(id);
        return id;
    }

    public List<Budget> listBudgets(Long userId, String periodType, Long categoryId) {
        return budgetDAO.findByUser(userId, periodType, categoryId);
    }

    public Map<String, Object> getBudgetDailySeries(Long budgetId) {
        List<BudgetDAO.DailySpent> series = budgetDAO.getDailySeries(budgetId);
        Map<String, Object> result = new HashMap<>();
        result.put("labels", series.stream().map(d -> d.date.toString()).toArray(String[]::new));
        result.put("data", series.stream().map(d -> d.spent).toArray());
        return result;
    }

    public void recalcSpent(Long budgetId) {
        BigDecimal total = budgetDAO.calcTotalSpent(budgetId);
        budgetDAO.updateSpent(budgetId, total);
    }
}


