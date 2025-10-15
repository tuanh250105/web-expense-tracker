package com.expensemanager.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DaySummary {
    private LocalDate date;
    private BigDecimal income;
    private BigDecimal expense;

    public DaySummary() {}

    public DaySummary(LocalDate date, BigDecimal income, BigDecimal expense) {
        this.date = date;
        this.income = income;
        this.expense = expense;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public void setIncome(BigDecimal income) {
        this.income = income;
    }

    public BigDecimal getExpense() {
        return expense;
    }

    public void setExpense(BigDecimal expense) {
        this.expense = expense;
    }
}
