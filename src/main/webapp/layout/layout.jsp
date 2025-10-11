<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--
  Created by IntelliJ IDEA.
  User: khodo
  Date: 13/9/25
  Time: 14:20
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>BudgetBuddy Layout</title>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">
    <!-- CSS chung -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/layout/layout.css">

    <!-- CSS riêng cho từng trang -->
    <c:if test="${not empty pageCss}">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/${pageCss}">
    </c:if>


</head>
<body>
<header>
    <div class="logo">
        <i class="fa-solid fa-wallet"></i>
        <span>BudgetBuddy</span>
    </div>

    <!-- Banner động -->
    <div class="banner-text">
        <span class="text-rotate">
        <span>💰 Quản lý chi tiêu thông minh</span>
        <span>📊 Theo dõi tài chính mọi lúc mọi nơi</span>
        <span>🚀 BudgetBuddy – Tương lai quản lý tài chính</span>
        </span>
    </div>

    <div class="header-actions">
        <i class="fa-regular fa-bell notification">
            <span class="badge">3</span>
        </i>
        <button class="btn-login"><i class="fa-solid fa-right-to-bracket"></i> Login / Sign up</button>
        <label class="switch">
            <input type="checkbox" id="darkModeToggle">
            <span class="slider"></span>
        </label>
        <span class="menu-toggle"><i class="fa-solid fa-bars"></i></span>
    </div>
</header>

<nav class="sidebar">
    <ul>
        <li class="active"><a href="#"><i class="fa-solid fa-chart-line"></i> <span>Overallview</span></a></li>
        <li><a href="${pageContext.request.contextPath}/transaction"><i class="fa-solid fa-receipt"></i> <span>Giao dịch</span></a></li>
        <li><a href="#"><i class="fa-solid fa-calendar-days"></i> <span>Giao dịch hằng tháng</span></a></li>
        <li><a href="#"><i class="fa-solid fa-building-columns"></i> <span>Tài khoản</span></a></li>
        <li><a href="#"><i class="fa-solid fa-wallet"></i> <span>Ngân sách</span></a></li>
        <li><a href="#"><i class="fa-solid fa-hand-holding-dollar"></i> <span>Quản lý nợ</span></a></li>
        <hr>
        <li><a href="#"><i class="fa-solid fa-chart-pie"></i> <span>Tổng hợp biểu đồ</span></a></li>
        <li><a href="#"><i class="fa-solid fa-calendar"></i> <span>Lịch</span></a></li>
        <li><a href="#"><i class="fa-solid fa-people-group"></i> <span>Chi tiêu nhóm</span></a></li>
        <li><a href="#"><i class="fa-solid fa-file-import"></i> <span>Import / Export file</span></a></li>
        <li><a href="#"><i class="fa-solid fa-list-ul"></i> <span>Thống kê Top Categories</span></a></li>
        <li><a href="#"><i class="fa-solid fa-folder-open"></i> <span>Quản lí danh mục</span></a></li>
        <hr>
        <li><a href="#"><i class="fa-solid fa-star"></i> <span>* Thưởng thêm</span></a></li>
        <li><a href="#"><i class="fa-solid fa-calendar-check"></i> <span>Quản lý sự kiện tài chính</span></a></li>
        <hr>
        <li><a href="#"><i class="fa-solid fa-circle-question"></i> <span>Help - Hỏi đáp</span></a></li>
        <li><a href="#"><i class="fa-solid fa-message"></i> <span>Góp ý - Liên hệ</span></a></li>
    </ul>
</nav>

<main class="content">
        <jsp:include page="${view}" />
</main>

<!-- JS chung -->
<script defer src="${pageContext.request.contextPath}/assets/js/layout.js"></script>

<!-- JS riêng cho từng trang -->
<c:if test="${not empty pageJs}">
    <script defer src="${pageContext.request.contextPath}/assets/js/${pageJs}"></script>
</c:if>
</body>
</html>
