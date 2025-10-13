package com.expensemanager.model;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "debts")
public class Debt {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_OVERDUE = "OVERDUE";

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    private UUID userId;

    @Column(name = "creditor_name", nullable = false)
    private String creditorName;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(length = 20)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String note;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getCreditorName() { return creditorName; }
    public void setCreditorName(String creditorName) { this.creditorName = creditorName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getFormattedAmount() {
        if (this.amount == null) return "";
        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return fmt.format(this.amount);
    }

    public boolean isOverdue() {
        if (this.dueDate == null || STATUS_PAID.equalsIgnoreCase(this.status)) return false;
        return this.dueDate.isBefore(LocalDate.now());
    }

    public long getDaysUntilDue() {
        if (this.dueDate == null) return Long.MAX_VALUE;
        return ChronoUnit.DAYS.between(LocalDate.now(), this.dueDate);
    }

    @Override
    public String toString() {
        return "Debt{" +
                "id=" + id +
                ", userId=" + userId +
                ", creditorName='" + creditorName + '\'' +
                ", amount=" + amount +
                ", dueDate=" + dueDate +
                ", status='" + status + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
