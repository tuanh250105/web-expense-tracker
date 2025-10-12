package com.expensemanager.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Account Entity - Matches actual Supabase accounts schema
 * Table: accounts
 */
@Entity
@Table(name = "accounts")
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    

    @Column(name = "balance")
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "currency", length = 3)
    private String currency = "USD";
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // === CONSTRUCTORS ===
    public Account() {
        // Default constructor for JPA
    }
    
    public Account(User user, String name) {
        this.user = user;
        this.name = name;
        this.balance = BigDecimal.ZERO;
        this.currency = "USD";
    }
    
    public Account(User user, String name, BigDecimal balance) {
        this.user = user;
        this.name = name;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.currency = "USD";
    }
    
    public Account(User user, String name, BigDecimal balance, String currency) {
        this.user = user;
        this.name = name;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.currency = currency != null ? currency : "USD";
    }

    // === JPA LIFECYCLE METHODS ===
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
        if (this.currency == null || this.currency.trim().isEmpty()) {
            this.currency = "USD";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // === GETTERS AND SETTERS ===
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // === BUSINESS LOGIC METHODS ===
    
    /**
     * Add amount to account balance
     */
    public void credit(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.balance = this.balance.add(amount);
        }
    }
    
    /**
     * Subtract amount from account balance
     */
    public void debit(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.balance = this.balance.subtract(amount);
        }
    }
    
    /**
     * Check if account has sufficient balance for debit
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        if (amount == null) return true;
        return this.balance.compareTo(amount) >= 0;
    }
    
    /**
     * Check if account balance is positive
     */
    public boolean hasPositiveBalance() {
        return this.balance.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Check if account balance is zero
     */
    public boolean hasZeroBalance() {
        return this.balance.compareTo(BigDecimal.ZERO) == 0;
    }


    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", user=" + (user != null ? user.getId() : null) +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                ", currency='" + currency + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
