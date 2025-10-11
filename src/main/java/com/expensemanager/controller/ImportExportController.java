package com.expensemanager.controller;

import com.expensemanager.model.Account;
import com.expensemanager.model.Transaction;
import com.expensemanager.service.AccountService;
import com.expensemanager.service.ImportExportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@WebServlet(name = "ImportExportController", value = "/import-export")
@MultipartConfig
public class ImportExportController extends HttpServlet {

    private ImportExportService importExportService;
    private AccountService accountService;

    @Override
    public void init() throws ServletException {
        importExportService = new ImportExportService();
        accountService = new AccountService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        loadView(request, response, null, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isBlank()) action = "view";

        try {
            switch (action) {
                case "preview": {
                    Part filePart = request.getPart("file");
                    if (filePart == null || filePart.getSize() == 0) {
                        loadView(request, response, "Vui lòng chọn file để import.", null);
                        return;
                    }

                    String fileName = filePart.getSubmittedFileName();
                    String fileType = getFileExtension(fileName);
                    String accountIdStr = request.getParameter("account");

                    if (accountIdStr == null || accountIdStr.isBlank()) {
                        loadView(request, response, "Vui lòng chọn tài khoản để import.", null);
                        return;
                    }

                    UUID accountId = UUID.fromString(accountIdStr);
                    try (InputStream input = filePart.getInputStream()) {
                        List<Transaction> previewList =
                                importExportService.previewImport(input, fileType, accountId);

                        HttpSession session = request.getSession();
                        session.setAttribute("importPreviewList", previewList);
                        session.setAttribute("importPreviewAccountId", accountId);

                        request.setAttribute("previewTransactions", previewList);
                        request.setAttribute("selectedAccountId", accountIdStr);
                        loadView(request, response, null, "Đã đọc file " + fileName + ". Kiểm tra bảng xem trước rồi bấm Xác nhận Import.");
                    }
                    return;
                }

                case "import": {
                    HttpSession session = request.getSession(false);
                    if (session == null) {
                        loadView(request, response, "Phiên làm việc hết hạn. Hãy xem trước lại file.", null);
                        return;
                    }

                    @SuppressWarnings("unchecked")
                    List<Transaction> toSave = (List<Transaction>) session.getAttribute("importPreviewList");
                    UUID accountId = (UUID) session.getAttribute("importPreviewAccountId");

                    if (toSave == null || toSave.isEmpty() || accountId == null) {
                        loadView(request, response, "Không có dữ liệu để import.", null);
                        return;
                    }

                    importExportService.saveTransactions(toSave);
                    session.removeAttribute("importPreviewList");
                    session.removeAttribute("importPreviewAccountId");

                    loadView(request, response, null, "Import thành công " + toSave.size() + " giao dịch.");
                    return;
                }

                case "export": {
                    String accountIdStr = request.getParameter("account");
                    String startDate = request.getParameter("startDate");
                    String endDate = request.getParameter("endDate");
                    String format = request.getParameter("format");

                    if (accountIdStr == null || accountIdStr.isBlank()) {
                        loadView(request, response, "Vui lòng chọn tài khoản để export.", null);
                        return;
                    }

                    UUID accountId = UUID.fromString(accountIdStr);
                    byte[] exported = importExportService.exportByAccount(accountId, startDate, endDate, format);

                    if (exported == null || exported.length == 0) {
                        loadView(request, response, "Không có dữ liệu để xuất.", null);
                        return;
                    }

                    importExportService.writeExportResponse(response, format, exported);
                    return;
                }

                default:
                    loadView(request, response, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadView(request, response, "Lỗi Import/Export: " + e.getMessage(), null);
        }
    }

    private void loadView(HttpServletRequest request, HttpServletResponse response, String error, String success)
            throws ServletException, IOException {
        List<Account> accounts = accountService.getAllAccounts();
        request.setAttribute("accounts", accounts);
        if (error != null) request.setAttribute("error", error);
        if (success != null) request.setAttribute("success", success);
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
