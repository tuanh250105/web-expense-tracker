package com.expensemanager.service;

import com.expensemanager.dao.CategoryDAO;
import com.expensemanager.model.Category;
import com.expensemanager.model.User;

import java.util.List;
import java.util.UUID;

/**
 * Service layer cho Category
 * Quản lý logic lưu, cập nhật, xóa, truy vấn danh mục
 */
public class CategoryService {

    private final CategoryDAO categoryDAO = new CategoryDAO();

    /**
     * Lấy danh mục theo user
     */
    public List<Category> getCategoriesByUser(UUID userId) {
        return categoryDAO.findAllByUser(userId);
    }

    /**
     * Lấy toàn bộ danh mục
     */
    public List<Category> getAllCategories(UUID userId) {
        return categoryDAO.findAll(userId); // ✅ đổi từ getAll() sang findAll()
    }

    /**
     * Lấy danh mục theo ID
     */
    public Category getCategoryById(UUID id) {
        return categoryDAO.findById(id);
    }

    /**
     * Thêm mới danh mục (có gắn User)
     */
    public void saveCategory(Category category, User user) {
        category.setUser(user);
        categoryDAO.save(category);
    }

    /**
     * Cập nhật danh mục (giữ nguyên user)
     */
    public void updateCategory(Category category) {
        categoryDAO.update(category);
    }

    /**
     * Cập nhật danh mục có gắn user (phiên bản mới cho Controller hiện tại)
     */
    public void updateCategory(Category category, User user) {
        category.setUser(user);
        categoryDAO.update(category);
    }

    /**
     * Xóa danh mục theo ID
     */
    public void deleteCategory(UUID id) {
        categoryDAO.delete(id);
    }

    /**
     * Lấy danh mục theo loại (income/expense)
     */
    public List<Category> getCategoriesByType(String type, UUID userId) {
        return categoryDAO.findByType(type, userId); // ✅ đổi từ getByType() sang findByType()
    }

    /**
     * Đếm tổng số danh mục của 1 user
     */
    public long countCategoriesByUser(UUID userId) {
        return categoryDAO.countByUserId(userId);
    }

    /**
     * Đếm danh mục theo loại
     */
    public long countCategoriesByUserAndType(UUID userId, String type) {
        return categoryDAO.countByUserIdAndType(userId, type);
    }

    /**
     * Tìm danh mục theo tên
     */
    public List<Category> searchByName(UUID userId, String name) {
        return categoryDAO.findByUserIdAndName(userId, name);
    }

    /**
     * Lấy danh mục phổ biến (dựa theo số lần dùng)
     */
    public List<Category> getTopUsedCategories(UUID userId, int limit) {
        return categoryDAO.findTopCategoriesByUsage(userId, limit);
    }
}
