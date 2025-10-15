package com.expensemanager.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.expensemanager.model.DaySummary;
import com.expensemanager.model.Transaction;
import com.expensemanager.service.TransactionService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/calendar")
public class CalendarViewServlet extends HttpServlet {
    private TransactionService transactionService = new TransactionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    UUID userId = (UUID) request.getSession().getAttribute("userId");
    System.out.println("[CalendarViewServlet] userId=" + userId);

        // Xử lý chuyển tháng
        String monthParam = request.getParameter("month");
        YearMonth currentMonth;
        if (monthParam != null && !monthParam.isEmpty()) {
            currentMonth = YearMonth.parse(monthParam);
        } else {
            currentMonth = YearMonth.now();
        }

        // Tên tháng và năm cho hiển thị
        String currentMonthName = currentMonth.getMonth().toString(); // Có thể dùng thư viện để chuyển sang tiếng Việt nếu cần
        int currentYear = currentMonth.getYear();
        request.setAttribute("currentMonth", currentMonth);
        request.setAttribute("currentMonthName", currentMonthName.substring(0,1).toUpperCase() + currentMonthName.substring(1).toLowerCase());
        request.setAttribute("currentYear", currentYear);

        // Tính tháng trước và tháng sau
        YearMonth prevMonth = currentMonth.minusMonths(1);
        YearMonth nextMonth = currentMonth.plusMonths(1);
        request.setAttribute("prevMonth", prevMonth);
        request.setAttribute("nextMonth", nextMonth);

        // Tạo dữ liệu các tuần trong tháng cho lịch
        List<List<LocalDate>> calendarWeeks = new ArrayList<>();
        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        int firstDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        int daysInMonth = currentMonth.lengthOfMonth();
        List<LocalDate> week = new ArrayList<>();
        // Thêm các ngày trống đầu tuần
        for (int i = 1; i < firstDayOfWeek; i++) {
            week.add(null);
        }
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            week.add(date);
            if (week.size() == 7) {
                calendarWeeks.add(week);
                week = new ArrayList<>();
            }
        }
        // Thêm các ngày trống cuối tuần nếu cần
        if (!week.isEmpty()) {
            while (week.size() < 7) {
                week.add(null);
            }
            calendarWeeks.add(week);
        }
        request.setAttribute("calendarWeeks", calendarWeeks);

        // Lấy dữ liệu tổng hợp ngày
    Map<String, DaySummary> daySummaryMap = transactionService.getDaySummaries(userId, currentMonth);
        System.out.println("[CalendarViewServlet] daySummaryMap.size=" + (daySummaryMap != null ? daySummaryMap.size() : "null"));
        if (daySummaryMap != null) {
            for (String k : daySummaryMap.keySet()) {
                System.out.println("  " + k + ": " + daySummaryMap.get(k));
            }
        }
        request.setAttribute("daySummaryMap", daySummaryMap);

        // Xử lý ngày được chọn
        String selectedDateStr = request.getParameter("selectedDate");
        LocalDate selectedDate = null;
        if (selectedDateStr != null && !selectedDateStr.isEmpty()) {
            selectedDate = LocalDate.parse(selectedDateStr);
            request.setAttribute("selectedDate", selectedDate);
        }

        List<Transaction> selectedTransactions = new ArrayList<>();
        DaySummary selectedDaySummary = null;
        if (selectedDate != null) {
            selectedTransactions = transactionService.getTransactionsForDay(userId, selectedDate);
            selectedDaySummary = daySummaryMap.get(selectedDate.toString());
        }
        request.setAttribute("selectedTransactions", selectedTransactions);
        request.setAttribute("selectedDaySummary", selectedDaySummary);

        request.setAttribute("view", "/views/calendar.jsp");
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
        
    }
}
