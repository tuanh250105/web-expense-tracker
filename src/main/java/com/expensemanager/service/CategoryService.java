package com.expensemanager.service;

import com.expensemanager.dao.CategoryDAO;
import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.exception.CategoryInUseException;
import com.expensemanager.exception.DuplicateCategoryException;
import com.expensemanager.model.Category;
import com.expensemanager.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryService{

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public List<Category> getCategoriesByUser(UUID userId) {
        return categoryDAO.findAllByUser(userId);
    }

    public List<Category> getAllCategories(UUID userId) {
        return categoryDAO.findAll(userId);
    }

    public Category getCategoryById(UUID id) {
        return categoryDAO.findById(id);
    }

    public void validateCategoryName(Category category) {
        if (category == null || category.getUser() == null) return;

        Optional<Category> existingCategory = categoryDAO.findByUserIdAndExactName(category.getUser().getId(), category.getName());

        if (existingCategory.isPresent()) {
            boolean isSameObject = category.getId() != null && existingCategory.get().getId().equals(category.getId());
            if (!isSameObject) {
                throw new DuplicateCategoryException("Tên danh mục '" + category.getName() + "' đã tồn tại. Vui lòng chọn tên khác.");
            }
        }
    }

    // Các phương thức save và update giờ chỉ tập trung vào việc lưu, không kiểm tra nữa.
    public void saveCategory(Category category, User user) {
        category.setUser(user);
        categoryDAO.save(category);
    }

    public void updateCategory(Category category, User user) {
        category.setUser(user);
        categoryDAO.update(category);
    }

    public void deleteCategory(UUID id) {
        if (isCategoryInUse(id)) {
            throw new CategoryInUseException("Không thể xóa danh mục này vì đang có giao dịch sử dụng.");
        }
        categoryDAO.delete(id);
    }

    public boolean isCategoryInUse(UUID categoryId) {
        return transactionDAO.countByCategoryId(categoryId) > 0;
    }

    public List<Category> getCategoriesByType(String type, UUID userId) {
        return categoryDAO.findByType(type, userId);
    }

    public long countCategoriesByUser(UUID userId) {
        return categoryDAO.countByUserId(userId);
    }

    public long countCategoriesByUserAndType(UUID userId, String type) {
        return categoryDAO.countByUserIdAndType(userId, type);
    }

    public List<Category> searchByName(UUID userId, String name) {
        return categoryDAO.findByUserIdAndName(userId, name);
    }

    public List<Category> getTopUsedCategories(UUID userId, int limit) {
        return categoryDAO.findTopCategoriesByUsage(userId, limit);
    }


}
