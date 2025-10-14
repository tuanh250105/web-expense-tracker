<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản Lý Tài Khoản - BudgetBuddy</title>
    
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
            max-width: 1400px;
            margin: 0 auto;
        }
        
        .header {
            background: white;
            padding: 30px;
            border-radius: 20px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
            margin-bottom: 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .header h1 {
            color: #667eea;
            font-size: 2rem;
            font-weight: 700;
        }
        
        .header h1 i {
            margin-right: 15px;
        }
        
        .btn-add {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            padding: 15px 30px;
            border-radius: 12px;
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .btn-add:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
        }
        
        /* Summary Cards */
        .summary-cards {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .summary-card {
            background: white;
            padding: 25px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
            display: flex;
            align-items: center;
            gap: 20px;
        }
        
        .summary-card .icon {
            font-size: 3rem;
            width: 70px;
            height: 70px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 15px;
        }
        
        .summary-card.total .icon {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        
        .summary-card.visible .icon {
            background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
            color: white;
        }
        
        .summary-card.hidden .icon {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
            color: white;
        }
        
        .summary-card .info h3 {
            color: #888;
            font-size: 0.9rem;
            font-weight: 500;
            margin-bottom: 5px;
        }
        
        .summary-card .info p {
            color: #333;
            font-size: 1.8rem;
            font-weight: 700;
        }
        
        /* Accounts Grid */
        .accounts-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
            gap: 25px;
            margin-bottom: 30px;
        }
        
        .account-card {
            background: white;
            border-radius: 20px;
            padding: 25px;
            box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
            transition: all 0.3s ease;
            position: relative;
            overflow: hidden;
        }
        
        .account-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 5px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }
        
        .account-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
        }
        
        .account-card.hidden {
            opacity: 0.6;
        }
        
        .account-card.hidden::before {
            background: linear-gradient(135deg, #ccc 0%, #999 100%);
        }
        
        .account-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 20px;
        }
        
        .account-name {
            font-size: 1.3rem;
            font-weight: 600;
            color: #333;
            flex: 1;
        }
        
        .account-actions {
            display: flex;
            gap: 8px;
        }
        
        .btn-icon {
            background: transparent;
            border: none;
            cursor: pointer;
            font-size: 1.2rem;
            color: #888;
            transition: all 0.3s ease;
            width: 35px;
            height: 35px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        
        .btn-icon:hover {
            background: #f0f0f0;
            color: #667eea;
        }
        
        .btn-icon.delete:hover {
            background: #ffe0e0;
            color: #f5576c;
        }
        
        .account-balance {
            font-size: 2rem;
            font-weight: 700;
            color: #667eea;
            margin-bottom: 15px;
        }
        
        .account-stats {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 15px;
            padding-top: 15px;
            border-top: 1px solid #eee;
        }
        
        .stat-item {
            text-align: center;
        }
        
        .stat-item .label {
            font-size: 0.75rem;
            color: #888;
            margin-bottom: 5px;
        }
        
        .stat-item .value {
            font-size: 1rem;
            font-weight: 600;
            color: #333;
        }
        
        .stat-item .value.income {
            color: #38ef7d;
        }
        
        .stat-item .value.expense {
            color: #f5576c;
        }
        
        /* Modal */
        .modal {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            z-index: 1000;
            align-items: center;
            justify-content: center;
        }
        
        .modal.show {
            display: flex;
        }
        
        .modal-content {
            background: white;
            border-radius: 20px;
            padding: 40px;
            max-width: 500px;
            width: 90%;
            max-height: 90vh;
            overflow-y: auto;
            animation: slideDown 0.3s ease;
        }
        
        @keyframes slideDown {
            from {
                opacity: 0;
                transform: translateY(-50px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        .modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 30px;
        }
        
        .modal-header h2 {
            color: #667eea;
            font-size: 1.5rem;
        }
        
        .btn-close {
            background: transparent;
            border: none;
            font-size: 1.5rem;
            cursor: pointer;
            color: #888;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #333;
            font-weight: 500;
        }
        
        .form-group input,
        .form-group select {
            width: 100%;
            padding: 12px 15px;
            border: 2px solid #eee;
            border-radius: 10px;
            font-size: 1rem;
            font-family: 'Poppins', sans-serif;
            transition: all 0.3s ease;
        }
        
        .form-group input:focus,
        .form-group select:focus {
            outline: none;
            border-color: #667eea;
        }
        
        .btn-submit {
            width: 100%;
            padding: 15px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 12px;
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
        }
        
        .btn-submit:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
        }
        
        .loading {
            text-align: center;
            padding: 50px;
            font-size: 1.2rem;
            color: white;
        }
        
        .error {
            background: #ffe0e0;
            color: #f5576c;
            padding: 15px;
            border-radius: 10px;
            margin-bottom: 20px;
        }
        
        .success {
            background: #e0ffe0;
            color: #38ef7d;
            padding: 15px;
            border-radius: 10px;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <h1><i class="fas fa-wallet"></i> Quản Lý Tài Khoản</h1>
            <button class="btn-add" onclick="openAddModal()">
                <i class="fas fa-plus"></i> Thêm Tài Khoản
            </button>
        </div>
        
        <!-- Summary Cards -->
        <div class="summary-cards">
            <div class="summary-card total">
                <div class="icon"><i class="fas fa-coins"></i></div>
                <div class="info">
                    <h3>Tổng Số Dư</h3>
                    <p id="total-balance">0₫</p>
                </div>
            </div>
            <div class="summary-card visible">
                <div class="icon"><i class="fas fa-eye"></i></div>
                <div class="info">
                    <h3>Tài Khoản Hiện</h3>
                    <p id="visible-count">0</p>
                </div>
            </div>
            <div class="summary-card hidden">
                <div class="icon"><i class="fas fa-eye-slash"></i></div>
                <div class="info">
                    <h3>Tài Khoản Ẩn</h3>
                    <p id="hidden-count">0</p>
                </div>
            </div>
        </div>
        
        <!-- Accounts Grid -->
        <div id="accounts-container" class="accounts-grid">
            <div class="loading">
                <i class="fas fa-spinner fa-spin"></i> Đang tải dữ liệu...
            </div>
        </div>
    </div>
    
    <!-- Add/Edit Account Modal -->
    <div id="account-modal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2 id="modal-title">Thêm Tài Khoản Mới</h2>
                <button class="btn-close" onclick="closeModal()"><i class="fas fa-times"></i></button>
            </div>
            
            <div id="modal-message"></div>
            
            <form id="account-form" onsubmit="return false;">
                <input type="hidden" id="account-id" name="accountId">
                
                <div class="form-group">
                    <label for="account-name">Tên Tài Khoản <span style="color: red;">*</span></label>
                    <input type="text" id="account-name" name="name" placeholder="VD: Ví Tiền Mặt, MBBank, VietcomBank..." required>
                </div>
                
                <div class="form-group">
                    <label for="account-balance">Số Dư Ban Đầu (₫)</label>
                    <input type="number" id="account-balance" name="balance" placeholder="0" value="0" step="1000">
                </div>
                
                <div class="form-group">
                    <label for="account-currency">Loại Tiền Tệ</label>
                    <select id="account-currency" name="currency">
                        <option value="VND" selected>VND (₫)</option>
                        <option value="USD">USD ($)</option>
                        <option value="EUR">EUR (€)</option>
                    </select>
                </div>
                
                <button type="button" class="btn-submit" onclick="saveAccount()">
                    <i class="fas fa-save"></i> Lưu Tài Khoản
                </button>
            </form>
        </div>
    </div>
    
    <script>
        var contextPath = '<%= request.getContextPath() %>';
        var apiBase = contextPath + '/api/accounts';
        var accounts = [];
        var hiddenAccounts = new Set(); // Store hidden account IDs
        
        // Load accounts on page load
        document.addEventListener('DOMContentLoaded', function() {
            loadAccounts();
        });
        
        // ==================== LOAD ACCOUNTS ====================
        
        function loadAccounts() {
            console.log('📊 Loading accounts...');
            
            fetch(apiBase + '/stats')
                .then(function(response) {
                    if (!response.ok) throw new Error('HTTP ' + response.status);
                    return response.json();
                })
                .then(function(data) {
                    console.log('✅ Accounts loaded:', data);
                    accounts = data;
                    displayAccounts();
                    updateSummary();
                })
                .catch(function(error) {
                    console.error('❌ Error loading accounts:', error);
                    document.getElementById('accounts-container').innerHTML = 
                        '<div class="error">Lỗi tải dữ liệu: ' + error.message + '</div>';
                });
        }
        
        // ==================== DISPLAY ACCOUNTS ====================
        
        function displayAccounts() {
            var container = document.getElementById('accounts-container');
            
            if (accounts.length === 0) {
                container.innerHTML = '<div style="color: white; text-align: center; padding: 50px; grid-column: 1/-1;">' +
                    '<i class="fas fa-inbox" style="font-size: 4rem; margin-bottom: 20px; opacity: 0.5;"></i>' +
                    '<h3>Chưa có tài khoản nào</h3>' +
                    '<p>Nhấn "Thêm Tài Khoản" để bắt đầu</p>' +
                    '</div>';
                return;
            }
            
            var html = '';
            accounts.forEach(function(account) {
                var id = account.id;
                var name = account.name;
                var balance = parseFloat(account.balance) || 0;
                var currency = account.currency || 'VND';
                var txCount = account[4] || 0;
                var income = parseFloat(account[5]) || 0;
                var expense = parseFloat(account[6]) || 0;
                
                var isHidden = hiddenAccounts.has(id);
                var hiddenClass = isHidden ? 'hidden' : '';
                var eyeIcon = isHidden ? 'fa-eye-slash' : 'fa-eye';
                
                html = html + '<div class="account-card ' + hiddenClass + '" data-id="' + id + '">';
                html = html + '  <div class="account-header">';
                html = html + '    <div class="account-name">' + name + '</div>';
                html = html + '    <div class="account-actions">';
                html = html + '      <button class="btn-icon" onclick="toggleVisibility(\'' + id + '\')" title="' + (isHidden ? 'Hiện' : 'Ẩn') + '">';
                html = html + '        <i class="fas ' + eyeIcon + '"></i>';
                html = html + '      </button>';
                html = html + '      <button class="btn-icon" onclick="openEditModal(\'' + id + '\')" title="Sửa">';
                html = html + '        <i class="fas fa-edit"></i>';
                html = html + '      </button>';
                html = html + '      <button class="btn-icon delete" onclick="deleteAccount(\'' + id + '\', \'' + name + '\')" title="Xóa">';
                html = html + '        <i class="fas fa-trash"></i>';
                html = html + '      </button>';
                html = html + '    </div>';
                html = html + '  </div>';
                html = html + '  <div class="account-balance">' + formatCurrency(balance, currency) + '</div>';
                html = html + '  <div class="account-stats">';
                html = html + '    <div class="stat-item">';
                html = html + '      <div class="label">Giao Dịch</div>';
                html = html + '      <div class="value">' + txCount + '</div>';
                html = html + '    </div>';
                html = html + '    <div class="stat-item">';
                html = html + '      <div class="label">Thu Nhập</div>';
                html = html + '      <div class="value income">+' + formatCurrency(income, currency) + '</div>';
                html = html + '    </div>';
                html = html + '    <div class="stat-item">';
                html = html + '      <div class="label">Chi Tiêu</div>';
                html = html + '      <div class="value expense">-' + formatCurrency(expense, currency) + '</div>';
                html = html + '    </div>';
                html = html + '  </div>';
                html = html + '  <button class="btn-submit" style="margin-top: 15px;" onclick="viewAccountDetails(\'' + id + '\')">';
                html = html + '    <i class="fas fa-list"></i> Xem Chi Tiết Giao Dịch';
                html = html + '  </button>';
                html = html + '</div>';
            });
            
            container.innerHTML = html;
        }
        
        // ==================== UPDATE SUMMARY ====================
        
        function updateSummary() {
            var totalBalance = 0;
            var visibleCount = 0;
            var hiddenCount = 0;
            
            accounts.forEach(function(account) {
                var id = account.id;
                var balance = parseFloat(account.balance) || 0;
                
                if (hiddenAccounts.has(id)) {
                    hiddenCount = hiddenCount + 1;
                } else {
                    visibleCount = visibleCount + 1;
                    totalBalance = totalBalance + balance;
                }
            });
            
            document.getElementById('total-balance').textContent = formatCurrency(totalBalance, 'VND');
            document.getElementById('visible-count').textContent = visibleCount;
            document.getElementById('hidden-count').textContent = hiddenCount;
        }
        
        // ==================== TOGGLE VISIBILITY ====================
        
        function toggleVisibility(accountId) {
            if (hiddenAccounts.has(accountId)) {
                hiddenAccounts.delete(accountId);
            } else {
                hiddenAccounts.add(accountId);
            }
            displayAccounts();
            updateSummary();
        }
        
        // ==================== MODAL FUNCTIONS ====================
        
        function openAddModal() {
            document.getElementById('modal-title').textContent = 'Thêm Tài Khoản Mới';
            document.getElementById('account-form').reset();
            document.getElementById('account-id').value = '';
            document.getElementById('modal-message').innerHTML = '';
            document.getElementById('account-modal').classList.add('show');
        }
        
        function openEditModal(accountId) {
            var account = accounts.find(function(a) { return a.id === accountId; });
            if (!account) return;
            
            document.getElementById('modal-title').textContent = 'Sửa Tài Khoản';
            document.getElementById('account-id').value = account.id;
            document.getElementById('account-name').value = account.name;
            document.getElementById('account-balance').value = account.balance;
            document.getElementById('account-currency').value = account.currency;
            document.getElementById('modal-message').innerHTML = '';
            document.getElementById('account-modal').classList.add('show');
        }
        
        function closeModal() {
            document.getElementById('account-modal').classList.remove('show');
        }
        
        // ==================== SAVE ACCOUNT ====================
        
        function saveAccount() {
            var accountId = document.getElementById('account-id').value;
            var name = document.getElementById('account-name').value.trim();
            var balance = parseFloat(document.getElementById('account-balance').value) || 0;
            var currency = document.getElementById('account-currency').value;
            
            if (!name) {
                showModalMessage('error', 'Vui lòng nhập tên tài khoản!');
                return;
            }
            
            var data = {
                name: name,
                balance: balance,
                currency: currency
            };
            
            var url = apiBase + (accountId ? '/' + accountId : '/');
            var method = accountId ? 'PUT' : 'POST';
            
            console.log('💾 Saving account:', method, url, data);
            
            fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            })
            .then(function(response) {
                console.log('📡 Response status:', response.status);
                
                // Parse JSON regardless of status
                return response.json().then(function(json) {
                    if (!response.ok) {
                        // Throw with server error message
                        throw new Error(json.error || 'HTTP ' + response.status);
                    }
                    return json;
                });
            })
            .then(function(result) {
                console.log('✅ Account saved:', result);
                showModalMessage('success', result.message || 'Lưu thành công!');
                setTimeout(function() {
                    closeModal();
                    loadAccounts();
                }, 1500);
            })
            .catch(function(error) {
                console.error('❌ Error saving account:', error);
                
                // Show detailed error message
                var errorMsg = error.message || 'Unknown error';
                console.error('📋 Error details:', errorMsg);
                
                showModalMessage('error', 'Lỗi: ' + errorMsg);
            });
        }
        
        // ==================== DELETE ACCOUNT ====================
        
        function deleteAccount(accountId, accountName) {
            if (!confirm('Bạn có chắc muốn xóa tài khoản "' + accountName + '"?\n\nCảnh báo: Tất cả giao dịch liên quan sẽ không còn liên kết với tài khoản này!')) {
                return;
            }
            
            console.log('🗑️ Deleting account:', accountId);
            
            fetch(apiBase + '/' + accountId, {
                method: 'DELETE'
            })
            .then(function(response) {
                if (!response.ok) throw new Error('HTTP ' + response.status);
                return response.json();
            })
            .then(function(result) {
                console.log('✅ Account deleted:', result);
                alert('Xóa tài khoản thành công!');
                loadAccounts();
            })
            .catch(function(error) {
                console.error('❌ Error deleting account:', error);
                alert('Lỗi xóa tài khoản: ' + error.message);
            });
        }
        
        // ==================== VIEW ACCOUNT DETAILS ====================
        
        function viewAccountDetails(accountId) {
            // Redirect to account transactions page
            window.location.href = contextPath + '/views/account-transactions.jsp?accountId=' + accountId;
        }
        
        // ==================== HELPER FUNCTIONS ====================
        
        function formatCurrency(amount, currency) {
            var formatted = Math.abs(amount).toLocaleString('vi-VN');
            if (currency === 'VND') return formatted + '₫';
            if (currency === 'USD') return '$' + formatted;
            if (currency === 'EUR') return '€' + formatted;
            return formatted;
        }
        
        function showModalMessage(type, message) {
            var msgDiv = document.getElementById('modal-message');
            msgDiv.className = type;
            msgDiv.textContent = message;
        }
    </script>
</body>
</html>
