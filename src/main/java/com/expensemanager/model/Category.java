package com.expensemanager.model;

import jakarta.persistence.*;
import java.sql.Date;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

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