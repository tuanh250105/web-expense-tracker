<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi Tiết Giao Dịch - BudgetBuddy</title>
    
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Poppins', sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
        }
        
        .header {
            background: white;
            padding: 30px;
            border-radius: 20px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
            margin-bottom: 30px;
        }
        
        .header-top {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }
        
        .header h1 {
            color: #667eea;
            font-size: 2rem;
            font-weight: 700;
        }
        
        .btn-back {
            background: #eee;
            color: #333;
            border: none;
            padding: 12px 25px;
            border-radius: 10px;
            font-size: 1rem;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.3s ease;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        
        .btn-back:hover {
            background: #ddd;
        }
        
        .account-info {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            padding-top: 20px;
            border-top: 1px solid #eee;
        }
        
        .info-item {
            text-align: center;
        }
        
        .info-item .label {
            color: #888;
            font-size: 0.9rem;
            margin-bottom: 5px;
        }
        
        .info-item .value {
            color: #333;
            font-size: 1.5rem;
            font-weight: 700;
        }
        
        .info-item .value.balance {
            color: #667eea;
        }
        
        .info-item .value.income {
            color: #38ef7d;
        }
        
        .info-item .value.expense {
            color: #f5576c;
        }
        
        /* Transactions Table */
        .transactions-container {
            background: white;
            padding: 30px;
            border-radius: 20px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
        }
        
        .table-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }
        
        .table-header h2 {
            color: #333;
            font-size: 1.5rem;
        }
        
        .filter-group {
            display: flex;
            gap: 10px;
        }
        
        .filter-group select {
            padding: 10px 15px;
            border: 2px solid #eee;
            border-radius: 10px;
            font-family: 'Poppins', sans-serif;
            cursor: pointer;
        }
        
        .transactions-table {
            width: 100%;
            border-collapse: collapse;
        }
        
        .transactions-table thead {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        
        .transactions-table th {
            padding: 15px;
            text-align: left;
            font-weight: 600;
        }
        
        .transactions-table th:first-child {
            border-radius: 10px 0 0 10px;
        }
        
        .transactions-table th:last-child {
            border-radius: 0 10px 10px 0;
        }
        
        .transactions-table tbody tr {
            border-bottom: 1px solid #eee;
            transition: all 0.3s ease;
        }
        
        .transactions-table tbody tr:hover {
            background: #f9f9f9;
        }
        
        .transactions-table td {
            padding: 15px;
        }
        
        .transaction-type {
            display: inline-block;
            padding: 5px 15px;
            border-radius: 20px;
            font-size: 0.85rem;
            font-weight: 600;
        }
        
        .transaction-type.income {
            background: #e0ffe0;
            color: #38ef7d;
        }
        
        .transaction-type.expense {
            background: #ffe0e0;
            color: #f5576c;
        }
        
        .amount {
            font-weight: 700;
            font-size: 1.1rem;
        }
        
        .amount.income {
            color: #38ef7d;
        }
        
        .amount.expense {
            color: #f5576c;
        }
        
        .category-badge {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 5px 12px;
            background: #f0f0f0;
            border-radius: 20px;
            font-size: 0.9rem;
        }
        
        .loading {
            text-align: center;
            padding: 50px;
            color: #888;
        }
        
        .empty {
            text-align: center;
            padding: 50px;
            color: #888;
        }
        
        .empty i {
            font-size: 4rem;
            margin-bottom: 20px;
            opacity: 0.5;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <div class="header-top">
                <h1 id="account-name"><i class="fas fa-wallet"></i> Đang tải...</h1>
                <button class="btn-back" onclick="goBack()">
                    <i class="fas fa-arrow-left"></i> Quay Lại
                </button>
            </div>
            
            <div class="account-info">
                <div class="info-item">
                    <div class="label">Số Dư Hiện Tại</div>
                    <div class="value balance" id="account-balance">0₫</div>
                </div>
                <div class="info-item">
                    <div class="label">Tổng Thu Nhập</div>
                    <div class="value income" id="total-income">0₫</div>
                </div>
                <div class="info-item">
                    <div class="label">Tổng Chi Tiêu</div>
                    <div class="value expense" id="total-expense">0₫</div>
                </div>
                <div class="info-item">
                    <div class="label">Số Giao Dịch</div>
                    <div class="value" id="transaction-count">0</div>
                </div>
            </div>
        </div>
        
        <!-- Transactions Table -->
        <div class="transactions-container">
            <div class="table-header">
                <h2><i class="fas fa-list"></i> Danh Sách Giao Dịch</h2>
                <div class="filter-group">
                    <select id="filter-type" onchange="filterTransactions()">
                        <option value="all">Tất cả loại</option>
                        <option value="income">Thu nhập</option>
                        <option value="expense">Chi tiêu</option>
                    </select>
                </div>
            </div>
            
            <div id="transactions-list">
                <div class="loading">
                    <i class="fas fa-spinner fa-spin"></i><br>
                    Đang tải giao dịch...
                </div>
            </div>
        </div>
    </div>
    
    <script>
        var contextPath = '<%= request.getContextPath() %>';
        var accountId = new URLSearchParams(window.location.search).get('accountId');
        var transactions = [];
        var accountInfo = null;
        
        // Load data on page load
        document.addEventListener('DOMContentLoaded', function() {
            if (!accountId) {
                alert('Không tìm thấy ID tài khoản!');
                goBack();
                return;
            }
            loadAccountInfo();
            loadTransactions();
        });
        
        // ==================== LOAD ACCOUNT INFO ====================
        
        function loadAccountInfo() {
            fetch(contextPath + '/api/accounts/' + accountId)
                .then(function(response) {
                    if (!response.ok) throw new Error('HTTP ' + response.status);
                    return response.json();
                })
                .then(function(data) {
                    accountInfo = data;
                    displayAccountInfo();
                })
                .catch(function(error) {
                    console.error('Error loading account:', error);
                });
        }
        
        function displayAccountInfo() {
            if (!accountInfo) return;
            
            document.getElementById('account-name').innerHTML = 
                '<i class="fas fa-wallet"></i> ' + accountInfo.name;
            document.getElementById('account-balance').textContent = 
                formatCurrency(accountInfo.balance || 0);
        }
        
        // ==================== LOAD TRANSACTIONS ====================
        
        function loadTransactions() {
            console.log('Loading transactions for account:', accountId);
            
            fetch(contextPath + '/api/accounts/' + accountId + '/transactions')
                .then(function(response) {
                    if (!response.ok) throw new Error('HTTP ' + response.status);
                    return response.json();
                })
                .then(function(data) {
                    console.log('Transactions loaded:', data);
                    transactions = data;
                    displayTransactions();
                    updateStats();
                })
                .catch(function(error) {
                    console.error('Error loading transactions:', error);
                    document.getElementById('transactions-list').innerHTML = 
                        '<div class="empty"><i class="fas fa-exclamation-triangle"></i><h3>Lỗi tải dữ liệu</h3><p>' + error.message + '</p></div>';
                });
        }
        
        // ==================== DISPLAY TRANSACTIONS ====================
        
        function displayTransactions() {
            var filterType = document.getElementById('filter-type').value;
            var filtered = transactions;
            
            if (filterType !== 'all') {
                filtered = transactions.filter(function(tx) {
                    return tx[5] === filterType;
                });
            }
            
            var container = document.getElementById('transactions-list');
            
            if (filtered.length === 0) {
                container.innerHTML = '<div class="empty">' +
                    '<i class="fas fa-inbox"></i>' +
                    '<h3>Chưa có giao dịch nào</h3>' +
                    '<p>Tài khoản này chưa có giao dịch</p>' +
                    '</div>';
                return;
            }
            
            var html = '<table class="transactions-table">';
            html = html + '<thead>';
            html = html + '<tr>';
            html = html + '<th>Ngày</th>';
            html = html + '<th>Danh Mục</th>';
            html = html + '<th>Ghi Chú</th>';
            html = html + '<th>Loại</th>';
            html = html + '<th>Số Tiền</th>';
            html = html + '</tr>';
            html = html + '</thead>';
            html = html + '<tbody>';
            
            filtered.forEach(function(tx) {
                var txId = tx[0];
                var categoryName = tx[3] || 'Không rõ';
                var amount = parseFloat(tx[4]) || 0;
                var type = tx[5] || 'expense';
                var note = tx[6] || '-';
                var date = tx[7] || '';
                
                var typeText = type === 'income' ? 'Thu nhập' : 'Chi tiêu';
                var amountClass = type === 'income' ? 'income' : 'expense';
                var amountPrefix = type === 'income' ? '+' : '-';
                
                html = html + '<tr>';
                html = html + '<td>' + formatDate(date) + '</td>';
                html = html + '<td><span class="category-badge">' + categoryName + '</span></td>';
                html = html + '<td>' + note + '</td>';
                html = html + '<td><span class="transaction-type ' + type + '">' + typeText + '</span></td>';
                html = html + '<td><span class="amount ' + amountClass + '">' + amountPrefix + formatCurrency(amount) + '</span></td>';
                html = html + '</tr>';
            });
            
            html = html + '</tbody>';
            html = html + '</table>';
            
            container.innerHTML = html;
        }
        
        // ==================== UPDATE STATS ====================
        
        function updateStats() {
            var totalIncome = 0;
            var totalExpense = 0;
            
            transactions.forEach(function(tx) {
                var amount = parseFloat(tx[4]) || 0;
                var type = tx[5];
                
                if (type === 'income') {
                    totalIncome = totalIncome + amount;
                } else {
                    totalExpense = totalExpense + amount;
                }
            });
            
            document.getElementById('total-income').textContent = '+' + formatCurrency(totalIncome);
            document.getElementById('total-expense').textContent = '-' + formatCurrency(totalExpense);
            document.getElementById('transaction-count').textContent = transactions.length;
        }
        
        // ==================== FILTER TRANSACTIONS ====================
        
        function filterTransactions() {
            displayTransactions();
        }
        
        // ==================== HELPER FUNCTIONS ====================
        
        function formatCurrency(amount) {
            return Math.abs(amount).toLocaleString('vi-VN') + '₫';
        }
        
        function formatDate(dateStr) {
            if (!dateStr) return '-';
            var date = new Date(dateStr);
            var day = date.getDate().toString().padStart(2, '0');
            var month = (date.getMonth() + 1).toString().padStart(2, '0');
            var year = date.getFullYear();
            var hours = date.getHours().toString().padStart(2, '0');
            var minutes = date.getMinutes().toString().padStart(2, '0');
            return day + '/' + month + '/' + year + ' ' + hours + ':' + minutes;
        }
        
        function goBack() {
            window.location.href = contextPath + '/views/accounts-management.jsp';
        }
    </script>
</body>
</html>
