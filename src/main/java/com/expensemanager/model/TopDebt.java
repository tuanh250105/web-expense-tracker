package com.expensemanager.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing a top debt
 */
public class TopDebt {
    private String name;
    private String lender;
    private BigDecimal amount;
    private LocalDate dueDate;
    private DebtPriority priority;

    // Constructors
    public TopDebt() {}

    public TopDebt(String name, String lender, BigDecimal amount, LocalDate dueDate, DebtPriority priority) {
        this.name = name;
        this.lender = lender;
        this.amount = amount;
        this.dueDate = dueDate;
        this.priority = priority;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLender() {
        return lender;
    }

    public void setLender(String lender) {
        this.lender = lender;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public DebtPriority getPriority() {
        return priority;
    }

    public void setPriority(DebtPriority priority) {
        this.priority = priority;
    }

    // Helper methods
    public String getFormattedAmount() {
        if (amount == null) return "₫0";
        return "₫" + String.format("%,d", amount.longValue());
    }

    public String getFormattedDueDate() {
        if (dueDate == null) return "";
        return "Hạn: " + dueDate.format(DateTimeFormatter.ofPattern("dd/MM"));
    }

    public String getPriorityClass() {
        if (priority == null) return "normal";
        return switch (priority) {
            case URGENT -> "urgent";
            case WARNING -> "warning";
            case NORMAL -> "normal";
        };
    }

    public String getPriorityIcon() {
        if (priority == null) return "fa-calendar";
        return switch (priority) {
            case URGENT -> "fa-exclamation-triangle";
            case WARNING -> "fa-clock";
            case NORMAL -> "fa-calendar";
        };
    }

    @Override
    public String toString() {
        return "TopDebt{" +
                "name='" + name + '\'' +
                ", lender='" + lender + '\'' +
                ", amount=" + amount +
                ", dueDate=" + dueDate +
                ", priority=" + priority +
                '}';
    }

    // Enum for debt priority
    public enum DebtPriority {
        URGENT, WARNING, NORMAL
    }
}