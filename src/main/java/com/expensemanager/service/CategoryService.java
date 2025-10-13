package com.expensemanager.service;

import com.expensemanager.model.Category;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CategoryService {
    public void deleteCategory(UUID id) {}
    public Category getCategoryById(UUID id) { return null; }
    public List<Category> getCategoriesByUser(UUID userId) { return Collections.emptyList(); }
    public void saveCategory(Category c, com.expensemanager.model.User u) {}
    public void updateCategory(Category c) {}
    public List<Category> getAllCategories() { return Collections.emptyList(); }
}


