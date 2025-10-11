package com.expensemanager.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reward_prizes")
public class RewardPrize {
    @Id
    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "weight", nullable = false)
    private Integer weight = 1;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    // Đơn giản: lưu JSONB thành String; sau này có thể dùng @JdbcTypeCode(JSON)
    @Column(name = "meta")
    private String meta;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    // getters/setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getMeta() { return meta; }
    public void setMeta(String meta) { this.meta = meta; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
