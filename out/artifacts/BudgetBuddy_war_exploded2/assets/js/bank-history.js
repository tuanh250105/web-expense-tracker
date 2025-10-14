// Bank History JavaScript Functions
document.addEventListener('DOMContentLoaded', function() {
    let currentAccountId = null;
    let transactions = [];
    let categories = [];
    let accounts = [];

    // API endpoints
    const API_BASE = '/BudgetBuddy/api';

    // Initialize page
    initializeBankHistory();

    async function initializeBankHistory() {
        try {
            await loadAccounts();
            await loadCategories();
            
            if (currentAccountId) {
                await loadTransactions();
            }
            
            bindEventListeners();
            console.log('Bank History initialized');
        } catch (error) {
            console.error('Failed to initialize bank history:', error);
            showNotification('❌ Không thể tải dữ liệu', 'error');
        }
    }

function categorizeTransaction(transactionId, description, amount, category) {
    if (!category) {
        showNotification('⚠️ Vui lòng chọn danh mục!', 'warning');
        return;
    }
    
    // Get category name for display (using database IDs)
    const categoryNames = {
        '1': '💰 Lương',
        '2': '🎁 Thưởng',
        '3': '🍽️ Ăn uống',
        '4': '🚗 Di chuyển',
        '5': '🎮 Giải trí'
    };
    
    // Visual feedback - change dropdown color to show it's categorized
    const selectElement = document.getElementById(`category-${transactionId}`);
    if (selectElement) {
        selectElement.classList.add('categorized');
    }
    
    // Show success message
    showNotification(`✅ Đã phân loại "${description}" vào ${categoryNames[category]}`, 'success');
    
    // Log for development
    console.log('Categorizing transaction:', {
        id: transactionId,
        description: description,
        amount: amount,
        category: category,
        categoryName: categoryNames[category]
    });
    
    // TODO: In real app, this would send AJAX request to server
    // Example: 
    // fetch('/BudgetBuddy/bank-history', { 
    //     method: 'POST', 
    //     headers: {
    //         'Content-Type': 'application/json',
    //     },
    //     body: JSON.stringify({
    //         transactionId: transactionId,
    //         categoryId: category
    //     })
    // })
    // .then(response => response.json())
    // .then(data => {
    //     if (data.success) {
    //         showNotification('Phân loại thành công!', 'success');
    //     } else {
    //         showNotification('Có lỗi xảy ra khi phân loại', 'error');
    //     }
    // });
}

function syncBankData() {
    showNotification('🔄 Đang đồng bộ dữ liệu từ ngân hàng...', 'info');
    
    // TODO: Implement actual bank sync
    setTimeout(() => {
        showNotification('✅ Đã đồng bộ dữ liệu thành công!', 'success');
        // Refresh page or update data
        // location.reload();
    }, 2000);
}

function exportToExpenseTracker() {
    showNotification('📊 Đang xuất dữ liệu sang danh sách quản lý chi tiêu...', 'info');
    
    // TODO: Implement export functionality
    setTimeout(() => {
        showNotification('✅ Đã xuất dữ liệu thành công!', 'success');
    }, 1500);
}

function viewCategorizedItems() {
    showNotification('📋 Hiển thị danh sách các giao dịch đã được phân loại...', 'info');
    
    // TODO: Navigate to categorized items view or show modal
    // window.location.href = '/BudgetBuddy/categorized-transactions';
}

function showNotification(message, type) {
    // Remove existing notifications
    const existingNotification = document.querySelector('.notification');
    if (existingNotification) {
        existingNotification.remove();
    }
    
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.innerHTML = message;
    
    document.body.appendChild(notification);
    
    // Animate in
    setTimeout(() => {
        notification.classList.add('show');
    }, 100);
    
    // Auto remove
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 4000);
}

// Utility functions for future use
function formatCurrency(amount) {
    if (amount == null) return '₫0';
    
    const absAmount = Math.abs(amount);
    const formatted = absAmount.toLocaleString('vi-VN');
    
    return amount < 0 ? `-₫${formatted}` : `₫${formatted}`;
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
}

function formatTime(dateString) {
    const date = new Date(dateString);
    return date.toLocaleTimeString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
}

// Function to populate transaction table (for future use when data comes from server)
function populateTransactionTable(transactions) {
    const tbody = document.querySelector('.transaction-table tbody');
    if (!tbody) return;
    
    if (!transactions || transactions.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" class="empty-state">
                    <i class="fas fa-inbox"></i>
                    <div class="title">Chưa có giao dịch nào</div>
                    <div class="subtitle">Dữ liệu giao dịch sẽ hiển thị khi có dữ liệu từ ngân hàng</div>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = transactions.map(transaction => `
        <tr>
            <td>
                <div class="transaction-date">${formatDate(transaction.transactionDate)}</div>
                <div class="transaction-time">${formatTime(transaction.transactionDate)}</div>
            </td>
            <td>
                <div class="transaction-description">${transaction.description || transaction.note}</div>
                <div class="transaction-ref">Ref: ${transaction.reference || transaction.id}</div>
                ${transaction.note ? `<div class="transaction-note">${transaction.note}</div>` : ''}
            </td>
            <td class="text-right">
                <span class="transaction-amount ${transaction.amount < 0 ? 'negative' : 'positive'}">
                    ${formatCurrency(transaction.amount)}
                </span>
            </td>
            <td class="text-center">
                <select id="category-${transaction.id}" 
                        onchange="categorizeTransaction('${transaction.id}', '${transaction.description || transaction.note}', ${Math.abs(transaction.amount)}, this.value)"
                        class="category-select">
                    <option value="">-- Chọn loại --</option>
                    <optgroup label="Thu nhập">
                        <option value="1">💰 Lương</option>
                        <option value="2">🎁 Thưởng</option>
                    </optgroup>
                    <optgroup label="Chi tiêu">
                        <option value="3">🍽️ Ăn uống</option>
                        <option value="4">🚗 Di chuyển</option>
                        <option value="5">🎮 Giải trí</option>
                    </optgroup>
                </select>
            </td>
        </tr>
    `).join('');
}

// Function to update statistics (for future use)
function updateStatistics(stats) {
    const totalIncomeEl = document.querySelector('.stat-card .stat-icon.income + .stat-content p');
    const totalExpenseEl = document.querySelector('.stat-card .stat-icon.expense + .stat-content p');
    const currentBalanceEl = document.querySelector('.stat-card .stat-icon.balance + .stat-content p');
    
    if (totalIncomeEl && stats.totalIncome !== undefined) {
        totalIncomeEl.textContent = formatCurrency(stats.totalIncome);
    }
    
    if (totalExpenseEl && stats.totalExpense !== undefined) {
        totalExpenseEl.textContent = formatCurrency(stats.totalExpense);
    }
    
    if (currentBalanceEl && stats.currentBalance !== undefined) {
        currentBalanceEl.textContent = formatCurrency(stats.currentBalance);
    }
}});