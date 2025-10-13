package com.expensemanager.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "budgets")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dùng cho JDBC DAO hiện có; tránh JPA map trùng cột
    @Transient
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "period_type", nullable = false, length = 16)
    private String periodType; // WEEK or MONTH

    @Column(name = "start_date")
    private java.time.LocalDateTime startDate;

    @Column(name = "end_date")
    private java.time.LocalDateTime endDate;

    @Column(name = "category_id")
    private Long categoryId; // nullable

    @Column(name = "limit_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal limitAmount;

    @Column(name = "spent_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal spentAmount;

    @Column(name = "note")
    private String note;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }

    public java.time.LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(java.time.LocalDateTime startDate) { this.startDate = startDate; }

    public java.time.LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(java.time.LocalDateTime endDate) { this.endDate = endDate; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public BigDecimal getLimitAmount() { return limitAmount; }
    public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }

    public BigDecimal getSpentAmount() { return spentAmount; }
    public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}


