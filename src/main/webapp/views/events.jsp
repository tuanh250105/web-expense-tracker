<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<section>
  <h2>Sự kiện tài chính</h2>
  <div>
    <strong>Điểm hiện có:</strong>
    <span id="pointScore"><c:out value="${points.score}"/></span>
    <button onclick="redeemPoints(10)">Minigame: Redeem 10 điểm</button>
  </div>

  <form method="post" action="${pageContext.request.contextPath}/events">
    <input type="hidden" name="action" value="create"/>
    <label>Tên sự kiện: <input type="text" name="name" required></label>
    <label>Mục tiêu: <input type="number" step="0.01" name="goalAmount"></label>
    <label>Bắt đầu: <input type="date" name="startDate"></label>
    <label>Kết thúc: <input type="date" name="endDate"></label>
    <button type="submit">Tạo sự kiện</button>
  </form>

  <hr/>
  <h3>Danh sách sự kiện</h3>
  <table class="table">
    <thead><tr><th>Tên</th><th>Mục tiêu</th><th>Thời gian</th><th>Trạng thái</th><th>Hành động</th></tr></thead>
    <tbody>
      <c:forEach var="e" items="${events}">
        <tr>
          <td>${e.name}</td>
          <td>${e.goalAmount}</td>
          <td>${e.startDate} → ${e.endDate}</td>
          <td>${e.status}</td>
          <td>
            <form method="post" action="${pageContext.request.contextPath}/events/${e.id}/attach-tx" style="display:inline">
              <input type="number" name="transactionId" placeholder="ID giao dịch" required>
              <button type="submit">Attach TX</button>
            </form>
            <button type="button" onclick="loadEventSeries(${e.id})">Xem biểu đồ</button>
            <form method="post" action="${pageContext.request.contextPath}/events" style="display:inline">
              <input type="hidden" name="action" value="archive"/>
              <input type="hidden" name="id" value="${e.id}"/>
              <button type="submit">Archive</button>
            </form>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>

  <canvas id="eventChart" width="600" height="240"></canvas>
</section>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script src="${pageContext.request.contextPath}/charts/eventChart.js"></script>
<script>
  function redeemPoints(amount){
    const base = window.BB_CTX || '';
    fetch(`${base}/points/redeem`,{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},body:`amount=${amount}`})
      .then(r=>r.json()).then(j=>{ if(j.ok){ const el=document.getElementById('pointScore'); el.textContent = (parseInt(el.textContent||'0') - amount); }});
  }
</script>


