package com.expensemanager.controller;

import com.expensemanager.model.Category;
import com.expensemanager.service.CategoryService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Controller quản lý các thao tác: Xem, Thêm, Sửa, Xóa danh mục.
 * Dữ liệu đang sử dụng danh sách giả lập trong CategoryService để test.
 */
@WebServlet("/categories")
public class CategoryController extends HttpServlet {

    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        String idParam = req.getParameter("id");

        // --- Xử lý xóa ---
        if ("delete".equals(action) && idParam != null) {
            categoryService.deleteCategoryForTest(UUID.fromString(idParam));
            resp.sendRedirect(req.getContextPath() + "/categories");
            return;
        }

        // --- Xử lý sửa ---
        if ("edit".equals(action) && idParam != null) {
            Category editCategory = categoryService.getCategoryByIdForTest(UUID.fromString(idParam));
            req.setAttribute("editCategory", editCategory);
        }

        // --- Luôn load danh sách danh mục để hiển thị ---
        List<Category> categories = categoryService.getAllCategoriesForTest();
        req.setAttribute("categories", categories);

        // Trỏ layout
        req.setAttribute("view", "/views/categories.jsp");
        req.getRequestDispatcher("/layout/layout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String idStr = req.getParameter("id");
        String name = req.getParameter("name");
        String type = req.getParameter("type");
        String parentIdStr = req.getParameter("parentId");
        String icon = req.getParameter("icon");
        String color = req.getParameter("color");

        Category category = new Category();
        if (idStr != null && !idStr.isEmpty()) {
            category.setId(UUID.fromString(idStr));
        }
        category.setName(name);
        category.setCategoryType(type);
        if (parentIdStr != null && !parentIdStr.isEmpty()) {
            category.setParentId(UUID.fromString(parentIdStr));
        }
        category.setIcon(icon);
        category.setColor(color);

        // Nếu có id → cập nhật, ngược lại → thêm mới
        if (idStr != null && !idStr.isEmpty()) {
            categoryService.updateCategoryForTest(category);
        } else {
            categoryService.addCategoryForTest(category);
        }

        resp.sendRedirect(req.getContextPath() + "/categories");
    }
}
