<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
  <title>Admin Dashboard - BudgetBuddy</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;600&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
  <style>
    body {
      font-family: 'Poppins', sans-serif;
      background: linear-gradient(120deg, #6366f1 0%, #60a5fa 40%, #84addf 70%, #b6b6d4 100%);
      background-size: 300% 300%;
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      animation: gradientMove 12s ease-in-out infinite alternate;
    }
    @keyframes gradientMove {
      0% { background-position: 0% 50%; }
      50% { background-position: 100% 50%; }
      100% { background-position: 0% 50%; }
    }
    .admin-container {
      background: rgba(255,255,255,0.10);
      border-radius: 24px;
      box-shadow: 0 15px 50px 0 rgba(0, 0, 0, 0.45);
      padding: 40px 32px;
      width: 100%;
      max-width: 1200px;
      min-width: 350px;
      position: relative;
      overflow: visible;
      animation: fadeIn 1s;
      backdrop-filter: blur(12px);
      -webkit-backdrop-filter: blur(12px);
      border: 1px solid rgba(255,255,255,0.12);
    }
    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(40px); }
      to { opacity: 1; transform: none; }
    }
    .logo {
      text-align: center;
      margin-bottom: 18px;
    }
    .logo i {
      font-size: 3rem;
      background: linear-gradient(90deg, #6366f1 0%, #60a5fa 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      color: transparent;
      animation: bounce 1.2s infinite alternate;
    }
    @keyframes bounce {
      from { transform: translateY(0); }
      to { transform: translateY(-10px); }
    }
    h2 {
      text-align: center;
      font-weight: 700;
      color: #374151;
      margin-bottom: 12px;
      letter-spacing: 1px;
    }
    .stats {
      margin: 24px 0 16px 0;
      display: flex;
      flex-wrap: wrap;
      gap: 24px;
      justify-content: center;
    }
    .stat-box {
      background: rgba(255,255,255,0.18);
      border-radius: 16px;
      box-shadow: 0 4px 30px rgba(0, 0, 0, 0.10);
      padding: 18px 28px;
      min-width: 180px;
      text-align: center;
      color: #374151;
      font-size: 1.1rem;
      font-weight: 600;
      margin-bottom: 8px;
    }
    .stat-box .fa {
      font-size: 2rem;
      margin-bottom: 8px;
      color: #6366f1;
    }
    .analysis {
      margin-top: 24px;
    }
    .analysis h3 {
      color: #374151;
      font-size: 1.08rem;
      margin-bottom: 10px;
      font-weight: 600;
    }
    .analysis-table {
      width: 100%;
      border-collapse: collapse;
      background: rgba(255,255,255,0.08);
      border-radius: 12px;
      table-layout: auto;
      min-width: 600px;
      max-width: 100%;
      overflow-x: auto;
      display: table;
    }
    .user-list-scroll {
      max-height: 340px;
      overflow-y: auto;
      overflow-x: auto;
      margin-bottom: 16px;
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0,0,0,0.08);
      background: rgba(255,255,255,0.13);
      width: 100%;
      min-width: 600px;
      max-width: 100%;
      box-sizing: border-box;
    }
    .analysis-table th, .analysis-table td {
      padding: 10px 14px;
      border-bottom: 1px solid #e5e7eb;
      text-align: left;
      color: #374151;
    }
    .analysis-table th {
      background: #f3f4f6;
      font-weight: 700;
    }
    .analysis-table tr:last-child td {
      border-bottom: none;
    }
    @media (max-width: 700px) {
      .admin-container { padding: 18px 6px; }
      .stat-box { min-width: 120px; padding: 12px 8px; font-size: 1rem; }
    }
  </style>
</head>
<body>
<div class="admin-container">
  <div class="logo">
    <i class="fa-solid fa-user-shield"></i>
  </div>
  <h2>Admin Dashboard</h2>
  <div class="stats">
    <div class="stat-box">
      <i class="fa fa-users"></i>
      Tổng số người dùng<br>
      <span style="font-size:1.5rem;font-weight:700;">${totalUsers}</span>
    </div>
    <div class="stat-box">
      <i class="fa fa-user-plus"></i>
      Tài khoản mới hôm nay<br>
      <span style="font-size:1.5rem;font-weight:700;">${newUsersToday}</span>
    </div>
    <div class="stat-box">
      <i class="fa fa-chart-line"></i>
      Tài khoản mới tuần này<br>
      <span style="font-size:1.5rem;font-weight:700;">${newUsersWeek}</span>
    </div>
  </div>
  <div class="analysis">
    <h3>Phân tích tài khoản được tạo theo từng mốc thời gian</h3>
    <table class="analysis-table">
      <thead>
        <tr>
          <th>Mốc thời gian</th>
          <th>Số tài khoản tạo mới</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="stat" items="${userStatsByPeriod}">
          <tr>
            <td>${stat.period}</td>
            <td>${stat.count}</td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
    <h3 style="margin-top:32px;">Danh sách người dùng</h3>
    <div class="user-list-scroll">
      <table class="analysis-table">
        <thead>
          <tr>
            <th>Họ tên</th>
            <th>Email</th>
            <th>Username</th>
            <th>Quyền</th>
            <th>Ngày tạo</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="u" items="${userList}">
            <tr>
              <td>${u.fullName}</td>
              <td>${u.email}</td>
              <td>${u.username}</td>
              <td>${u.role}</td>
              <td><c:out value="${u.createdAt}"/></td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </div>
</div>
</body>
</html>
