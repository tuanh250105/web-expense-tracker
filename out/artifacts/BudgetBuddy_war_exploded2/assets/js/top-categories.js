// Top Categories Page JavaScript - Tách riêng từ JSP

class TopCategoriesManager {
    constructor() {
        this.API_BASE = window.APP_CONFIG.API_BASE;
        this.CHART_COLORS = window.APP_CONFIG.CHART_COLORS;
        
        this.currentChart = null;
        this.categoriesData = [];
        this.filteredData = [];
        
        this.init();
    }

    async init() {
        try {
            this.showLoading(true);
            await this.loadCategoriesData();
            this.initializeCharts();
            this.populateTable();
            this.bindEventListeners();
            this.animateOnScroll();
            this.showNotification('✅ Dữ liệu đã được tải thành công', 'success');
        } catch (error) {
            console.error('Failed to initialize page:', error);
            this.showNotification('❌ Không thể tải dữ liệu ban đầu', 'error');
        } finally {
            this.showLoading(false);
        }
    }

    // === API DATA HANDLING ===

    async loadCategoriesData() {
        try {
            console.log('🌐 Loading categories data from:', `${this.API_BASE}/api/top-categories`);
            
            const response = await fetch(`${this.API_BASE}/api/top-categories`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const result = await response.json();
            
            if (result.success && Array.isArray(result.data)) {
                this.categoriesData = result.data;
                this.filteredData = [...this.categoriesData];
                console.log('✅ Categories data loaded successfully:', this.categoriesData);
                
                // Update summary cards
                this.updateSummaryCards();
            } else {
                throw new Error(result.message || "Invalid API response format");
            }
        } catch (error) {
            console.error('❌ Failed to load categories data:', error);
            this.showNotification('⚠️ API lỗi, sử dụng dữ liệu mẫu', 'info');
            this.categoriesData = this.getMockData();
            this.filteredData = [...this.categoriesData];
            this.updateSummaryCards();
        }
    }

    updateSummaryCards() {
        const expenseData = this.categoriesData.filter(item => item[1] === 'expense');
        const totalExpense = expenseData.reduce((sum, item) => sum + (parseFloat(item[4]) || 0), 0);
        const totalTransactions = this.categoriesData.reduce((sum, item) => sum + (parseInt(item[3]) || 0), 0);
        
        // Find top expense category
        const topExpenseCategory = expenseData.sort((a, b) => 
            (parseFloat(b[4]) || 0) - (parseFloat(a[4]) || 0)
        )[0];

        // Update DOM elements
        document.getElementById('totalExpense').textContent = this.formatCurrency(totalExpense);
        document.getElementById('totalTransactions').textContent = totalTransactions.toLocaleString();
        
        if (topExpenseCategory) {
            document.getElementById('topCategoryName').textContent = topExpenseCategory[0];
            document.getElementById('topCategoryAmount').textContent = this.formatCurrency(topExpenseCategory[4]);
        }
        
        // Calculate average daily expense (simple calculation)
        const avgDailyExpense = totalExpense / 30; // Assuming 30 days
        document.getElementById('avgDailyExpense').textContent = this.formatCurrency(avgDailyExpense);
    }

    // === CHART FUNCTIONS ===

    initializeCharts() {
        this.createTopCategoriesChart('pie');
        this.createTrendChart();
    }

    prepareChartData() {
        const expenseCategories = this.categoriesData
            .filter(item => item[1] === 'expense' && parseInt(item[3]) > 0)
            .sort((a, b) => (parseFloat(b[4]) || 0) - (parseFloat(a[4]) || 0))
            .slice(0, 6);

        const labels = expenseCategories.map(item => `${item[2] || '📊'} ${item[0]}`);
        const data = expenseCategories.map(item => parseFloat(item[4]) || 0);
        
        return {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: this.CHART_COLORS.slice(0, labels.length),
                borderColor: '#ffffff',
                borderWidth: 2
            }]
        };
    }

    createTopCategoriesChart(type) {
        const ctx = document.getElementById('topCategoriesChart')?.getContext('2d');
        if (!ctx) return;
        
        // Destroy previous chart
        if (this.currentChart) {
            this.currentChart.destroy();
        }

        const chartData = this.prepareChartData();
        
        this.currentChart = new Chart(ctx, {
            type: type,
            data: chartData,
            options: this.getChartOptions(type)
        });
    }

    createTrendChart() {
        const ctx = document.getElementById('trendChart')?.getContext('2d');
        if (!ctx) return;

        // Mock data for trend chart - replace with real API data
        const trendData = {
            labels: ['Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10'],
            datasets: [{
                label: 'Chi tiêu',
                data: [1200000, 1900000, 800000, 1600000, 2000000, 1500000],
                borderColor: '#FF6B6B',
                backgroundColor: 'rgba(255, 107, 107, 0.1)',
                fill: true,
                tension: 0.4
            }]
        };

        new Chart(ctx, {
            type: 'line',
            data: trendData,
            options: this.getChartOptions('line')
        });
    }

    getChartOptions(type) {
        const options = {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: (type === 'pie' || type === 'doughnut') ? 'right' : 'top',
                    labels: {
                        padding: 20,
                        usePointStyle: true,
                        font: { family: 'Poppins' }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: (context) => {
                            const value = context.parsed || context.raw || 0;
                            return `${context.label}: ${this.formatCurrency(value)}`;
                        }
                    }
                }
            }
        };

        if (type === 'bar' || type === 'line') {
            options.scales = {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: value => this.formatCurrency(value)
                    }
                }
            };
        }
        
        if (type === 'line') {
            options.plugins.legend.display = true;
        }

        return options;
    }

    // === TABLE FUNCTIONS ===

    populateTable() {
        const tableBody = document.getElementById('categoriesTableBody');
        if (!tableBody) return;

        const categoriesWithTransactions = this.filteredData.filter(item => parseInt(item[3]) > 0);
        
        if (categoriesWithTransactions.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="6" class="text-center p-4">Không có dữ liệu để hiển thị</td></tr>';
            return;
        }

        tableBody.innerHTML = '';
        const maxAmount = this.getMaxAmount();
        
        categoriesWithTransactions.forEach((item, index) => {
            const [name, type, icon, count, total] = item;
            const amount = parseFloat(total) || 0;
            const percentage = maxAmount > 0 ? ((amount / maxAmount) * 100).toFixed(1) : 0;

            const row = document.createElement('tr');
            row.innerHTML = `
                <td><strong>#${index + 1}</strong></td>
                <td>
                    <div class="category-info">
                        <span class="category-icon">${icon || '📊'}</span>
                        <span class="category-name">${name}</span>
                    </div>
                </td>
                <td>
                    <span class="category-type ${type}">
                        ${type === 'income' ? '💰 Thu nhập' : '💸 Chi tiêu'}
                    </span>
                </td>
                <td>${count} giao dịch</td>
                <td class="amount ${type}">${this.formatCurrency(amount)}</td>
                <td>
                    <div class="progress-bar">
                        <div class="progress-fill ${type}" style="width: ${Math.min(100, percentage)}%"></div>
                    </div>
                    <small style="color: #718096; font-size: 12px;">${percentage}%</small>
                </td>
            `;
            tableBody.appendChild(row);
        });
    }

    filterTable() {
        const searchTerm = document.getElementById('searchInput').value.toLowerCase();
        this.filteredData = this.categoriesData.filter(item => 
            item[0].toLowerCase().includes(searchTerm)
        );
        this.populateTable();
    }

    sortTable() {
        const sortValue = document.getElementById('sortSelect').value;
        
        this.filteredData.sort((a, b) => {
            switch (sortValue) {
                case 'amount-desc':
                    return (parseFloat(b[4]) || 0) - (parseFloat(a[4]) || 0);
                case 'amount-asc':
                    return (parseFloat(a[4]) || 0) - (parseFloat(b[4]) || 0);
                case 'count-desc':
                    return (parseInt(b[3]) || 0) - (parseInt(a[3]) || 0);
                case 'name-asc':
                    return a[0].localeCompare(b[0]);
                default:
                    return 0;
            }
        });
        
        this.populateTable();
    }

    // === EVENT HANDLERS ===

    bindEventListeners() {
        // Chart type buttons
        document.querySelectorAll('.chart-type-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const type = btn.dataset.type;
                document.querySelectorAll('.chart-type-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                this.createTopCategoriesChart(type);
            });
        });

        // Refresh button
        document.getElementById('refreshData')?.addEventListener('click', () => this.refreshData());
        
        // Export button
        document.getElementById('exportReport')?.addEventListener('click', () => this.exportReport());
        
        // Search input
        document.getElementById('searchInput')?.addEventListener('input', () => this.filterTable());
        
        // Sort select
        document.getElementById('sortSelect')?.addEventListener('change', () => this.sortTable());
        
        // Time filter
        document.getElementById('timeFilter')?.addEventListener('change', (e) => {
            this.showNotification(`Đã áp dụng bộ lọc: ${e.target.options[e.target.selectedIndex].text}`, 'info');
        });
    }

    async refreshData() {
        const refreshBtn = document.getElementById('refreshData');
        const originalText = refreshBtn.innerHTML;
        
        try {
            refreshBtn.disabled = true;
            refreshBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang tải...';
            
            await this.loadCategoriesData();
            this.initializeCharts();
            this.populateTable();
            this.showNotification('✅ Dữ liệu đã được cập nhật', 'success');
        } catch (error) {
            this.showNotification('❌ Không thể cập nhật dữ liệu', 'error');
        } finally {
            refreshBtn.disabled = false;
            refreshBtn.innerHTML = originalText;
        }
    }

    exportReport() {
        this.showNotification('📊 Tính năng xuất báo cáo đang được phát triển', 'info');
    }

    // === UTILITY FUNCTIONS ===

    getMaxAmount() {
        const amounts = this.categoriesData
            .filter(item => item[1] === 'expense')
            .map(item => parseFloat(item[4]) || 0);
        return Math.max(...amounts, 0);
    }

    formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount);
    }

    getMockData() {
        return [
            ['Ăn uống', 'expense', '🍔', '15', '2500000'],
            ['Giao thông', 'expense', '🚗', '8', '1200000'],
            ['Mua sắm', 'expense', '🛒', '12', '3200000'],
            ['Lương', 'income', '💰', '2', '15000000'],
            ['Giải trí', 'expense', '🎬', '5', '800000'],
            ['Hóa đơn', 'expense', '🏠', '4', '1500000']
        ];
    }

    // === UI UTILITIES ===

    showLoading(show) {
        const overlay = document.getElementById('loadingOverlay');
        if (overlay) {
            overlay.classList.toggle('show', show);
        }
    }

    showNotification(message, type = 'info') {
        const container = document.getElementById('notificationContainer');
        if (!container) return;

        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.textContent = message;
        
        container.appendChild(notification);
        
        setTimeout(() => {
            notification.remove();
        }, 5000);
    }

    animateOnScroll() {
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('visible');
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.1 });

        document.querySelectorAll('.chart-container, .table-container').forEach(el => {
            observer.observe(el);
        });
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Initializing Top Categories Manager...');
    window.topCategoriesManager = new TopCategoriesManager();
});