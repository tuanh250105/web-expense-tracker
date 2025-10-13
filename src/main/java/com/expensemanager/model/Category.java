package com.expensemanager.model;


import jakarta.persistence.*;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false, length = 20)
    private String type; // income / expense

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Column(name = "icon_path")
    private String iconPath;

    @Column(name = "color")
    private String color;

    @Column(name = "created_at")
    private Date createdAt;

    @OneToMany(mappedBy = "parent")
    private List<Category> children;

    // Constructors
    public Category() {}

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }



    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Category getParent() { return parent; }
    public void setParent(Category parent) { this.parent = parent; }

    public String getIconPath() { return iconPath; }
    public void setIconPath(String iconPath) { this.iconPath = iconPath; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public List<Category> getChildren() { return children; }
    public void setChildren(List<Category> children) { this.children = children; }
}