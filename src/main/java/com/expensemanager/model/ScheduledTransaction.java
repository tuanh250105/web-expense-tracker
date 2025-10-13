package com.expensemanager.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "scheduled_transactions")
public class ScheduledTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "type", nullable = false, length = 20)
    private String type; // income / expense

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "schedule_cron", length = 50)
    private String scheduleCron;

    @Column(name = "next_run", nullable = false)
    private Timestamp nextRun;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    @Transient
    private String categoryName;

    // Constructors
    public ScheduledTransaction() {}

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public UUID getAccountId() { return account != null ? account.getId() : null; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) {
        this.category = category;
        if (category != null) this.categoryName = category.getName();
    }

    public UUID getCategoryId() { return category != null ? category.getId() : null; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public void setAmount(double amount) { this.amount = BigDecimal.valueOf(amount); }

    public void setAmount(String amountStr) {
        if (amountStr != null && !amountStr.isEmpty()) {
            this.amount = new BigDecimal(amountStr.replace(",", ""));
        }
    }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getScheduleCron() { return scheduleCron; }
    public void setScheduleCron(String scheduleCron) { this.scheduleCron = scheduleCron; }

    public String getRepeatLabel() {
        if (scheduleCron == null || scheduleCron.isBlank()) return "Không xác định";
        scheduleCron = scheduleCron.trim();
        // Hàng ngày
        if (scheduleCron.equals("0 0 * * * ?"))
            return "Hàng ngày";
        // Hàng tuần
        if (scheduleCron.matches("0 0 0 \\? \\* [0-7]\\s*"))
            return "Hàng tuần";
        // Hàng tháng (0 0 10 * ?)
        if (scheduleCron.matches("0 0 \\d{1,2} \\* \\?"))
            return "Hàng tháng";
        // Hàng năm (0 0 10 10 ?)
        if (scheduleCron.matches("0 0 \\d{1,2} \\d{1,2} \\?"))
            return "Hàng năm";
        return scheduleCron; // fallback
    }

    public Timestamp getNextRun() { return nextRun; }
    public void setNextRun(Timestamp nextRun) { this.nextRun = nextRun; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}