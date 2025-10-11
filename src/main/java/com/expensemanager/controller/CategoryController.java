package com.expensemanager.controller;

import com.expensemanager.model.Category;
import com.expensemanager.service.CategoryService;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

@WebServlet("/categories")
public class CategoryController extends HttpServlet {
    private CategoryService categoryService;

    @Override
    public void init() {
        categoryService = new CategoryService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        try {
            switch (action) {
                case "new":
                    showNewForm(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                case "delete":
                    deleteCategory(request, response);
                    break;
                default:
                    listCategories(request, response);
                    break;
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String idParam = request.getParameter("id");
            Category category;

            // Nếu có id => sửa, nếu không có => thêm mới
            if (idParam != null && !idParam.isEmpty()) {
                Integer id = Integer.parseInt(idParam);
                category = categoryService.getCategoryById(id);
            } else {
                category = new Category();
                category.setCreatedAt(new Date(System.currentTimeMillis()));
            }

            // Gán dữ liệu từ form
            String name = request.getParameter("name");
            String type = request.getParameter("type");
            String icon = request.getParameter("icon");
            String color = request.getParameter("color");
            String parentIdParam = request.getParameter("parentId");

            category.setName(name);
            category.setType(type);
            category.setIconPath(icon);
            category.setColor(color);

            if (parentIdParam != null && !parentIdParam.isEmpty()) {
                Category parent = categoryService.getCategoryById(Integer.parseInt(parentIdParam));
                category.setParent(parent);
            } else {
                category.setParent(null);
            }

            // Xử lý thêm hoặc cập nhật
            if (category.getId() != null) {
                categoryService.updateCategory(category);
            } else {
                categoryService.addCategory(category);
            }

            response.sendRedirect("categories");

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    // ------------------ CÁC HÀM HỖ TRỢ ------------------

    private void listCategories(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Category> categories = categoryService.getAllCategories();

        // Cập nhật lại icon và màu sắc (phòng khi giá trị bị null hoặc trống)
        for (Category c : categories) {
            if (c.getIconPath() == null || c.getIconPath().isEmpty()) {
                c.setIconPath("fa-solid fa-tag"); // icon mặc định
            }
            if (c.getColor() == null || c.getColor().isEmpty()) {
                c.setColor("#4a90e2"); // màu mặc định
            }
        }

        request.setAttribute("categories", categories);
        request.setAttribute("view", "/views/categories.jsp");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/layout/layout.jsp");
        dispatcher.forward(request, response);
    }

    private void showNewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        listCategories(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer id = Integer.parseInt(request.getParameter("id"));
        Category editCategory = categoryService.getCategoryById(id);

        // Nếu icon hoặc màu bị trống thì thêm mặc định để hiển thị preview
        if (editCategory.getIconPath() == null || editCategory.getIconPath().isEmpty()) {
            editCategory.setIconPath("fa-solid fa-tag");
        }
        if (editCategory.getColor() == null || editCategory.getColor().isEmpty()) {
            editCategory.setColor("#4a90e2");
        }

        request.setAttribute("editCategory", editCategory);
        request.setAttribute("categories", categoryService.getAllCategories());
        request.setAttribute("view", "/views/categories.jsp");

        RequestDispatcher dispatcher = request.getRequestDispatcher("/layout/layout.jsp");
        dispatcher.forward(request, response);
    }

    private void deleteCategory(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer id = Integer.parseInt(request.getParameter("id"));
        categoryService.deleteCategory(id);
        response.sendRedirect("categories");
    }
}
