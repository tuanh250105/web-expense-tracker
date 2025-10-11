<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>BudgetBuddy Layout</title>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">

    <!-- Asset khung: dùng contextPath cho chắc -->
    <link rel="stylesheet" href="<%=ctx%>/layout/layout.css">
</head>
<body>
<header>
    <div class="logo">
        <i class="fa-solid fa-wallet"></i>
        <span>BudgetBuddy</span>
    </div>

    <div class="banner-text">
        <span class="text-rotate">
            <span>💰 Quản lý chi tiêu thông minh</span>
            <span>📊 Theo dõi tài chính mọi lúc mọi nơi</span>
            <span>🚀 BudgetBuddy – Tương lai quản lý tài chính</span>
        </span>
    </div>

    <div class="header-actions">
        <i class="fa-regular fa-bell notification"><span class="badge">3</span></i>
        <button class="btn-login"><i class="fa-solid fa-right-to-bracket"></i> Login / Sign up</button>
        <label class="switch"><input type="checkbox" id="darkModeToggle"><span class="slider"></span></label>
        <span class="menu-toggle"><i class="fa-solid fa-bars"></i></span>
    </div>
</header>

<nav class="sidebar">
    <ul>
        <li class="active"><a href="#"><i class="fa-solid fa-chart-line"></i> <span>Overallview</span></a></li>
        <li><a href="#"><i class="fa-solid fa-receipt"></i> <span>Giao dịch</span></a></li>
        <li><a href="#"><i class="fa-solid fa-calendar-days"></i> <span>Giao dịch hằng tháng</span></a></li>
        <li><a href="#"><i class="fa-solid fa-building-columns"></i> <span>Tài khoản</span></a></li>
        <li><a href="#"><i class="fa-solid fa-wallet"></i> <span>Ngân sách</span></a></li>
        <li><a href="#"><i class="fa-solid fa-hand-holding-dollar"></i> <span>Quản lý nợ</span></a></li>
        <hr>
        <!-- 2 mục bạn phụ trách: link tới dispatcher pages -->
        <li><a href="<%=ctx%>/layout/layout.jsp?view=/views/analytics.jsp"><i class="fa-solid fa-chart-pie"></i> <span>Tổng hợp biểu đồ</span></a></li>
        <li><a href="#"><i class="fa-solid fa-calendar"></i> <span>Lịch</span></a></li>
        <li><a href="#"><i class="fa-solid fa-people-group"></i> <span>Chi tiêu nhóm</span></a></li>
        <li><a href="#"><i class="fa-solid fa-file-import"></i> <span>Import / Export file</span></a></li>
        <li><a href="#"><i class="fa-solid fa-list-ul"></i> <span>Thống kê Top Categories</span></a></li>
        <li><a href="#"><i class="fa-solid fa-folder-open"></i> <span>Quản lí danh mục</span></a></li>
        <hr>
        <li><a href="<%=ctx%>/layout/layout.jsp?view=/views/rewards.jsp"><i class="fa-solid fa-star"></i> <span>Thưởng thêm</span></a></li>
        <li><a href="#"><i class="fa-solid fa-calendar-check"></i> <span>Quản lý sự kiện tài chính</span></a></li>
        <hr>
        <li><a href="#"><i class="fa-solid fa-circle-question"></i> <span>Help - Hỏi đáp</span></a></li>
        <li><a href="#"><i class="fa-solid fa-message"></i> <span>Góp ý - Liên hệ</span></a></li>
    </ul>
</nav>

<main class="content">
    <h2>Trang nội dung</h2>
    <p>Đây là chỗ code phần chức năng.</p>
    <%
        String v = request.getParameter("view");
        if (v != null && v.startsWith("/views/") && v.endsWith(".jsp")) {
            request.setAttribute("view", v);
        }
    %>
    <jsp:include page="${view}" />
</main>

<script src="layout.js"></script>
</body>
</html>
