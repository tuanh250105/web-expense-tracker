package com.expensemanager.model;

import java.time.LocalDateTime;

public class Budget {
    private String id;
    private String userId;
    private int categoryId;
    private String categoryName;
    private int limitAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int spentAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Budget() {
    }

    public Budget(String id, String userId, int categoryId, String categoryName, int limitAmount, LocalDateTime startDate, LocalDateTime endDate, int spentAmount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.limitAmount = limitAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.spentAmount = spentAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getLimitAmount() { return limitAmount; }
    public void setLimitAmount(int limitAmount) { this.limitAmount = limitAmount; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public int getSpentAmount() { return spentAmount; }
    public void setSpentAmount(int spentAmount) { this.spentAmount = spentAmount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public double getProgress() {
        return limitAmount > 0 ? (double) spentAmount / limitAmount * 100 : 0;
    }
}