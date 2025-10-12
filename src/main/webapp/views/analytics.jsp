<%@ page contentType="text/html; charset=UTF-8" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/analytics.css"/>

<div class="bb-ana">
    <div class="bb-ana-hero">
        <h1>📊 Tổng hợp biểu đồ</h1>
        <p class="bb-note">Chọn khoảng thời gian, loại, nhóm dữ liệu và kiểu biểu đồ</p>
    </div>

    <div class="bb-ana-toolbar" id="bb-appbar">
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

        <label>Ứng dụng</label>
        <select id="bb-app"></select>

        <label>Top</label>
        <input type="number" id="bb-top" value="5" min="1" max="20" style="width:90px">

        <label>Màu</label><input type="color" id="bb-color" value="#4e79a7">

        <button id="bb-apply" class="bb-btn">Áp dụng</button>
        <button id="bb-export" class="bb-btn bb-ghost">Export PNG</button>
    </div>

    <div class="bb-ana-stats">
        <div class="bb-stat"><div class="bb-note">Tổng thu</div><b id="bb-sum-in">0</b></div>
        <div class="bb-stat"><div class="bb-note">Tổng chi</div><b id="bb-sum-out">0</b></div>
        <div class="bb-stat"><div class="bb-note">Cân bằng</div><b id="bb-sum-bal">0</b></div>
    </div>

    <div class="bb-ana-canvas">
        <canvas id="bb-analytics-chart" height="360"></canvas>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4"></script>

<script src="${pageContext.request.contextPath}/assets/js/analytics.js"></script>

