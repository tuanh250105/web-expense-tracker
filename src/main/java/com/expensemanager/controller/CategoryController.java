package com.expensemanager.controller;

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

        // 🔹 Nếu chưa đăng nhập → tạo user test (fakeUser)
        if (session == null || session.getAttribute("user") == null) {
            System.out.println("⚠️ Chưa đăng nhập — bật chế độ test với user mặc định.");
            isGuest = true;
        } else {
            user = (User) session.getAttribute("user");
            userId = user.getId();
        }

        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            UUID id = UUID.fromString(request.getParameter("id"));
            categoryService.deleteCategory(id);
            response.sendRedirect(request.getContextPath() + "/categories");
            return;
        } else if ("edit".equals(action)) {
            UUID id = UUID.fromString(request.getParameter("id"));
            Category editCategory = categoryService.getCategoryById(id);
            request.setAttribute("editCategory", editCategory);
        }

        // 📋 Lấy danh mục của user (test hoặc thật)
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

        // ⚠️ Nếu chưa đăng nhập thì báo lỗi
        if (user == null) {
            request.setAttribute("error", "⚠️ Bạn cần đăng nhập để thêm hoặc chỉnh sửa danh mục!");
            request.setAttribute("view", "/views/categories.jsp");
            request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
            return;
        }

        // 🔹 Lấy dữ liệu form
        String idParam = request.getParameter("id");
        String name = request.getParameter("name");
        String type = request.getParameter("type");
        String iconPath = request.getParameter("icon");
        String color = request.getParameter("color");
        String parentId = request.getParameter("parentId");

        Category category;
        boolean isNew = (idParam == null || idParam.isEmpty());

        if (isNew) {
            // ✅ Tạo mới category
            category = new Category();
            category.setUser(user);
        } else {
            // ✅ Cập nhật category đã có
            UUID categoryId = UUID.fromString(idParam);
            category = categoryService.getCategoryById(categoryId);

            // Nếu category không tồn tại hoặc khác user → báo lỗi
            if (category == null || category.getUser() == null ||
                    !category.getUser().getId().equals(user.getId())) {
                request.setAttribute("error", "❌ Không tìm thấy danh mục hoặc bạn không có quyền chỉnh sửa.");
                List<Category> categories = categoryService.getCategoriesByUser(user.getId());
                request.setAttribute("categories", categories);
                request.setAttribute("view", "/views/categories.jsp");
                request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
                return;
            }
        }

        // ✅ Cập nhật thông tin từ form
        category.setName(name);
        category.setType(type);
        category.setIconPath(iconPath);
        category.setColor(color);

        if (parentId != null && !parentId.isEmpty()) {
            Category parent = categoryService.getCategoryById(UUID.fromString(parentId));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        // ✅ Lưu hoặc cập nhật
        if (isNew) {
            categoryService.saveCategory(category, user);
        } else {
            categoryService.updateCategory(category, user); // 👈 gắn lại user khi cập nhật
        }

        response.sendRedirect(request.getContextPath() + "/categories");
    }
}
