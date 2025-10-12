package com.expensemanager.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private String type;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private BigDecimal amount;
    private String note;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @CreationTimestamp // Tự động gán thời gian khi tạo mới
    private LocalDateTime create_at;

    @CreationTimestamp // Tự động gán thời gian khi tạo mới
    private LocalDateTime update_at;


    public Transaction(){

    }

    public Transaction(UUID id, Account account, String type, Category category, BigDecimal amount, String note, LocalDateTime transactionDate, LocalDateTime create_at, LocalDateTime update_at) {
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

    public UUID getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public String getType() {
        return type;
    }

    public Category getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getNote() {
        return note;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public LocalDateTime getCreate_at() {
        return create_at;
    }

    public LocalDateTime getUpdate_at() {
        return update_at;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setCreate_at(LocalDateTime create_at) {
        this.create_at = create_at;
    }

    public void setUpdate_at(LocalDateTime update_at) {
        this.update_at = update_at;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "update_at=" + update_at +
                ", create_at=" + create_at +
                ", transactionDate=" + transactionDate +
                ", note='" + note + '\'' +
                ", amount=" + amount +
                ", category=" + category +
                ", type='" + type + '\'' +
                ", account=" + account +
                ", id=" + id +
                '}';
    }
}