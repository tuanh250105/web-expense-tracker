package com.expensemanager.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "group_expenses")
public class GroupExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paid_by")
    private String userId;

    @Column(name = "amount")
    private Integer amount;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
}
