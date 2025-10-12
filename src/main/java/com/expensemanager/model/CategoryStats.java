package com.expensemanager.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Model class representing a category with statistics
 */
public class CategoryStats {
    private String name;
    private String iconClass;
    private String iconName;
    private BigDecimal totalAmount;
    private int transactionCount;
    private double percentage;
    private TrendType trend;
    private double trendValue;
    private BigDecimal averageTransaction;
    private LocalDateTime lastUpdated;

    // Constructors
    public CategoryStats() {}

    public CategoryStats(String name, String iconClass, String iconName, 
                        BigDecimal totalAmount, int transactionCount) {
        this.name = name;
        this.iconClass = iconClass;
        this.iconName = iconName;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
        this.lastUpdated = LocalDateTime.now();
        
        // Calculate average transaction
        if (transactionCount > 0) {
            this.averageTransaction = totalAmount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP);
        } else {
            this.averageTransaction = BigDecimal.ZERO;
        }
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconClass() {
        return iconClass;
    }

    public void setIconClass(String iconClass) {
        this.iconClass = iconClass;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
        // Recalculate average when total amount changes
        if (transactionCount > 0) {
            this.averageTransaction = totalAmount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP);
        }
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
        // Recalculate average when count changes
        if (transactionCount > 0 && totalAmount != null) {
            this.averageTransaction = totalAmount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP);
        }
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public TrendType getTrend() {
        return trend;
    }

    public void setTrend(TrendType trend) {
        this.trend = trend;
    }

    public double getTrendValue() {
        return trendValue;
    }

    public void setTrendValue(double trendValue) {
        this.trendValue = trendValue;
    }

    public BigDecimal getAverageTransaction() {
        return averageTransaction;
    }

    public void setAverageTransaction(BigDecimal averageTransaction) {
        this.averageTransaction = averageTransaction;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // Helper methods
    public String getFormattedAmount() {
        if (totalAmount == null) return "₫0";
        return "₫" + String.format("%,d", totalAmount.longValue());
    }

    public String getFormattedPercentage() {
        return String.format("%.1f%%", percentage);
    }

    public String getFormattedTrendValue() {
        return String.format("%.1f%%", Math.abs(trendValue));
    }

    public String getTrendIcon() {
        if (trend == null) return "fa-minus";
        switch (trend) {
            case UP: return "fa-arrow-up";
            case DOWN: return "fa-arrow-down";
            default: return "fa-minus";
        }
    }

    public String getTrendClass() {
        if (trend == null) return "stable";
        switch (trend) {
            case UP: return "up";
            case DOWN: return "down";
            default: return "stable";
        }
    }

    @Override
    public String toString() {
        return "CategoryStats{" +
                "name='" + name + '\'' +
                ", totalAmount=" + totalAmount +
                ", transactionCount=" + transactionCount +
                ", percentage=" + percentage +
                ", trend=" + trend +
                '}';
    }

    // Enum for trend types
    public enum TrendType {
        UP, DOWN, STABLE
    }
}