<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/group.css">

<div class="group-expense-page">
    <div class="group-expense-container">
        <!-- Header -->
        <div class="group-expense-header">
            <h2>Chi Tiêu Nhóm</h2>
            <button class="btn-add-group">+ Tạo Nhóm Mới</button>
        </div>

        <!-- Tổng chi tiêu -->
        <p><strong>Tổng chi tiêu nhóm:</strong> <span id="groupTotal">0 VND</span></p>

        <!-- Danh sách nhóm (render bằng JS) -->
        <div id="groupList" class="group-list"></div>
    </div>
</div>