package com.expensemanager.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = true)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;

    @Column(nullable = false)
    private String type; // "income" | "expense"

    @Column(nullable = false)
    private int amount;

    @Column
    private String note;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @CreationTimestamp
    @Column(name = "create_at", nullable = false, updatable = false)
    private LocalDateTime create_at;

    @UpdateTimestamp
    @Column(name = "update_at")
    private LocalDateTime update_at;

    public Transaction() {}

    public Transaction(UUID id, Account account, String type, Category category,
                       int amount, String note, LocalDateTime transactionDate,
                       LocalDateTime create_at, LocalDateTime update_at) {
        this.id = id;
        this.account = account;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.note = note;
        this.transactionDate = transactionDate;
        this.create_at = create_at;
        this.update_at = update_at;
    }

    // --- Getter/Setter giữ nguyên snake_case ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public LocalDateTime getCreate_at() { return create_at; }
    public void setCreate_at(LocalDateTime create_at) { this.create_at = create_at; }

    public LocalDateTime getUpdate_at() { return update_at; }
    public void setUpdate_at(LocalDateTime update_at) { this.update_at = update_at; }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", account=" + (account != null ? account.getId() : null) +
                ", category=" + (category != null ? category.getId() : null) +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", note='" + note + '\'' +
                ", transactionDate=" + transactionDate +
                ", create_at=" + create_at +
                ", update_at=" + update_at +
                '}';
    }
}
