package com.expensemanager.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private String type;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private int amount;
    private String note;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    private LocalDateTime create_at;

    private LocalDateTime update_at;


    public Transaction(){

    }

    public Transaction(int id, Account account, String type, Category category, int amount, String note, LocalDateTime transactionDate, LocalDateTime create_at, LocalDateTime update_at) {
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

    public int getId() {
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

    public int getAmount() {
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

    public void setId(int id) {
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

    public void setAmount(int amount) {
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
}
