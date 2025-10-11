package com.expensemanager.controller;

import com.expensemanager.dao.CategoryDAO;
import com.expensemanager.model.Account;
import com.expensemanager.model.Category;
import com.expensemanager.model.ScheduledTransaction;
import com.expensemanager.service.ScheduledTransactionService;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@WebServlet("/scheduled_transactions")
public class ScheduledTransactionController extends HttpServlet {
    private ScheduledTransactionService service = new ScheduledTransactionService();
    private CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UUID userId = (UUID) session.getAttribute("userId");
        if (userId == null) {
            userId = UUID.fromString("4efcb554-f4c5-442a-b57c-89d213861501");
            session.setAttribute("userId", userId);
            System.out.println("🔧 Hardcode userId cho test: " + userId);
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

            List<Category> allCategories = categoryDAO.getAll();
            request.setAttribute("allCategories", allCategories);

            String ajaxHeader = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(ajaxHeader)) {
                request.getRequestDispatcher("/views/scheduled_transactions.jsp").forward(request, response);
            } else {
                request.setAttribute("view", "/views/scheduled_transactions.jsp");
                request.getRequestDispatcher("layout/layout.jsp").forward(request, response);
            }
        } else if ("new".equals(action)) {
            String type = request.getParameter("type");
            List<Category> categories = categoryDAO.getByType(type != null ? type : "income");
            request.setAttribute("categories", categories);
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
        HttpSession session = request.getSession();
        UUID userId = (UUID) session.getAttribute("userId");

        // Hardcode userId (nếu chưa có)
        if (userId == null) {
            userId = UUID.fromString("4efcb554-f4c5-442a-b57c-89d213861501");
            session.setAttribute("userId", userId);
            System.out.println("🔧 Hardcode userId cho test: " + userId);
        }

        String action = request.getParameter("action");
        System.out.println("➡️ [POST] Action = " + action);

        // =================== SKIP ===================
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

        // =================== RUN NOW ===================
        if ("runNow".equals(action)) {
            try {
                UUID id = UUID.fromString(request.getParameter("id"));
                service.runNow(id, userId);
            } catch (Exception e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Lỗi khi chạy ngay giao dịch!");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/scheduled_transactions?action=list");
            return;
        }

        // =================== CREATE MỚI ===================
        try {
            String accountName = request.getParameter("account");
            String categoryIdStr = request.getParameter("categoryId");
            String nextRunStr = request.getParameter("nextRun");
            String repeatType = request.getParameter("repeatType");
            String scheduleCronParam = request.getParameter("scheduleCron");

            System.out.println("🧾 [CREATE] Params:");
            System.out.println("   account: " + accountName);
            System.out.println("   categoryId: " + categoryIdStr);
            System.out.println("   nextRun: " + nextRunStr);
            System.out.println("   repeatType: " + repeatType);
            System.out.println("   scheduleCron from form: '" + scheduleCronParam + "'");

            // === Lấy Account từ DB ===
            if (accountName == null || accountName.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Vui lòng chọn tài khoản!");
                return;
            }
            Account acc = service.findAccountByName(accountName, userId);
            if (acc == null) {
                System.out.println("❌ Không tìm thấy Account với name='" + accountName + "'");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Không tìm thấy tài khoản '" + accountName + "'. Vui lòng chọn tài khoản hợp lệ!");
                return;
            }

            // === Lấy Category từ DB ===
            Integer categoryId;
            try {
                categoryId = Integer.parseInt(categoryIdStr);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Hạng mục không hợp lệ!");
                return;
            }
            Category cat = service.findCategoryById(categoryId);
            if (cat == null) {
                System.out.println("❌ Không tìm thấy Category ID=" + categoryId);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Không tìm thấy danh mục ID=" + categoryId + ". Vui lòng chọn danh mục hợp lệ!");
                return;
            }

            // === Tạo ScheduledTransaction ===
            ScheduledTransaction t = new ScheduledTransaction();
            t.setAccount(acc);
            t.setCategory(cat);
            t.setType(request.getParameter("type"));
            t.setAmount(request.getParameter("amount"));
            t.setNote(request.getParameter("note"));

            // ← SỬA: Xử lý scheduleCron với fallback dựa trên nextRun
            t.setScheduleCron(scheduleCronParam);
            if (t.getScheduleCron() == null || t.getScheduleCron().trim().isEmpty()) {
                String cron = generateCronFromRepeatTypeAndNextRun(repeatType, nextRunStr);  // Fallback
                t.setScheduleCron(cron);
                System.out.println("🔧 Fallback scheduleCron từ repeatType '" + repeatType + "' và nextRun '" + nextRunStr + "': " + cron);
            } else {
                System.out.println("✅ Sử dụng scheduleCron từ form: " + t.getScheduleCron());
            }

            // ← SỬA: Parse nextRun với giờ hiện tại (thời điểm tạo)
            Timestamp nextRunTs;
            if (nextRunStr == null || nextRunStr.trim().isEmpty()) {
                // Nếu rỗng, dùng thời điểm hiện tại
                nextRunTs = new Timestamp(System.currentTimeMillis());
                System.out.println("🔧 nextRun rỗng, dùng thời điểm hiện tại: " + nextRunTs);
            } else {
                try {
                    java.time.LocalDateTime now = java.time.LocalDateTime.now();  // Giờ hiện tại
                    java.time.LocalDate date = java.time.LocalDate.parse(nextRunStr.trim());  // Parse ngày từ form
                    java.time.LocalDateTime fullDateTime = date.atTime(now.toLocalTime());  // Kết hợp ngày + giờ hiện tại
                    nextRunTs = Timestamp.valueOf(fullDateTime);
                    System.out.println("✅ nextRun với giờ hiện tại: " + nextRunTs + " (ngày từ form: " + date + ", giờ hiện tại: " + now.toLocalTime() + ")");
                } catch (Exception e) {
                    // Fallback thời điểm hiện tại nếu parse fail
                    nextRunTs = new Timestamp(System.currentTimeMillis());
                    System.out.println("❌ Lỗi parse nextRun: " + nextRunStr + " - dùng thời điểm hiện tại: " + nextRunTs);
                }
            }
            t.setNextRun(nextRunTs);

            // === Lưu xuống DB ===
            service.createTransaction(t);
            System.out.println("✅ Tạo mới ScheduledTransaction thành công!");

            response.sendRedirect(request.getContextPath() + "/scheduled_transactions?action=list");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Có lỗi xảy ra khi thêm giao dịch định kỳ!");
        }
    }

    // ← GIỮ NGUYÊN: Helper generate cron (không thay đổi)
    private String generateCronFromRepeatTypeAndNextRun(String repeatType, String nextRunStr) {
        if (repeatType == null) repeatType = "none";
        switch (repeatType.toLowerCase()) {
            case "daily": return "0 0 * * * ?";
            case "weekly":
                if (nextRunStr != null && !nextRunStr.trim().isEmpty()) {
                    try {
                        LocalDate date = LocalDate.parse(nextRunStr);
                        int weekday = date.getDayOfWeek().getValue(); // 1=Thứ 2, 7=Chủ Nhật
                        return "0 0 0 ? * " + weekday; // ✅ sửa thành đúng chuẩn Quartz
                    } catch (Exception e) {
                        System.out.println("Lỗi parse weekday từ nextRun: " + nextRunStr);
                    }
                }
                return "0 0 0 ? * 1"; // default Thứ 2

            case "monthly":
                if (nextRunStr != null && !nextRunStr.trim().isEmpty()) {
                    try {
                        java.time.LocalDate date = java.time.LocalDate.parse(nextRunStr);
                        int day = date.getDayOfMonth();  // Ngày = 10
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
                        int month = date.getMonthValue();  // Tháng = 10
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