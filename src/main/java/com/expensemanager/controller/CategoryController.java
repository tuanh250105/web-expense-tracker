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

        // 🔹 Nếu chưa đăng nhập → cho xem giao diện nhưng không thao tác
        if (session == null || session.getAttribute("user") == null) {
            System.out.println("⚠️ Chưa đăng nhập — hiển thị chế độ khách (readonly).");
            isGuest = true;
        } else {
            user = (User) session.getAttribute("user");
            userId = user.getId();
        }

        String action = request.getParameter("action");

        // ⚠️ Nếu là khách thì không cho thao tác
        if (isGuest && ("delete".equals(action) || "edit".equals(action))) {
            request.setAttribute("error", "⚠️ Bạn cần đăng nhập để thao tác chỉnh sửa danh mục!");
        } else if ("delete".equals(action)) {
            UUID id = UUID.fromString(request.getParameter("id"));
            categoryService.deleteCategory(id);
            response.sendRedirect(request.getContextPath() + "/categories");
            return;
        } else if ("edit".equals(action)) {
            UUID id = UUID.fromString(request.getParameter("id"));
            Category editCategory = categoryService.getCategoryById(id);
            request.setAttribute("editCategory", editCategory);
        }

        // 📋 Nếu có user → lấy danh mục theo user
        // nếu không có user → có thể hiển thị danh mục chung (hoặc trống)
        List<Category> categories = (userId != null)
                ? categoryService.getCategoriesByUser(userId)
                : List.of(); // khách thì không có danh mục riêng

        request.setAttribute("categories", categories);
        request.setAttribute("readonly", isGuest); // ⚠️ Gửi flag ra JSP để disable nút
        request.setAttribute("view", "/views/categories.jsp");
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // ⚠️ Nếu chưa đăng nhập thì chỉ hiển thị cảnh báo, không thêm được
        if (user == null) {
            request.setAttribute("error", "⚠️ Bạn cần đăng nhập để thêm hoặc chỉnh sửa danh mục!");
            request.setAttribute("view", "/views/categories.jsp");
            request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
            return;
        }

        UUID userId = user.getId();

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
