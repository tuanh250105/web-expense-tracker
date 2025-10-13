package com.expensemanager.controller;

import com.expensemanager.dao.AccountDAO;
import com.expensemanager.dao.GroupDAO;
import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.dao.UserDAO;
import com.expensemanager.service.DashboardService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet("/api/*")
public class GroupServlet extends HttpServlet {

    private Gson gson;
    private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {
        gson = new Gson();
        emf = Persistence.createEntityManagerFactory("default");
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
            JsonObject jsonBody = gson.fromJson(req.getReader(), JsonObject.class);
            String name = jsonBody.get("name").getAsString();
            String description = jsonBody.get("description").getAsString();

            if (name == null || name.trim().isEmpty()) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Tên nhóm không được để trống.");
                return;
            }
            // Gọi helper để thực thi logic trong một transaction
            executeInTransaction(resp, service -> {
                Map<String, Object> newGroup = service.createGroup(name, description);
                sendResponse(resp, HttpServletResponse.SC_CREATED, newGroup);
            });
        } catch (JsonSyntaxException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Dữ liệu JSON không hợp lệ.");
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage());
        }
    }

    private void handleDeleteGroup(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UUID groupId = UUID.fromString(req.getPathInfo().split("/")[2]);
            executeInTransaction(resp, service -> {
                service.deleteGroup(groupId);
                sendResponse(resp, HttpServletResponse.SC_OK, Map.of("message", "Xóa nhóm thành công."));
            });
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "ID nhóm không hợp lệ hoặc thiếu.");
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage());
        }
    }

    private void handleAddMember(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UUID groupId = UUID.fromString(req.getPathInfo().split("/")[2]);
            JsonObject jsonBody = gson.fromJson(req.getReader(), JsonObject.class);
            UUID userId = UUID.fromString(jsonBody.get("userId").getAsString());

            executeInTransaction(resp, service -> {
                // SỬA LỖI: Tên phương thức đúng là "addMember"
                service.addMember(groupId, userId);
                sendResponse(resp, HttpServletResponse.SC_OK, Map.of("message", "Thêm thành viên thành công."));
            });
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage());
        }
    }

    private void handleRemoveMember(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String[] parts = req.getPathInfo().split("/");
            UUID groupId = UUID.fromString(parts[2]);
            UUID userId = UUID.fromString(parts[4]);

            executeInTransaction(resp, service -> {
                // SỬA LỖI: Tên phương thức đúng là "removeMember"
                service.removeMember(groupId, userId);
                sendResponse(resp, HttpServletResponse.SC_OK, Map.of("message", "Xóa thành viên thành công."));
            });
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage());
        }
    }

    private void handleSearchUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String nameQuery = req.getParameter("name");
        if (nameQuery == null || nameQuery.trim().isEmpty()) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Cần cung cấp tham số 'name' để tìm kiếm.");
            return;
        }

        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            UserDAO userDAO = new UserDAO(em);
            GroupDAO groupDAO = new GroupDAO(em);
            TransactionDAO transactionDAO = new TransactionDAO();
            // SỬA LỖI: AccountDAO cần EntityManager khi khởi tạo
            AccountDAO accountDAO = new AccountDAO();

            DashboardService service = new DashboardService(transactionDAO, groupDAO, userDAO, accountDAO);

            List<Map<String, String>> users = service.searchUsersByName(nameQuery);
            sendResponse(resp, HttpServletResponse.SC_OK, users);
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    // --- HELPERS ---

    private void executeInTransaction(HttpServletResponse resp, TransactionalOperation operation) throws IOException {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();

            // Khởi tạo các DAO cần thiết
            UserDAO userDAO = new UserDAO(em);
            GroupDAO groupDAO = new GroupDAO(em);
            TransactionDAO transactionDAO = new TransactionDAO();
            // SỬA LỖI: AccountDAO cần EntityManager khi khởi tạo
            AccountDAO accountDAO = new AccountDAO();

            // Khởi tạo Service và "tiêm" các DAO vào
            DashboardService service = new DashboardService(transactionDAO, groupDAO, userDAO, accountDAO);

            em.getTransaction().begin();
            operation.execute(service); // Thực thi hành động nghiệp vụ
            em.getTransaction().commit();

        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            // Ném lại lỗi để có thể bắt ở các handler và gửi thông báo lỗi phù hợp
            throw new IOException(e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @FunctionalInterface
    interface TransactionalOperation {
        void execute(DashboardService service) throws Exception;
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

    @Override
    public void destroy() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
