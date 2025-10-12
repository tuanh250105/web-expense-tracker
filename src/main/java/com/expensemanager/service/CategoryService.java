package com.expensemanager.service;

import java.util.List;
import java.util.UUID;

import com.expensemanager.dao.CategoryDAO;
import com.expensemanager.model.Category;
import com.expensemanager.model.User;

/**
 * CategoryService - Business logic cho Category
 */
public class CategoryService {

    private final CategoryDAO categoryDAO = new CategoryDAO();

    public List<Category> getCategoriesByUser(UUID userId) {
        return categoryDAO.findAllByUser(userId);
    }
    
    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    public Category getCategoryById(UUID id) {
        return categoryDAO.findById(id).orElse(null);
    }

    public void saveCategory(Category category, User user) {
        category.setUser(user);
        categoryDAO.save(category);
    }

    public void updateCategory(Category category) {
        categoryDAO.update(category);
    }

    public void deleteCategory(UUID id) {
        categoryDAO.delete(id);
    }
}
