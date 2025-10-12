package com.expensemanager.controller;

import com.expensemanager.model.Transaction;
import com.expensemanager.service.ImportExportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@WebServlet(name = "ImportExportController", value = "/import-export")
@MultipartConfig
public class ImportExportController extends HttpServlet {

    private final ImportExportService importExportService = new ImportExportService();
    private final AccountService accountService = new AccountService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = null;
        UUID userId = null;
        boolean isGuest = false;

        // 🔹 Nếu chưa đăng nhập → hiển thị chế độ khách (readonly)
        if (session == null || session.getAttribute("user") == null) {
            isGuest = true;
            request.setAttribute("error", "⚠️ Bạn đang ở chế độ khách — không thể import hoặc export dữ liệu!");
        } else {
            user = (User) session.getAttribute("user");
            userId = user.getId();
        }

        // 🔹 Lấy danh sách tài khoản (nếu có user), còn không thì danh sách trống
        List<Account> accounts = (userId != null)
                ? accountService.getAccountsByUser(userId)
                : List.of();

        request.setAttribute("readonly", isGuest); // gắn flag để JSP ẩn/khóa nút
        request.setAttribute("accounts", accounts);
        request.setAttribute("view", "/views/import_export.jsp");
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // ⚠️ Nếu chưa đăng nhập → chỉ hiển thị cảnh báo, không thực hiện được thao tác
        if (user == null) {
            loadView(
                    request, response,
                    "⚠️ Bạn cần đăng nhập để thực hiện import hoặc export dữ liệu!",
                    null, List.of()
            );
            return;
        }

        UUID userId = user.getId();
        String action = request.getParameter("action");
        if (action == null || action.isBlank()) action = "view";

        try {
            switch (action) {
                case "preview": {
                    Part filePart = request.getPart("file");
                    if (filePart == null || filePart.getSize() == 0) {
                        loadView(request, response, "Vui lòng chọn file để import.", null, accountService.getAccountsByUser(userId));
                        return;
                    }

                    String fileName = filePart.getSubmittedFileName();
                    String fileType = getFileExtension(fileName);
                    String accountIdStr = request.getParameter("account");

                    if (accountIdStr == null || accountIdStr.isBlank()) {
                        loadView(request, response, "Vui lòng chọn tài khoản để import.", null, accountService.getAccountsByUser(userId));
                        return;
                    }

                    UUID accountId = UUID.fromString(accountIdStr);
                    try (InputStream input = filePart.getInputStream()) {
                        List<Transaction> previewList =
                                importExportService.previewImport(input, fileType, accountId);

                        if (previewList == null || previewList.isEmpty()) {
                            loadView(request, response, "Không có dữ liệu hợp lệ trong file.", null, accountService.getAccountsByUser(userId));
                            return;
                        }

                        // ✅ Lưu tạm vào session để Import dùng lại
                        session.setAttribute("importPreviewList", previewList);
                        session.setAttribute("importPreviewAccountId", accountId);

                        // Gửi dữ liệu ra view
                        request.setAttribute("previewTransactions", previewList);
                        request.setAttribute("selectedAccountId", accountIdStr);
                        loadView(request, response, null,
                                "Đã đọc file " + fileName + ". Kiểm tra bảng xem trước rồi bấm Xác nhận Import.",
                                accountService.getAccountsByUser(userId));
                    }
                    return;
                }

                case "import": {
                    @SuppressWarnings("unchecked")
                    List<Transaction> toSave = (List<Transaction>) session.getAttribute("importPreviewList");
                    UUID accountId = (UUID) session.getAttribute("importPreviewAccountId");

                    System.out.println("🟢 Debug import: toSave=" + (toSave != null ? toSave.size() : "null") +
                            ", accountId=" + accountId);

                    if (toSave == null || toSave.isEmpty() || accountId == null) {
                        loadView(request, response, "Không có dữ liệu để import (kiểm tra lại bước xem trước).", null,
                                accountService.getAccountsByUser(userId));
                        return;
                    }

                    importExportService.saveTransactions(toSave);
                    session.removeAttribute("importPreviewList");
                    session.removeAttribute("importPreviewAccountId");

                    loadView(request, response, null,
                            "Import thành công " + toSave.size() + " giao dịch.",
                            accountService.getAccountsByUser(userId));
                    return;
                }

                case "export": {
                    String accountIdStr = request.getParameter("account");
                    String startDate = request.getParameter("startDate");
                    String endDate = request.getParameter("endDate");
                    String format = request.getParameter("format");

                    if (accountIdStr == null || accountIdStr.isBlank()) {
                        loadView(request, response, "Vui lòng chọn tài khoản để export.", null, accountService.getAccountsByUser(userId));
                        return;
                    }

                    UUID accountId = UUID.fromString(accountIdStr);
                    byte[] exported = importExportService.exportByAccount(accountId, startDate, endDate, format);

                    if (exported == null || exported.length == 0) {
                        loadView(request, response, "Không có dữ liệu để xuất.", null, accountService.getAccountsByUser(userId));
                        return;
                    }

                    importExportService.writeExportResponse(response, format, exported);
                    return;
                }

                default:
                    loadView(request, response, null, null, accountService.getAccountsByUser(userId));
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadView(request, response, "Lỗi Import/Export: " + e.getMessage(), null, accountService.getAccountsByUser(userId));
        }
    }

    private void loadView(HttpServletRequest request, HttpServletResponse response,
                          String error, String success, List<Account> accounts)
            throws ServletException, IOException {
        if (error != null) request.setAttribute("error", error);
        if (success != null) request.setAttribute("success", success);
        request.setAttribute("accounts", accounts);
        request.setAttribute("view", "/views/import_export.jsp");
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int idx = fileName.lastIndexOf('.');
        return (idx >= 0 && idx < fileName.length() - 1)
                ? fileName.substring(idx + 1).toLowerCase()
                : "";
    }
}
