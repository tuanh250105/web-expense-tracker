<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>BudgetBuddy Layout</title>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
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
        <i class="fa-regular fa-bell notification">
            <span class="badge">3</span>
        </i>
        <button class="btn-login">
            <i class="fa-solid fa-right-to-bracket"></i> Login / Sign up
        </button>
        <label class="switch">
            <input type="checkbox" id="darkModeToggle">
            <span class="slider"></span>
        </label>
        <span class="menu-toggle"><i class="fa-solid fa-bars"></i></span>
    </div>
</header>

<nav class="sidebar">
    <ul>
        <li>
            <a href="#" data-page="overview">📊 Overview</a>
        </li>
        <li>
            <a href="#" data-page="group_expense">👥 Chi tiêu nhóm</a>
        </li>
    </ul>
</nav>

<main class="content">
    <div id="content-area"></div>
</main>

<!-- Truyền contextPath sang JS -->
<script>
    window.contextPath = "${pageContext.request.contextPath}";
    console.log("contextPath in layout.jsp:", window.contextPath);
    if (!window.contextPath) {
        console.warn("⚠️ contextPath rỗng, có thể ảnh hưởng đến URL fetch");
    }
</script>

<!-- Tải Chart.js phiên bản UMD -->
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/layout.js?v=<%= System.currentTimeMillis() %>" defer></script>

</body>
</html>