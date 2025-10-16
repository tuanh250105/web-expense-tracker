package com.expensemanager.controller;

import com.expensemanager.dao.*;
import com.expensemanager.model.User;
import com.expensemanager.service.DashboardService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet("/api/*")
public class GroupServlet extends HttpServlet {

    private Gson gson;

    @Override
    public void init() throws ServletException {
        gson = new Gson();
    }

    // --- BỘ ĐỊNH TUYẾN (ROUTER) ---

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if ("/groups".equals(pathInfo)) {
            handleCreateGroup(req, resp);
        } else if (pathInfo != null && pathInfo.matches("/groups/[^/]+/members")) {
            handleAddMember(req, resp);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint không tồn tại.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.matches("/groups/[^/]+/?")) {
            handleDeleteGroup(req, resp);
        } else if (pathInfo != null && pathInfo.matches("/groups/[^/]+/members/[^/]+")) {
            handleRemoveMember(req, resp);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint không tồn tại.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if ("/users/search".equals(pathInfo)) {
            handleSearchUser(req, resp);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint không tồn tại.");
        }
    }

    // --- CÁC HÀM XỬ LÝ (HANDLERS) ---

    private void handleCreateGroup(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UUID userId = getUserIdFromSession(req, resp);
            if (userId == null) return;

            JsonObject jsonBody = gson.fromJson(req.getReader(), JsonObject.class);
            String name = jsonBody.has("name") ? jsonBody.get("name").getAsString() : null;
            String description = jsonBody.has("description") ? jsonBody.get("description").getAsString() : "";

            if (name == null || name.trim().isEmpty()) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Tên nhóm không được để trống.");
                return;
            }

            DashboardService service = createDashboardService(userId);
            Map<String, Object> newGroup = service.createGroup(name, description);
            sendResponse(resp, HttpServletResponse.SC_CREATED, newGroup);

        } catch (JsonSyntaxException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Dữ liệu JSON không hợp lệ.");
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage());
        }
    }

    private void handleDeleteGroup(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UUID userId = getUserIdFromSession(req, resp);
            if (userId == null) return;

            UUID groupId = UUID.fromString(req.getPathInfo().split("/")[2]);
            DashboardService service = createDashboardService(userId);
            service.deleteGroup(groupId);

            sendResponse(resp, HttpServletResponse.SC_OK, Map.of("message", "Xóa nhóm thành công."));
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "ID nhóm không hợp lệ hoặc thiếu.");
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage());
        }
    }

    private void handleAddMember(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UUID userId = getUserIdFromSession(req, resp);
            if (userId == null) return;

            UUID groupId = UUID.fromString(req.getPathInfo().split("/")[2]);
            JsonObject jsonBody = gson.fromJson(req.getReader(), JsonObject.class);
            UUID memberId = UUID.fromString(jsonBody.get("userId").getAsString());

            DashboardService service = createDashboardService(userId);
            service.addMember(groupId, memberId);

            sendResponse(resp, HttpServletResponse.SC_OK, Map.of("message", "Thêm thành viên thành công."));
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage());
        }
    }

    private void handleRemoveMember(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UUID currentUserId = getUserIdFromSession(req, resp);
            if (currentUserId == null) return;

            String[] parts = req.getPathInfo().split("/");
            UUID groupId = UUID.fromString(parts[2]);
            UUID memberIdToRemove = UUID.fromString(parts[4]);

            DashboardService service = createDashboardService(currentUserId);
            service.removeMember(groupId, memberIdToRemove);

            sendResponse(resp, HttpServletResponse.SC_OK, Map.of("message", "Xóa thành viên thành công."));
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage());
        }
    }

    private void handleSearchUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UUID userId = getUserIdFromSession(req, resp);
            if (userId == null) return;

            String nameQuery = req.getParameter("name");
            if (nameQuery == null || nameQuery.trim().isEmpty()) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Cần cung cấp tham số 'name' để tìm kiếm.");
                return;
            }

            DashboardService service = createDashboardService(userId);
            List<Map<String, Object>> users = service.searchUsersByName(nameQuery);

            sendResponse(resp, HttpServletResponse.SC_OK, users);

        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // --- HỖ TRỢ (HELPERS) ---

    private UUID getUserIdFromSession(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Phiên đăng nhập không tồn tại.");
            return null;
        }

        User user = (User) session.getAttribute("user");
        if (user == null || user.getId() == null) {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập để tiếp tục.");
            return null;
        }

        return user.getId();
    }

    /**
     * ✅ Tạo DashboardService mà không cần EntityManager.
     * DAO tự xử lý EntityManager bên trong.
     */
    private DashboardService createDashboardService(UUID userId) {
        UserDAO userDAO = new UserDAO();
        GroupDAO groupDAO = new GroupDAO();
        TransactionDAO transactionDAO = new TransactionDAO();
        AccountDAO accountDAO = new AccountDAO();
        return new DashboardService(userId, transactionDAO, groupDAO, userDAO, accountDAO);
    }

    private void sendResponse(HttpServletResponse resp, int status, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(status);
        resp.getWriter().write(gson.toJson(data));
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        sendResponse(resp, status, Collections.singletonMap("error", message));
    }
}
