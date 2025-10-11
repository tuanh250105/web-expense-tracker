package com.expensemanager.service;

import com.expensemanager.dao.CategoryDAO; // Vẫn import lớp CategoryDAO
import com.expensemanager.model.Category;

import java.util.List;

public class CategoryService {

    private final CategoryDAO categoryDAO;

    public CategoryService() {
        // Chỉ cần thay đổi dòng này: không còn "Impl" nữa
        this.categoryDAO = new CategoryDAO();
    }

    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    public void addCategory(Category category) {
        categoryDAO.save(category);
    }

    public void deleteCategory(Integer id) {
        categoryDAO.delete(id);
    }

    public Category getCategoryById(Integer id) {
        return categoryDAO.findById(id);
    }

    public void updateCategory(Category updated) {
        categoryDAO.update(updated);
    }
}