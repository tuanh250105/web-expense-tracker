package com.expensemanager.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "reward_spins")
public class RewardSpin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "prize_code")
    private String prizeCode;

    @Column(name = "prize_label")
    private String prizeLabel;

    @Column(name = "points_spent")
    private Integer pointsSpent = 0;

    @Column(name = "created_at", columnDefinition = "timestamp with time zone")
    private OffsetDateTime createdAt;

    // ✅ Relationships (optional lazy fetch)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prize_code", insertable = false, updatable = false)
    private RewardPrize rewardPrize;

    public RewardSpin() {}

    // ===== Getter/Setter =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getPrizeCode() { return prizeCode; }
    public void setPrizeCode(String prizeCode) { this.prizeCode = prizeCode; }

    public String getPrizeLabel() { return prizeLabel; }
    public void setPrizeLabel(String prizeLabel) { this.prizeLabel = prizeLabel; }

    public Integer getPointsSpent() { return pointsSpent; }
    public void setPointsSpent(Integer pointsSpent) { this.pointsSpent = pointsSpent; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public RewardPrize getRewardPrize() { return rewardPrize; }
    public void setRewardPrize(RewardPrize rewardPrize) { this.rewardPrize = rewardPrize; }
}
