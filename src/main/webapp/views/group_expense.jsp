<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- Nạp file CSS cho trang và modal --%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/group.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modal.css">
<script>
    window.contextPath = '${pageContext.request.contextPath}';
</script>
<div class="group-expense-page">
    <div class="group-expense-container">
        <div class="group-expense-header">
            <h2>Chi Tiêu Nhóm</h2>
            <button class="btn-add-group">+ Tạo Nhóm Mới</button>
        </div>

        <p><strong>Tổng chi tiêu nhóm:</strong> <span id="groupTotal">0 VND</span></p>

        <div id="groupList" class="group-list">
            <%-- Nội dung sẽ được JS chèn vào đây --%>
        </div>
    </div>
</div>

<%--
  Nạp file JS chính với type="module".
  Trình duyệt sẽ tự động xử lý việc nạp các file được import bên trong.
--%>
<script type="module" src="${pageContext.request.contextPath}/assets/js/group_expense.js"></script>