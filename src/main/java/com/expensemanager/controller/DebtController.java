// package com.expensemanager.controller;

// import com.expensemanager.dao.DebtDAO;
// import com.expensemanager.dao.UserDAO;
// import com.expensemanager.model.Debt;
// import com.expensemanager.service.DebtService;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.annotation.WebServlet;
// import jakarta.servlet.http.HttpServlet;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

// import java.io.IOException;
// import java.math.BigDecimal;
// import java.text.NumberFormat;
// import java.time.LocalDate;
// import java.util.List;
// import java.util.Locale;
// import java.util.UUID;

// @WebServlet(urlPatterns = {"/debt", "/debts", "/debts/*"})
// public class DebtController extends HttpServlet {

//     private DebtService debtService;
//     private UserDAO userDAO;

//     @Override
//     public void init() {
//         try {
//             DebtDAO dao = new DebtDAO();
//             debtService = new DebtService(dao);
//             userDAO = new UserDAO();
//         } catch (Exception ex) {
//             ex.printStackTrace();
//             debtService = new DebtService();
//             userDAO = new UserDAO();
//         }
//     }

//     @Override
//     protected void doGet(HttpServletRequest request, HttpServletResponse response)
//             throws ServletException, IOException {

//         String path = request.getPathInfo();
//         if (path == null || path.equals("/")) {
//             showDebtList(request, response);
//             return;
//         }

//         switch (path) {
//             case "/list":
//                 showDebtList(request, response);
//                 break;

//             case "/remind":
//                 UUID userIdForRemind = getSessionUserId(request);
//                 List<Debt> overdue = debtService.getOverdueDebts(userIdForRemind);
//                 request.setAttribute("overdue", overdue);
//                 request.setAttribute("debts", debtService.getAllDebts(userIdForRemind));
//                 request.setAttribute("flashMessage",
//                         overdue.isEmpty() ? "Không có khoản nợ quá hạn."
//                                 : "Có " + overdue.size() + " khoản nợ quá hạn!");
//                 request.setAttribute("view", "/views/debt.jsp");
//                 request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
//                 break;

//             default:
//                 showDebtList(request, response);
//                 break;
//         }
//     }

//     private void showDebtList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//         UUID userId = getSessionUserId(request);

//         List<Debt> list = debtService.getAllDebts(userId);
//         List<?> users = userDAO.findAll();

//         BigDecimal totalUnpaid = debtService.getTotalUnpaid(userId);
//         BigDecimal totalOverdue = debtService.getTotalOverdue(userId);
//         List<Debt> nearDueList = debtService.getNearDueDebts(userId);
//         int nearDueCount = debtService.getNearDueCount(userId);
//         List<Debt> overdueList = debtService.getOverdueDebts(userId);
//         int overdueCount = overdueList.size();

//         NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
//         String totalUnpaidStr = nf.format(totalUnpaid);
//         String totalOverdueStr = nf.format(totalOverdue);

//         request.setAttribute("users", users);
//         request.setAttribute("debts", list);
//         request.setAttribute("totalUnpaid", totalUnpaid);
//         request.setAttribute("totalOverdue", totalOverdue);
//         request.setAttribute("totalUnpaidStr", totalUnpaidStr);
//         request.setAttribute("totalOverdueStr", totalOverdueStr);
//         request.setAttribute("nearDueCount", nearDueCount);
//         request.setAttribute("nearDueList", nearDueList);
//         request.setAttribute("overdueList", overdueList);
//         request.setAttribute("overdueCount", overdueCount);
//         request.setAttribute("hasOverdue", !overdueList.isEmpty());
//         request.setAttribute("view", "/views/debt.jsp");

//         request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
//     }

//     @Override
//     protected void doPost(HttpServletRequest request, HttpServletResponse response)
//             throws ServletException, IOException {
//         String action = request.getParameter("action");
//         if (action == null) action = "add";

//         try {
//             switch (action) {
//                 case "add":
//                     Debt newDebt = parseDebtFromRequest(request);
//                     debtService.addDebt(newDebt);
//                     break;

//                 case "edit":
//                     Debt updated = parseDebtFromRequest(request);
//                     String idStr = request.getParameter("id");
//                     if (idStr != null && !idStr.isEmpty()) {
//                         updated.setId(UUID.fromString(idStr));
//                         debtService.updateDebt(updated);
//                     }
//                     break;

//                 case "delete":
//                     String delId = request.getParameter("id");
//                     if (delId != null && !delId.isEmpty()) {
//                         debtService.deleteDebt(UUID.fromString(delId));
//                     }
//                     break;

//                 case "pay":
//                     String payId = request.getParameter("id");
//                     if (payId != null && !payId.isEmpty()) {
//                         debtService.markAsPaid(UUID.fromString(payId));
//                     }
//                     break;

//                 default:
//                     break;
//             }
//         } catch (Exception ex) {
//             ex.printStackTrace();
//             request.getSession().setAttribute("flashError", "Lỗi xử lý: " + ex.getMessage());
//         }

//         response.sendRedirect(request.getContextPath() + "/debt");
//     }

//     private Debt parseDebtFromRequest(HttpServletRequest request) {
//         Debt debt = new Debt();

//         String userIdStr = null;
//         Object sessUser = request.getSession().getAttribute("userId");
//         if (sessUser != null) {
//             userIdStr = sessUser.toString();
//         }
//         String userIdParam = request.getParameter("userId");
//         if (userIdParam != null && !userIdParam.isEmpty()) {
//             userIdStr = userIdParam;
//         }

//         if (userIdStr == null || userIdStr.isEmpty()) {
//             userIdStr = "00000000-0000-0000-0000-000000000047";
//             request.getSession().setAttribute("userId", userIdStr);
//         }

//         try {
//             debt.setUserId(UUID.fromString(userIdStr));
//         } catch (IllegalArgumentException ex) {
//             throw new IllegalArgumentException("userId không hợp lệ: " + userIdStr);
//         }

//         String creditor = request.getParameter("creditorName");
//         String amountStr = request.getParameter("amount");
//         String dueDateStr = request.getParameter("dueDate");
//         String status = request.getParameter("status");
//         String note = request.getParameter("note");

//         debt.setCreditorName(creditor != null ? creditor : "");
//         if (amountStr != null && !amountStr.isEmpty()) {
//             debt.setAmount(new BigDecimal(amountStr));
//         }
//         if (dueDateStr != null && !dueDateStr.isEmpty()) {
//             debt.setDueDate(LocalDate.parse(dueDateStr));
//         }
//         debt.setStatus(status != null ? status : Debt.STATUS_PENDING);
//         debt.setNote(note != null ? note : "");
//         return debt;
//     }

//     private UUID getSessionUserId(HttpServletRequest request) {
//         Object sess = request.getSession().getAttribute("userId");
//         if (sess == null) return null;
//         try {
//             return UUID.fromString(sess.toString());
//         } catch (Exception ex) {
//             return null;
//         }
//     }
// }
