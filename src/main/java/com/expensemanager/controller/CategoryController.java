package com.expensemanager.controller;

import com.expensemanager.exception.CategoryInUseException;
import com.expensemanager.exception.DuplicateCategoryException;
import com.expensemanager.model.Category;
import com.expensemanager.model.User;
import com.expensemanager.service.CategoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet("/categories")
public class CategoryController extends HttpServlet {

    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = null;
        UUID userId = null;
        boolean isGuest = false;

        if (session == null || session.getAttribute("user") == null) {
            isGuest = true;
        } else {
            user = (User) session.getAttribute("user");
            userId = user.getId();
        }

        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            try {
                UUID id = UUID.fromString(request.getParameter("id"));
                categoryService.deleteCategory(id);
                response.sendRedirect(request.getContextPath() + "/categories");
                return;
            } catch (CategoryInUseException e) {
                request.setAttribute("error", e.getMessage());
            }
        } else if ("edit".equals(action)) {
            UUID id = UUID.fromString(request.getParameter("id"));
            Category editCategory = categoryService.getCategoryById(id);
            request.setAttribute("editCategory", editCategory);
        }

        List<Category> categories = (userId != null)
                ? categoryService.getCategoriesByUser(userId)
                : List.of();

        request.setAttribute("categories", categories);
        request.setAttribute("readonly", isGuest);
        request.setAttribute("view", "/views/categories.jsp");
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            request.setAttribute("error", "⚠️ Bạn cần đăng nhập để thực hiện thao tác này!");
            request.setAttribute("view", "/views/categories.jsp");
            request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
            return;
        }

        String idParam = request.getParameter("id");
        String name = request.getParameter("name");
        String type = request.getParameter("type");
        String iconPath = request.getParameter("icon");
        String color = request.getParameter("color");
        String parentId = request.getParameter("parentId");

        Category category = new Category();
        category.setName(name);
        category.setType(type);
        category.setIconPath(iconPath);
        category.setColor(color);
        category.setUser(user);

        boolean isNew = (idParam == null || idParam.isEmpty());

        if (!isNew) {
            category.setId(UUID.fromString(idParam));
        }

        if (parentId != null && !parentId.isEmpty()) {
            Category parent = categoryService.getCategoryById(UUID.fromString(parentId));
            category.setParent(parent);
        }

        try {
            // GIAI ĐOẠN 1: XÁC THỰC VÀ LƯU DANH MỤC CHÍNH
            categoryService.validateCategoryName(category);

            if (isNew) {
                categoryService.saveCategory(category, user);
            } else {
                categoryService.updateCategory(category, user);
            }

            // GIAI ĐOẠN 2: TỰ ĐỘNG TẠO DANH MỤC CON (NẾU CẦN)
            boolean isNewParentCategory = isNew && (parentId == null || parentId.isEmpty());
            if (isNewParentCategory) {
                Category childCategory = new Category();
                childCategory.setName(category.getName() + " (Khác)");
                childCategory.setType(category.getType());
                childCategory.setIconPath(category.getIconPath());
                childCategory.setColor(category.getColor());
                childCategory.setUser(user);
                childCategory.setParent(category); // Gán cha là danh mục vừa tạo

                // Kiểm tra tên của danh mục con trước khi lưu
                categoryService.validateCategoryName(childCategory);
                categoryService.saveCategory(childCategory, user);
            }

            // Nếu mọi thứ thành công, chuyển hướng
            response.sendRedirect(request.getContextPath() + "/categories");

        } catch (DuplicateCategoryException e) {
            // BẮT LỖI TÊN TRÙNG LẶP (CHO CẢ CHA VÀ CON)
            request.setAttribute("error", e.getMessage());
            request.setAttribute("editCategory", category); // Giữ lại dữ liệu người dùng đã nhập

            List<Category> categories = categoryService.getCategoriesByUser(user.getId());
            request.setAttribute("categories", categories);
            request.setAttribute("view", "/views/categories.jsp");
            request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
        }
    }
}
