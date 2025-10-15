package com.expensemanager.controller;

import com.expensemanager.dao.DebtDAO;
import com.expensemanager.dao.UserDAO;
import com.expensemanager.model.Debt;
import com.expensemanager.model.User;
import com.expensemanager.service.DebtService;
import com.expensemanager.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/debt", "/debts", "/debts/*"})
public class DebtController extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("[DebtController] init done");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        EntityManager em = null;
        try {
            em = JpaUtil.getEntityManager();
            DebtDAO debtDAO = new DebtDAO(em);
            UserDAO userDAO = new UserDAO(em);
            DebtService debtService = new DebtService(debtDAO);

            String path = request.getPathInfo();
            if (path == null || path.equals("/")) {
                showDebtList(request, response, debtService, userDAO);
                return;
            }

            switch (path) {
                case "/list":
                    showDebtList(request, response, debtService, userDAO);
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
                    showDebtList(request, response, debtService, userDAO);
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ServletException(ex);
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    private void showDebtList(HttpServletRequest request, HttpServletResponse response,
                              DebtService debtService, UserDAO userDAO) throws ServletException, IOException {
        UUID userId = getSessionUserId(request);

        List<Debt> list = debtService.getAllDebts(userId);

        List<User> users;
        try {
            users = (List<User>) userDAO.findAll();
        } catch (Throwable t) {
            t.printStackTrace();
            users = Collections.emptyList();
        }
        Map<UUID, String> debtUsernamesMap = users.stream()
                .filter(u -> u.getId() != null)
                .collect(Collectors.toMap(User::getId, u -> u.getUsername(), (a,b) -> a));

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
        request.setAttribute("debtUsernamesMap", debtUsernamesMap);
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
        if (action == null || action.isEmpty()) action = request.getParameter("formAction");
        if (action == null || action.isEmpty()) action = request.getParameter("_action");
        if (action == null || action.isEmpty()) action = "add";

        System.out.println("[DebtController] doPost action=" + action + " sessionUser=" + request.getSession().getAttribute("userId"));

        EntityManager em = null;
        try {
            em = JpaUtil.getEntityManager();
            DebtDAO debtDAO = new DebtDAO(em);
            UserDAO userDAO = new UserDAO(em);
            DebtService debtService = new DebtService(debtDAO);

            switch (action) {
                case "add":
                    Debt newDebt = parseDebtFromRequest(request);
                    Debt saved = debtService.addDebt(newDebt);
                    System.out.println("[DebtController] added debt -> id=" + (saved != null ? saved.getId() : "null") + " userId=" + newDebt.getUserId());
                    break;

                case "edit":
                    Debt updated = parseDebtFromRequest(request);
                    String idStr = request.getParameter("id");
                    if (idStr != null && !idStr.isEmpty()) {
                        updated.setId(UUID.fromString(idStr));
                        boolean ok = debtService.updateDebt(updated);
                        System.out.println("[DebtController] update result=" + ok + " id=" + idStr);
                    }
                    break;

                case "delete":
                    String delId = request.getParameter("id");
                    if (delId != null && !delId.isEmpty()) {
                        boolean del = debtService.deleteDebt(UUID.fromString(delId));
                        System.out.println("[DebtController] delete result=" + del + " id=" + delId);
                    }
                    break;

                case "pay":
                    String payId = request.getParameter("id");
                    if (payId != null && !payId.isEmpty()) {
                        boolean mark = debtService.markAsPaid(UUID.fromString(payId));
                        System.out.println("[DebtController] markAsPaid result=" + mark + " id=" + payId);
                    }
                    break;

                default:
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("Server error: " + ex.getMessage());
                response.getWriter().flush();
            } catch (IOException ioEx) {
                ioEx.printStackTrace();
            }

            return;
        } finally {
            if (em != null && em.isOpen()) em.close();
        }

        if (!response.isCommitted() && response.getStatus() == HttpServletResponse.SC_OK) {
            response.sendRedirect(request.getContextPath() + "/debt");
        }
    }

    private Debt parseDebtFromRequest(HttpServletRequest request) {
        Debt debt = new Debt();

        UUID sessionUserId = getSessionUserId(request);
        if (sessionUserId == null) {
            String paramUserId = request.getParameter("userId");
            if (paramUserId != null && !paramUserId.isEmpty()) {
                try {
                    sessionUserId = UUID.fromString(paramUserId);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        if (sessionUserId == null) {
            sessionUserId = UUID.fromString("00000000-0000-0000-0000-000000000047");
            request.getSession().setAttribute("userId", sessionUserId.toString());
            System.out.println("[DebtController] Warning: userId missing, fallback to sample " + sessionUserId);
        } else {
            HttpSession sess = request.getSession();
            Object uidAttr = sess.getAttribute("userId");
            if (uidAttr == null) {
                sess.setAttribute("userId", sessionUserId.toString());
            }
        }

        debt.setUserId(sessionUserId);
        debt.setCreditorName(request.getParameter("creditorName"));

        String amt = request.getParameter("amount");
        if (amt != null && !amt.isEmpty()) {
            try {
                debt.setAmount(new BigDecimal(amt));
            } catch (NumberFormatException nfe) {
                debt.setAmount(BigDecimal.ZERO);
                System.out.println("[DebtController] parseDebtFromRequest: invalid amount -> " + amt);
            }
        } else {
            debt.setAmount(BigDecimal.ZERO);
        }
        String due = request.getParameter("dueDate");
        if (due != null && !due.isEmpty()) {
            try {
                debt.setDueDate(LocalDate.parse(due));
            } catch (Exception e) {
                debt.setDueDate(LocalDate.now());
                System.out.println("[DebtController] parseDebtFromRequest: invalid dueDate -> " + due + ", fallback to today");
            }
        } else {
            debt.setDueDate(LocalDate.now());
        }

        debt.setStatus(request.getParameter("status") != null && !request.getParameter("status").isEmpty()
                ? request.getParameter("status") : Debt.STATUS_PENDING);
        debt.setNote(request.getParameter("note") != null ? request.getParameter("note") : "");

        System.out.println("[DebtController] parseDebtFromRequest -> userId=" + debt.getUserId()
                + " creditor=" + debt.getCreditorName() + " amount=" + debt.getAmount()
                + " dueDate=" + debt.getDueDate() + " status=" + debt.getStatus());

        return debt;
    }

    private UUID getSessionUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;

        Object userAttr = session.getAttribute("user");
        if (userAttr instanceof User) {
            User user = (User) userAttr;
            if (user != null && user.getId() != null) return user.getId();
        }

        Object uid = session.getAttribute("userId");
        if (uid instanceof UUID) {
            return (UUID) uid;
        } else if (uid instanceof String) {
            try {
                return UUID.fromString((String) uid);
            } catch (IllegalArgumentException ignored) { }
        }
        return null;
    }
}
