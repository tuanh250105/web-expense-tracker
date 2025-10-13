package com.expensemanager.controller;

import com.expensemanager.dao.DebtDAO;
import com.expensemanager.dao.UserDAO;
import com.expensemanager.model.Debt;
import com.expensemanager.service.DebtService;
import com.expensemanager.util.JpaUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.persistence.EntityManager;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@WebServlet(urlPatterns = {"/debt", "/debts", "/debts/*"})
public class DebtController extends HttpServlet {

    private DebtService debtService;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
            DebtDAO debtDAO = new DebtDAO(em);
            userDAO = new UserDAO(em);
            debtService = new DebtService(debtDAO);
        } catch (Exception ex) {
            ex.printStackTrace();
            // fallback nếu JpaUtil lỗi
            DebtDAO debtDAO = new DebtDAO(); // DAO mặc định tạo EM
            userDAO = new UserDAO();
            debtService = new DebtService(debtDAO);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();
        if (path == null || path.equals("/")) {
            showDebtList(request, response);
            return;
        }

        switch (path) {
            case "/list":
                showDebtList(request, response);
                break;

            case "/remind":
                UUID userIdForRemind = getSessionUserId(request);
                List<Debt> overdue = debtService.getOverdueDebts(userIdForRemind);
                request.setAttribute("overdue", overdue);
                request.setAttribute("debts", debtService.getAllDebts(userIdForRemind));
                request.setAttribute("flashMessage",
                        overdue.isEmpty() ? "Không có khoản nợ quá hạn."
                                : "Có " + overdue.size() + " khoản nợ quá hạn!");
                request.setAttribute("view", "/views/debt.jsp");
                request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
                break;

            default:
                showDebtList(request, response);
                break;
        }
    }

    private void showDebtList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UUID userId = getSessionUserId(request);

        List<Debt> list = debtService.getAllDebts(userId);

        List<?> users;
        try {
            users = userDAO.findAll();
        } catch (Throwable t) {
            t.printStackTrace();
            users = java.util.Collections.emptyList();
        }

        BigDecimal totalUnpaid = debtService.getTotalUnpaid(userId);
        BigDecimal totalOverdue = debtService.getTotalOverdue(userId);
        List<Debt> nearDueList = debtService.getNearDueDebts(userId);
        int nearDueCount = debtService.getNearDueCount(userId);
        List<Debt> overdueList = debtService.getOverdueDebts(userId);
        int overdueCount = overdueList.size();

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String totalUnpaidStr = nf.format(totalUnpaid);
        String totalOverdueStr = nf.format(totalOverdue);

        request.setAttribute("users", users);
        request.setAttribute("debts", list);
        request.setAttribute("totalUnpaid", totalUnpaid);
        request.setAttribute("totalOverdue", totalOverdue);
        request.setAttribute("totalUnpaidStr", totalUnpaidStr);
        request.setAttribute("totalOverdueStr", totalOverdueStr);
        request.setAttribute("nearDueCount", nearDueCount);
        request.setAttribute("nearDueList", nearDueList);
        request.setAttribute("overdueList", overdueList);
        request.setAttribute("overdueCount", overdueCount);
        request.setAttribute("hasOverdue", !overdueList.isEmpty());
        request.setAttribute("view", "/views/debt.jsp");

        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null) action = "add";

        try {
            switch (action) {
                case "add":
                    Debt newDebt = parseDebtFromRequest(request);
                    debtService.addDebt(newDebt);
                    break;

                case "edit":
                    Debt updated = parseDebtFromRequest(request);
                    String idStr = request.getParameter("id");
                    if (idStr != null && !idStr.isEmpty()) {
                        updated.setId(UUID.fromString(idStr));
                        debtService.updateDebt(updated);
                    }
                    break;

                case "delete":
                    String delId = request.getParameter("id");
                    if (delId != null && !delId.isEmpty()) {
                        debtService.deleteDebt(UUID.fromString(delId));
                    }
                    break;

                case "pay":
                    String payId = request.getParameter("id");
                    if (payId != null && !payId.isEmpty()) {
                        debtService.markAsPaid(UUID.fromString(payId));
                    }
                    break;

                default:
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            request.getSession().setAttribute("flashError", "Lỗi xử lý: " + ex.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/debt");
    }

    private Debt parseDebtFromRequest(HttpServletRequest request) {
        Debt debt = new Debt();

        String userIdStr = (request.getSession().getAttribute("userId") != null)
                ? request.getSession().getAttribute("userId").toString() : null;
        String userIdParam = request.getParameter("userId");
        if (userIdParam != null && !userIdParam.isEmpty()) userIdStr = userIdParam;

        if (userIdStr == null || userIdStr.isEmpty()) {
            userIdStr = "00000000-0000-0000-0000-000000000047";
            request.getSession().setAttribute("userId", userIdStr);
        }

        debt.setUserId(UUID.fromString(userIdStr));
        debt.setCreditorName(request.getParameter("creditorName"));
        if (request.getParameter("amount") != null && !request.getParameter("amount").isEmpty()) {
            debt.setAmount(new BigDecimal(request.getParameter("amount")));
        }
        if (request.getParameter("dueDate") != null && !request.getParameter("dueDate").isEmpty()) {
            debt.setDueDate(LocalDate.parse(request.getParameter("dueDate")));
        }
        debt.setStatus(request.getParameter("status") != null ? request.getParameter("status") : Debt.STATUS_PENDING);
        debt.setNote(request.getParameter("note") != null ? request.getParameter("note") : "");

        return debt;
    }

    private UUID getSessionUserId(HttpServletRequest request) {
        Object sess = request.getSession().getAttribute("userId");
        if (sess == null) return null;
        try {
            return UUID.fromString(sess.toString());
        } catch (Exception ex) {
            return null;
        }
    }
}
