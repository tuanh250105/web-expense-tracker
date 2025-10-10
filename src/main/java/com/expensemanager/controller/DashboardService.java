//package com.expensemanager.controller;
//
//import com.expensemanager.entity.Category;
//import com.expensemanager.entity.Transaction;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.Query;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.stream.Collectors;
//
//public class DashboardService {
//
//    public EntityManager em; // được inject từ servlet
//
//    /** 🔹 Lấy dữ liệu tổng quan từ database */
//    public Map<String, Object> getOverviewData(String period) {
//        Map<String, Object> data = new HashMap<>();
//
//        try {
//            // 1️⃣ Xác định ngày bắt đầu dựa trên period
//            LocalDate startDate = LocalDate.now();
//            switch (period.toLowerCase()) {
//                case "week":
//                    startDate = LocalDate.now().minusWeeks(1);
//                    break;
//                case "year":
//                    startDate = LocalDate.now().minusYears(1);
//                    break;
//                case "month":
//                default:
//                    startDate = LocalDate.now().minusMonths(1);
//                    break;
//            }
//
//            LocalDateTime startDateTime = startDate.atStartOfDay();
//
//            // 2️⃣ Lấy 5 giao dịch gần đây nhất
//            String recentTxQuery = "SELECT t.category.name, t.transactionDate, t.amount " +
//                    "FROM Transaction t " +
//                    "WHERE t.transactionDate IS NOT NULL " +
//                    "ORDER BY t.transactionDate DESC";
//            Query recentTxJpql = em.createQuery(recentTxQuery, Object[].class);
//            recentTxJpql.setMaxResults(5);
//            List<Object[]> recentTxList = recentTxJpql.getResultList();
//            List<Map<String, Object>> transactions = recentTxList.stream().map(row -> {
//                Map<String, Object> tx = new HashMap<>();
//                tx.put("category", row[0] != null ? row[0].toString() : "Không xác định");
//                tx.put("date", ((LocalDateTime) row[1]).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
//                tx.put("amount", ((Number) row[2]).doubleValue());
//                return tx;
//            }).collect(Collectors.toList());
//            data.put("transactions", transactions);
//
//            // 3️⃣ Lấy transactions từ DB
//            String query = "SELECT t.type, t.category.name, t.amount " +
//                    "FROM Transaction t " +
//                    "WHERE t.transactionDate >= :startDate";
//            Query jpqlQuery = em.createQuery(query, Object[].class);
//            jpqlQuery.setParameter("startDate", startDateTime);
//            List<Object[]> txList = jpqlQuery.getResultList();
//
//            double totalIncome = 0;
//            double totalExpense = 0;
//            Map<String, Double> categoryTotals = new HashMap<>();
//
//            for (Object[] row : txList) {
//                String type = (String) row[0];
//                String category = row[1] != null ? (String) row[1] : "Không xác định";
//                double amount = ((Number) row[2]).doubleValue();
//
//                if ("income".equalsIgnoreCase(type)) {
//                    totalIncome += amount;
//                } else if ("expense".equalsIgnoreCase(type)) {
//                    totalExpense += amount;
//                }
//
//                categoryTotals.merge(category, amount, Double::sum);
//            }
//
//            // 4️⃣ Biểu đồ Thu – Chi
//            Map<String, Object> incomeExpense = new HashMap<>();
//            LocalDate today = LocalDate.now();
//            DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("dd/MM");
//            DateTimeFormatter monthFormat = DateTimeFormatter.ofPattern("MM/yyyy");
//
//            if ("week".equalsIgnoreCase(period)) {
//                List<String> labels = new ArrayList<>();
//                List<Double> incomeData = new ArrayList<>();
//                List<Double> expenseData = new ArrayList<>();
//
//                for (int i = 6; i >= 0; i--) {
//                    LocalDate date = today.minusDays(i);
//                    labels.add(date.format(dayFormat));
//
//                    String dailyQuery = "SELECT t.type, SUM(t.amount) " +
//                            "FROM Transaction t " +
//                            "WHERE DATE(t.transactionDate) = :date GROUP BY t.type";
//                    Query dq = em.createQuery(dailyQuery, Object[].class);
//                    dq.setParameter("date", date);
//                    List<Object[]> results = dq.getResultList();
//
//                    double dailyIncome = 0, dailyExpense = 0;
//                    for (Object[] row : results) {
//                        String type = (String) row[0];
//                        double amount = ((Number) row[1]).doubleValue();
//                        if ("income".equalsIgnoreCase(type)) dailyIncome = amount;
//                        if ("expense".equalsIgnoreCase(type)) dailyExpense = amount;
//                    }
//                    incomeData.add(dailyIncome / 1_000_000);
//                    expenseData.add(dailyExpense / 1_000_000);
//                }
//
//                incomeExpense.put("labels", labels);
//                incomeExpense.put("income", incomeData);
//                incomeExpense.put("expense", expenseData);
//            } else if ("month".equalsIgnoreCase(period)) {
//                List<String> labels = new ArrayList<>();
//                List<Double> incomeData = new ArrayList<>();
//                List<Double> expenseData = new ArrayList<>();
//
//                LocalDate firstDay = today.withDayOfMonth(1);
//                int daysInMonth = today.lengthOfMonth();
//
//                for (int d = 1; d <= daysInMonth; d++) {
//                    LocalDate date = firstDay.plusDays(d - 1);
//                    labels.add(date.format(dayFormat));
//
//                    String dailyQuery = "SELECT t.type, SUM(t.amount) " +
//                            "FROM Transaction t " +
//                            "WHERE MONTH(t.transactionDate) = :month AND YEAR(t.transactionDate) = :year " +
//                            "AND DAY(t.transactionDate) = :day GROUP BY t.type";
//                    Query dq = em.createQuery(dailyQuery, Object[].class);
//                    dq.setParameter("month", date.getMonthValue());
//                    dq.setParameter("year", date.getYear());
//                    dq.setParameter("day", d);
//                    List<Object[]> results = dq.getResultList();
//
//                    double dailyIncome = 0, dailyExpense = 0;
//                    for (Object[] row : results) {
//                        String type = (String) row[0];
//                        double amount = ((Number) row[1]).doubleValue();
//                        if ("income".equalsIgnoreCase(type)) dailyIncome = amount;
//                        if ("expense".equalsIgnoreCase(type)) dailyExpense = amount;
//                    }
//                    incomeData.add(dailyIncome / 1_000_000);
//                    expenseData.add(dailyExpense / 1_000_000);
//                }
//
//                incomeExpense.put("labels", labels);
//                incomeExpense.put("income", incomeData);
//                incomeExpense.put("expense", expenseData);
//            } else if ("year".equalsIgnoreCase(period)) {
//                List<String> labels = new ArrayList<>();
//                List<Double> incomeData = new ArrayList<>();
//                List<Double> expenseData = new ArrayList<>();
//
//                for (int m = 1; m <= 12; m++) {
//                    labels.add(String.format("%02d/%d", m, today.getYear()));
//
//                    String monthlyQuery = "SELECT t.type, SUM(t.amount) " +
//                            "FROM Transaction t " +
//                            "WHERE MONTH(t.transactionDate) = :month AND YEAR(t.transactionDate) = :year GROUP BY t.type";
//                    Query dq = em.createQuery(monthlyQuery, Object[].class);
//                    dq.setParameter("month", m);
//                    dq.setParameter("year", today.getYear());
//                    List<Object[]> results = dq.getResultList();
//
//                    double monthlyIncome = 0, monthlyExpense = 0;
//                    for (Object[] row : results) {
//                        String type = (String) row[0];
//                        double amount = ((Number) row[1]).doubleValue();
//                        if ("income".equalsIgnoreCase(type)) monthlyIncome = amount;
//                        if ("expense".equalsIgnoreCase(type)) monthlyExpense = amount;
//                    }
//                    incomeData.add(monthlyIncome / 1_000_000);
//                    expenseData.add(monthlyExpense / 1_000_000);
//                }
//
//                incomeExpense.put("labels", labels);
//                incomeExpense.put("income", incomeData);
//                incomeExpense.put("expense", expenseData);
//            }
//
//            data.put("incomeExpense", incomeExpense);
//
//            // 5️⃣ Biểu đồ theo loại
//            Map<String, Object> categories = new HashMap<>();
//            categories.put("labels", categoryTotals.isEmpty() ? List.of("Không có dữ liệu") : new ArrayList<>(categoryTotals.keySet()));
//            categories.put("data", categoryTotals.isEmpty() ? List.of(0.0) : categoryTotals.values().stream()
//                    .map(val -> val / 1_000_000)
//                    .collect(Collectors.toList()));
//            data.put("categories", categories);
//
//            // 6️⃣ Biểu đồ số dư theo thời gian
//            String balanceQuery = "SELECT t.transactionDate, COALESCE(SUM(CASE WHEN t.type = 'income' THEN t.amount ELSE -t.amount END), 0) " +
//                    "FROM Transaction t " +
//                    "WHERE t.transactionDate >= :startDate GROUP BY t.transactionDate ORDER BY t.transactionDate";
//            Query balanceJpqlQuery = em.createQuery(balanceQuery, Object[].class);
//            balanceJpqlQuery.setParameter("startDate", startDateTime);
//            List<Object[]> balanceData = balanceJpqlQuery.getResultList();
//
//            List<String> balanceLabels = new ArrayList<>();
//            List<Double> balanceValues = new ArrayList<>();
//            double cumulativeBalance = 0;
//            if (!balanceData.isEmpty()) {
//                for (Object[] row : balanceData) {
//                    balanceLabels.add(((LocalDateTime) row[0]).toLocalDate().toString());
//                    cumulativeBalance += ((Number) row[1]).doubleValue();
//                    balanceValues.add(cumulativeBalance / 1_000_000);
//                }
//            } else {
//                balanceLabels.add("Hiện tại");
//                balanceValues.add(0.0);
//            }
//
//            Map<String, Object> balance = new HashMap<>();
//            balance.put("labels", balanceLabels);
//            balance.put("data", balanceValues);
//            data.put("balance", balance);
//
//            // 7️⃣ Tổng chi phí
//            data.put("totalExpense", totalExpense / 1_000_000);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            data.put("error", e.getMessage());
//        }
//
//        return data;
//    }
//}
package com.expensemanager.controller;

import jakarta.persistence.EntityManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardService {

    public EntityManager em;

    // --- CẤU TRÚC DỮ LIỆU GIẢ LẬP MỚI ---
    private static final Map<String, String> MOCK_USERS = Map.of(
            "user-an", "An Nguyễn",
            "user-binh", "Bình Trần",
            "user-chau", "Châu Lê",
            "user-dung", "Dũng Phạm",
            "user-ha", "Hà Mai"
    );

    // Giả định người dùng đang đăng nhập là "An Nguyễn"
    private static final String CURRENT_USER_ID = "user-an";

    private static final List<Map<String, Object>> MOCK_GROUPS = Arrays.asList(
            Map.of("id", "group-1", "name", "Nhóm Du Lịch Đà Lạt", "description", "Chuyến đi cuối năm.", "ownerId", "user-an"),
            Map.of("id", "group-2", "name", "Tiền Ăn Trưa Văn Phòng", "description", " склад cho bữa trưa hàng ngày.", "ownerId", "user-binh"),
            Map.of("id", "group-3", "name", "Dự Án Từ Thiện", "description", "Quyên góp cho trẻ em vùng cao.", "ownerId", "user-an")
    );

    private static final List<Map<String, String>> MOCK_GROUP_MEMBERS = Arrays.asList(
            // Nhóm 1
            Map.of("groupId", "group-1", "userId", "user-an", "role", "OWNER"),
            Map.of("groupId", "group-1", "userId", "user-chau", "role", "MEMBER"),
            Map.of("groupId", "group-1", "userId", "user-dung", "role", "MEMBER"),
            // Nhóm 2
            Map.of("groupId", "group-2", "userId", "user-an", "role", "MEMBER"),
            Map.of("groupId", "group-2", "userId", "user-binh", "role", "OWNER"),
            Map.of("groupId", "group-2", "userId", "user-ha", "role", "MEMBER"),
            // Nhóm 3
            Map.of("groupId", "group-3", "userId", "user-an", "role", "OWNER"),
            Map.of("groupId", "group-3", "userId", "user-ha", "role", "MEMBER")
    );
    private final Random random = new Random(12345);

    // ... (Các phương thức getOverviewData không thay đổi)
    public Map<String, Object> getOverviewData(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        return getOverviewDataForDateRange(startDate, endDate, "day");
    }

    public Map<String, Object> getOverviewData(int year, int month, int week) {
        LocalDate[] weekDates = getStartAndEndOfWeek(year, month, week);
        return getOverviewDataForDateRange(weekDates[0], weekDates[1], "day");
    }

    public Map<String, Object> getOverviewData(String period) {
        LocalDate today = LocalDate.of(2025, 10, 10);
        LocalDate startDate;
        LocalDate endDate = today;
        String groupBy = "day";

        switch (period) {
            case "week":
                startDate = today.minusDays(6);
                break;
            case "year":
                startDate = today.with(TemporalAdjusters.firstDayOfYear());
                endDate = today.with(TemporalAdjusters.lastDayOfYear());
                groupBy = "month";
                break;
            case "month":
            default:
                startDate = today.with(TemporalAdjusters.firstDayOfMonth());
                endDate = today.with(TemporalAdjusters.lastDayOfMonth());
                break;
        }
        return getOverviewDataForDateRange(startDate, endDate, groupBy);
    }
    // ... (Phần còn lại của các phương thức xử lý biểu đồ giữ nguyên)
    private Map<String, Object> getOverviewDataForDateRange(LocalDate startDate, LocalDate endDate, String groupBy) {
        Map<String, Object> data = new HashMap<>();
        try {
            List<Map<String, Object>> allTransactions = generateRandomTransactions(LocalDate.of(2025, 10, 10));

            List<Map<String, Object>> filteredTransactions = allTransactions.stream()
                    .filter(tx -> {
                        LocalDate txDate = LocalDate.parse((String) tx.get("dateSort"));
                        return !txDate.isBefore(startDate) && !txDate.isAfter(endDate);
                    })
                    .collect(Collectors.toList());

            data.put("transactions", filteredTransactions.stream().limit(6).collect(Collectors.toList()));
            data.put("monthlyIncomeExpense", getMonthlyPieChartData(allTransactions));

            // THAY ĐỔI QUAN TRỌNG: Gọi hàm mới để lấy dữ liệu nhóm
            data.put("groupExpenses", getGroupExpenseData(allTransactions));

            if ("month".equals(groupBy)) {
                calculateGroupedDataByMonth(data, filteredTransactions, startDate);
            } else {
                calculateGroupedDataByDay(data, filteredTransactions, startDate, endDate);
            }

        } catch (Exception e) {
            e.printStackTrace();
            data.put("error", "Lỗi server: " + e.getClass().getSimpleName());
        }
        return data;
    }

    // --- HÀM MỚI: LẤY DỮ LIỆU CHI TIÊU NHÓM DỰA TRÊN USER HIỆN TẠI ---
    private Map<String, Object> getGroupExpenseData(List<Map<String, Object>> allTransactions) {
        // Lọc ra các ID nhóm mà người dùng hiện tại tham gia
        Set<String> userGroupIds = MOCK_GROUP_MEMBERS.stream()
                .filter(member -> CURRENT_USER_ID.equals(member.get("userId")))
                .map(member -> member.get("groupId"))
                .collect(Collectors.toSet());

        List<Map<String, Object>> groupDetails = new ArrayList<>();
        double grandTotal = 0;

        // Lặp qua các nhóm mà người dùng tham gia
        for (String groupId : userGroupIds) {
            Map<String, Object> groupInfo = MOCK_GROUPS.stream()
                    .filter(g -> groupId.equals(g.get("id")))
                    .findFirst().orElse(null);

            if (groupInfo != null) {
                // Tính tổng chi tiêu cho nhóm này
                double groupTotal = allTransactions.stream()
                        .filter(tx -> groupId.equals(tx.get("groupId")) && "Chi phí".equals(tx.get("type")))
                        .mapToDouble(tx -> ((Number) tx.get("amount")).doubleValue())
                        .sum();

                grandTotal += groupTotal;

                // Lấy danh sách thành viên của nhóm
                List<Map<String, String>> members = MOCK_GROUP_MEMBERS.stream()
                        .filter(m -> groupId.equals(m.get("groupId")))
                        .map(m -> Map.of("id", m.get("userId"), "name", MOCK_USERS.get(m.get("userId"))))
                        .collect(Collectors.toList());

                // Xây dựng đối tượng chi tiết cho nhóm
                Map<String, Object> detail = new LinkedHashMap<>();
                detail.put("id", groupId);
                detail.put("name", groupInfo.get("name"));
                detail.put("description", groupInfo.get("description"));
                detail.put("totalAmount", groupTotal / 1_000_000);
                detail.put("members", members);
                detail.put("isCurrentUserOwner", CURRENT_USER_ID.equals(groupInfo.get("ownerId")));

                groupDetails.add(detail);
            }
        }

        return Map.of("total", grandTotal / 1_000_000, "details", groupDetails);
    }

    // Cập nhật hàm generateRandomTransactions để gán groupId
    private List<Map<String, Object>> generateRandomTransactions(LocalDate today) {
        List<Map<String, Object>> list = new ArrayList<>();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate startDate = today.minusYears(2).with(TemporalAdjusters.firstDayOfYear());
        LocalDate endDate = today.with(TemporalAdjusters.lastDayOfYear());
        double runningBalance = 20_000_000;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            int transactionsPerDay = random.nextInt(4) + 2;
            for (int i = 0; i < transactionsPerDay; i++) {
                boolean isIncome = random.nextInt(10) < 1;
                String category = isIncome ? "Lương" : "Ăn uống"; // Đơn giản hóa
                double amount = isIncome ? (1_000_000 + random.nextDouble() * 10_000_000) : Math.min(50_000 + random.nextDouble() * 1_500_000, runningBalance * 0.1);

                Map<String, Object> tx = new HashMap<>();
                tx.put("category", category);
                tx.put("date", date.format(dateFmt));
                tx.put("dateSort", date.toString());
                tx.put("amount", Math.round(amount));
                tx.put("type", isIncome ? "Thu nhập" : "Chi phí");

                // Gán ngẫu nhiên một groupId cho mỗi giao dịch
                if (!isIncome) {
                    tx.put("groupId", MOCK_GROUPS.get(random.nextInt(MOCK_GROUPS.size())).get("id"));
                }

                list.add(tx);
                runningBalance += isIncome ? amount : -amount;
                if (runningBalance < 0) runningBalance = 0;
            }
        }
        list.sort(Comparator.comparing((Map<String, Object> tx) -> (String) tx.get("dateSort")).reversed());
        return list;
    }

    // --- Các hàm tiện ích khác giữ nguyên ---
    private void calculateGroupedDataByDay(Map<String, Object> data, List<Map<String, Object>> transactions, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> incomeExpense = new HashMap<>();
        Map<String, Object> balance = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Double> incomeData = new ArrayList<>();
        List<Double> expenseData = new ArrayList<>();
        List<Double> balanceData = new ArrayList<>();
        DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("dd/MM");
        double runningBalance = 10_000_000 / 1_000_000;
        double totalExpense = 0.0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            labels.add(date.format(dayFormat));
            double dailyIncome = 0.0;
            double dailyExpense = 0.0;
            for (Map<String, Object> tx : transactions) {
                if (LocalDate.parse((String) tx.get("dateSort")).equals(date)) {
                    double amount = ((Number) tx.get("amount")).doubleValue() / 1_000_000;
                    if ("Thu nhập".equals(tx.get("type"))) dailyIncome += amount;
                    else dailyExpense += amount;
                }
            }
            incomeData.add(dailyIncome);
            expenseData.add(dailyExpense);
            totalExpense += dailyExpense;
            runningBalance += dailyIncome - dailyExpense;
            balanceData.add(Math.max(0, runningBalance));
        }
        incomeExpense.put("labels", labels);
        incomeExpense.put("income", incomeData);
        incomeExpense.put("expense", expenseData);
        data.put("incomeExpense", incomeExpense);
        balance.put("labels", labels);
        balance.put("data", balanceData);
        data.put("balance", balance);
        data.put("categories", getCategoriesPieChartData(transactions));
    }
    private void calculateGroupedDataByMonth(Map<String, Object> data, List<Map<String, Object>> transactions, LocalDate startDate) {
        Map<String, Object> incomeExpense = new HashMap<>();
        Map<String, Object> balance = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Double> incomeData = new ArrayList<>();
        List<Double> expenseData = new ArrayList<>();
        List<Double> balanceData = new ArrayList<>();
        DateTimeFormatter monthFormat = DateTimeFormatter.ofPattern("MM/yy");
        double runningBalance = 10_000_000 / 1_000_000;
        double totalExpense = 0.0;
        for (int m = 1; m <= 12; m++) {
            final int currentMonth = m;
            LocalDate monthDate = startDate.withMonth(m);
            labels.add(monthDate.format(monthFormat));
            double monthlyIncome = 0.0;
            double monthlyExpense = 0.0;
            for (Map<String, Object> tx : transactions) {
                if (LocalDate.parse((String) tx.get("dateSort")).getMonthValue() == currentMonth) {
                    double amount = ((Number) tx.get("amount")).doubleValue() / 1_000_000;
                    if ("Thu nhập".equals(tx.get("type"))) monthlyIncome += amount;
                    else monthlyExpense += amount;
                }
            }
            incomeData.add(monthlyIncome);
            expenseData.add(monthlyExpense);
            totalExpense += monthlyExpense;
            runningBalance += monthlyIncome - monthlyExpense;
            balanceData.add(Math.max(0, runningBalance));
        }
        incomeExpense.put("labels", labels);
        incomeExpense.put("income", incomeData);
        incomeExpense.put("expense", expenseData);
        data.put("incomeExpense", incomeExpense);
        balance.put("labels", labels);
        balance.put("data", balanceData);
        data.put("balance", balance);
        data.put("categories", getCategoriesPieChartData(transactions));
    }
    private Map<String, Object> getMonthlyPieChartData(List<Map<String, Object>> allTransactions) {
        LocalDate today = LocalDate.of(2025, 10, 10);
        Map<String, Object> monthlyIncomeExpense = new HashMap<>();
        double currentIncome = allTransactions.stream().filter(tx -> "Thu nhập".equals(tx.get("type")) && LocalDate.parse((String) tx.get("dateSort")).getMonth() == today.getMonth()).mapToDouble(tx -> ((Number) tx.get("amount")).doubleValue()).sum() / 1_000_000;
        double currentExpense = allTransactions.stream().filter(tx -> !"Thu nhập".equals(tx.get("type")) && LocalDate.parse((String) tx.get("dateSort")).getMonth() == today.getMonth()).mapToDouble(tx -> ((Number) tx.get("amount")).doubleValue()).sum() / 1_000_000;
        LocalDate prevMonth = today.minusMonths(1);
        double prevIncome = allTransactions.stream().filter(tx -> "Thu nhập".equals(tx.get("type")) && LocalDate.parse((String) tx.get("dateSort")).getMonth() == prevMonth.getMonth()).mapToDouble(tx -> ((Number) tx.get("amount")).doubleValue()).sum() / 1_000_000;
        double prevExpense = allTransactions.stream().filter(tx -> !"Thu nhập".equals(tx.get("type")) && LocalDate.parse((String) tx.get("dateSort")).getMonth() == prevMonth.getMonth()).mapToDouble(tx -> ((Number) tx.get("amount")).doubleValue()).sum() / 1_000_000;
        monthlyIncomeExpense.put("currentMonth", Map.of("income", currentIncome, "expense", currentExpense));
        monthlyIncomeExpense.put("previousMonth", Map.of("income", prevIncome, "expense", prevExpense));
        return monthlyIncomeExpense;
    }
    private Map<String, Object> getCategoriesPieChartData(List<Map<String, Object>> transactions) {
        Map<String, Double> categoryTotals = transactions.stream().filter(tx -> !"Thu nhập".equals(tx.get("type"))).collect(Collectors.groupingBy(tx -> (String) tx.get("category"), Collectors.summingDouble(tx -> ((Number) tx.get("amount")).doubleValue() / 1_000_000)));
        List<String> labels = new ArrayList<>(categoryTotals.keySet());
        List<Double> data = new ArrayList<>(categoryTotals.values());
        return Map.of("labels", labels, "data", data);
    }
    private LocalDate[] getStartAndEndOfWeek(int year, int month, int weekNum) {
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        LocalDate date = firstDayOfMonth;
        int currentWeek = 1;
        while (currentWeek < weekNum) {
            date = date.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            if (date.getMonthValue() != month) {
                return new LocalDate[]{firstDayOfMonth.with(TemporalAdjusters.lastDayOfMonth()), firstDayOfMonth.with(TemporalAdjusters.lastDayOfMonth())};
            }
            currentWeek++;
        }
        LocalDate startDate = (weekNum == 1) ? firstDayOfMonth : date;
        LocalDate endDate = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        if (endDate.getMonthValue() != month) {
            endDate = firstDayOfMonth.with(TemporalAdjusters.lastDayOfMonth());
        }
        return new LocalDate[]{startDate, endDate};
    }
}




