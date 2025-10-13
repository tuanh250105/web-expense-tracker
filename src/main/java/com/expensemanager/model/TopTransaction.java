package com.expensemanager.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing a top transaction
 */
public class TopTransaction {
    private String name;
    private String category;
    private String categoryIcon;
    private String categoryIconClass;
    private BigDecimal amount;
    private LocalDate date;

    // Constructors
    public TopTransaction() {}

    public TopTransaction(String name, String category, String categoryIcon,
                          String categoryIconClass, BigDecimal amount, LocalDate date) {
        this.name = name;
        this.category = category;
        this.categoryIcon = categoryIcon;
        this.categoryIconClass = categoryIconClass;
        this.amount = amount;
        this.date = date;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    public String getCategoryIconClass() {
        return categoryIconClass;
    }

    public void setCategoryIconClass(String categoryIconClass) {
        this.categoryIconClass = categoryIconClass;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    // Helper methods
    public String getFormattedAmount() {
        if (amount == null) return "₫0";
        return "₫" + String.format("%,d", amount.longValue());
    }

    public String getFormattedDate() {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("dd/MM"));
    }

    @Override
    public String toString() {
        return "TopTransaction{" +
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                '}';
    }
}