package com.expensemanager.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.expensemanager.model.Account;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.model.User;
import com.expensemanager.service.AccountService;
import com.expensemanager.service.CategoryService;
import com.expensemanager.service.SepayService;
import com.expensemanager.service.SepayService.SepayTransaction;
import com.expensemanager.service.TransactionServicestart;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controller để xử lý việc đồng bộ giao dịch từ ngân hàng.
 * Bao gồm cả API endpoints cho categories và accounts.
 */
@WebServlet(urlPatterns = {
        "/bank-sync",
        "/api/bank-sync",
        "/api/bank-sync/*",
        "/api/categories/*",
        //"/api/accounts/*"
})
public class BankSyncController extends HttpServlet {

    private final SepayService sepayService = new SepayService();
    private final TransactionServicestart transactionService = new TransactionServicestart();
    private final AccountService accountService = new AccountService();
    private final CategoryService categoryService = new CategoryService();
    private final Gson gson = new Gson();

    /**
     * Xử lý yêu cầu GET
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();

        // === API ENDPOINTS ===

        // API: Get Categories
        if ("/api/categories".equals(servletPath) || servletPath.startsWith("/api/categories/")) {
            handleGetCategories(request, response);
            return;
        }

        // API: Get Accounts
        if ("/api/accounts".equals(servletPath) || servletPath.startsWith("/api/accounts/")) {
            handleGetAccounts(request, response);
            return;
        }

        // === BANK SYNC ACTIONS ===

        String action = request.getParameter("action");

        // Handle sync action via GET (for AJAX calls)
        if ("sync".equals(action)) {
            handleSyncAction(request, response);
            return;
        }

        // Handle pending action - get transactions from session
        if ("pending".equals(action)) {
            handlePendingAction(request, response);
            return;
        }

        // === DEFAULT: SHOW PAGE ===

        // Tải dữ liệu cần thiết cho form (dropdown tài khoản, danh mục)
        loadFormData(request);

        // Chuyển tiếp đến view để hiển thị trang
        request.setAttribute("view", "/views/bank-sync.jsp");
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }

    /**
     * API: Get all categories for current user
     * Endpoint: GET /api/categories/
     */
    private void handleGetCategories(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Lấy user từ session
            HttpSession session = request.getSession(false);
            if (session == null) {
                System.err.println("❌ No session found");
                sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập");
                return;
            }

            User user = (User) session.getAttribute("user");
            if (user == null) {
                System.err.println("❌ No user in session");
                sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập");
                return;
            }

            UUID userId = user.getId();
            System.out.println("📂 API: Fetching categories for user: " + userId);

            // Lấy danh sách categories
            List<Category> categories = categoryService.getAllCategories(userId);

            if (categories == null) {
                System.err.println("❌ categoryService returned null");
                categories = new ArrayList<>();
            }

            System.out.println("✅ Found " + categories.size() + " categories");

            // Chuyển đổi sang format đơn giản cho JSON
            List<Map<String, Object>> categoryList = categories.stream()
                    .map(cat -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", cat.getId().toString());
                        map.put("name", cat.getName());
                        map.put("type", cat.getType());
                        map.put("iconPath", cat.getIconPath() != null ? cat.getIconPath() : "fa-solid fa-tag");

                        System.out.println("  📌 Category: " + cat.getName() + " (ID: " + cat.getId() + ")");
                        return map;
                    })
                    .collect(Collectors.toList());

            // Trả về JSON response
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", categoryList);

            String jsonResponse = gson.toJson(result);
            System.out.println("📤 Sending " + categoryList.size() + " categories");

            response.getWriter().write(jsonResponse);

        } catch (Exception e) {
            System.err.println("❌ Error in handleGetCategories: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi khi tải danh mục: " + e.getMessage());
        }
    }

    /**
     * API: Get all accounts for current user
     * Endpoint: GET /api/accounts/
     */
    private void handleGetAccounts(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Lấy user từ session
            HttpSession session = request.getSession(false);
            if (session == null) {
                sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập");
                return;
            }

            User user = (User) session.getAttribute("user");
            if (user == null) {
                sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập");
                return;
            }

            System.out.println("💼 API: Fetching accounts for user: " + user.getId());

            // Lấy danh sách accounts
            List<Account> accounts = accountService.getAllAccounts();

            if (accounts == null) {
                accounts = new ArrayList<>();
            }

            System.out.println("✅ Found " + accounts.size() + " accounts");

            // Chuyển đổi sang format JSON
            List<Map<String, Object>> accountList = accounts.stream()
                    .map(acc -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", acc.getId().toString());
                        map.put("name", acc.getName());
                        map.put("balance", acc.getBalance());
                        map.put("currency", "VND");
                        return map;
                    })
                    .collect(Collectors.toList());

            // Trả về JSON response
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", accountList);

            response.getWriter().write(gson.toJson(result));

        } catch (Exception e) {
            System.err.println("❌ Error in handleGetAccounts: " + e.getMessage());
            e.printStackTrace();
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi khi tải tài khoản: " + e.getMessage());
        }
    }

    /**
     * Handle pending action - return pending transactions from session
     */
    private void handlePendingAction(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            HttpSession session = request.getSession(false);

            if (session == null) {
                response.getWriter().write("{\"transactions\":[]}");
                return;
            }

            @SuppressWarnings("unchecked")
            List<SepayTransaction> pendingTransactions =
                    (List<SepayTransaction>) session.getAttribute("pendingBankTransactions");

            if (pendingTransactions == null || pendingTransactions.isEmpty()) {
                response.getWriter().write("{\"transactions\":[]}");
                return;
            }

            // Build JSON manually (simple approach)
            StringBuilder json = new StringBuilder("{\"transactions\":[");
            for (int i = 0; i < pendingTransactions.size(); i++) {
                SepayTransaction tx = pendingTransactions.get(i);
                if (i > 0) json.append(",");
                json.append("{")
                        .append("\"id\":\"").append(escapeJson(tx.getId())).append("\",")
                        .append("\"amount\":").append(tx.getAmount()).append(",")
                        .append("\"content\":\"").append(escapeJson(tx.getContent())).append("\",")
                        .append("\"transactionDate\":\"").append(escapeJson(tx.getTransactionDate())).append("\",")
                        .append("\"accountNumber\":\"").append(escapeJson(tx.getAccountNumber())).append("\",")
                        .append("\"bankName\":\"").append(escapeJson(tx.getBankName())).append("\",")
                        .append("\"type\":\"").append(escapeJson(tx.getType())).append("\"")
                        .append("}");
            }
            json.append("]}");

            response.getWriter().write(json.toString());
            System.out.println("✅ Returned " + pendingTransactions.size() + " pending transactions");

        } catch (Exception e) {
            System.err.println("❌ Error fetching pending transactions: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    /**
     * Handle sync action from AJAX GET request
     */
    private void handleSyncAction(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            int days = 30;
            String daysParam = request.getParameter("days");
            if (daysParam != null) {
                days = Integer.parseInt(daysParam);
            }

            System.out.println("🏦 Sync request: fetching " + days + " days of transactions");
            List<SepayTransaction> transactions = sepayService.fetchTransactions(days);

            // Save to session for later import
            HttpSession session = request.getSession();
            session.setAttribute("pendingBankTransactions", transactions);

            // Return JSON response
            String json = String.format(
                    "{\"status\":\"success\",\"message\":\"Đã lấy %d giao dịch từ ngân hàng\",\"count\":%d}",
                    transactions.size(), transactions.size()
            );
            response.getWriter().write(json);
            System.out.println("✅ Sync successful: " + transactions.size() + " transactions");

        } catch (Exception e) {
            System.err.println("❌ Sync failed: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String json = String.format(
                    "{\"status\":\"error\",\"message\":\"Lỗi khi đồng bộ: %s\"}",
                    e.getMessage().replace("\"", "'")
            );
            response.getWriter().write(json);
        }
    }

    /**
     * Xử lý yêu cầu POST
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if this is an API call to save single transaction
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.equals("/save")) {
            handleSaveTransaction(request, response);
            return;
        }

        String action = request.getParameter("action");

        try {
            if ("fetch".equals(action)) {
                handleFetchTransactions(request, response);
            } else if ("import".equals(action)) {
                handleImportTransactions(request, response);
            } else {
                request.setAttribute("error", "Hành động không được hỗ trợ.");
                loadFormData(request);
                request.setAttribute("view", "/views/bank-sync.jsp");
                request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
            }
        } catch (Exception e) {
            System.err.println("Lỗi trong BankSyncController doPost: " + e.getMessage());
            request.setAttribute("error", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại.");
            loadFormData(request);
            request.setAttribute("view", "/views/bank-sync.jsp");
            request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
        }
    }

    /**
     * Xử lý logic lấy giao dịch từ SePay và lưu vào session
     */
    private void handleFetchTransactions(HttpServletRequest request, HttpServletResponse response)
            throws IOException, InterruptedException, ServletException {

        System.out.println("🏦 Bắt đầu lấy giao dịch từ SePay...");
        List<SepayTransaction> transactions = sepayService.fetchTransactions(30);

        HttpSession session = request.getSession();
        session.setAttribute("pendingBankTransactions", transactions);

        System.out.println("✅ Lấy thành công " + transactions.size() + " giao dịch.");
        response.sendRedirect(request.getContextPath() + "/bank-sync");
    }

    /**
     * Xử lý logic nhập các giao dịch đã chọn vào database
     */
    private void handleImportTransactions(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        List<SepayTransaction> pendingTransactions =
                (List<SepayTransaction>) session.getAttribute("pendingBankTransactions");

        String[] selectedIndices = request.getParameterValues("selectedTransaction");

        if (selectedIndices == null || selectedIndices.length == 0) {
            request.setAttribute("error", "Bạn chưa chọn giao dịch nào để nhập.");
            loadFormData(request);
            request.getRequestDispatcher("/views/bank-sync.jsp").forward(request, response);
            return;
        }

        System.out.println("📥 Bắt đầu nhập " + selectedIndices.length + " giao dịch đã chọn...");
        int successCount = 0;

        for (String indexStr : selectedIndices) {
            int index = Integer.parseInt(indexStr);
            SepayTransaction sepayTx = pendingTransactions.get(index);

            String accountId = request.getParameter("accountId_" + index);
            String categoryId = request.getParameter("categoryId_" + index);
            String note = sepayTx.getContent();

            Transaction transaction = new Transaction();
            transaction.setAmount(BigDecimal.valueOf(sepayTx.getAmount()));
            transaction.setNote(note);
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setType(sepayTx.getAmount() > 0 ? "income" : "expense");

            if (accountId != null && !accountId.isEmpty()) {
                Account account = accountService.getAccountById(UUID.fromString(accountId));
                transaction.setAccount(account);
            }
            if (categoryId != null && !categoryId.isEmpty()) {
                Category category = categoryService.getCategoryById(UUID.fromString(categoryId));
                transaction.setCategory(category);
            }

            transactionService.addTransaction(transaction);
            successCount++;
        }

        session.removeAttribute("pendingBankTransactions");

        System.out.println("✅ Nhập thành công " + successCount + " giao dịch.");
        response.sendRedirect(request.getContextPath() + "/transactions?import_success=" + successCount);
    }

    /**
     * Handle API POST /save - Save single transaction from bank sync
     */
    private void handleSaveTransaction(HttpServletRequest request, HttpServletResponse response)
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

            System.out.println("📥 Received save transaction request: " + jsonString);

            // Parse JSON
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = gson.fromJson(jsonString, Map.class);

            String accountId = (String) jsonMap.get("accountId");
            String categoryId = (String) jsonMap.get("categoryId");
            Object amountObj = jsonMap.get("amount");
            String note = (String) jsonMap.get("note");
            String transactionDateStr = (String) jsonMap.get("transactionDate");
            String traceCode = (String) jsonMap.get("traceCode");

            System.out.println("📝 Parsed data: accountId=" + accountId + ", categoryId=" + categoryId +
                    ", amount=" + amountObj + ", note=" + note);

            // Create transaction
            Transaction transaction = new Transaction();

            // Set amount
            if (amountObj instanceof Number) {
                transaction.setAmount(new BigDecimal(((Number) amountObj).doubleValue()));
            } else {
                transaction.setAmount(new BigDecimal(amountObj.toString()));
            }

            // Set type based on amount
            transaction.setType(transaction.getAmount().compareTo(BigDecimal.ZERO) >= 0 ? "income" : "expense");
            transaction.setNote(note);

            // Set transaction date
            if (transactionDateStr != null && !transactionDateStr.isEmpty()) {
                try {
                    transaction.setTransactionDate(LocalDateTime.parse(transactionDateStr));
                } catch (Exception e) {
                    System.err.println("⚠️ Cannot parse date: " + transactionDateStr + ", using now()");
                    transaction.setTransactionDate(LocalDateTime.now());
                }
            } else {
                transaction.setTransactionDate(LocalDateTime.now());
            }

            // Load and set account
            if (accountId != null && !accountId.isEmpty()) {
                Account account = accountService.getAccountById(UUID.fromString(accountId));
                if (account != null) {
                    transaction.setAccount(account);
                    System.out.println("✅ Account loaded: " + account.getName());
                } else {
                    throw new IllegalArgumentException("Account not found: " + accountId);
                }
            }

            // Load and set category
            if (categoryId != null && !categoryId.isEmpty()) {
                Category category = categoryService.getCategoryById(UUID.fromString(categoryId));
                if (category != null) {
                    transaction.setCategory(category);
                    System.out.println("✅ Category loaded: " + category.getName());
                } else {
                    throw new IllegalArgumentException("Category not found: " + categoryId);
                }
            }

            // Save to database
            System.out.println("💾 Saving transaction to database...");
            transactionService.addTransaction(transaction);
            System.out.println("✅ Transaction saved successfully with ID: " + transaction.getId());

            // Return success response
            String jsonResponse = "{\"success\":true,\"message\":\"Giao dịch đã được lưu thành công\",\"id\":\"" +
                    transaction.getId() + "\"}";
            response.getWriter().write(jsonResponse);

        } catch (Exception e) {
            System.err.println("❌ Error saving transaction: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    /**
     * Tải dữ liệu cần thiết cho các form trên trang
     */
    private void loadFormData(HttpServletRequest request) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            System.err.println("⚠️ No user in session for loadFormData");
            return;
        }

        UUID userId = user.getId();
        List<Account> accounts = accountService.getAllAccounts();
        List<Category> categories = categoryService.getAllCategories(userId);

        System.out.println("📋 Loading form data: " + accounts.size() + " accounts, " + categories.size() + " categories");

        request.setAttribute("accounts", accounts);
        request.setAttribute("categories", categories);
    }

    /**
     * Helper: Send JSON error response
     */
    private void sendJsonError(HttpServletResponse response, int statusCode, String message) throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        response.setStatus(statusCode);
        response.getWriter().write(gson.toJson(error));
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
}