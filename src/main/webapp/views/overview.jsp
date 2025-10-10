<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">

<div class="container overview-page">
    <h2>Tổng Quan</h2>
    <p>Chào mừng đến với <strong>BudgetBuddy</strong> - Ứng dụng quản lý chi tiêu cá nhân!</p>

    <div class="dashboard-container">
        <!-- Danh sách giao dịch gần đây -->
        <div class="recent-transactions">
            <h3>Các giao dịch</h3>
            <ul id="transaction-list">
                <!-- Danh sách giao dịch sẽ được thêm động -->
            </ul>
        </div>

        <!-- Phần tổng quan và biểu đồ -->
        <div class="overview-panel">
            <h3>Dòng tiền (Các giao dịch)</h3>
            <div class="pie-charts">
                <div class="chart-card">
                    <h4>Tháng trước</h4>
                    <canvas id="previousMonthChart"></canvas>
                </div>
                <div class="chart-card">
                    <h4>Tháng nay</h4>
                    <canvas id="currentMonthChart"></canvas>
                </div>
            </div>
            <div class="balance-section">
                <span>Số dư hiện tại:</span>
                <span id="current-balance"></span>
            </div>

            <!-- Các biểu đồ -->
            <section class="charts-grid">
                <div class="chart-card">
                    <h3>Biểu đồ Thu - Chi</h3>
                    <!-- Time Selector Mới -->
                    <div class="time-selector-container">
                        <select id="timeUnitIncomeExpense" class="customize">
                            <option value="week">Tuần</option>
                            <option value="month" selected>Tháng</option>
                            <option value="year">Năm</option>
                        </select>
                        <select id="specificPeriodIncomeExpense" class="customize specific-period-selector"></select>
                    </div>
                    <canvas id="incomeExpenseChart"></canvas>
                </div>
                <div class="chart-card">
                    <h3>Biểu đồ Phân Loại Chi Tiêu</h3>
                    <!-- Time Selector Mới -->
                    <div class="time-selector-container">
                        <select id="timeUnitCategory" class="customize">
                            <option value="week">Tuần</option>
                            <option value="month" selected>Tháng</option>
                            <option value="year">Năm</option>
                        </select>
                        <select id="specificPeriodCategory" class="customize specific-period-selector"></select>
                    </div>
                    <canvas id="categoryChart"></canvas>
                </div>
                <div class="chart-card wide">
                    <h3>Biến Động Số Dư</h3>
                    <!-- Time Selector Mới -->
                    <div class="time-selector-container">
                        <select id="timeUnitBalance" class="customize">
                            <option value="week">Tuần</option>
                            <option value="month" selected>Tháng</option>
                            <option value="year">Năm</option>
                        </select>
                        <select id="specificPeriodBalance" class="customize specific-period-selector"></select>
                    </div>
                    <canvas id="balanceChart"></canvas>
                </div>
            </section>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/overview.js" defer></script>
