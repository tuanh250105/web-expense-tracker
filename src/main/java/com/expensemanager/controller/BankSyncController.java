package com.expensemanager.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.expensemanager.model.Account;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.service.AccountService;
import com.expensemanager.service.CategoryService;
import com.expensemanager.service.SepayService;
import com.expensemanager.service.SepayService.SepayTransaction;
import com.expensemanager.service.TransactionService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controller để xử lý việc đồng bộ giao dịch từ ngân hàng.
 * Người dùng sẽ xem trước các giao dịch và chọn để nhập vào hệ thống.
 */
@WebServlet(urlPatterns = {"/bank-sync", "/api/bank-sync", "/api/bank-sync/*"})
public class BankSyncController extends HttpServlet {

    private final SepayService sepayService = new SepayService();
    private final TransactionService transactionService = new TransactionService();
    private final AccountService accountService = new AccountService();
    private final CategoryService categoryService = new CategoryService();

    /**
     * Xử lý yêu cầu GET: Hiển thị trang đồng bộ hoặc xử lý sync action.
     * Trang này sẽ hiển thị các giao dịch đang chờ xử lý (nếu có).
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
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
        
        // Giả sử có session check
        // UUID userId = (UUID) session.getAttribute("user_id");

        // ✅ Tải dữ liệu cần thiết cho form (dropdown tài khoản, danh mục)
        loadFormData(request);

        // ✅ Chuyển tiếp đến view để hiển thị trang
        request.setAttribute("view", "/views/bank-sync.jsp");
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
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
     * Xử lý yêu cầu POST: Thực hiện hành động "Lấy giao dịch" hoặc "Nhập giao dịch".
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
                // 1️⃣ Hành động: LẤY GIAO DỊCH từ SePay API
                handleFetchTransactions(request, response);
            } else if ("import".equals(action)) {
                // 2️⃣ Hành động: NHẬP CÁC GIAO DỊCH ĐÃ CHỌN vào database
                handleImportTransactions(request, response);
            } else {
                // Hành động không hợp lệ
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
     * Xử lý logic lấy giao dịch từ SePay và lưu vào session.
     */
    private void handleFetchTransactions(HttpServletRequest request, HttpServletResponse response)
            throws IOException, InterruptedException, ServletException {
        
        System.out.println("🏦 Bắt đầu lấy giao dịch từ SePay...");
        List<SepayTransaction> transactions = sepayService.fetchTransactions(30); // Lấy 30 ngày

        // ✅ Lưu các giao dịch vừa lấy được vào session để chờ người dùng xác nhận
        HttpSession session = request.getSession();
        session.setAttribute("pendingBankTransactions", transactions);
        
        System.out.println("✅ Lấy thành công " + transactions.size() + " giao dịch. Chuyển hướng về trang review.");
        response.sendRedirect(request.getContextPath() + "/bank-sync");
    }

    /**
     * Xử lý logic nhập các giao dịch đã chọn vào cơ sở dữ liệu.
     */
    private void handleImportTransactions(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        
        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        List<SepayTransaction> pendingTransactions = 
            (List<SepayTransaction>) session.getAttribute("pendingBankTransactions");

        // Lấy danh sách các giao dịch được người dùng tick chọn từ form
        String[] selectedIndices = request.getParameterValues("selectedTransaction");

        if (selectedIndices == null || selectedIndices.length == 0) {
            request.setAttribute("error", "Bạn chưa chọn giao dịch nào để nhập.");
            loadFormData(request); // Tải lại dữ liệu cho form
            request.getRequestDispatcher("/views/bank-sync.jsp").forward(request, response);
            return;
        }

        System.out.println("📥 Bắt đầu nhập " + selectedIndices.length + " giao dịch đã chọn...");
        int successCount = 0;
        
        // Lặp qua các giao dịch được chọn để xử lý
        for (String indexStr : selectedIndices) {
            int index = Integer.parseInt(indexStr);
            SepayTransaction sepayTx = pendingTransactions.get(index);

            // Lấy tài khoản và danh mục người dùng đã chọn cho giao dịch này
            String accountId = request.getParameter("accountId_" + index);
            String categoryId = request.getParameter("categoryId_" + index);
            String note = sepayTx.getContent(); // Fix: getContent() instead of getDescription()
            
            // Chuyển đổi SepayTransaction thành Transaction của hệ thống
            Transaction transaction = new Transaction();
            transaction.setAmount(BigDecimal.valueOf(sepayTx.getAmount()));
            transaction.setNote(note);
            transaction.setTransactionDate(LocalDateTime.now()); // Fix: Use LocalDateTime.now()
            transaction.setType(sepayTx.getAmount() > 0 ? "income" : "expense");
            
            // Get Account and Category objects
            if (accountId != null && !accountId.isEmpty()) {
                Account account = accountService.getAccountById(UUID.fromString(accountId));
                transaction.setAccount(account);
            }
            if (categoryId != null && !categoryId.isEmpty()) {
                Category category = categoryService.getCategoryById(UUID.fromString(categoryId));
                transaction.setCategory(category);
            }
            
            // Lưu vào database
            transactionService.addTransaction(transaction);
            successCount++;
        }
        
        // ✅ Xóa các giao dịch chờ khỏi session sau khi đã xử lý xong
        session.removeAttribute("pendingBankTransactions");

        System.out.println("✅ Nhập thành công " + successCount + " giao dịch.");
        // Chuyển hướng về trang danh sách giao dịch chính với thông báo thành công
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
            
            // Parse JSON using Gson
            com.google.gson.Gson gson = new com.google.gson.Gson();
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> jsonMap = gson.fromJson(jsonString, java.util.Map.class);
            
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
            
            // Set note
            transaction.setNote(note);
            
            // Set transaction date - parse from string or use now
            if (transactionDateStr != null && !transactionDateStr.isEmpty()) {
                try {
                    // Parse ISO datetime string: "2025-10-12T10:14:00"
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
     * Tải dữ liệu cần thiết cho các form trên trang (danh sách tài khoản, danh mục).
     */
    private void loadFormData(HttpServletRequest request) {
        // Giả sử userId được lấy từ session
        // UUID userId = (UUID) request.getSession().getAttribute("user_id");
        List<Account> accounts = accountService.getAllAccounts();
        List<Category> categories = categoryService.getAllCategories(); // Hoặc getCategoriesByUser(userId);
        
        request.setAttribute("accounts", accounts);
        request.setAttribute("categories", categories);
    }
}