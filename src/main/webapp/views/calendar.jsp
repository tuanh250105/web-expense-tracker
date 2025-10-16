<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    // Expect attributes: summary (Map<LocalDate, BigDecimal[]>), month, year, selectedDate, transactions
%>

<style>
  .calendar-container { font-family: Poppins, Arial, sans-serif; }
  .calendar-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
  .calendar-header .title { font-weight:600; font-size:18px; }
  .calendar-grid { width:100%; border-collapse:collapse; table-layout:fixed; }
  .calendar-grid th, .calendar-grid td { border:1px solid #e5e7eb; padding:8px; vertical-align:top; height:92px; }
  .calendar-grid th { background:#f9fafb; text-align:center; font-weight:600; }
  .day-number { font-weight:600; }
  .amounts { margin-top:6px; font-size:12px; line-height:1.4; }
  .income { color:#059669; }
  .expense { color:#dc2626; }
  .day-cell { position:relative; }
  .selected { background:#f1f5f9; }
  .legend { margin:8px 0 12px; font-size:12px; color:#6b7280; }
  .detail-table { width:100%; border-collapse:collapse; margin-top:14px; }
  .detail-table th, .detail-table td { border:1px solid #e5e7eb; padding:8px; text-align:left; }
  .detail-table th { background:#f9fafb; }
  .controls a { text-decoration:none; padding:6px 10px; border:1px solid #e5e7eb; border-radius:8px; color:#111827; }
  .controls a:hover { background:#f3f4f6; }
</style>

<div class="calendar-container">
  <div class="calendar-header">
    <div class="controls">
      <c:set var="prevMonth" value="${month == 1 ? 12 : month - 1}"/>
      <c:set var="prevYear" value="${month == 1 ? year - 1 : year}"/>
      <c:set var="nextMonth" value="${month == 12 ? 1 : month + 1}"/>
      <c:set var="nextYear" value="${month == 12 ? year + 1 : year}"/>
      <a href="${pageContext.request.contextPath}/calendar?month=${prevMonth}&year=${prevYear}">« Prev</a>
      <a href="${pageContext.request.contextPath}/calendar?month=${nextMonth}&year=${nextYear}" style="margin-left:6px;">Next »</a>
    </div>
    <div class="title">Tháng ${month}, ${year}</div>
    <div></div>
  </div>

  <div class="legend">Ô ngày hiển thị: số ngày, tổng Thu (xanh), tổng Chi (đỏ). Nhấn vào ngày để xem chi tiết.</div>

  <table class="calendar-grid">
    <thead>
      <tr>
        <th>Mon</th>
        <th>Tue</th>
        <th>Wed</th>
        <th>Thu</th>
        <th>Fri</th>
        <th>Sat</th>
        <th>Sun</th>
      </tr>
    </thead>
    <tbody>
    <%
      int m = (Integer) request.getAttribute("month");
      int y = (Integer) request.getAttribute("year");
      java.time.YearMonth ym = java.time.YearMonth.of(y, m);
      java.time.LocalDate first = ym.atDay(1);
      // Shift Monday(1)..Sunday(7) to 0-based start offset where Monday is first column
      int startOffset = first.getDayOfWeek().getValue() - 1; // Monday=1 -> 0
      int daysInMonth = ym.lengthOfMonth();
      java.time.LocalDate todaySelected = (java.time.LocalDate) request.getAttribute("selectedDate");
      @SuppressWarnings("unchecked")
      java.util.Map<java.time.LocalDate, java.math.BigDecimal[]> sum = (java.util.Map<java.time.LocalDate, java.math.BigDecimal[]>) request.getAttribute("summary");
      java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0");

      int cell = 0;
      int day = 1;
      for (int row = 0; row < 6; row++) {
        out.write("<tr>");
        for (int col = 0; col < 7; col++) {
          if (cell < startOffset || day > daysInMonth) {
            out.write("<td class='day-cell' style='background:#fafafa'></td>");
          } else {
            java.time.LocalDate d = ym.atDay(day);
            boolean isSelected = todaySelected != null && d.equals(todaySelected);
            String cls = isSelected ? "day-cell selected" : "day-cell";
            java.math.BigDecimal inc = java.math.BigDecimal.ZERO;
            java.math.BigDecimal exp = java.math.BigDecimal.ZERO;
            if (sum != null && sum.containsKey(d)) {
              java.math.BigDecimal[] arr = sum.get(d);
              if (arr != null) {
                if (arr[0] != null) inc = arr[0];
                if (arr[1] != null) exp = arr[1];
              }
            }
            String href = request.getContextPath() + "/calendar?month=" + m + "&year=" + y + "&date=" + d.toString();
            out.write("<td class='" + cls + "'>");
            out.write("<a href='" + href + "' style='text-decoration:none;color:inherit;display:block;height:100%'>");
            out.write("<div class='day-number'>" + day + "</div>");
            out.write("<div class='amounts'>");
            out.write("<div class='income'>+ " + df.format(inc) + "</div>");
            out.write("<div class='expense'>- " + df.format(exp) + "</div>");
            out.write("</div>");
            out.write("</a>");
            out.write("</td>");
            day++;
          }
          cell++;
        }
        out.write("</tr>");
        if (day > daysInMonth) break;
      }
    %>
    </tbody>
  </table>

  <c:if test="${not empty selectedDate}">
    <h3 style="margin-top:16px; font-weight:600;">Chi tiết ngày ${selectedDate}</h3>
    <table class="detail-table">
      <thead>
        <tr>
          <th>Type</th>
          <th>Category</th>
          <th>Amount</th>
          <th>Note</th>
        </tr>
      </thead>
      <tbody>
        <c:choose>
          <c:when test="${empty transactions}">
            <tr><td colspan="4">Không có giao dịch</td></tr>
          </c:when>
          <c:otherwise>
            <c:forEach var="t" items="${transactions}">
              <tr>
                <td>
                  <c:choose>
                    <c:when test="${t.type eq 'income'}">
                      <span style="color:#059669;font-weight:600;">INCOME</span>
                    </c:when>
                    <c:otherwise>
                      <span style="color:#dc2626;font-weight:600;">EXPENSE</span>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td>${t.category.name}</td>
                <td>
                  <c:choose>
                    <c:when test="${t.type eq 'income'}">+ đ<fmt:formatNumber value="${t.amount}" pattern=",##0"/></c:when>
                    <c:otherwise>- đ<fmt:formatNumber value="${t.amount}" pattern=",##0"/></c:otherwise>
                  </c:choose>
                </td>
                <td>${t.note}</td>
              </tr>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </tbody>
    </table>
  </c:if>
</div>
