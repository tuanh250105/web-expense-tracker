package com.expensemanager.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "img_src")
    private String imgSrc;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // --- Mối quan hệ ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "group")
    private Set<GroupMember> groupMembers; // SỬA LỖI: Đổi thành Set<GroupMember>

    @OneToMany(mappedBy = "group")
    private Set<GroupExpense> groupExpenses;

    // --- Getters and Setters ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImgSrc() { return imgSrc; }
    public void setImgSrc(String imgSrc) { this.imgSrc = imgSrc; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public Set<GroupMember> getGroupMembers() { return groupMembers; }
    public void setGroupMembers(Set<GroupMember> groupMembers) { this.groupMembers = groupMembers; }
    public Set<GroupExpense> getGroupExpenses() { return groupExpenses; }
    public void setGroupExpenses(Set<GroupExpense> groupExpenses) { this.groupExpenses = groupExpenses; }
}