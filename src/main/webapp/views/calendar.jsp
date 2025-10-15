<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>Lịch Chi Tiêu</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/calendar.css">
</head>
<body>
    <h2>Lịch tháng ${currentMonth.monthValue}/${currentMonth.year}</h2>
        <!-- Month navigation -->
        <div style="display:flex;justify-content:center;align-items:center;margin-bottom:1em;">
            <form method="get" action="calendar" style="margin:0;">
                <input type="hidden" name="month" value="${prevMonth.year}-${(prevMonth.monthValue < 10) ? '0' : ''}${prevMonth.monthValue}" />
                <button type="submit" style="background:none;border:none;font-size:2em;padding:0 1em;">&#8592;</button>
            </form>
            <span style="font-size:1.5em;font-weight:bold;">${currentMonthName} ${currentYear}</span>
            <form method="get" action="calendar" style="margin:0;">
                <input type="hidden" name="month" value="${nextMonth.year}-${(nextMonth.monthValue < 10) ? '0' : ''}${nextMonth.monthValue}" />
                <button type="submit" style="background:none;border:none;font-size:2em;padding:0 1em;">&#8594;</button>
            </form>
        </div>

        <!-- Calendar table -->
        <table style="width:100%;border-collapse:separate;border-spacing:0 8px;background:#fff;border-radius:12px;box-shadow:0 2px 8px #eee;">
            <thead>
                <tr style="background:#f7f7f7;">
                    <th style="padding:0.5em 0.2em;color:#888;">Mon</th>
                    <th style="padding:0.5em 0.2em;color:#888;">Tue</th>
                    <th style="padding:0.5em 0.2em;color:#888;">Wed</th>
                    <th style="padding:0.5em 0.2em;color:#888;">Thu</th>
                    <th style="padding:0.5em 0.2em;color:#888;">Fri</th>
                    <th style="padding:0.5em 0.2em;color:#888;">Sat</th>
                    <th style="padding:0.5em 0.2em;color:#888;">Sun</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="week" items="${calendarWeeks}">
                    <tr>
                        <c:forEach var="day" items="${week}">
                            <td style="text-align:center;padding:0.5em 0.2em;vertical-align:top;">
                                <c:if test="${day != null}">
                                    <form method="get" action="calendar" style="display:inline;">
                                        <input type="hidden" name="selectedDate" value="${day}" />
                                        <input type="hidden" name="month" value="${currentMonth.year}-${(currentMonth.monthValue < 10) ? '0' : ''}${currentMonth.monthValue}" />
                                        <button type="submit" style="background:none;border:none;padding:0;width:40px;height:40px;border-radius:50%;${selectedDate == day ? 'background:#eaf4fb;' : ''}">
                                            <button type="submit" class="calendar-btn<c:if test='${selectedDate == day}'> selected</c:if>'" style="background:none;border:none;padding:0;width:40px;height:40px;border-radius:50%;">
                                                <div style="font-weight:bold;">${day.dayOfMonth}</div>
                                            <c:if test="${daySummaryMap[day] != null}">
                                                <!-- Nếu có chi tiêu, ưu tiên chấm đỏ và số tiền chi tiêu -->
                                                <c:if test="${daySummaryMap[day.toString()] != null && daySummaryMap[day.toString()].expense > 0}">
                                                    <span style="color:red;font-size:1em;">●</span>
                                                    <div style="font-size:0.9em;color:red;margin-top:2px;">
                                                        -<fmt:formatNumber value="${daySummaryMap[day.toString()].expense}" type="currency"/>
                                                    </div>
                                                </c:if>
                                                <!-- Nếu không có chi tiêu, nhưng có thu nhập -->
                                                <c:if test="${daySummaryMap[day.toString()] != null && daySummaryMap[day.toString()].expense <= 0 && daySummaryMap[day.toString()].income > 0}">
                                                    <span style="color:green;font-size:1em;">●</span>
                                                    <div style="font-size:0.9em;color:green;margin-top:2px;">
                                                        <fmt:formatNumber value="${daySummaryMap[day.toString()].income}" type="currency"/>
                                                    </div>
                                                </c:if>
                                            </c:if>
                                        </button>
                                    </form>
                                </c:if>
                            </td>
                        </c:forEach>
                    </tr>
                </c:forEach>
            </tbody>
        </table>

    <!-- Transaction details for selected day -->
    <c:if test="${not empty selectedDate}">
        <div class="day-details-card" style="margin-top:2em;padding:1em;border-radius:8px;background:#fff;box-shadow:0 2px 8px #eee;">
            <h3>
                <fmt:formatDate value="${selectedDate}" pattern="EEEE, MMMM dd, yyyy"/>
            </h3>
            <div>
                <span style="color:green;">● Income:</span>
                <fmt:formatNumber value="${selectedDaySummary.income}" type="currency"/>
                <span style="color:red;margin-left:2em;">● Expenses:</span>
                <fmt:formatNumber value="${selectedDaySummary.expense}" type="currency"/>
            </div>
            <c:forEach var="txn" items="${selectedTransactions}">
                <div class="txn-row" style="display:flex;align-items:center;margin-top:1em;padding:0.5em;border-radius:6px;background:#fafafa;">
                    <img src="${txn.categoryIcon}" class="txn-icon" style="width:40px;height:40px;margin-right:1em;"/>
                    <div style="flex:1;">
                        <div><b>${txn.categoryName}</b> (${txn.type})</div>
                        <div>${txn.accountName}</div>
                    </div>
                    <c:choose>
                        <c:when test="${txn.amount > 0}">
                            <div class="txn-amount income">
                                <fmt:formatNumber value="${txn.amount}" type="currency"/>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="txn-amount expense">
                                <fmt:formatNumber value="${txn.amount}" type="currency"/>
                            </div>
                        </c:otherwise>
                    </c:choose>
                    <div>
                        <fmt:formatDate value="${txn.date}" pattern="MM/dd/yyyy"/>
                    </div>
                </div>
            </c:forEach>
            <c:if test="${empty selectedTransactions}">
                <div style="margin-top:1em;color:#888;">No transactions for this day.</div>
            </c:if>
        </div>
    </c:if>
</body>
</html>
