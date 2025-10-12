package com.expensemanager.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.expensemanager.model.Account;
import com.expensemanager.model.Transaction;
import com.expensemanager.service.AccountService;
import com.expensemanager.service.TransactionService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 🔹 URL mapping để phù hợp với frontend API calls: /api/bank-history/*
@WebServlet("/api/bank-history/*")
public class BankHistoryController extends HttpServlet {

    // 🔹 Khởi tạo các Service cần thiết, thay vì dùng EntityManager trực tiếp
    private final TransactionService transactionService = new TransactionService();
    private final AccountService accountService = new AccountService();

    /**
     * Xử lý yêu cầu GET: Hiển thị danh sách giao dịch và form thêm mới.
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

        // 🔹 Xử lý các hành động như XÓA giao dịch
        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            try {
                UUID transactionId = UUID.fromString(request.getParameter("id"));
                transactionService.deleteTransaction(transactionId);
                // Sau khi xóa, redirect về trang danh sách để tải lại dữ liệu mới
                response.sendRedirect(request.getContextPath() + "/api/bank-history");
                return; // Kết thúc xử lý tại đây
            } catch (Exception e) {
                // Xử lý lỗi nếu ID không hợp lệ hoặc xóa thất bại
                System.err.println("Lỗi khi xóa giao dịch: " + e.getMessage());
                request.setAttribute("error", "Không thể xóa giao dịch này.");
            }
        }
        
        // ✅ Tải dữ liệu cần thiết cho trang JSP
        loadTransactionData(request);

        // ✅ Chuyển tiếp tới view để hiển thị
        request.setAttribute("view", "/views/transactions.jsp");
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
            String accountIdFilter = request.getParameter("accountId");
            
            System.out.println("🔍 API Request - Loading transactions...");
            System.out.println("📌 Account Filter: " + accountIdFilter);
            
            // Get transactions from service
            List<Transaction> transactions = transactionService.getFilteredTransactions(accountIdFilter);
            
            System.out.println("✅ Found " + transactions.size() + " transactions");
            
            // Build JSON response manually
            StringBuilder json = new StringBuilder();
            json.append("{\"success\":true,\"data\":[");
            
            for (int i = 0; i < transactions.size(); i++) {
                Transaction tx = transactions.get(i);
                if (i > 0) json.append(",");
                
                json.append("{")
                    .append("\"id\":\"").append(tx.getId()).append("\",")
                    .append("\"type\":\"").append(escapeJson(tx.getType())).append("\",")
                    .append("\"amount\":").append(tx.getAmount()).append(",")
                    .append("\"note\":\"").append(escapeJson(tx.getNote())).append("\",")
                    .append("\"transactionDate\":\"").append(tx.getTransactionDate()).append("\",")
                    .append("\"categoryName\":\"").append(tx.getCategory() != null ? escapeJson(tx.getCategory().getName()) : "").append("\",")
                    .append("\"accountName\":\"").append(tx.getAccount() != null ? escapeJson(tx.getAccount().getName()) : "").append("\"")
                    .append("}");
            }
            
            json.append("]}");
            
            response.getWriter().write(json.toString());
            System.out.println("✅ JSON response sent successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Error in API request: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
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
     * Xử lý yêu cầu POST: Thêm mới một giao dịch.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Giả sử có session check
        // HttpSession session = request.getSession(false);
        
        try {
            // 🔹 Lấy dữ liệu từ form thay vì đọc JSON
            String accountIdStr = request.getParameter("accountId");
            String categoryIdStr = request.getParameter("categoryId");
            String note = request.getParameter("note");
            String amountStr = request.getParameter("amount");
            String type = request.getParameter("type"); // "income" hoặc "expense"

            // ✅ Tạo đối tượng Transaction từ dữ liệu form
            Transaction newTransaction = new Transaction();
            newTransaction.setNote(note);
            newTransaction.setType(type);
            
            if (amountStr != null && !amountStr.isEmpty()) {
                newTransaction.setAmount(new BigDecimal(amountStr));
            }
            
            // Lấy Account và Category objects
            if (accountIdStr != null && !accountIdStr.isEmpty()) {
                Account account = accountService.getAccountById(UUID.fromString(accountIdStr));
                newTransaction.setAccount(account);
            }
            if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
                // TODO: Get Category from CategoryService
                // Category category = categoryService.getCategoryById(UUID.fromString(categoryIdStr));
                // newTransaction.setCategory(category);
            }
            
            // ✅ Gọi service để lưu giao dịch
            transactionService.addTransaction(newTransaction);
            
        } catch (NumberFormatException e) {
            System.err.println("Lỗi định dạng số: " + e.getMessage());
            request.setAttribute("error", "Số tiền không hợp lệ.");
            // Tải lại dữ liệu và hiển thị lại form với lỗi
            loadTransactionData(request);
            request.setAttribute("view", "/views/transactions.jsp");
            request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
            return;
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm giao dịch: " + e.getMessage());
            request.setAttribute("error", "Đã xảy ra lỗi khi thêm giao dịch.");
            // Tải lại dữ liệu và hiển thị lại form với lỗi
            loadTransactionData(request);
            request.setAttribute("view", "/views/transactions.jsp");
            request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
            return;
        }

        // ✅ Sau khi thêm thành công, redirect về trang danh sách (Post-Redirect-Get Pattern)
        response.sendRedirect(request.getContextPath() + "/transactions");
    }
    
    /**
     * Phương thức trợ giúp để tải dữ liệu và đặt vào request.
     * Tránh lặp code trong cả doGet và doPost khi cần hiển thị lại trang.
     */
    private void loadTransactionData(HttpServletRequest request) {
        // Lấy các tham số lọc từ URL
        String accountIdFilter = request.getParameter("filterAccountId");
        
        // ✅ Lấy danh sách giao dịch từ service (có thể có bộ lọc)
        List<Transaction> transactions = transactionService.getFilteredTransactions(accountIdFilter);
        
        // ✅ Lấy danh sách tài khoản để hiển thị trong dropdown bộ lọc/form
        List<Account> accounts = accountService.getAllAccounts();
        
        // ✅ Đặt dữ liệu vào request để JSP có thể truy cập
        request.setAttribute("transactions", transactions);
        request.setAttribute("accounts", accounts);
        
        // (Tương tự, bạn có thể tải danh sách categories ở đây)
        // List<Category> categories = categoryService.getCategoriesByUser(...);
        // request.setAttribute("categories", categories);
    }
}