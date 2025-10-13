package com.expensemanager.service;

import com.expensemanager.dao.AccountDAO;
import com.expensemanager.dao.GroupDAO;
import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.dao.UserDAO;
import com.expensemanager.model.Group;
import com.expensemanager.model.GroupMember;
import com.expensemanager.model.Transaction; // QUAN TRỌNG: Đảm bảo import Transaction
import com.expensemanager.model.User;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DashboardService
 * Xử lý nghiệp vụ cho phần tổng quan tài chính (Dashboard).
 * Đã được viết lại để tối ưu hóa hiệu năng và sửa lỗi truy vấn lặp.
 */
public class DashboardService {

    // ========== DAO dependencies ==========
    private final TransactionDAO transactionDAO;
    private final GroupDAO groupDAO;
    private final UserDAO userDAO;
    private final AccountDAO accountDAO;

    // ========== Constant values ==========

    private static UUID CURRENT_USER_ID;
    private static final BigDecimal ONE_UNIT = new BigDecimal("1");
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MM");

    public DashboardService(UUID user_id, TransactionDAO transactionDAO, GroupDAO groupDAO, UserDAO userDAO, AccountDAO accountDAO) {
        this.CURRENT_USER_ID = user_id;
        this.transactionDAO = transactionDAO;
        this.groupDAO = groupDAO;
        this.userDAO = userDAO;
        this.accountDAO = accountDAO;
    }

    // =========================================
    // PUBLIC API (được Controller gọi)
    // =========================================

    public Map<String, Object> getOverviewData(String period) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        String groupBy = "day";

        switch (period) {
            case "week":
                startDate = today.minusDays(6);
                break;
            case "year":
                startDate = today.with(TemporalAdjusters.firstDayOfYear());
                groupBy = "month";
                break;
            case "month":
            default:
                startDate = today.with(TemporalAdjusters.firstDayOfMonth());
                break;
        }
        return getOverviewDataForRange(startDate, today, groupBy);
    }

    public Map<String, Object> getOverviewData(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        return getOverviewDataForRange(startDate, endDate, "day");
    }

    public Map<String, Object> getOverviewData(int year, int month, int week) {
        LocalDate[] range = getWeekRange(year, month, week);
        return getOverviewDataForRange(range[0], range[1], "day");
    }


    // =========================================
    // MAIN DATA AGGREGATOR (ĐÃ TỐI ƯU HÓA)
    // =========================================

    private Map<String, Object> getOverviewDataForRange(LocalDate startDate, LocalDate endDate, String groupBy) {
        Map<String, Object> data = new HashMap<>();
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        try {
            // TỐI ƯU HÓA: Lấy tất cả giao dịch trong khoảng thời gian chỉ MỘT LẦN DUY NHẤT.
            List<Transaction> allTransactionsInRange;
            if ("year".equals(groupBy)) {
                // Nếu xem cả năm, cần lấy dữ liệu cả năm để tính toán đúng
                LocalDateTime yearStart = LocalDate.of(startDate.getYear(), 1, 1).atStartOfDay();
                LocalDateTime yearEnd = LocalDate.of(startDate.getYear(), 12, 31).atTime(LocalTime.MAX);
                allTransactionsInRange = transactionDAO.getAllTransactionsByMonthAndYear(CURRENT_USER_ID, yearStart, yearEnd);
            } else {
                allTransactionsInRange = transactionDAO.getAllTransactionsByMonthAndYear(CURRENT_USER_ID, startDateTime, endDateTime);
            }


            // Các hàm con bây giờ sẽ xử lý trên danh sách đã có, không truy vấn CSDL nữa.
            data.put("recentTransactions", getRecentTransactionsFromList(allTransactionsInRange));
            data.put("groupExpenses", getGroupExpenses());
            data.put("monthlySummary", getMonthlySummary());
            data.put("currentBalance", getCurrentBalance());

            if ("month".equals(groupBy)) {
                calculateMonthlyBalanceChanges(data, startDate.getYear(), allTransactionsInRange);
            } else {
                calculateDailyBalanceChanges(data, startDate, endDate, allTransactionsInRange);
            }

        } catch (Exception e) {
            e.printStackTrace();
            data.put("error", "Lỗi server khi xử lý dữ liệu: " + e.getMessage());
        }
        return data;
    }

    // =========================================
    // 1️⃣ Biến động số dư (Balance Changes) (ĐÃ TỐI ƯU HÓA)
    // =========================================

    private void calculateDailyBalanceChanges(Map<String, Object> data, LocalDate startDate, LocalDate endDate, List<Transaction> allTransactions) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> incomeList = new ArrayList<>();
        List<BigDecimal> expenseList = new ArrayList<>();
        List<BigDecimal> balanceList = new ArrayList<>();

        BigDecimal runningBalance = getOpeningBalance(startDate);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            labels.add(date.format(DAY_FORMAT));
            final LocalDate currentDate = date; // Biến final để dùng trong lambda

            // TÍNH TOÁN TRONG JAVA: Lọc và tính tổng từ danh sách đã có, KHÔNG GỌI DAO.
            BigDecimal income = allTransactions.stream()
                    .filter(t -> "income".equalsIgnoreCase(t.getType()) && t.getTransactionDate().toLocalDate().isEqual(currentDate))
                    .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expense = allTransactions.stream()
                    .filter(t -> "expense".equalsIgnoreCase(t.getType()) && t.getTransactionDate().toLocalDate().isEqual(currentDate))
                    .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            incomeList.add(income.divide(ONE_UNIT, 2, RoundingMode.HALF_UP));
            expenseList.add(expense.divide(ONE_UNIT, 2, RoundingMode.HALF_UP));

            runningBalance = runningBalance.add(income).subtract(expense);
            balanceList.add(runningBalance.divide(ONE_UNIT, 2, RoundingMode.HALF_UP).max(BigDecimal.ZERO));
        }

        data.put("balanceChanges", Map.of(
                "labels", labels, "income", incomeList, "expense", expenseList, "runningBalance", balanceList
        ));
        data.put("categories", getCategoriesPieDataFromList(allTransactions));
    }

    private void calculateMonthlyBalanceChanges(Map<String, Object> data, int year, List<Transaction> allTransactionsForYear) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> incomeList = new ArrayList<>();
        List<BigDecimal> expenseList = new ArrayList<>();
        List<BigDecimal> balanceList = new ArrayList<>();

        BigDecimal runningBalance = getOpeningBalance(LocalDate.of(year, 1, 1));

        for (int month = 1; month <= 12; month++) {
            labels.add(String.format("%02d", month));
            final int currentMonth = month;

            // TÍNH TOÁN TRONG JAVA
            BigDecimal income = allTransactionsForYear.stream()
                    .filter(t -> "income".equalsIgnoreCase(t.getType()) && t.getTransactionDate().getMonthValue() == currentMonth)
                    .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expense = allTransactionsForYear.stream()
                    .filter(t -> "expense".equalsIgnoreCase(t.getType()) && t.getTransactionDate().getMonthValue() == currentMonth)
                    .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            incomeList.add(income.divide(ONE_UNIT, 2, RoundingMode.HALF_UP));
            expenseList.add(expense.divide(ONE_UNIT, 2, RoundingMode.HALF_UP));
            runningBalance = runningBalance.add(income).subtract(expense);
            balanceList.add(runningBalance.divide(ONE_UNIT, 2, RoundingMode.HALF_UP).max(BigDecimal.ZERO));
        }

        data.put("balanceChanges", Map.of(
                "labels", labels, "income", incomeList, "expense", expenseList, "runningBalance", balanceList
        ));
        data.put("categories", getCategoriesPieDataFromList(allTransactionsForYear));
    }

    // =========================================
    // 2️⃣ Lấy số dư
    // =========================================

    private BigDecimal getCurrentBalance() {
        BigDecimal totalBalance = accountDAO.getTotalBalanceByUser(CURRENT_USER_ID);
        return totalBalance.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getOpeningBalance(LocalDate date) {
        LocalDateTime startOfAllTime = LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime endOfPreviousDay = date.atStartOfDay().minusSeconds(1);

        List<Transaction> pastTransactions = transactionDAO.getAllTransactionsByMonthAndYear(CURRENT_USER_ID, startOfAllTime, endOfPreviousDay);

        BigDecimal totalIncome = getSumByTypeFromList(pastTransactions, "income");
        BigDecimal totalExpense = getSumByTypeFromList(pastTransactions, "expense");

        return totalIncome.subtract(totalExpense);
    }

    // =========================================
    // 3️⃣ Biểu đồ & Thống kê
    // =========================================

    private Map<String, Object> getMonthlySummary() {
        LocalDate today = LocalDate.now();
        LocalDate currentStart = today.with(TemporalAdjusters.firstDayOfMonth());
        List<Transaction> currentMonthTrans = transactionDAO.getAllTransactionsByMonthAndYear(CURRENT_USER_ID, currentStart.atStartOfDay(), today.atTime(LocalTime.MAX));
        BigDecimal currentIncome = getSumByTypeFromList(currentMonthTrans, "income");
        BigDecimal currentExpense = getSumByTypeFromList(currentMonthTrans, "expense");

        LocalDate prevMonth = today.minusMonths(1);
        LocalDate prevStart = prevMonth.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate prevEnd = prevMonth.with(TemporalAdjusters.lastDayOfMonth());
        List<Transaction> prevMonthTrans = transactionDAO.getAllTransactionsByMonthAndYear(CURRENT_USER_ID, prevStart.atStartOfDay(), prevEnd.atTime(LocalTime.MAX));
        BigDecimal prevIncome = getSumByTypeFromList(prevMonthTrans, "income");
        BigDecimal prevExpense = getSumByTypeFromList(prevMonthTrans, "expense");

        return Map.of(
                "currentMonth", Map.of("income", currentIncome.divide(ONE_UNIT, 2, RoundingMode.HALF_UP), "expense", currentExpense.divide(ONE_UNIT, 2, RoundingMode.HALF_UP)),
                "previousMonth", Map.of("income", prevIncome.divide(ONE_UNIT, 2, RoundingMode.HALF_UP), "expense", prevExpense.divide(ONE_UNIT, 2, RoundingMode.HALF_UP))
        );
    }
    private Map<String, Object> getCategoriesPieDataFromList(List<Transaction> allTransactions) {
        List<Transaction> expenseTransactions = allTransactions.stream()
                .filter(t -> "expense".equalsIgnoreCase(t.getType()))
                .collect(Collectors.toList());

        List<Map<String, Object>> groupedData = transactionDAO.groupTransactionsByCategory(expenseTransactions, 10);

        List<String> labels = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();
        for (Map<String, Object> item : groupedData) {
            labels.add((String) item.get("categoryName"));
            BigDecimal amount = BigDecimal.valueOf((Double) item.get("total"));
            values.add(amount.divide(ONE_UNIT, 2, RoundingMode.HALF_UP));
        }
        return Map.of("labels", labels, "data", values);
    }

    // =========================================
    // 4️⃣ & 5️⃣: Nhóm chi tiêu & Tiện ích (Không thay đổi)
    // =========================================

    private Map<String, Object> getGroupExpenses() {
        List<Object[]> groups = groupDAO.findGroupsByUserId(CURRENT_USER_ID);
        List<Map<String, Object>> groupDetails = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Object[] g : groups) {
            UUID groupId = (UUID) g[0];
            BigDecimal raw = groupDAO.sumExpensesByGroupId(groupId);
            BigDecimal sum = (raw != null) ? raw.divide(ONE_UNIT, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            total = total.add(sum);

            Map<String, Object> groupMap = new LinkedHashMap<>();
            groupMap.put("id", groupId.toString());
            groupMap.put("name", g[1]);
            groupMap.put("description", g[2]);
            groupMap.put("totalAmount", sum);
            groupMap.put("isCurrentUserOwner", CURRENT_USER_ID.equals((UUID) g[3]));

            List<Map<String, String>> members = groupDAO.findMembersByGroupId(groupId).stream()
                    .map(m -> Map.of("id", ((UUID) m[0]).toString(), "name", (String) m[1]))
                    .collect(Collectors.toList());
            groupMap.put("members", members);

            groupDetails.add(groupMap);
        }

        return Map.of("total", total, "details", groupDetails);
    }

    public Map<String, Object> createGroup(String name, String description) {
        User currentUser = userDAO.findById(CURRENT_USER_ID);
        if (currentUser == null) throw new IllegalStateException("Không tìm thấy người dùng hiện tại");

        Group group = new Group();
        group.setName(name);
        group.setDescription(description);
        group.setCreatedBy(currentUser);
        group.setCreatedAt(LocalDateTime.now());
        groupDAO.save(group);

        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(currentUser);
        member.setJoinedAt(LocalDateTime.now());
        groupDAO.saveMember(member);

        return Map.of(
                "id", group.getId().toString(),
                "name", group.getName(),
                "description", group.getDescription(),
                "totalAmount", BigDecimal.ZERO,
                "members", List.of(Map.of("id", currentUser.getId().toString(), "name", currentUser.getFullName())),
                "isCurrentUserOwner", true
        );
    }

    public void deleteGroup(UUID groupId) {
        Group group = groupDAO.findById(groupId);
        if (group == null) throw new IllegalArgumentException("Không tìm thấy nhóm");
        if (!group.getCreatedBy().getId().equals(CURRENT_USER_ID))
            throw new SecurityException("Bạn không có quyền xóa nhóm này");
        groupDAO.delete(group);
    }

    public void addMember(UUID groupId, UUID userId) {
        if (groupDAO.isUserMember(groupId, userId))
            throw new IllegalStateException("Người dùng đã là thành viên nhóm");

        Group group = groupDAO.findById(groupId);
        User user = userDAO.findById(userId);
        if (group == null || user == null)
            throw new IllegalArgumentException("Không tìm thấy nhóm hoặc người dùng");

        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(user);
        member.setJoinedAt(LocalDateTime.now());
        groupDAO.saveMember(member);
    }

    public void removeMember(UUID groupId, UUID userId) {
        Group group = groupDAO.findById(groupId);
        if (group == null) throw new IllegalArgumentException("Không tìm thấy nhóm");
        if (group.getCreatedBy().getId().equals(userId))
            throw new IllegalArgumentException("Không thể xóa chủ nhóm");
        if (groupDAO.countMembers(groupId) <= 1)
            throw new IllegalStateException("Không thể xóa thành viên cuối cùng");

        groupDAO.deleteMember(groupId, userId);
    }

    public List<Map<String, String>> searchUsersByName(String name) {
        return userDAO.searchByName(name, CURRENT_USER_ID).stream()
                .map(u -> Map.of("id", u.getId().toString(), "name", u.getFullName()))
                .collect(Collectors.toList());
    }


    // =========================================
    // 6️⃣ Utility Helpers
    // =========================================

    private BigDecimal getSumByTypeFromList(List<Transaction> transactions, String type) {
        return transactions.stream()
                .filter(t -> type.equalsIgnoreCase(t.getType()))
                .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private LocalDate[] getWeekRange(int year, int month, int weekNum) {
        LocalDate first = LocalDate.of(year, month, 1);
        LocalDate start = first.plusWeeks(weekNum - 1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if (start.getMonthValue() != month) start = first;
        LocalDate end = start.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        if (end.getMonthValue() != month) end = first.with(TemporalAdjusters.lastDayOfMonth());
        return new LocalDate[]{start, end};
    }

    private List<Map<String, Object>> getRecentTransactionsFromList(List<Transaction> allTransactions) {
        return allTransactions.stream()
                .limit(6) // Danh sách đã được DAO sắp xếp sẵn
                .map(t -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("date", t.getTransactionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    map.put("category", (t.getCategory() != null) ? t.getCategory().getName() : "Chưa phân loại");
                    map.put("amount", t.getAmount());
                    return map;
                })
                .collect(Collectors.toList());
    }
}
