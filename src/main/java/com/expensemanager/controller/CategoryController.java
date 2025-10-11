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

        // 🔹 Kiểm tra session
        if (session == null || session.getAttribute("user_id") == null) {
            System.out.println("⚠️ Session chưa có user_id — người dùng chưa đăng nhập!");
            // Nếu chưa login thì không tải dữ liệu, chỉ hiển thị thông báo
            request.setAttribute("error", "Bạn chưa đăng nhập. Hãy đăng nhập để xem danh mục của bạn!");
            request.setAttribute("view", "/views/categories.jsp");
            request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
            return;
        }

        UUID userId = (UUID) session.getAttribute("user_id");

        // ✅ Lấy danh mục theo user
        List<Category> categories = categoryService.getCategoriesByUser(userId);
        request.setAttribute("categories", categories);

        String action = request.getParameter("action");
        if ("edit".equals(action)) {
            UUID id = UUID.fromString(request.getParameter("id"));
            Category editCategory = categoryService.getCategoryById(id);
            request.setAttribute("editCategory", editCategory);
        } else if ("delete".equals(action)) {
            UUID id = UUID.fromString(request.getParameter("id"));
            categoryService.deleteCategory(id);
            response.sendRedirect(request.getContextPath() + "/categories");
            return;
        }

        // Hiển thị trong layout
        request.setAttribute("view", "/views/categories.jsp");
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user_id") == null) {
            System.out.println("⚠️ Session chưa có user_id — người dùng chưa đăng nhập!");
            request.setAttribute("error", "Bạn chưa đăng nhập. Hãy đăng nhập để thêm danh mục!");
            request.setAttribute("view", "/views/categories.jsp");
            request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
            return;
        }

        UUID userId = (UUID) session.getAttribute("user_id");
        User user = new User();
        user.setId(userId);

        // 🔹 Lấy dữ liệu form
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

        if (parentId != null && !parentId.isEmpty()) {
            Category parent = categoryService.getCategoryById(UUID.fromString(parentId));
            category.setParent(parent);
        }

        if (idParam == null || idParam.isEmpty()) {
            categoryService.saveCategory(category, user);
        } else {
            category.setId(UUID.fromString(idParam));
            categoryService.updateCategory(category);
        }

        response.sendRedirect(request.getContextPath() + "/categories");
    }
}
