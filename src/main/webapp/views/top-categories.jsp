<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Top Categories - BudgetBuddy</title>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        
        body { 
            font-family: 'Poppins', Arial, sans-serif; 
            padding: 20px; 
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
        }
        
        .top-categories-container { 
            max-width: 1400px; 
            margin: 0 auto; 
        }
        
        .page-header {
            background: white;
            padding: 30px;
            border-radius: 12px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            margin-bottom: 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .page-header h1 {
            font-size: 28px;
            color: #2d3748;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .page-header .actions {
            display: flex;
            gap: 10px;
        }
        
        .status { 
            padding: 12px 20px; 
            margin: 10px 0; 
            border-radius: 8px; 
            font-weight: 500;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .status.loading { background: #fff3cd; color: #856404; }
        .status.success { background: #d4edda; color: #155724; }
        .status.error { background: #f8d7da; color: #721c24; }
        
        .summary-cards {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .summary-card {
            background: white;
            padding: 25px;
            border-radius: 12px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            display: flex;
            align-items: center;
            gap: 20px;
            transition: transform 0.3s ease, box-shadow 0.3s ease;
        }
        
        .summary-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 15px rgba(0,0,0,0.2);
        }
        
        .summary-card .icon {
            width: 60px;
            height: 60px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
        }
        
        .summary-card.card-1 .icon { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }
        .summary-card.card-2 .icon { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; }
        .summary-card.card-3 .icon { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); color: white; }
        .summary-card.card-4 .icon { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); color: white; }
        
        .summary-card .info h3 {
            font-size: 14px;
            color: #718096;
            font-weight: 500;
            margin-bottom: 8px;
        }
        
        .summary-card .info .value {
            font-size: 24px;
            font-weight: 700;
            color: #2d3748;
        }
        
        .summary-card .info .subtitle {
            font-size: 12px;
            color: #a0aec0;
            margin-top: 4px;
        }
        
        .main-content {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin-bottom: 20px;
        }
        
        .chart-section, .table-section {
            background: white;
            padding: 25px;
            border-radius: 12px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }
        
        .section-header {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 20px;
            padding-bottom: 15px;
            border-bottom: 2px solid #e2e8f0;
        }
        
        .section-header h2 {
            font-size: 18px;
            color: #2d3748;
            font-weight: 600;
        }
        
        .chart-wrapper {
            position: relative;
            height: 400px;
        }
        
        table { 
            width: 100%; 
            border-collapse: collapse; 
            margin-top: 10px; 
        }
        
        th, td { 
            padding: 15px 12px; 
            text-align: left; 
            border-bottom: 1px solid #e2e8f0; 
        }
        
        th { 
            background: #f7fafc;
            color: #4a5568; 
            font-weight: 600;
            font-size: 13px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
        tr:hover { 
            background: #f7fafc; 
        }
        
        .category-info {
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .category-icon {
            font-size: 20px;
        }
        
        .category-name {
            font-weight: 500;
            color: #2d3748;
        }
        
        .amount {
            font-weight: 600;
            color: #2d3748;
        }
        
        .transactions {
            color: #718096;
        }
        
        .btn { 
            padding: 10px 20px; 
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white; 
            border: none; 
            border-radius: 8px; 
            cursor: pointer;
            font-weight: 500;
            transition: all 0.3s ease;
            display: inline-flex;
            align-items: center;
            gap: 8px;
        }
        
        .btn:hover { 
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
        }
        
        .btn-secondary {
            background: white;
            color: #667eea;
            border: 2px solid #667eea;
        }
        
        .btn-secondary:hover {
            background: #f7fafc;
        }
        
        @media (max-width: 1024px) {
            .main-content {
                grid-template-columns: 1fr;
            }
        }
        
        @media (max-width: 768px) {
            .summary-cards {
                grid-template-columns: 1fr;
            }
            
            .page-header {
                flex-direction: column;
                gap: 15px;
            }
        }
    </style>
</head>
<body>
    <div class="top-categories-container">
        <!-- Page Header -->
        <div class="page-header">
            <h1>
                <i class="fas fa-chart-pie"></i>
                Top Categories - Chi tiết theo danh mục
            </h1>
            <div class="actions">
                <button class="btn btn-secondary" onclick="loadData()">
                    <i class="fas fa-sync-alt"></i> Tải lại
                </button>
                <button class="btn" onclick="exportReport()">
                    <i class="fas fa-download"></i> Xuất báo cáo
                </button>
            </div>
        </div>
        
        <div id="status" class="status loading">
            <i class="fas fa-spinner fa-spin"></i>
            <span>Đang tải dữ liệu...</span>
        </div>
        
        <!-- Summary Cards -->
        <div class="summary-cards">
            <div class="summary-card card-1">
                <div class="icon">
                    <i class="fas fa-wallet"></i>
                </div>
                <div class="info">
                    <h3>Tổng chi tiêu</h3>
                    <div class="value" id="totalSpent">₫0</div>
                    <div class="subtitle">Tất cả categories</div>
                </div>
            </div>
            
            <div class="summary-card card-2">
                <div class="icon">
                    <i class="fas fa-crown"></i>
                </div>
                <div class="info">
                    <h3>Top Category</h3>
                    <div class="value" id="topCategory">-</div>
                    <div class="subtitle" id="topCategoryAmount">₫0</div>
                </div>
            </div>
            
            <div class="summary-card card-3">
                <div class="icon">
                    <i class="fas fa-list"></i>
                </div>
                <div class="info">
                    <h3>Tổng categories</h3>
                    <div class="value" id="totalCategories">0</div>
                    <div class="subtitle">Đang theo dõi</div>
                </div>
            </div>
            
            <div class="summary-card card-4">
                <div class="icon">
                    <i class="fas fa-exchange-alt"></i>
                </div>
                <div class="info">
                    <h3>Tổng giao dịch</h3>
                    <div class="value" id="totalTransactions">0</div>
                    <div class="subtitle">Tất cả categories</div>
                </div>
            </div>
        </div>
        
        <!-- Main Content: Chart and Table -->
        <div class="main-content">
            <!-- Chart Section -->
            <div class="chart-section">
                <div class="section-header">
                    <i class="fas fa-chart-pie"></i>
                    <h2>Top 10 Danh mục chi tiêu</h2>
                </div>
                <div class="chart-wrapper">
                    <canvas id="categoriesChart"></canvas>
                </div>
            </div>
            
            <!-- Table Section -->
            <div class="table-section">
                <div class="section-header">
                    <i class="fas fa-table"></i>
                    <h2>Chi tiết theo danh mục</h2>
                </div>
                <table id="dataTable">
                    <thead>
                        <tr>
                            <th>Rank</th>
                            <th>Danh mục</th>
                            <th>Số tiền</th>
                            <th>% Tổng</th>
                            <th>Giao dịch</th>
                            <th>TB/Giao dịch</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td colspan="6" style="text-align: center; padding: 40px; color: #a0aec0;">
                                <i class="fas fa-spinner fa-spin" style="font-size: 24px; margin-bottom: 10px;"></i>
                                <div>Đang tải dữ liệu...</div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <script>
        var API_BASE = '${pageContext.request.contextPath}/api';
        var categoriesData = [];
        var categoriesChart = null;
        
        function updateStatus(message, type, icon) {
            var statusDiv = document.getElementById('status');
            var iconHtml = icon || '<i class="fas fa-spinner fa-spin"></i>';
            statusDiv.innerHTML = iconHtml + '<span>' + message + '</span>';
            statusDiv.className = 'status ' + type;
        }
        
        function formatCurrency(amount) {
            return new Intl.NumberFormat('vi-VN', {
                style: 'currency',
                currency: 'VND'
            }).format(amount);
        }
        
        function exportReport() {
            alert('Tính năng xuất báo cáo đang được phát triển!');
        }
        
        function getIconForCategory(categoryName) {
            if (!categoryName) return '📊';
            var name = categoryName.toLowerCase();
            
            // Map category names to icons
            var iconMap = {
                'food': '🍔', 'ăn uống': '🍔', 'an uong': '🍔',
                'transport': '🚗', 'giao thông': '🚗', 'di lai': '🚗',
                'shopping': '🛒', 'mua sắm': '🛒', 'mua sam': '🛒',
                'entertainment': '🎬', 'giải trí': '🎬', 'giai tri': '🎬',
                'salary': '💰', 'lương': '💰', 'luong': '💰',
                'utilities': '💡', 'hóa đơn': '💡', 'tien ich': '💡',
                'health': '🏥', 'y tế': '🏥', 'y te': '🏥',
                'education': '📚', 'giáo dục': '📚', 'giao duc': '📚',
                'bonus': '🎁', 'tiền thưởng': '🎁',
                'investment': '📈', 'đầu tư': '📈'
            };
            
            for (var key in iconMap) {
                if (name.includes(key)) {
                    return iconMap[key];
                }
            }
            
            return '📊';
        }
        
        async function loadData() {
            updateStatus('Đang tải dữ liệu từ Supabase...', 'loading', '<i class="fas fa-spinner fa-spin"></i>');
            
            try {
                var url = API_BASE + '/top-categories';
                console.log('🌐 Fetching from:', url);
                
                var response = await fetch(url);
                console.log('📡 Response status:', response.status, response.statusText);
                
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status + ': ' + response.statusText);
                }
                
                var result = await response.json();
                console.log('📦 Result:', result);
                console.log('📈 Data length:', result.data ? result.data.length : 0);
                
                if (result.success && result.data && result.data.length > 0) {
                    categoriesData = result.data;
                    console.log('✅ Categories loaded:', categoriesData.length);
                    
                    updateSummaryCards();
                    initializeChart();
                    displayTable();
                    
                    updateStatus('Đã tải ' + result.data.length + ' categories thành công!', 'success', '<i class="fas fa-check-circle"></i>');
                    
                    setTimeout(function() {
                        document.getElementById('status').style.display = 'none';
                    }, 3000);
                } else {
                    throw new Error('Không có dữ liệu');
                }
                
            } catch (error) {
                console.error('❌ Error:', error);
                updateStatus('Lỗi: ' + error.message, 'error', '<i class="fas fa-exclamation-triangle"></i>');
                
                var tbody = document.querySelector('#dataTable tbody');
                tbody.innerHTML = 
                    '<tr><td colspan="6" style="text-align:center; color:#f44336; padding:40px;">' +
                    '<i class="fas fa-exclamation-triangle" style="font-size: 32px; margin-bottom: 10px;"></i><br>' +
                    '<strong>Lỗi: ' + error.message + '</strong><br><br>' +
                    'API URL: ' + API_BASE + '/categories/stats' +
                    '</td></tr>';
            }
        }
        
        function updateSummaryCards() {
            console.log('📊 Updating summary cards...');
            
            if (!categoriesData || categoriesData.length === 0) {
                console.warn('⚠️ No data to update');
                return;
            }
            
            var totalAmount = 0;
            var totalTransactions = 0;
            
            for (var i = 0; i < categoriesData.length; i++) {
                totalAmount += parseFloat(categoriesData[i].totalAmount) || 0;
                totalTransactions += parseInt(categoriesData[i].transactionCount) || 0;
            }
            
            var topCategory = categoriesData[0];
            
            document.getElementById('totalSpent').textContent = formatCurrency(totalAmount);
            document.getElementById('topCategory').textContent = topCategory.categoryName || 'N/A';
            document.getElementById('topCategoryAmount').textContent = formatCurrency(topCategory.totalAmount || 0);
            document.getElementById('totalCategories').textContent = categoriesData.length;
            document.getElementById('totalTransactions').textContent = totalTransactions;
            
            console.log('✅ Summary cards updated');
        }
        
        function initializeChart() {
            console.log('📊 Initializing chart...');
            
            var ctx = document.getElementById('categoriesChart');
            if (!ctx) {
                console.error('❌ Chart canvas not found');
                return;
            }
            
            if (categoriesChart) {
                categoriesChart.destroy();
            }
            
            var chartData = categoriesData.slice(0, 10);
            var labels = [];
            var amounts = [];
            
            for (var i = 0; i < chartData.length; i++) {
                labels.push(chartData[i].categoryName || 'N/A');
                amounts.push(parseFloat(chartData[i].totalAmount) || 0);
            }
            
            categoriesChart = new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels: labels,
                    datasets: [{
                        data: amounts,
                        backgroundColor: [
                            '#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A',
                            '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E9',
                            '#F8C471', '#82E0AA'
                        ],
                        borderWidth: 3,
                        borderColor: '#fff'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: {
                                padding: 15,
                                usePointStyle: true,
                                font: { size: 12, family: 'Poppins' }
                            }
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    return context.label + ': ' + formatCurrency(context.parsed);
                                }
                            }
                        }
                    }
                }
            });
            
            console.log('✅ Chart initialized');
        }
        
        function displayTable() {
            console.log('📋 Displaying table...');
            
            var tbody = document.querySelector('#dataTable tbody');
            
            if (!categoriesData || categoriesData.length === 0) {
                tbody.innerHTML = 
                    '<tr><td colspan="6" style="text-align:center; padding:40px; color: #a0aec0;">' +
                    '<i class="fas fa-inbox" style="font-size: 48px; margin-bottom: 10px; display: block;"></i>' +
                    '<strong>Không có dữ liệu</strong><br>' +
                    '<small>Vui lòng thêm giao dịch hoặc tải lại trang</small>' +
                    '</td></tr>';
                return;
            }
            
            tbody.innerHTML = '';
            
            var totalAmount = 0;
            for (var i = 0; i < categoriesData.length; i++) {
                totalAmount += parseFloat(categoriesData[i].totalAmount) || 0;
            }
            
            categoriesData.forEach(function(cat, index) {
                // cat là Object: {categoryName, categoryType, totalAmount, transactionCount}
                var name = cat.categoryName || 'N/A';
                var type = cat.categoryType || 'N/A';
                var icon = getIconForCategory(name);
                var count = parseInt(cat.transactionCount) || 0;
                var amount = parseFloat(cat.totalAmount) || 0;
                
                var percentage = totalAmount > 0 ? ((amount / totalAmount) * 100).toFixed(1) : 0;
                var avgPerTransaction = count > 0 ? (amount / count) : 0;
                
                var row = document.createElement('tr');
                row.innerHTML = 
                    '<td><strong>#' + (index + 1) + '</strong></td>' +
                    '<td>' +
                        '<div class="category-info">' +
                            '<span class="category-icon">' + icon + '</span>' +
                            '<span class="category-name">' + name + '</span>' +
                        '</div>' +
                    '</td>' +
                    '<td class="amount">' + formatCurrency(amount) + '</td>' +
                    '<td>' + percentage + '%</td>' +
                    '<td class="transactions">' + count + '</td>' +
                    '<td>' + formatCurrency(avgPerTransaction) + '</td>';
                
                tbody.appendChild(row);
            });
            
            console.log('✅ Table populated');
        }
        
        // Auto-load on page ready
        window.addEventListener('DOMContentLoaded', function() {
            console.log('🚀 Top Categories page loaded');
            console.log('📍 API Base:', API_BASE);
            console.log('📊 Chart.js available:', typeof Chart !== 'undefined');
            
            setTimeout(function() {
                loadData();
            }, 500);
        });
    </script>
</body>
</html>