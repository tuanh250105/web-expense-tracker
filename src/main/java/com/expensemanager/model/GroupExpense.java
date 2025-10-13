package com.expensemanager.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_expenses")
public class GroupExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    // Thuộc tính này có tên là "paidBy", khớp với "mappedBy" trong User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by", nullable = false)
    private User paidBy;

    @Column(name = "amount")
    private int amount;

    @Column(name = "description")
    private String description;

    @Column(name = "expense_date")
    private LocalDateTime expenseDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // --- Getters and Setters ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }
    public User getPaidBy() { return paidBy; }
    public void setPaidBy(User paidBy) { this.paidBy = paidBy; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    // ... các getters & setters khác
}

