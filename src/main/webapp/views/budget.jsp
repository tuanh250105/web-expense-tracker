<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<section>
  <h2>Ngân sách</h2>
  <form method="post" action="${pageContext.request.contextPath}/budgets" class="budget-form">
    <input type="hidden" name="action" value="create"/>
    <label>Chu kỳ:
      <select name="periodType">
        <option value="WEEK">Tuần</option>
        <option value="MONTH">Tháng</option>
      </select>
    </label>
    <label>Bắt đầu: <input type="date" name="periodStart" required></label>
    <label>Kết thúc: <input type="date" name="periodEnd" required></label>
    <label>Danh mục (optional): <input type="number" name="categoryId" placeholder="ID danh mục"></label>
    <label>Hạn mức: <input type="number" step="0.01" name="limitAmount" required></label>
    <label>Ghi chú: <input type="text" name="note"></label>
    <button type="submit">Lưu ngân sách</button>
  </form>

  <hr/>
  <h3>Danh sách ngân sách</h3>
  <table class="table">
    <thead>
      <tr>
        <th>Chu kỳ</th><th>Thời gian</th><th>Danh mục</th><th>Hạn mức</th><th>Đã chi</th><th>Tiến độ</th><th>Chi tiết</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="b" items="${budgets}">
        <tr>
          <td>${b.periodType}</td>
          <td>${b.periodStart} → ${b.periodEnd}</td>
          <td><c:choose><c:when test="${b.categoryId != null}">${b.categoryId}</c:when><c:otherwise>Tất cả</c:otherwise></c:choose></td>
          <td>${b.limitAmount}</td>
          <td>${b.spentAmount}</td>
          <td>
            <div style="background:#eee;width:160px;height:10px;position:relative;border-radius:6px;">
              <c:set var="pct" value="${(b.spentAmount * 100.0) / b.limitAmount}"/>
              <div style="background:#4caf50;height:10px;border-radius:6px;width:${pct}%"></div>
            </div>
          </td>
          <td><button type="button" onclick="loadBudgetSeries(${b.id})">Xem</button></td>
        </tr>
      </c:forEach>
    </tbody>
  </table>

  <canvas id="budgetChart" width="600" height="240"></canvas>
</section>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script src="${pageContext.request.contextPath}/charts/budgetChart.js"></script>


