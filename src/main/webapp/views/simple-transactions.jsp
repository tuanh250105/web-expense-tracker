<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Simple Transaction Test</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { 
            font-family: Arial, sans-serif; 
            padding: 20px; 
            background: #f5f5f5; 
        }
        .container { 
            max-width: 1200px; 
            margin: 0 auto; 
            background: white; 
            padding: 20px; 
            border-radius: 8px; 
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        h1 { 
            color: #333; 
            margin-bottom: 20px; 
            padding-bottom: 10px; 
            border-bottom: 2px solid #4CAF50;
        }
        .status { 
            padding: 10px; 
            margin: 10px 0; 
            border-radius: 4px; 
            font-weight: bold;
        }
        .status.loading { background: #fff3cd; color: #856404; }
        .status.success { background: #d4edda; color: #155724; }
        .status.error { background: #f8d7da; color: #721c24; }
        table { 
            width: 100%; 
            border-collapse: collapse; 
            margin-top: 20px; 
        }
        th, td { 
            padding: 12px; 
            text-align: left; 
            border-bottom: 1px solid #ddd; 
        }
        th { 
            background: #4CAF50; 
            color: white; 
            font-weight: bold;
        }
        tr:hover { background: #f5f5f5; }
        .amount-positive { color: #4CAF50; font-weight: bold; }
        .amount-negative { color: #f44336; font-weight: bold; }
        .btn { 
            padding: 10px 20px; 
            background: #4CAF50; 
            color: white; 
            border: none; 
            border-radius: 4px; 
            cursor: pointer; 
            margin: 5px;
        }
        .btn:hover { background: #45a049; }
        .debug { 
            background: #f0f0f0; 
            padding: 15px; 
            margin: 10px 0; 
            border-left: 4px solid #2196F3; 
            font-family: monospace; 
            font-size: 12px;
            overflow-x: auto;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🧪 Simple Transaction Data Test</h1>
        
        <div id="status" class="status loading">⏳ Đang tải dữ liệu...</div>
        
        <div>
            <button class="btn" onclick="loadData()">🔄 Reload Data</button>
            <button class="btn" onclick="showDebugInfo()">🔍 Show Debug Info</button>
        </div>
        
        <div id="debug" class="debug" style="display:none;"></div>
        
        <table id="dataTable">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Type</th>
                    <th>Amount</th>
                    <th>Note</th>
                    <th>Date</th>
                    <th>Category</th>
                    <th>Account</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td colspan="7" style="text-align: center; padding: 40px;">
                        ⏳ Loading...
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <script>
        var API_BASE = '${pageContext.request.contextPath}/api';
        var debugInfo = {
            contextPath: '${pageContext.request.contextPath}',
            apiBase: '',
            timestamp: new Date().toISOString(),
            requests: []
        };
        
        debugInfo.apiBase = API_BASE;
        
        function updateStatus(message, type) {
            var statusDiv = document.getElementById('status');
            statusDiv.textContent = message;
            statusDiv.className = 'status ' + type;
        }
        
        function showDebugInfo() {
            var debugDiv = document.getElementById('debug');
            debugDiv.style.display = debugDiv.style.display === 'none' ? 'block' : 'none';
            debugDiv.innerHTML = '<strong>Debug Information:</strong><br>' + 
                                 JSON.stringify(debugInfo, null, 2);
        }
        
        function formatCurrency(amount) {
            return new Intl.NumberFormat('vi-VN', {
                style: 'currency',
                currency: 'VND'
            }).format(amount);
        }
        
        function formatDate(dateString) {
            if (!dateString) return 'N/A';
            var date = new Date(dateString);
            return date.toLocaleString('vi-VN');
        }
        
        async function loadData() {
            updateStatus('⏳ Đang tải dữ liệu từ Supabase...', 'loading');
            
            try {
                var url = API_BASE + '/bank-history/';
                console.log('🌐 Fetching from:', url);
                
                var requestInfo = {
                    url: url,
                    timestamp: new Date().toISOString(),
                    status: 'pending'
                };
                
                var response = await fetch(url);
                requestInfo.status = response.status;
                requestInfo.statusText = response.statusText;
                requestInfo.ok = response.ok;
                
                console.log('📡 Response status:', response.status, response.statusText);
                
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status + ': ' + response.statusText);
                }
                
                var result = await response.json();
                requestInfo.result = result;
                debugInfo.requests.push(requestInfo);
                
                console.log('📦 Result:', result);
                console.log('📊 Data type:', typeof result.data);
                console.log('📈 Data length:', result.data ? result.data.length : 0);
                
                if (result.success && result.data) {
                    displayData(result.data);
                    updateStatus('✅ Đã tải ' + result.data.length + ' giao dịch thành công!', 'success');
                } else {
                    throw new Error('Invalid response format: ' + JSON.stringify(result));
                }
                
            } catch (error) {
                console.error('❌ Error:', error);
                updateStatus('❌ Lỗi: ' + error.message, 'error');
                
                var tbody = document.querySelector('#dataTable tbody');
                tbody.innerHTML = 
                    '<tr><td colspan="7" style="text-align:center; color:#f44336; padding:40px;">' +
                    '❌ Lỗi: ' + error.message + '<br><br>' +
                    'API URL: ' + API_BASE + '/bank-history/<br>' +
                    'Check console for details' +
                    '</td></tr>';
            }
        }
        
        function displayData(transactions) {
            console.log('🎨 Displaying ' + transactions.length + ' transactions');
            
            var tbody = document.querySelector('#dataTable tbody');
            
            if (transactions.length === 0) {
                tbody.innerHTML = 
                    '<tr><td colspan="7" style="text-align:center; padding:40px;">' +
                    '📭 Không có dữ liệu giao dịch<br>' +
                    'Database is empty' +
                    '</td></tr>';
                return;
            }
            
            tbody.innerHTML = '';
            
            transactions.forEach(function(tx, index) {
                console.log('Row ' + index + ':', tx);
                
                // tx là Object[] từ query: [id, type, amount, note, transaction_date, category_name, icon_path, account_name]
                var row = document.createElement('tr');
                
                var id = tx[0] || 'N/A';
                var type = tx[1] || 'N/A';
                var amount = tx[2] || 0;
                var note = tx[3] || 'No note';
                var date = tx[4] || 'N/A';
                var category = tx[5] || 'Uncategorized';
                var account = tx[7] || 'Unknown';
                
                var amountClass = amount >= 0 ? 'amount-positive' : 'amount-negative';
                
                row.innerHTML = 
                    '<td>' + id.substring(0, 8) + '...</td>' +
                    '<td><strong>' + type.toUpperCase() + '</strong></td>' +
                    '<td class="' + amountClass + '">' + formatCurrency(amount) + '</td>' +
                    '<td>' + note + '</td>' +
                    '<td>' + formatDate(date) + '</td>' +
                    '<td>' + category + '</td>' +
                    '<td>' + account + '</td>';
                
                tbody.appendChild(row);
            });
            
            console.log('✅ Table populated successfully!');
        }
        
        // Auto-load on page ready
        window.addEventListener('DOMContentLoaded', function() {
            console.log('🚀 Page loaded, API Base:', API_BASE);
            setTimeout(loadData, 500);
        });
    </script>
</body>
</html>