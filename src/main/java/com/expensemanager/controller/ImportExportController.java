package com.expensemanager.controller;

import com.expensemanager.service.ImportExportService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

@WebServlet("/import-export")
@MultipartConfig
public class ImportExportController extends HttpServlet {

    private final ImportExportService service = new ImportExportService();

    private String getUserId(HttpServletRequest req) {
        Object u = req.getSession().getAttribute("userId");
        if (u == null) {
            u = UUID.randomUUID().toString();
            req.getSession().setAttribute("userId", u);
        }
        return u.toString();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = getUserId(req);
        List<Account> accounts = service.getAccounts(userId);  // Load cho select
        req.setAttribute("accounts", accounts);

        req.setAttribute("view", "/views/import_export.jsp");
        req.getRequestDispatcher("/layout/layout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        String userId = getUserId(req);

        try {
            if ("preview_import".equals(action)) {
                Part filePart = req.getPart("file-upload");
                UUID accountId = UUID.fromString(req.getParameter("import-account"));
                List<Transaction> preview = service.previewImport(userId, filePart.getInputStream(), accountId);
                req.getSession().setAttribute("previewTransactions", preview);  // Lưu session cho confirm
                req.setAttribute("previewTransactions", preview);  // Cho JSP render table
                req.setAttribute("msg", "Preview sẵn sàng!");

                doGet(req, resp);  // Forward với data

            } else if ("confirm_import".equals(action)) {
                @SuppressWarnings("unchecked")
                List<Transaction> txs = (List<Transaction>) req.getSession().getAttribute("previewTransactions");
                if (txs != null) {
                    service.confirmImport(txs);
                    req.getSession().removeAttribute("previewTransactions");
                    req.setAttribute("msg", "Import thành công!");
                } else {
                    req.setAttribute("msg", "Không có data preview!");
                }
                doGet(req, resp);

            } else if ("export".equals(action)) {
                String accountStr = req.getParameter("export-account");
                UUID accountId = "all".equals(accountStr) ? null : UUID.fromString(accountStr);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                Timestamp start = new Timestamp(df.parse(req.getParameter("start-date")).getTime());
                Timestamp end = new Timestamp(df.parse(req.getParameter("end-date")).getTime() + 86399999);  // End of day
                String format = req.getParameter("file-format");

                byte[] bytes = service.exportTransactions(userId, accountId, start, end, format);
                resp.setContentType(format.equals("csv") ? "text/csv" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                resp.setHeader("Content-Disposition", "attachment; filename=transactions." + format);
                resp.getOutputStream().write(bytes);

            } else {
                req.setAttribute("msg", "Action không hợp lệ!");
                doGet(req, resp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("msg", "Lỗi: " + e.getMessage());
            doGet(req, resp);
        }
    }
}