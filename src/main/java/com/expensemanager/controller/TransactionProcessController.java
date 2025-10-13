package com.expensemanager.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.expensemanager.service.SepayService.SepayTransaction;
import com.expensemanager.util.JpaUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controller để xử lý việc lưu giao dịch từ pending vào database
 */
@WebServlet("/api/transaction-process")
public class TransactionProcessController extends HttpServlet {

    private Gson gson;

    @Override
    public void init() throws ServletException {
        try {
            gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .create();
            System.out.println("✅ TransactionProcessController initialized!");
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize TransactionProcessController: " + e.getMessage());
            throw new ServletException("Failed to initialize TransactionProcessController", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String action = request.getParameter("action");

            if ("save".equals(action)) {
                // Lưu 1 transaction cụ thể
                saveTransaction(request, response);
            } else if ("save-all".equals(action)) {
                // Lưu tất cả pending transactions
                saveAllTransactions(request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid action\"}");
            }

        } catch (Exception e) {
            System.err.println("❌ TransactionProcessController error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Lưu 1 transaction với category được chọn
     */
    private void saveTransaction(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String referenceNumber = request.getParameter("reference");
        String categoryIdStr = request.getParameter("categoryId");
        String note = request.getParameter("note");

        if (referenceNumber == null || categoryIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Missing reference or categoryId\"}");
            return;
        }

        int categoryId;
        try {
            categoryId = Integer.parseInt(categoryIdStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Invalid categoryId\"}");
            return;
        }

        // Lấy pending transaction từ session
        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        List<SepayTransaction> pendingTransactions =
                (List<SepayTransaction>) session.getAttribute("pendingBankTransactions");

        if (pendingTransactions == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"No pending transactions found\"}");
            return;
        }

        // Tìm transaction với reference number
        SepayTransaction targetTransaction = null;
        for (SepayTransaction tx : pendingTransactions) {
            if (referenceNumber.equals(tx.getReferenceNumber())) {
                targetTransaction = tx;
                break;
            }
        }

        if (targetTransaction == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\":\"Transaction not found\"}");
            return;
        }

        // Lưu vào database
        boolean success = saveTransactionToDatabase(targetTransaction, categoryId, note);

        if (success) {
            // Đánh dấu là đã xử lý
            targetTransaction.setProcessed(true);
            response.getWriter().write("{\"status\":\"success\", \"message\":\"Transaction saved successfully\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Failed to save transaction\"}");
        }
    }

    /**
     * Lưu tất cả pending transactions
     */
    private void saveAllTransactions(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        List<SepayTransaction> pendingTransactions =
                (List<SepayTransaction>) session.getAttribute("pendingBankTransactions");

        if (pendingTransactions == null || pendingTransactions.isEmpty()) {
            response.getWriter().write("{\"status\":\"success\", \"message\":\"No transactions to save\", \"count\":0}");
            return;
        }

        int savedCount = 0;
        for (SepayTransaction tx : pendingTransactions) {
            if (!tx.isProcessed()) {
                // Sử dụng category mặc định = 1 (Uncategorized)
                if (saveTransactionToDatabase(tx, 1, "Imported from bank")) {
                    tx.setProcessed(true);
                    savedCount++;
                }
            }
        }

        response.getWriter().write("{\"status\":\"success\", \"message\":\"Saved " + savedCount + " transactions\", \"count\":" + savedCount + "}");
    }

    /**
     * Lưu transaction vào database
     */
    private boolean saveTransactionToDatabase(SepayTransaction sepayTx, int categoryId, String note) {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            // Tạo SQL native để insert vào transactions table
            String sql = """
                INSERT INTO transactions (amount, note, transaction_date, category_id, account_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, 1, ?, ?)
                """;

            LocalDateTime now = LocalDateTime.now();
            String noteToSave = note != null ? note : sepayTx.getContent();

            em.createNativeQuery(sql)
                    .setParameter(1, sepayTx.getAmount())
                    .setParameter(2, noteToSave)
                    .setParameter(3, sepayTx.getTransactionDate())
                    .setParameter(4, categoryId)
                    .setParameter(5, now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .setParameter(6, now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .executeUpdate();

            em.getTransaction().commit();

            System.out.println("✅ Saved transaction: " + sepayTx.getReferenceNumber() + " - " + sepayTx.getAmount());
            return true;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to save transaction: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public void destroy() {
        // JpaUtil sẽ tự quản lý EntityManagerFactory
        System.out.println("✅ TransactionProcessController destroyed!");
    }
}