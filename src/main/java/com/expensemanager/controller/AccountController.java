package com.expensemanager.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.expensemanager.model.Account;
import com.expensemanager.model.User;
import com.expensemanager.service.AccountService;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * AccountController - Quản lý các tài khoản ngân hàng (Ví, Thẻ tín dụng, v.v.).
 * Controller này tuân theo mô hình MVC, xử lý logic và chuyển tiếp đến view (JSP).
 */
@WebServlet(urlPatterns = {"/accounts", "/api/accounts", "/api/accounts/*", "/api/bank-history/accounts"})
public class AccountController extends HttpServlet {

    // 🔹 Sử dụng Service Layer để tách biệt logic nghiệp vụ khỏi controller
    private final AccountService accountService = new AccountService();

    /**
     * Xử lý yêu cầu GET: Hiển thị danh sách tài khoản và form để sửa/thêm mới.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if this is an API call (expects JSON response)
        String pathInfo = request.getPathInfo();
        String acceptHeader = request.getHeader("Accept");
        boolean isApiCall = (acceptHeader != null && acceptHeader.contains("application/json")) ||
                (pathInfo != null && !pathInfo.isEmpty());

        if (isApiCall) {
            // Handle API call - return JSON
            handleApiRequest(request, response);
            return;
        }

        // Giả sử có session check tương tự các controller khác
        // ...

        try {
            // 🔹 Kiểm tra tham số 'action' từ URL để xử lý Sửa hoặc Xóa
            String action = request.getParameter("action");

            if ("delete".equals(action)) {
                // Hành động XÓA
                UUID accountId = UUID.fromString(request.getParameter("id"));
                accountService.deleteAccount(accountId);
                // Chuyển hướng về trang danh sách để làm mới dữ liệu
                response.sendRedirect(request.getContextPath() + "/accounts");
                return; // Kết thúc xử lý

            } else if ("edit".equals(action)) {
                // Hành động SỬA: Tải thông tin tài khoản cần sửa vào form
                UUID accountId = UUID.fromString(request.getParameter("id"));
                Account editAccount = accountService.getAccountById(accountId);
                // Đặt vào request để JSP có thể điền vào form
                request.setAttribute("editAccount", editAccount);
            }

        } catch (Exception e) {
            System.err.println("Lỗi trong AccountController doGet: " + e.getMessage());
            request.setAttribute("error", "Đã xảy ra lỗi khi xử lý yêu cầu của bạn.");
        }

        // ✅ Tải danh sách tất cả tài khoản để hiển thị
        loadAllAccounts(request);

        // ✅ Chuyển tiếp đến view để hiển thị trang
        request.setAttribute("view", "/views/account-transactions.jsp");
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }

    /**
     * Handle API requests that expect JSON response
     */
    private void handleApiRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            System.out.println("🔍 AccountController API Request");

            // Check if this is a stats request
            String pathInfo = request.getPathInfo();
            System.out.println("📍 Path info: " + pathInfo);

            if (pathInfo != null && pathInfo.contains("stats")) {
                // Handle stats endpoint for accounts-management page
                handleStatsRequest(request, response);
                return;
            }

            // Get all accounts
            List<Account> accounts = accountService.getAllAccounts();

            System.out.println("✅ Found " + accounts.size() + " accounts");

            // Build JSON response
            StringBuilder json = new StringBuilder();
            json.append("{\"success\":true,\"data\":[");

            for (int i = 0; i < accounts.size(); i++) {
                Account acc = accounts.get(i);
                if (i > 0) json.append(",");

                json.append("{")
                        .append("\"id\":\"").append(acc.getId()).append("\",")
                        .append("\"name\":\"").append(escapeJson(acc.getName())).append("\",")
                        .append("\"balance\":").append(acc.getBalance()).append(",")
                        .append("\"currency\":\"").append(escapeJson(acc.getCurrency())).append("\"")
                        .append("}");
            }

            json.append("]}");

            response.getWriter().write(json.toString());
            System.out.println("✅ JSON response sent successfully");

        } catch (Exception e) {
            System.err.println("❌ Error in AccountController API: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    /**
     * Handle stats request for accounts-management page
     */
    private void handleStatsRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            System.out.println("📊 Handling stats request");

            // Get all accounts with full details
            List<Account> accounts = accountService.getAllAccounts();

            // Build JSON array (not wrapped in success/data)
            StringBuilder json = new StringBuilder();
            json.append("[");

            for (int i = 0; i < accounts.size(); i++) {
                Account acc = accounts.get(i);
                if (i > 0) json.append(",");

                json.append("{")
                        .append("\"id\":\"").append(acc.getId()).append("\",")
                        .append("\"name\":\"").append(escapeJson(acc.getName())).append("\",")
                        .append("\"balance\":").append(acc.getBalance()).append(",")
                        .append("\"currency\":\"").append(escapeJson(acc.getCurrency())).append("\"")
                        .append("}");
            }

            json.append("]");

            response.getWriter().write(json.toString());
            System.out.println("✅ Stats response sent: " + accounts.size() + " accounts");

        } catch (Exception e) {
            System.err.println("❌ Error in stats request: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("[]");
        }
    }

    /**
     * Escape special characters for JSON
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Xử lý yêu cầu POST: Thêm mới hoặc cập nhật một tài khoản.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if this is an API call (expects JSON response)
        String contentType = request.getContentType();
        String acceptHeader = request.getHeader("Accept");
        boolean isApiCall = (contentType != null && contentType.contains("application/json")) ||
                (acceptHeader != null && acceptHeader.contains("application/json"));

        if (isApiCall) {
            handleApiPost(request, response);
            return;
        }

        // Handle form submission (existing code)
        try {
            // 🔹 Lấy dữ liệu từ form HTML
            String idParam = request.getParameter("id");
            String name = request.getParameter("name");
            String balanceStr = request.getParameter("balance");
            String currency = request.getParameter("currency");

            // ✅ Tạo đối tượng Account từ dữ liệu form
            Account account = new Account();
            account.setName(name);
            account.setCurrency(currency);
            // TODO: Lấy User từ session và set vào account
            // User user = userService.findById(userId);
            // account.setUser(user);

            if (balanceStr != null && !balanceStr.isEmpty()) {
                account.setBalance(new BigDecimal(balanceStr));
            } else {
                account.setBalance(BigDecimal.ZERO);
            }

            // 🔹 Phân biệt giữa Thêm mới và Cập nhật dựa vào 'id'
            if (idParam == null || idParam.isEmpty()) {
                // THÊM MỚI: id không tồn tại
                accountService.addAccount(account);
            } else {
                // CẬP NHẬT: id đã có
                account.setId(UUID.fromString(idParam));
                accountService.updateAccount(account);
            }

        } catch (Exception e) {
            System.err.println("Lỗi trong AccountController doPost: " + e.getMessage());
            request.setAttribute("error", "Không thể lưu tài khoản. Vui lòng kiểm tra lại dữ liệu.");

            // Nếu có lỗi, tải lại dữ liệu và hiển thị lại form với thông báo lỗi
            loadAllAccounts(request);
            request.setAttribute("view", "/views/accounts.jsp");
            request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
            return;
        }

        // ✅ Chuyển hướng về trang danh sách sau khi lưu thành công (Post-Redirect-Get)
        response.sendRedirect(request.getContextPath() + "/accounts");
    }

    /**
     * Handle API POST request (JSON)
     */
    private void handleApiPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Read JSON from request body
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            String jsonString = sb.toString();

            System.out.println("📥 Received JSON: " + jsonString);

            // Parse JSON using Gson
            Gson gson = new Gson();
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = gson.fromJson(jsonString, Map.class);

            String name = (String) jsonMap.get("name");
            Object balanceObj = jsonMap.get("balance");
            String currency = (String) jsonMap.get("currency");

            System.out.println("📝 Parsed: name=" + name + ", balance=" + balanceObj + ", currency=" + currency);

            // Create new account
            Account account = new Account();
            account.setName(name);
            account.setCurrency(currency != null && !currency.isEmpty() ? currency : "VND");

            // Handle balance (can be Number or String)
            if (balanceObj != null) {
                if (balanceObj instanceof Number) {
                    account.setBalance(new BigDecimal(((Number) balanceObj).doubleValue()));
                } else {
                    account.setBalance(new BigDecimal(balanceObj.toString()));
                }
            } else {
                account.setBalance(BigDecimal.ZERO);
            }

            // Set user (hardcoded for testing - TODO: get from session)
            User user = new User();
            user.setId(UUID.fromString("67b78d51-4eec-491c-bbf0-30e982def9e0")); // testuser from database
            account.setUser(user);

            System.out.println("💾 Attempting to save account: " + account.getName());
            System.out.println("👤 User ID: " + user.getId());

            // Save account
            try {
                accountService.addAccount(account);
                System.out.println("✅ Account saved to database successfully");
            } catch (Exception saveEx) {
                System.err.println("❌ Failed to save account: " + saveEx.getClass().getName() + " - " + saveEx.getMessage());
                saveEx.printStackTrace();
                throw saveEx; // Re-throw to outer catch
            }

            // Return success response
            String jsonResponse = "{\"success\":true,\"message\":\"Tài khoản đã được thêm thành công\",\"id\":\"" + account.getId() + "\"}";
            response.getWriter().write(jsonResponse);
            System.out.println("✅ Account created successfully with ID: " + account.getId());

        } catch (Exception e) {
            System.err.println("❌ Error in API POST: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();

            // Print full stack trace
            System.err.println("=== Full Stack Trace ===");
            for (StackTraceElement ste : e.getStackTrace()) {
                System.err.println("  at " + ste.toString());
            }

            // Build detailed error message for debugging
            StringBuilder errorDetails = new StringBuilder();
            errorDetails.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());

            // Add cause if exists
            if (e.getCause() != null) {
                errorDetails.append(" | Cause: ").append(e.getCause().getClass().getSimpleName())
                        .append(" - ").append(e.getCause().getMessage());
            }

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"error\":\"" + escapeJson(errorDetails.toString()) + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // PUT requests are handled as POST for now
        handleApiPost(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"error\":\"Account ID is required\"}");
                return;
            }

            String accountId = pathInfo.substring(1); // Remove leading "/"
            System.out.println("🗑️ Deleting account: " + accountId);

            accountService.deleteAccount(UUID.fromString(accountId));

            response.getWriter().write("{\"success\":true,\"message\":\"Tài khoản đã được xóa thành công\"}");
            System.out.println("✅ Account deleted successfully");

        } catch (Exception e) {
            System.err.println("❌ Error in DELETE: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    /**
     * Phương thức trợ giúp để tải danh sách tài khoản và đặt vào request.
     */
    private void loadAllAccounts(HttpServletRequest request) {
        List<Account> accounts = accountService.getAllAccounts();
        request.setAttribute("accounts", accounts);
    }
}