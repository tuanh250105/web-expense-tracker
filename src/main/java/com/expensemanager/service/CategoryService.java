package com.expensemanager.service;

import com.expensemanager.model.Category;
import java.util.*;

/**
 * Service tạm thời sử dụng danh sách giả lập (ArrayList)
 * để test giao diện và logic CRUD của danh mục.
 */
public class CategoryService {

    private static final List<Category> categoryList = new ArrayList<>();

    static {
        categoryList.add(new Category(UUID.randomUUID(), "Ăn uống", "expense", null, "fa-solid fa-utensils", "#f28b82"));
        categoryList.add(new Category(UUID.randomUUID(), "Đi lại", "expense", null, "fa-solid fa-car", "#aecbfa"));
        categoryList.add(new Category(UUID.randomUUID(), "Lương", "income", null, "fa-solid fa-money-bill", "#ccff90"));
    }

    // --- Lấy tất cả danh mục ---
    public List<Category> getAllCategoriesForTest() {
        return categoryList;
    }

    // --- Thêm danh mục mới ---
    public void addCategoryForTest(Category category) {
        category.setId(UUID.randomUUID());
        categoryList.add(category);
    }

    // --- Xóa danh mục ---
    public void deleteCategoryForTest(UUID id) {
        categoryList.removeIf(c -> c.getId().equals(id));
    }

    // --- Lấy danh mục theo ID ---
    public Category getCategoryByIdForTest(UUID id) {
        for (Category c : categoryList) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    // --- Cập nhật danh mục ---
    public void updateCategoryForTest(Category updated) {
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getId().equals(updated.getId())) {
                categoryList.set(i, updated);
                return;
            }
        }
    }
}
