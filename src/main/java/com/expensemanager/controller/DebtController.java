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
        // Không giữ EntityManager/DAO ở field để tránh EM kéo dài giữa các request.
        System.out.println("[DebtController] init done");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Mỗi request tạo 1 EM mới và đóng ở finally
        EntityManager em = null;
        try {
            em = JpaUtil.getEntityManagerFactory().createEntityManager();
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

        // lấy danh sách nợ cho user (hoặc tất cả nếu userId == null)
        List<Debt> list = debtService.getAllDebts(userId);

        // lấy users để hiển thị trong bảng (nếu cần) và build map id->username để JSP dùng (tránh NPE)
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

        // đọc action
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) action = request.getParameter("formAction");
        if (action == null || action.isEmpty()) action = request.getParameter("_action");
        if (action == null || action.isEmpty()) action = "add";

        System.out.println("[DebtController] doPost action=" + action + " sessionUser=" + request.getSession().getAttribute("userId"));

        EntityManager em = null;
        try {
            em = JpaUtil.getEntityManagerFactory().createEntityManager();
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
            request.getSession().setAttribute("flashError", "Lỗi xử lý: " + ex.getMessage());
        } finally {
            if (em != null && em.isOpen()) em.close();
        }

        // redirect để reload danh sách (PRG pattern)
        response.sendRedirect(request.getContextPath() + "/debt");
    }

    private Debt parseDebtFromRequest(HttpServletRequest request) {
        Debt debt = new Debt();

        // Lấy userId từ session; nếu không có, thử lấy từ request parameter "userId" (hidden) và lưu vào session
        Object sessUser = request.getSession().getAttribute("userId");
        String userIdStr = (sessUser != null) ? sessUser.toString() : null;

        String paramUserId = request.getParameter("userId");
        if ((userIdStr == null || userIdStr.isEmpty()) && paramUserId != null && !paramUserId.isEmpty()) {
            // chỉ set session khi session chưa có userId (khởi tạo lần đầu)
            userIdStr = paramUserId;
            request.getSession().setAttribute("userId", userIdStr);
            System.out.println("[DebtController] session userId set from request parameter: " + userIdStr);
        }

        if (userIdStr == null || userIdStr.isEmpty()) {
            // fallback sample (giữ như logic cũ)
            userIdStr = "00000000-0000-0000-0000-000000000047";
            request.getSession().setAttribute("userId", userIdStr);
        }

        try {
            debt.setUserId(UUID.fromString(userIdStr));
        } catch (Exception ex) {
            debt.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000047"));
        }

        debt.setCreditorName(request.getParameter("creditorName"));

        String amt = request.getParameter("amount");
        if (amt != null && !amt.isEmpty()) {
            try {
                debt.setAmount(new BigDecimal(amt));
            } catch (NumberFormatException nfe) {
                debt.setAmount(BigDecimal.ZERO);
            }
        }

        String due = request.getParameter("dueDate");
        if (due != null && !due.isEmpty()) {
            try {
                debt.setDueDate(LocalDate.parse(due));
            } catch (Exception e) {
                // ignore, keep null
            }
        }

        debt.setStatus(request.getParameter("status") != null ? request.getParameter("status") : Debt.STATUS_PENDING);
        debt.setNote(request.getParameter("note") != null ? request.getParameter("note") : "");

        return debt;
    }

    private UUID getSessionUserId(HttpServletRequest request) {
//        Object sess = request.getSession().getAttribute("userId");
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        UUID userId = (UUID) user.getId();
        if (session == null) return null;
        try {
            return userId;
        } catch (Exception ex) {
            return null;
        }
    }
}
