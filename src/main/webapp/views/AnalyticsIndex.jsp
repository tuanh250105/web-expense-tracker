<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>BB – Tổng hợp biểu đồ</title>
    <link rel="stylesheet" href="../assets/analytics/analytics.css">
</head>
<body class="bb-analytics">
<div class="bb-analytics-toolbar">
    <label>Từ</label><input type="date" id="bb-from">
    <label>Đến</label><input type="date" id="bb-to">
    <label>Loại</label>
    <select id="bb-kind">
        <option value="all">All</option>
        <option value="income">Income</option>
        <option value="expense">Expense</option>
    </select>
    <label>Nhóm</label>
    <select id="bb-group">
        <option value="day">Ngày</option>
        <option value="week">Tuần</option>
        <option value="month" selected>Tháng</option>
        <option value="year">Năm</option>
    </select>
    <label>Biểu đồ</label>
    <select id="bb-chart-type">
        <option value="bar" selected>Cột</option>
        <option value="pie">Tròn</option>
    </select>
    <label>Màu</label><input type="color" id="bb-color" value="#4e79a7">
    <button id="bb-apply">Áp dụng</button>
    <button id="bb-export">Export PNG</button>
</div>

<div class="bb-analytics-canvas">
    <canvas id="bb-analytics-chart"></canvas>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4"></script>
<script src="../assets/analytics/analytics-mock.js"></script>
<script src="../assets/analytics/analytics.js"></script>
</body>
</html>
