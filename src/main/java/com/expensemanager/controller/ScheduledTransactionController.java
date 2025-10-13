package com.expensemanager.controller;

import com.expensemanager.model.Account;
import com.expensemanager.model.Category;
import com.expensemanager.model.ScheduledTransaction;
import com.expensemanager.model.User;
import com.expensemanager.service.ScheduledTransactionService;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@WebServlet("/scheduled_transactions")
public class ScheduledTransactionController extends HttpServlet {
    private ScheduledTransactionService service = new ScheduledTransactionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = null;
        UUID userId = null;
        boolean isGuest = false;

        // 🔹 Nếu chưa đăng nhập → tạo user test (fakeUser)
        if (session == null || session.getAttribute("user") == null) {
            System.out.println("⚠️ Chưa đăng nhập — bật chế độ test với user mặc định.");
            isGuest = true;
        } else {
            user = (User) session.getAttribute("user");
            userId = user.getId();
        }

        String action = request.getParameter("action");

        if (action == null || "list".equals(action)) {
            String categorySelect = request.getParameter("categorySelect");
            String category = request.getParameter("category");
            String effectiveCategory = (categorySelect != null && !categorySelect.isEmpty()) ? categorySelect : category;
            String account = request.getParameter("account");
            String from = request.getParameter("from");
            String to = request.getParameter("to");
            String note = request.getParameter("note");
            String[] types = request.getParameterValues("types");
            if (types != null) {
                for (int i = 0; i < types.length; i++) {
                    types[i] = types[i].toLowerCase();
                }
            }

            List<ScheduledTransaction> transactions = service.listTransactions(effectiveCategory, account, from, to, note, types, userId);
            request.setAttribute("transactions", transactions);

            List<Category> allCategories = service.getAllCategories(userId);
            request.setAttribute("allCategories", allCategories);

            List<Account> allAccounts = service.getAccounts(userId);
            request.setAttribute("allAccounts", allAccounts);

            String ajaxHeader = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(ajaxHeader)) {
                request.getRequestDispatcher("/views/scheduled_transactions.jsp").forward(request, response);
            } else {
                request.setAttribute("view", "/views/scheduled_transactions.jsp");
                request.getRequestDispatcher("layout/layout.jsp").forward(request, response);
            }
        } else if ("new".equals(action)) {
            String type = request.getParameter("type");
            List<Category> categories = service.getCategoriesByType(type != null ? type : "income", userId);
            request.setAttribute("categories", categories);
            List<Account> accounts = service.getAccounts(userId);
            request.setAttribute("accounts", accounts);
            request.getRequestDispatcher("/views/new_scheduled_transaction.jsp").forward(request, response);
        } else if ("delete".equals(action)) {
            UUID id = UUID.fromString(request.getParameter("id"));
            service.removeTransaction(id, userId);
            response.sendRedirect(request.getContextPath() + "/scheduled_transactions?action=list");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        User user = null;
        UUID userId = null;
        boolean isGuest = false;

        // 🔹 Nếu chưa đăng nhập → tạo user test (fakeUser)
        if (session == null || session.getAttribute("user") == null) {
            System.out.println("⚠️ Chưa đăng nhập — bật chế độ test với user mặc định.");
            isGuest = true;
        } else {
            user = (User) session.getAttribute("user");
            userId = user.getId();
        }


        String action = request.getParameter("action");
        System.out.println("POST Action = " + action);

        //  SKIP
        if ("skip".equals(action)) {
            try {
                UUID id = UUID.fromString(request.getParameter("id"));
                service.skipTransaction(id, userId);
            } catch (Exception e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Lỗi khi bỏ qua giao dịch!");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/scheduled_transactions?action=list");
            return;
        }

        //CREATE MỚI 
        try {
            String accountIdStr = request.getParameter("accountId");
            String categoryIdStr = request.getParameter("categoryId");
            String nextRunStr = request.getParameter("nextRun");
            String repeatType = request.getParameter("repeatType");
            String scheduleCronParam = request.getParameter("scheduleCron");

            // Lấy Account từ DB
            if (accountIdStr == null || accountIdStr.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Vui lòng chọn tài khoản!");
                return;
            }
            UUID accountId;
            try {
                accountId = UUID.fromString(accountIdStr);
            } catch (IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID tài khoản không hợp lệ!");
                return;
            }
            Account acc = service.findAccountById(accountId, userId);
            if (acc == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Không tìm thấy tài khoản ID='" + accountId + "'. Vui lòng chọn tài khoản hợp lệ!");
                return;
            }

            // Lấy Category từ DB
            UUID categoryId;
            try {
                categoryId = UUID.fromString(categoryIdStr);
            } catch (IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Hạng mục không hợp lệ!");
                return;
            }
            Category cat = service.findCategoryById(categoryId);
            if (cat == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Không tìm thấy danh mục ID=" + categoryId + ". Vui lòng chọn danh mục hợp lệ!");
                return;
            }

            //Tạo ScheduledTransaction
            ScheduledTransaction t = new ScheduledTransaction();
            t.setAccount(acc);
            t.setCategory(cat);
            t.setType(request.getParameter("type"));
            t.setAmount(request.getParameter("amount"));
            t.setNote(request.getParameter("note"));

            // Xử lý scheduleCron với fallback dựa trên nextRun
            t.setScheduleCron(scheduleCronParam);
            if (t.getScheduleCron() == null || t.getScheduleCron().trim().isEmpty()) {
                String cron = generateCronFromRepeatTypeAndNextRun(repeatType, nextRunStr);  // Fallback
                t.setScheduleCron(cron);
            }

            // Parse nextRun với giờ hiện tại (thời điểm tạo)
            Timestamp nextRunTs;
            if (nextRunStr == null || nextRunStr.trim().isEmpty()) {
                nextRunTs = new Timestamp(System.currentTimeMillis());
            } else {
                try {
                    java.time.LocalDateTime now = java.time.LocalDateTime.now();  // Giờ hiện tại
                    java.time.LocalDate date = java.time.LocalDate.parse(nextRunStr.trim());  // Parse ngày từ form
                    java.time.LocalDateTime fullDateTime = date.atTime(now.toLocalTime());  // Kết hợp ngày + giờ hiện tại
                    nextRunTs = Timestamp.valueOf(fullDateTime);
                } catch (Exception e) {
                    nextRunTs = new Timestamp(System.currentTimeMillis());
                }
            }
            t.setNextRun(nextRunTs);

            // Lưu xuống DB
            service.createTransaction(t);
            response.sendRedirect(request.getContextPath() + "/scheduled_transactions?action=list");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Có lỗi xảy ra khi thêm giao dịch định kỳ!");
        }
    }
    // Helper generate cron
    private String generateCronFromRepeatTypeAndNextRun(String repeatType, String nextRunStr) {
        if (repeatType == null) repeatType = "none";
        switch (repeatType.toLowerCase()) {
            case "daily": return "0 0 * * * ?";
            case "weekly":
                if (nextRunStr != null && !nextRunStr.trim().isEmpty()) {
                    try {
                        LocalDate date = LocalDate.parse(nextRunStr);
                        int weekday = date.getDayOfWeek().getValue(); // 1=Thứ 2, 7=Chủ Nhật
                        return "0 0 0 ? * " + weekday;
                    } catch (Exception e) {
                        System.out.println("Lỗi parse weekday từ nextRun: " + nextRunStr);
                    }
                }
                return "0 0 0 ? * 1"; // default Thứ 2

            case "monthly":
                if (nextRunStr != null && !nextRunStr.trim().isEmpty()) {
                    try {
                        java.time.LocalDate date = java.time.LocalDate.parse(nextRunStr);
                        int day = date.getDayOfMonth();
                        if (day >= 1 && day <= 31) {
                            return "0 0 " + day + " * ?";
                        }
                    } catch (Exception e) {
                        System.out.println("Lỗi parse day từ nextRun: " + nextRunStr + " - dùng default ngày 1");
                    }
                }
                return "0 0 1 * ?";  // Default ngày 1
            case "yearly":
                if (nextRunStr != null && !nextRunStr.trim().isEmpty()) {
                    try {
                        java.time.LocalDate date = java.time.LocalDate.parse(nextRunStr);
                        int day = date.getDayOfMonth();
                        int month = date.getMonthValue();
                        if (day >= 1 && day <= 31 && month >= 1 && month <= 12) {
                            return "0 0 " + day + " " + month + " ?";
                        }
                    } catch (Exception e) {
                        System.out.println("Lỗi parse day/month từ nextRun: " + nextRunStr + " - dùng default 1/1");
                    }
                }
                return "0 0 1 1 ?";  // Default ngày 1 tháng 1
            default: return "";  // none
        }
    }
}
