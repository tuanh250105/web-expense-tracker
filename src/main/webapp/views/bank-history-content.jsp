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

        // Use server categories as cache
        var categoriesCache = serverCategories.length > 0 ? serverCategories : null;

        // Global variables
        var currentTransactions = currentTransactions || [];
        var currentAccounts = currentAccounts || [];
        var currentPage = currentPage || 1;
        var totalTransactions = totalTransactions || 0;

        // Load data when page loads
        document.addEventListener('DOMContentLoaded', function() {
            console.log('🚀 Bank History page loaded');
            console.log('📍 API Base:', API_BASE);
            console.log('📂 Categories available:', categoriesCache ? categoriesCache.length : 0);
            loadAccounts();
            loadTransactions();
        });

        // Load accounts for filter dropdown
        async function loadAccounts() {
            try {
                console.log('🏦 Loading accounts...');
                var url = API_BASE + '/bank-history/accounts';
                console.log('📡 Fetching:', url);

                var response = await fetch(url);
                console.log('📊 Response status:', response.status);

                if (!response.ok) {
                    throw new Error('Failed to fetch accounts: ' + response.status);
                }

                var result = await response.json();
                console.log('📦 Accounts result:', result);

                if (result.success && result.data) {
                    currentAccounts = result.data;
                    console.log('✅ Accounts loaded:', currentAccounts.length);
                    populateAccountFilter();
                } else {
                    throw new Error('Invalid response format');
                }

            } catch (error) {
                console.error('❌ Error loading accounts:', error);
                showErrorMessage('Không thể tải danh sách tài khoản');
            }
        }

        // Load transactions
        async function loadTransactions() {
            try {
                console.log('💳 Loading transactions...');
                var accountFilter = document.getElementById('account-filter').value;
                var url = API_BASE + '/bank-history/';

                if (accountFilter) {
                    url += '?accountId=' + encodeURIComponent(accountFilter);
                }

                console.log('📡 Fetching:', url);

                var response = await fetch(url);
                console.log('📊 Response status:', response.status, response.statusText);

                if (!response.ok) {
                    throw new Error('Failed to fetch transactions: ' + response.status);
                }

                var result = await response.json();
                console.log('📦 Transactions result:', result);

                if (result.success && result.data) {
                    currentTransactions = result.data;
                    totalTransactions = currentTransactions.length;

                    console.log('✅ Transactions loaded:', currentTransactions.length);

                    updateQuickStats();
                    populateTransactionTable();
                    updatePagination();
                } else {
                    throw new Error('Invalid response format');
                }

            } catch (error) {
                console.error('❌ Error loading transactions:', error);
                showErrorMessage('Không thể tải danh sách giao dịch');
            }
        }

        // Load categories - NOW USES SERVER DATA FIRST
        async function loadCategories() {
            // If we have categories from server, use them
            if (categoriesCache && categoriesCache.length > 0) {
                console.log('✅ Using categories from server:', categoriesCache.length);
                return categoriesCache;
            }

            // Otherwise, fetch from API as fallback
            try {
                console.log('⚠️ No server categories, fetching from API...');
                const response = await fetch(API_BASE + '/categories/');
                if (!response.ok) throw new Error('Failed to load categories');

                const result = await response.json();
                console.log('📦 Categories API response:', result);

                if (result.success && result.data) {
                    categoriesCache = result.data;
                    return result.data;
                } else {
                    console.error('❌ Invalid API response:', result);
                    return [];
                }
            } catch (error) {
                console.error('❌ Error loading categories:', error);
                return [];
            }
        }

        // Populate account filter dropdown
        function populateAccountFilter() {
            console.log('🏦 Populating account filter...');
            var select = document.getElementById('account-filter');
            if (!select) {
                console.error('❌ Account filter dropdown not found');
                return;
            }

            select.innerHTML = '<option value="">Tất cả tài khoản</option>';

            for (var i = 0; i < currentAccounts.length; i++) {
                var account = currentAccounts[i];
                var option = document.createElement('option');
                option.value = account.id;
                option.textContent = account.name + ' (' + formatCurrency(account.balance) + ')';
                select.appendChild(option);
            }

            console.log('✅ Account filter populated with', currentAccounts.length, 'accounts');
        }

        // Update quick stats
        function updateQuickStats() {
            console.log('📊 Updating quick stats...');
            var totalIn = 0;
            var totalOut = 0;

            for (var i = 0; i < currentTransactions.length; i++) {
                var transaction = currentTransactions[i];
                var type = transaction.type;
                var amount = parseFloat(transaction.amount) || 0;

                if (type === 'income') {
                    totalIn += amount;
                } else if (type === 'expense') {
                    totalOut += amount;
                }
            }

            var balance = totalIn - totalOut;

            var totalInEl = document.querySelector('.total-in');
            var totalOutEl = document.querySelector('.total-out');
            var balanceEl = document.querySelector('.balance');

            if (totalInEl) totalInEl.textContent = formatCurrency(totalIn);
            if (totalOutEl) totalOutEl.textContent = formatCurrency(totalOut);
            if (balanceEl) balanceEl.textContent = formatCurrency(balance);

            console.log('✅ Quick stats updated');
        }

        // Populate transaction table
        function populateTransactionTable() {
            console.log('📊 populateTransactionTable called, transactions:', currentTransactions.length);
            const tbody = document.querySelector('.transactions-table tbody');

            if (!tbody) {
                console.error('❌ Table tbody not found!');
                return;
            }

            if (currentTransactions.length === 0) {
                console.log('⚠️ No transactions to display');
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

            console.log('✅ Populating ' + currentTransactions.length + ' transactions');
            tbody.innerHTML = '';

            for (var i = 0; i < currentTransactions.length; i++) {
                var transaction = currentTransactions[i];
                var row = document.createElement('tr');

                var transactionId = transaction.id;
                var transactionType = transaction.type;
                var amount = parseFloat(transaction.amount) || 0;
                var note = transaction.note || 'Không có mô tả';
                var transactionDate = transaction.transactionDate;
                var categoryName = transaction.categoryName || 'Chưa phân loại';
                var accountName = transaction.accountName || 'Unknown Account';

                var amountClass = (transactionType === 'income') ? 'positive' : 'negative';

                row.innerHTML =
                    '<td>' +
                    '<div class="transaction-time">' +
                    '<span class="date">' + formatDate(transactionDate) + '</span>' +
                    '<span class="time">' + formatTime(transactionDate) + '</span>' +
                    '</div>' +
                    '</td>' +
                    '<td>' +
                    '<div class="transaction-content">' +
                    '<span class="description">' + note + '</span>' +
                    '<small class="account-name">(' + accountName + ')</small>' +
                    '</div>' +
                    '</td>' +
                    '<td class="text-right">' +
                    '<span class="amount ' + amountClass + '">' +
                    formatCurrency(amount) +
                    '</span>' +
                    '</td>' +
                    '<td>' +
                    '<span class="current-category">' + categoryName + '</span>' +
                    '<select class="category-select" onchange="updateTransactionCategory(\'' + transactionId + '\', this.value)">' +
                    buildCategoryOptions() +
                    '</select>' +
                    '</td>' +
                    '<td class="text-center">' +
                    '<button onclick="categorizeTransaction(\'' + transactionId + '\')" class="action-btn categorize">' +
                    '<i class="fas fa-tag"></i>' +
                    '</button>' +
                    '</td>';

                tbody.appendChild(row);
            }

            console.log('✅ Table populated successfully!');
        }

        // Build category options from loaded categories
        function buildCategoryOptions() {
            var options = '<option value="">Chọn danh mục</option>';

            if (categoriesCache && categoriesCache.length > 0) {
                for (var i = 0; i < categoriesCache.length; i++) {
                    var cat = categoriesCache[i];
                    options += '<option value="' + cat.id + '">' + cat.name + '</option>';
                }
            } else {
                // Fallback options if no categories loaded
                options +=
                    '<option value="food">Ăn uống</option>' +
                    '<option value="transport">Đi lại</option>' +
                    '<option value="shopping">Mua sắm</option>' +
                    '<option value="entertainment">Giải trí</option>' +
                    '<option value="utilities">Tiện ích</option>' +
                    '<option value="other">Khác</option>';
            }

            return options;
        }

        // Update pagination info
        function updatePagination() {
            const info = document.querySelector('.pagination-info');
            info.textContent = 'Hiển thị ' + currentTransactions.length + ' giao dịch • Tổng: ' + currentTransactions.length + ' giao dịch';
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
                '<i class="fas fa-exclamation-triangle"></i>' +
                '<span>' + message + '</span>';

            document.body.appendChild(errorDiv);

            setTimeout(function() {
                errorDiv.remove();
            }, 5000);
        }

        // Action functions
        function categorizeTransaction(transactionId) {
            alert('Phân loại giao dịch: ' + transactionId);
        }

        function updateTransactionCategory(transactionId, category) {
            console.log('Updating transaction', transactionId, 'to category', category);
            // API call to update category
        }

        // === SePay Bank Sync Functions ===

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
                        const pendingBtn = document.getElementById('view-pending-btn');
                        const pendingCount = document.getElementById('pending-count');
                        pendingCount.textContent = result.count;
                        pendingBtn.style.display = 'inline-block';
                    }
                } else {
                    throw new Error(result.message || 'Sync failed');
                }

            } catch (error) {
                console.error('Sync error:', error);
                showErrorMessage('Lỗi đồng bộ: ' + error.message);
            } finally {
                syncBtn.innerHTML = originalText;
                syncBtn.disabled = false;
            }
        }

        async function viewPendingTransactions() {
            try {
                const response = await fetch(API_BASE + '/bank-sync?action=pending');

                if (!response.ok) {
                    throw new Error('Failed to fetch pending: ' + response.status);
                }

                const result = await response.json();

                if (result.transactions && result.transactions.length > 0) {
                    showPendingTransactionsModal(result.transactions);
                } else {
                    showInfoMessage('Không có giao dịch nào đang chờ xử lý');
                }

            } catch (error) {
                console.error('Error fetching pending transactions:', error);
                showErrorMessage('Lỗi tải giao dịch chờ: ' + error.message);
            }
        }

        async function showPendingTransactionsModal(transactions) {
            window.pendingTransactionsData = transactions;

            const categories = await loadCategories();
            let categoryOptions = '<option value="">Chọn danh mục</option>';
            categories.forEach(function(cat) {
                categoryOptions += '<option value="' + cat.id + '">' + cat.name + '</option>';
            });

            let transactionItems = '';
            transactions.forEach(function(tx) {
                const amountClass = tx.amount >= 0 ? 'positive' : 'negative';
                transactionItems += '<div class="pending-transaction" data-reference="' + tx.id + '">' +
                    '<div class="tx-info">' +
                    '<div class="tx-amount ' + amountClass + '">' + formatCurrency(tx.amount) + '</div>' +
                    '<div class="tx-details">' +
                    '<div class="tx-content">' + tx.content + '</div>' +
                    '<div class="tx-date">' + formatDateTime(tx.transactionDate) + '</div>' +
                    '<div class="tx-bank">' + tx.bankName + ' - ' + tx.accountNumber + '</div>' +
                    '</div>' +
                    '</div>' +
                    '<div class="tx-actions">' +
                    '<select class="category-select">' +
                    categoryOptions +
                    '</select>' +
                    '<input type="text" class="note-input" placeholder="Ghi chú (tùy chọn)">' +
                    '<button onclick="saveTransaction(\'' + tx.id + '\')" class="save-btn">' +
                    '<i class="fas fa-save"></i> Lưu' +
                    '</button>' +
                    '</div>' +
                    '</div>';
            });

            const modalHtml = '<div id="pending-modal" class="modal-overlay">' +
                '<div class="modal-content">' +
                '<div class="modal-header">' +
                '<h3><i class="fas fa-university"></i> Giao dịch từ ngân hàng</h3>' +
                '<button onclick="closePendingModal()" class="close-btn">&times;</button>' +
                '</div>' +
                '<div class="modal-body">' +
                '<p>Đã tìm thấy ' + transactions.length + ' giao dịch từ SePay. Vui lòng chọn danh mục cho từng giao dịch:</p>' +
                '<div class="pending-transactions">' + transactionItems + '</div>' +
                '</div>' +
                '<div class="modal-footer">' +
                '<button onclick="closePendingModal()" class="cancel-btn">Đóng</button>' +
                '</div>' +
                '</div>' +
                '</div>';

            document.body.insertAdjacentHTML('beforeend', modalHtml);
        }

        function closePendingModal() {
            const modal = document.getElementById('pending-modal');
            if (modal) modal.remove();
        }

        async function saveTransaction(referenceNumber) {
            var selector = '[data-reference="' + referenceNumber + '"]';
            var txDiv = document.querySelector(selector);

            if (!txDiv) {
                showErrorMessage('Không tìm thấy giao dịch!');
                return;
            }

            var categorySelect = txDiv.querySelector('.category-select');
            var noteInput = txDiv.querySelector('.note-input');
            var saveBtn = txDiv.querySelector('.save-btn');

            var categoryId = categorySelect.value;
            var customNote = noteInput.value;

            if (!categoryId) {
                showErrorMessage('Vui lòng chọn danh mục');
                return;
            }

            var originalText = saveBtn.innerHTML;

            try {
                saveBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
                saveBtn.disabled = true;

                var txData = window.pendingTransactionsData.find(function(t) {
                    return t.id === referenceNumber;
                });

                if (!txData) {
                    throw new Error('Không tìm thấy dữ liệu giao dịch');
                }

                var accountId = '90949c6c-63ba-4ea5-a580-0b66df34d826';

                var transactionData = {
                    accountId: accountId,
                    categoryId: categoryId,
                    amount: parseFloat(txData.amount),
                    note: customNote || txData.content,
                    transactionDate: txData.transactionDate,
                    bankName: txData.bankName,
                    traceCode: txData.id
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
                    txDiv.style.opacity = '0.5';
                    saveBtn.innerHTML = '<i class="fas fa-check"></i> Đã lưu';
                    saveBtn.disabled = true;
                    showSuccessMessage('✅ Đã lưu giao dịch vào database!');

                    setTimeout(function() { loadTransactions(); }, 1500);
                } else {
                    throw new Error(result.error || 'Save failed');
                }

            } catch (error) {
                console.error('❌ Save error:', error);
                showErrorMessage('Lỗi lưu giao dịch: ' + error.message);
                saveBtn.innerHTML = originalText;
                saveBtn.disabled = false;
            }
        }

        function formatDateTime(dateTimeStr) {
            try {
                const date = new Date(dateTimeStr);
                return date.toLocaleString('vi-VN');
            } catch (e) {
                return dateTimeStr;
            }
        }

        function showSuccessMessage(message) {
            const notification = document.createElement('div');
            notification.className = 'notification success';
            notification.innerHTML = '<i class="fas fa-check-circle"></i> ' + message;
            document.body.appendChild(notification);

            setTimeout(function() {
                notification.remove();
            }, 3000);
        }

        function showInfoMessage(message) {
            const notification = document.createElement('div');
            notification.className = 'notification info';
            notification.innerHTML = '<i class="fas fa-info-circle"></i> ' + message;
            document.body.appendChild(notification);

            setTimeout(function() {
                notification.remove();
            }, 3000);
        }
    </script>
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
                <button id="view-pending-btn" class="pending-button" onclick="viewPendingTransactions()" style="display:none;">
                    <i class="fas fa-clock"></i>
                    <span>Xem giao dịch chờ (<span id="pending-count">0</span>)</span>
                </button>
            </div>
        </div>
    </div>

    <!-- Quick Stats -->
    <div class="quick-stats">
        <div class="stat-card">
            <div class="stat-icon income">
                <i class="fas fa-plus-circle"></i>
            </div>
            <div class="stat-content">
                <h3>Tổng tiền vào</h3>
                <p class="total-in">₫0</p>
            </div>
        </div>

        <div class="stat-card">
            <div class="stat-icon expense">
                <i class="fas fa-minus-circle"></i>
            </div>
            <div class="stat-content">
                <h3>Tổng tiền ra</h3>
                <p class="total-out">₫0</p>
            </div>
        </div>

        <div class="stat-card">
            <div class="stat-icon balance">
                <i class="fas fa-wallet"></i>
            </div>
            <div class="stat-content">
                <h3>Số dư hiện tại</h3>
                <p class="balance">₫0</p>
            </div>
        </div>
    </div>

    <!-- Transaction List -->
    <div class="transaction-list-container">
        <div class="transaction-header">
            <h3>
                <i class="fas fa-list-alt"></i> Danh sách giao dịch cần phân loại
            </h3>
            <div class="filter-controls">
                <select id="account-filter" onchange="loadTransactions()">
                    <option value="">Tất cả tài khoản</option>
                </select>
                <p>✨ Chọn dropdown để tự phân loại giao dịch vào danh mục mong muốn</p>
            </div>
        </div>

        <div class="transaction-table-wrapper">
            <table class="transactions-table">
                <thead>
                <tr>
                    <th>Thời gian</th>
                    <th>Nội dung chuyển khoản</th>
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
                        <div class="subtitle">Vui lòng chờ trong giây lát</div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>

        <div class="pagination">
            <div class="pagination-info">
                Hiển thị 0 giao dịch • Tổng: 0 giao dịch
            </div>
            <div class="pagination-buttons">
                <button class="pagination-btn" disabled>Trước</button>
                <button class="pagination-btn active">1</button>
                <button class="pagination-btn" disabled>Sau</button>
            </div>
        </div>
    </div>

    <!-- Quick Actions -->
    <div class="quick-actions">
        <h3>
            <i class="fas fa-magic"></i> Thao tác nhanh
        </h3>
        <div class="action-buttons">
            <button onclick="syncBankTransactions()" class="action-btn sync">
                <i class="fas fa-sync-alt"></i> Đồng bộ ngân hàng
            </button>
        </div>
    </div>
</div>

</body>
</html>
