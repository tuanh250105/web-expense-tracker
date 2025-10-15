<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch sử giao dịch ngân hàng - BudgetBuddy</title>

    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bank-history.css">
    <script>
        // API Configuration
        var API_BASE = '${pageContext.request.contextPath}/api';

        // ===== LẤY CATEGORIES TỪ SERVER-SIDE =====
        var serverCategories = [
            <c:forEach var="category" items="${categories}" varStatus="status">
            {
                id: '${category.id}',
                name: '${category.name}',
                type: '${category.type}',
                iconPath: '${category.iconPath}'
            }<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        ];

        console.log('📦 Categories from server:', serverCategories);
        var categoriesCache = serverCategories.length > 0 ? serverCategories : null;

        // Global variables
        var currentTransactions = [];
        var currentAccounts = [];
        var pendingBankTransactions = []; // GIAO DỊCH TỪ NGÂN HÀNG
        var currentPage = 1;
        var totalTransactions = 0;

        // Load data when page loads
        document.addEventListener('DOMContentLoaded', function() {
            console.log('🚀 Bank History page loaded');
            console.log('📍 API Base:', API_BASE);
            console.log('📂 Categories available:', categoriesCache ? categoriesCache.length : 0);
            loadCategories(); // LOAD CATEGORIES TRƯỚC
            loadAccounts();
            loadTransactions();
        });

        // Load accounts for filter dropdown
        async function loadAccounts() {
            try {
                console.log('🏦 Loading accounts...');
                var response = await fetch(API_BASE + '/bank-history/accounts');

                if (!response.ok) {
                    throw new Error('Failed to fetch accounts: ' + response.status);
                }

                var result = await response.json();

                if (result.success && result.data) {
                    currentAccounts = result.data;
                    console.log('✅ Accounts loaded:', currentAccounts.length);
                    populateAccountFilter();
                }

            } catch (error) {
                console.error('❌ Error loading accounts:', error);
                showErrorMessage('Không thể tải danh sách tài khoản');
            }
        }



        // Load categories
        async function loadCategories() {
            if (categoriesCache && categoriesCache.length > 0) {
                console.log('✅ Using categories from server:', categoriesCache.length);
                return categoriesCache;
            }

            try {
                console.log('⚠️ No server categories, fetching from API...');
                const response = await fetch(API_BASE + '/categories/');
                if (!response.ok) throw new Error('Failed to load categories');

                const result = await response.json();

                if (result.success && result.data) {
                    categoriesCache = result.data;
                    return result.data;
                } else {
                    return [];
                }
            } catch (error) {
                console.error('❌ Error loading categories:', error);
                return [];
            }
        }

        // Populate account filter dropdown
        function populateAccountFilter() {
            var select = document.getElementById('account-filter');
            if (!select) return;

            select.innerHTML = '<option value="">Tất cả tài khoản</option>';

            for (var i = 0; i < currentAccounts.length; i++) {
                var account = currentAccounts[i];
                var option = document.createElement('option');
                option.value = account.id;
                option.textContent = account.name + ' (' + formatCurrency(account.balance) + ')';
                select.appendChild(option);
            }
        }

        // Update quick stats - CHỈ TÍNH TỔNG TIỀN VÀO VÀ RA
        function updateQuickStats() {
            var totalIn = 0;
            var totalOut = 0;

            // Tính từ giao dịch thường
            for (var i = 0; i < currentTransactions.length; i++) {
                var transaction = currentTransactions[i];
                var amount = parseFloat(transaction.amount) || 0;

                if (amount > 0) {
                    totalIn += amount;
                } else {
                    totalOut += Math.abs(amount);
                }
            }

            // Tính từ giao dịch ngân hàng pending
            for (var j = 0; j < pendingBankTransactions.length; j++) {
                var bankTx = pendingBankTransactions[j];
                var bankAmount = parseFloat(bankTx.amount) || 0;

                if (bankAmount > 0) {
                    totalIn += bankAmount;
                } else {
                    totalOut += Math.abs(bankAmount);
                }
            }

            var totalInEl = document.querySelector('.total-in');
            var totalOutEl = document.querySelector('.total-out');

            if (totalInEl) totalInEl.textContent = formatCurrency(totalIn);
            if (totalOutEl) totalOutEl.textContent = formatCurrency(totalOut);
        }
        // Populate transaction table - HIỂN THỊ CẢ GIAO DỊCH BÌNh THƯỜNG VÀ TỪ NGÂN HÀNG
        function populateTransactionTable() {
            console.log('📊 Populating table...');
            const tbody = document.querySelector('.transactions-table tbody');

            if (!tbody) {
                console.error('❌ Table tbody not found!');
                return;
            }

            // KẾT HỢP cả giao dịch bình thường và giao dịch từ ngân hàng
            var allTransactions = [];

            // Thêm giao dịch bình thường
            for (var i = 0; i < currentTransactions.length; i++) {
                allTransactions.push({
                    source: 'normal',
                    data: currentTransactions[i]
                });
            }

            // Thêm giao dịch từ ngân hàng (chưa phân loại)
            for (var j = 0; j < pendingBankTransactions.length; j++) {
                allTransactions.push({
                    source: 'bank',
                    data: pendingBankTransactions[j]
                });
            }

            if (allTransactions.length === 0) {
                tbody.innerHTML =
                    '<tr>' +
                    '<td colspan="5" class="empty-state">' +
                    '<i class="fas fa-inbox"></i>' +
                    '<div class="title">Không có giao dịch</div>' +
                    '<div class="subtitle">Chưa có dữ liệu giao dịch nào</div>' +
                    '</td>' +
                    '</tr>';
                return;
            }

            tbody.innerHTML = '';

            for (var k = 0; k < allTransactions.length; k++) {
                var item = allTransactions[k];
                var row = document.createElement('tr');

                if (item.source === 'normal') {
                    // GIAO DỊCH BÌNH THƯỜNG (đã có trong DB)
                    var tx = item.data;
                    var amountClass = (tx.type === 'income') ? 'positive' : 'negative';

                    row.innerHTML =
                        '<td>' +
                        '<div class="transaction-time">' +
                        '<span class="date">' + formatDate(tx.transactionDate) + '</span>' +
                        '<span class="time">' + formatTime(tx.transactionDate) + '</span>' +
                        '</div>' +
                        '</td>' +
                        '<td>' +
                        '<div class="transaction-content">' +
                        '<span class="description">' + (tx.note || 'Không có mô tả') + '</span>' +
                        '<small class="account-name">(' + (tx.accountName || 'Unknown') + ')</small>' +
                        '</div>' +
                        '</td>' +
                        '<td class="text-right">' +
                        '<span class="amount ' + amountClass + '">' + formatCurrency(tx.amount) + '</span>' +
                        '</td>' +
                        '<td>' +
                        '<span class="current-category">' + (tx.categoryName || 'Chưa phân loại') + '</span>' +
                        '</td>' +
                        '<td class="text-center">' +
                        '<button onclick="editTransaction(\'' + tx.id + '\')" class="action-btn">' +
                        '<i class="fas fa-edit"></i>' +
                        '</button>' +
                        '</td>';

                } else {
                    // GIAO DỊCH TỪ NGÂN HÀNG (chưa lưu vào DB)
                    var bankTx = item.data;
                    var bankAmountClass = (bankTx.amount >= 0) ? 'positive' : 'negative';

                    row.className = 'bank-transaction-row'; // Class đặc biệt để CSS highlight
                    row.innerHTML =
                        '<td>' +
                        '<div class="transaction-time">' +
                        '<span class="date">' + formatDate(bankTx.transactionDate) + '</span>' +
                        '<span class="time">' + formatTime(bankTx.transactionDate) + '</span>' +
                        '<span class="bank-badge"><i class="fas fa-university"></i> Từ ngân hàng</span>' +
                        '</div>' +
                        '</td>' +
                        '<td>' +
                        '<div class="transaction-content">' +
                        '<span class="description">' + bankTx.content + '</span>' +
                        '<small class="account-name">(' + bankTx.bankName + ' - ' + bankTx.accountNumber + ')</small>' +
                        '</div>' +
                        '</td>' +
                        '<td class="text-right">' +
                        '<span class="amount ' + bankAmountClass + '">' + formatCurrency(bankTx.amount) + '</span>' +
                        '</td>' +
                        '<td>' +
                        '<select class="category-select" data-bank-tx-id="' + bankTx.id + '">' +
                        buildCategoryOptions() +
                        '</select>' +
                        '<input type="text" class="note-input" placeholder="Ghi chú (tùy chọn)" data-bank-tx-id="' + bankTx.id + '">' +
                        '</td>' +
                        '<td class="text-center">' +
                        '<button onclick="saveBankTransaction(\'' + bankTx.id + '\')" class="action-btn save-bank">' +
                        '<i class="fas fa-save"></i> Lưu' +
                        '</button>' +
                        '</td>';
                }

                tbody.appendChild(row);
            }

            console.log('✅ Table populated with ' + allTransactions.length + ' transactions');
        }

        // Build category options
        function buildCategoryOptions() {
            var options = '<option value="">Chọn danh mục</option>';

            if (categoriesCache && categoriesCache.length > 0) {
                for (var i = 0; i < categoriesCache.length; i++) {
                    var cat = categoriesCache[i];
                    options += '<option value="' + cat.id + '">' + cat.name + '</option>';
                }
            }

            return options;
        }

        // Update pagination
        function updatePagination() {
            const info = document.querySelector('.pagination-info');
            var total = currentTransactions.length + pendingBankTransactions.length;
            info.textContent = 'Hiển thị ' + total + ' giao dịch • Tổng: ' + total + ' giao dịch';
        }

        // === ĐỒNG BỘ NGÂN HÀNG - LOAD TRỰC TIẾP VÀO TABLE ===
        async function syncBankTransactions() {
            const syncBtn = document.getElementById('sync-bank-btn');
            const originalText = syncBtn.innerHTML;

            try {
                syncBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> <span>Đang đồng bộ...</span>';
                syncBtn.disabled = true;

                const response = await fetch(API_BASE + '/bank-sync?action=sync&days=30');

                if (!response.ok) {
                    throw new Error('Failed to sync: ' + response.status);
                }

                const result = await response.json();

                if (result.status === 'success') {
                    showSuccessMessage(result.message);

                    if (result.count > 0) {
                        // Load giao dịch pending và hiển thị trực tiếp trên table
                        await loadPendingBankTransactions();
                    } else {
                        showInfoMessage('Không có giao dịch mới');
                    }
                }

            } catch (error) {
                console.error('Sync error:', error);
                showErrorMessage('Lỗi đồng bộ: ' + error.message);
            } finally {
                syncBtn.innerHTML = originalText;
                syncBtn.disabled = false;
            }
        }

        // LOAD GIAO DỊCH TỪ NGÂN HÀNG VÀ HIỂN THỊ TRỰC TIẾP
        async function loadPendingBankTransactions() {
            try {
                const response = await fetch(API_BASE + '/bank-sync?action=pending');

                if (!response.ok) {
                    throw new Error('Failed to fetch pending: ' + response.status);
                }

                const result = await response.json();

                if (result.transactions && result.transactions.length > 0) {
                    pendingBankTransactions = result.transactions;
                    console.log('✅ Loaded ' + pendingBankTransactions.length + ' bank transactions');

                    // Hiển thị lại table với giao dịch từ ngân hàng
                    populateTransactionTable();
                    updatePagination();

                    showSuccessMessage('Đã tải ' + pendingBankTransactions.length + ' giao dịch từ ngân hàng!');
                } else {
                    showInfoMessage('Không có giao dịch nào đang chờ xử lý');
                }

            } catch (error) {
                console.error('Error loading bank transactions:', error);
                showErrorMessage('Lỗi tải giao dịch: ' + error.message);
            }
        }

        // LƯU GIAO DỊCH TỪ NGÂN HÀNG VÀO DATABASE
        async function saveBankTransaction(bankTxId) {
            var categorySelect = document.querySelector('.category-select[data-bank-tx-id="' + bankTxId + '"]');
            var noteInput = document.querySelector('.note-input[data-bank-tx-id="' + bankTxId + '"]');
            var saveBtn = event.target.closest('button');

            var categoryId = categorySelect.value;
            var customNote = noteInput.value;

            if (!categoryId) {
                showErrorMessage('Vui lòng chọn danh mục');
                return;
            }

            var originalText = saveBtn.innerHTML;


            saveBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
            saveBtn.disabled = true;

            var bankTx = pendingBankTransactions.find(function(t) {
                return t.id === bankTxId;
            });


            var accountId = '90949c6c-63ba-4ea5-a580-0b66df34d826';

            var transactionData = {
                accountId: accountId,
                categoryId: categoryId,
                amount: parseFloat(bankTx.amount),
                note: customNote || bankTx.content,
                transactionDate: bankTx.transactionDate,
                bankName: bankTx.bankName,
                traceCode: bankTx.id
            };

            var response = await fetch(API_BASE + '/bank-sync/save', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(transactionData)
            });

            if (!response.ok) {
                throw new Error('Failed to save: ' + response.status);
            }

            var result = await response.json();

            if (result.success) {
                showSuccessMessage('✅ Đã lưu giao dịch!');

                // Xóa giao dịch này khỏi pending list
                pendingBankTransactions = pendingBankTransactions.filter(function(t) {
                    return t.id !== bankTxId;
                });

                // Reload lại transactions
                await loadTransactions();
            } else {
                throw new Error(result.error || 'Save failed');
            }
        }

        // XEM GIAO DỊCH PENDING
        async function viewPendingTransactions() {
            await loadPendingBankTransactions();
        }

        // Edit transaction (existing)
        function editTransaction(transactionId) {
            alert('Chỉnh sửa giao dịch: ' + transactionId);
        }

        // Utility functions
        function formatCurrency(amount) {
            return new Intl.NumberFormat('vi-VN', {
                style: 'currency',
                currency: 'VND'
            }).format(amount);
        }

        function formatDate(dateString) {
            if (!dateString) return 'N/A';
            try {
                const date = new Date(dateString);
                if (isNaN(date.getTime())) return 'Invalid Date';
                return date.toLocaleDateString('vi-VN');
            } catch (e) {
                return 'Invalid Date';
            }
        }

        function formatTime(dateString) {
            if (!dateString) return '';
            try {
                const date = new Date(dateString);
                if (isNaN(date.getTime())) return '';
                return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
            } catch (e) {
                return '';
            }
        }

        function showErrorMessage(message) {
            const errorDiv = document.createElement('div');
            errorDiv.className = 'error-message';
            errorDiv.style.cssText =
                'position: fixed;' +
                'top: 20px;' +
                'right: 20px;' +
                'background: #ff4757;' +
                'color: white;' +
                'padding: 15px 20px;' +
                'border-radius: 8px;' +
                'z-index: 1000;' +
                'box-shadow: 0 4px 12px rgba(0,0,0,0.15);';

            errorDiv.innerHTML =
                '<i class="fas fa-exclamation-triangle"></i> ' +
                '<span>' + message + '</span>';

            document.body.appendChild(errorDiv);

            setTimeout(function() {
                errorDiv.remove();
            }, 5000);
        }

        function showSuccessMessage(message) {
            const notification = document.createElement('div');
            notification.className = 'notification success';
            notification.style.cssText =
                'position: fixed;' +
                'top: 20px;' +
                'right: 20px;' +
                'background: #2ecc71;' +
                'color: white;' +
                'padding: 15px 20px;' +
                'border-radius: 8px;' +
                'z-index: 1000;' +
                'box-shadow: 0 4px 12px rgba(0,0,0,0.15);';

            notification.innerHTML = '<i class="fas fa-check-circle"></i> ' + message;
            document.body.appendChild(notification);

            setTimeout(function() {
                notification.remove();
            }, 3000);
        }

        function showInfoMessage(message) {
            const notification = document.createElement('div');
            notification.className = 'notification info';
            notification.style.cssText =
                'position: fixed;' +
                'top: 20px;' +
                'right: 20px;' +
                'background: #3498db;' +
                'color: white;' +
                'padding: 15px 20px;' +
                'border-radius: 8px;' +
                'z-index: 1000;' +
                'box-shadow: 0 4px 12px rgba(0,0,0,0.15);';

            notification.innerHTML = '<i class="fas fa-info-circle"></i> ' + message;
            document.body.appendChild(notification);

            setTimeout(function() {
                notification.remove();
            }, 3000);
        }
    </script>
    <style>
        /* THÊM STYLE CHO GIAO DỊCH TỪ NGÂN HÀNG */
        .bank-transaction-row {
            background-color: #fff3cd !important;
            border-left: 4px solid #ffc107 !important;
        }

        .bank-badge {
            display: inline-block;
            background: #ffc107;
            color: #000;
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 11px;
            font-weight: 600;
            margin-left: 8px;
        }

        .category-select, .note-input {
            width: 100%;
            padding: 8px;
            margin: 4px 0;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 14px;
        }

        .save-bank {
            background: #28a745 !important;
            color: white !important;
            padding: 8px 16px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-weight: 600;
        }

        .save-bank:hover {
            background: #218838 !important;
        }

        .save-bank:disabled {
            background: #6c757d !important;
            cursor: not-allowed;
        }
    </style>
</head>
<body>

<div class="bank-history-content">
    <!-- Header Section -->
    <div class="bank-history-header">
        <div class="header-content">
            <div class="header-text">
                <h1>
                    <i class="fas fa-university"></i>
                    Lịch sử giao dịch ngân hàng
                </h1>
                <p>Xem chi tiết giao dịch và phân loại vào danh mục chi tiêu</p>
            </div>
            <div class="header-actions">
                <button id="sync-bank-btn" class="sync-button" onclick="syncBankTransactions()">
                    <i class="fas fa-sync-alt"></i>
                    <span>Đồng bộ ngân hàng</span>
                </button>
            </div>
        </div>
    </div>


    <!-- Transaction List -->
    <div class="transaction-list-container">
        <div class="transaction-header">
            <h3>
                <i class="fas fa-list-alt"></i> Danh sách giao dịch
            </h3>
            <div class="filter-controls">
                <p>✨ Giao dịch từ ngân hàng sẽ có màu vàng - chọn danh mục và nhấn Lưu</p>
            </div>
        </div>
        <div class="transaction-table-wrapper">
            <table class="transactions-table">
                <thead>
                <tr>
                    <th>Thời gian</th>
                    <th>Nội dung</th>
                    <th class="text-right">Số tiền</th>
                    <th>Danh mục</th>
                    <th class="text-center">Thao tác</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td colspan="5" class="empty-state">
                        <i class="fas fa-spinner fa-spin"></i>
                        <div class="title">Đang tải dữ liệu...</div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>

        <div class="pagination">
            <div class="pagination-info">
                Hiển thị 0 giao dịch • Tổng: 0 giao dịch
            </div>
        </div>
    </div>
</div>

</body>
</html>