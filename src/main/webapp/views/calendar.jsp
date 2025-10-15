<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Lịch Chi Tiêu</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/calendar.css">
</head>
<body>
    <h2>Lịch tháng ${currentMonth.monthValue}/${currentMonth.year}</h2>
    <table border="1" class="calendar-table">
        <thead>
            <tr>
                <th>CN</th><th>T2</th><th>T3</th><th>T4</th><th>T5</th><th>T6</th><th>T7</th>
            </tr>
        </thead>
        <tbody>
        <c:set var="day" value="1"/>
        <c:forEach var="week" begin="1" end="6">
            <tr>
            <c:forEach var="dow" begin="1" end="7">
                <c:choose>
                    <c:when test="${week == 1 && dow < currentMonth.atDay(1).getDayOfWeek().getValue()}">
                        <td></td>
                    </c:when>
                    <c:when test="${day > maxDay}">
                        <td></td>
                    </c:when>
                    <c:otherwise>
                        <c:set var="date" value="${currentMonth.year}-${currentMonth.monthValue < 10 ? '0' : ''}${currentMonth.monthValue}-${day < 10 ? '0' : ''}${day}" />
                        <c:set var="summary" value="${daySummaryMap[date]}" />
                        <td>
                            <div>${day}</div>
                            <c:if test="${summary != null}">
                                <div style="color:green">+${summary.income}</div>
                                <div style="color:red">-${summary.expense}</div>
                            </c:if>
                        </td>
                        <c:set var="day" value="${day + 1}"/>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</body>
</html>
