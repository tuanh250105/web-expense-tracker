package com.expensemanager.service;

import com.expensemanager.dao.AccountDAO;
import com.expensemanager.dao.GroupDAO;
import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.dao.UserDAO;
import com.expensemanager.model.Group;
import com.expensemanager.model.GroupMember;
import com.expensemanager.model.Transaction;
import com.expensemanager.model.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardService {

    private final TransactionDAO transactionDAO;
    private final GroupDAO groupDAO;
    private final UserDAO userDAO;
    private final AccountDAO accountDAO;

    private static UUID CURRENT_USER_ID;
    private static final BigDecimal ONE_UNIT = new BigDecimal("1");
    // Sử dụng format cho frontend
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MM");

    public DashboardService(UUID user_id, TransactionDAO transactionDAO, GroupDAO groupDAO, UserDAO userDAO, AccountDAO accountDAO) {
        CURRENT_USER_ID = user_id;
        this.transactionDAO = transactionDAO;
        this.groupDAO = groupDAO;
        this.userDAO = userDAO;
        this.accountDAO = accountDAO;
    }

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

    public Map<String, Object> getOverviewData(int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        return getOverviewDataForRange(startDate, endDate, "month");
    }

    // --- Sửa lỗi: Cung cấp dữ liệu BalanceChanges và Categories đúng định dạng ---
    private Map<String, Object> getOverviewDataForRange(LocalDate startDate, LocalDate endDate, String groupBy) {
        Map<String, Object> data = new HashMap<>();
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        try {
            List<Transaction> allTransactionsInRange;

            // Xử lý phạm vi dữ liệu cho groupBy="month" (period="year")
            if ("month".equals(groupBy)) {
                LocalDateTime yearStart = LocalDate.of(startDate.getYear(), 1, 1).atStartOfDay();
                LocalDateTime yearEnd = LocalDate.of(startDate.getYear(), 12, 31).atTime(LocalTime.MAX);
                allTransactionsInRange = transactionDAO.getAllTransactionsByMonthAndYear(CURRENT_USER_ID, yearStart, yearEnd);
            } else {
                allTransactionsInRange = transactionDAO.getAllTransactionsByMonthAndYear(CURRENT_USER_ID, startDateTime, endDateTime);
            }

            data.put("recentTransactions", getRecentTransactionsFromList(allTransactionsInRange));
            data.put("groupExpenses", getGroupExpenses());
            data.put("monthlySummary", getMonthlySummary());
            data.put("currentBalance", getCurrentBalance());

            // ✅ Đã sửa: Luôn cung cấp dữ liệu categories cho biểu đồ Pie/Donut
            data.put("categories", getCategoriesPieDataFromList(allTransactionsInRange));

            // ✅ Đã sửa: Gọi hàm chuyển đổi định dạng cho balanceChanges
            if ("month".equals(groupBy)) {
                List<Map<String, Object>> monthlyChanges = calculateMonthlyBalanceChanges(allTransactionsInRange, startDate.getYear());
                data.put("balanceChanges", convertMonthlyChangesToFrontendFormat(monthlyChanges)); // Chuyển đổi
            } else {
                List<Map<String, Object>> dailyChanges = calculateDailyBalanceChanges(allTransactionsInRange, startDate, endDate);
                data.put("balanceChanges", convertDailyChangesToFrontendFormat(dailyChanges)); // Chuyển đổi
            }

        } catch (Exception e) {
            e.printStackTrace();
            data.put("error", "Lỗi server khi xử lý dữ liệu: " + e.getMessage());
        }
        return data;
    }

    // --- Cần chuyển đổi từ List<Map> sang Map<List> cho JS ---
    private Map<String, Object> convertDailyChangesToFrontendFormat(List<Map<String, Object>> dailyChanges) {
        List<String> labels = dailyChanges.stream()
                .map(d -> {
                    Object dateObj = d.get("date");
                    if (dateObj instanceof LocalDate) {
                        return ((LocalDate) dateObj).format(DAY_FORMAT);
                    }
                    return (String) dateObj; // Trường hợp date là String
                })
                .collect(Collectors.toList());

        List<BigDecimal> income = dailyChanges.stream().map(d -> (BigDecimal) d.get("income")).collect(Collectors.toList());
        List<BigDecimal> expense = dailyChanges.stream().map(d -> (BigDecimal) d.get("expense")).collect(Collectors.toList());
        List<BigDecimal> runningBalance = dailyChanges.stream().map(d -> (BigDecimal) d.get("balance")).collect(Collectors.toList());

        return Map.of(
                "labels", labels,
                "income", income,
                "expense", expense,
                "runningBalance", runningBalance
        );
    }

    private Map<String, Object> convertMonthlyChangesToFrontendFormat(List<Map<String, Object>> monthlyChanges) {
        List<String> labels = monthlyChanges.stream()
                .map(d -> "Tháng " + d.get("month")) // Format tháng
                .collect(Collectors.toList());

        List<BigDecimal> income = monthlyChanges.stream().map(d -> (BigDecimal) d.get("income")).collect(Collectors.toList());
        List<BigDecimal> expense = monthlyChanges.stream().map(d -> (BigDecimal) d.get("expense")).collect(Collectors.toList());
        List<BigDecimal> runningBalance = monthlyChanges.stream().map(d -> (BigDecimal) d.get("balance")).collect(Collectors.toList());

        return Map.of(
                "labels", labels,
                "income", income,
                "expense", expense,
                "runningBalance", runningBalance
        );
    }

    // ===== 1️⃣ Daily Balance Changes (Giữ nguyên logic tính toán, trả về List<Map> thô) =====
    private List<Map<String, Object>> calculateDailyBalanceChanges(List<Transaction> allTransactions,
                                                                   LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> dailyChanges = new ArrayList<>();

        BigDecimal[] runningBalance = { getOpeningBalance(startDate) };

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDate currentDate = date;

            BigDecimal income = allTransactions.stream()
                    .filter(t -> "income".equalsIgnoreCase(t.getType())
                            && t.getTransactionDate().toLocalDate().isEqual(currentDate))
                    .map(t -> Optional.ofNullable(t.getAmount()).orElse(BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expense = allTransactions.stream()
                    .filter(t -> "expense".equalsIgnoreCase(t.getType())
                            && t.getTransactionDate().toLocalDate().isEqual(currentDate))
                    .map(t -> Optional.ofNullable(t.getAmount()).orElse(BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            runningBalance[0] = runningBalance[0].add(income).subtract(expense);

            dailyChanges.add(Map.of(
                    "date", currentDate, // Sử dụng LocalDate để dễ dàng format sau này
                    "income", income,
                    "expense", expense,
                    "balance", runningBalance[0]
            ));
        }

        return dailyChanges;
    }

    // ===== 2️⃣ Monthly Balance Changes (Giữ nguyên logic tính toán, trả về List<Map> thô) =====
    private List<Map<String, Object>> calculateMonthlyBalanceChanges(List<Transaction> allTransactions, int year) {
        List<Map<String, Object>> monthlyChanges = new ArrayList<>();

        // Tính số dư đầu kỳ là số dư trước ngày 1/1 của năm đang xét
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        BigDecimal[] runningBalance = { getOpeningBalance(startOfYear) };

        for (int month = 1; month <= 12; month++) {
            final int currentMonth = month;

            BigDecimal income = allTransactions.stream()
                    .filter(t -> "income".equalsIgnoreCase(t.getType())
                            && t.getTransactionDate().getYear() == year
                            && t.getTransactionDate().getMonthValue() == currentMonth)
                    .map(t -> Optional.ofNullable(t.getAmount()).orElse(BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expense = allTransactions.stream()
                    .filter(t -> "expense".equalsIgnoreCase(t.getType())
                            && t.getTransactionDate().getYear() == year
                            && t.getTransactionDate().getMonthValue() == currentMonth)
                    .map(t -> Optional.ofNullable(t.getAmount()).orElse(BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            runningBalance[0] = runningBalance[0].add(income).subtract(expense);

            monthlyChanges.add(Map.of(
                    "month", currentMonth,
                    "income", income,
                    "expense", expense,
                    "balance", runningBalance[0]
            ));
        }

        return monthlyChanges;
    }

    // ===== 3️⃣ Get Balances (Giữ nguyên) =====
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

    // ===== 4️⃣ Summary (Giữ nguyên) =====
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
                "currentMonth", Map.of("income", currentIncome, "expense", currentExpense),
                "previousMonth", Map.of("income", prevIncome, "expense", prevExpense)
        );
    }

    private Map<String, Object> getCategoriesPieDataFromList(List<Transaction> allTransactions) {
        List<Transaction> expenseTransactions = allTransactions.stream()
                .filter(t -> "expense".equalsIgnoreCase(t.getType()))
                .collect(Collectors.toList());

        // Giả định hàm này trả về List<Map> với key là "categoryName" và "total"
        List<Map<String, Object>> groupedData = transactionDAO.groupTransactionsByCategory(expenseTransactions, 10);

        List<String> labels = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();
        for (Map<String, Object> item : groupedData) {
            labels.add((String) item.get("categoryName"));
            Object totalObj = item.get("total");
            // Xử lý trường hợp total là Double hoặc BigDecimal
            BigDecimal amount = totalObj instanceof Double ? BigDecimal.valueOf((Double) totalObj) : (BigDecimal) totalObj;
            values.add(amount.setScale(2, RoundingMode.HALF_UP));
        }
        return Map.of("labels", labels, "data", values);
    }

    // ===== 5️⃣ Group & Utility (Giữ nguyên) =====
    private Map<String, Object> getGroupExpenses() {
        List<Object[]> groups = groupDAO.findGroupsByUserId(CURRENT_USER_ID);
        List<Map<String, Object>> groupDetails = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Object[] g : groups) {
            UUID groupId = (UUID) g[0];
            BigDecimal raw = groupDAO.sumExpensesByGroupId(groupId);
            BigDecimal sum = raw != null ? raw : BigDecimal.ZERO;
            total = total.add(sum);

            Map<String, Object> groupMap = new LinkedHashMap<>();
            groupMap.put("id", groupId.toString());
            groupMap.put("name", g[1]);
            groupMap.put("description", g[2]);
            groupMap.put("totalAmount", sum);
            groupMap.put("isCurrentUserOwner", CURRENT_USER_ID.equals((UUID) g[3]));

            List<Map<String, Object>> members = groupDAO.findMembersByGroupId(groupId).stream()
                    .map(m -> {
                        Map<String, Object> mem = new HashMap<>();
                        mem.put("id", ((UUID) m[0]).toString());
                        mem.put("name", m[1]);
                        return mem;
                    }).collect(Collectors.toList());
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

    public List<Map<String, Object>> searchUsersByName(String name) {
        return userDAO.searchByName(name, CURRENT_USER_ID).stream()
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId().toString());
                    m.put("name", u.getFullName());
                    return m;
                })
                .collect(Collectors.toList());
    }

    private BigDecimal getSumByTypeFromList(List<Transaction> transactions, String type) {
        return transactions.stream()
                .filter(t -> type.equalsIgnoreCase(t.getType()))
                .map(t -> Optional.ofNullable(t.getAmount()).orElse(BigDecimal.ZERO))
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
                .limit(6)
                .map(t -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("date", t.getTransactionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    map.put("category", t.getCategory() != null ? t.getCategory().getName() : "Chưa phân loại");
                    map.put("amount", t.getAmount());
                    return map;
                })
                .collect(Collectors.toList());
    }
}