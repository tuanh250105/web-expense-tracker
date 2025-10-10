(function () {
    // --- Helper Functions ---
    function cleanData(data) {
        return Array.isArray(data) ? data.map(val => (val == null || isNaN(val) ? 0 : Number(val))) : [];
    }

    function showLoading(elementId) {
        const element = document.getElementById(elementId);
        if (element) {
            element.innerHTML = '<p>Đang tải...</p>';
        }
    }

    function debounce(func, wait) {
        let timeout;
        return function (...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    }

    // --- Chart and Data Loading ---
    function loadOverviewData(data) {
        const txList = document.getElementById('transaction-list');
        if (txList) {
            txList.innerHTML = '';
            const transactions = data.transactions || [];
            transactions.forEach(tx => {
                const li = document.createElement('li');
                li.innerHTML = `<span>🔹</span><span>${tx.category || 'Không xác định'}</span><span>${tx.date || 'N/A'}</span><span>${(tx.amount / 1_000_000).toLocaleString('vi-VN')} triệu đ</span>`;
                txList.appendChild(li);
            });
        }

        const currentBalanceElem = document.getElementById('current-balance');
        if (currentBalanceElem) {
            const balance = data.balance || { data: [0] };
            const currentBalance = balance.data.length > 0 ? balance.data[balance.data.length - 1] : 0;
            currentBalanceElem.textContent = currentBalance.toLocaleString('vi-VN') + ' triệu đ';
        }
    }

    function updateOverviewChart(chart, key, period) {
        if (typeof window.contextPath === 'undefined') {
            console.error("❌ contextPath không được định nghĩa");
            return;
        }
        console.log(`🔄 Cập nhật biểu đồ [${key}] với period=${period}`);

        fetch(`${window.contextPath}/dashboard-data?period=${period}`)
            .then(res => {
                if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
                return res.json();
            })
            .then(json => {
                if (json.error) {
                    console.error("❌ Lỗi từ server:", json.error);
                    return;
                }
                console.log(`✅ Dữ liệu mới cho [${key}]:`, json[key]);

                const chartData = json[key] || {};

                if (key === "incomeExpense") {
                    chart.data.labels = chartData.labels || [];
                    chart.data.datasets[0].data = cleanData(chartData.income || []);
                    chart.data.datasets[1].data = cleanData(chartData.expense || []);
                } else if (key === "categories") {
                    chart.data.labels = chartData.labels || [];
                    chart.data.datasets[0].data = cleanData(chartData.data || []);
                } else if (key === "balance") {
                    chart.data.labels = chartData.labels || [];
                    chart.data.datasets[0].data = cleanData(chartData.data || []);
                }

                chart.update();
            })
            .catch(err => console.error("❌ Lỗi cập nhật biểu đồ:", err));
    }

    // --- Chart Rendering ---
    function renderMonthlyCharts(data) {
        const monthlyData = data.monthlyIncomeExpense || { previousMonth: {}, currentMonth: {} };

        const prevCtx = document.getElementById('previousMonthChart')?.getContext('2d');
        if (prevCtx) {
            if (window.previousMonthChart && typeof window.previousMonthChart.destroy === 'function') {
                window.previousMonthChart.destroy();
            }
            window.previousMonthChart = new Chart(prevCtx, {
                type: 'pie',
                data: {
                    labels: ['Thu nhập', 'Chi tiêu'],
                    datasets: [{
                        data: [monthlyData.previousMonth.income || 0, monthlyData.previousMonth.expense || 0],
                        backgroundColor: ['#4CAF50', '#F44336']
                    }]
                },
                options: { plugins: { legend: { position: 'right' } }, responsive: true }
            });
        }

        const currCtx = document.getElementById('currentMonthChart')?.getContext('2d');
        if (currCtx) {
            if (window.currentMonthChart && typeof window.currentMonthChart.destroy === 'function') {
                window.currentMonthChart.destroy();
            }
            window.currentMonthChart = new Chart(currCtx, {
                type: 'pie',
                data: {
                    labels: ['Thu nhập', 'Chi tiêu'],
                    datasets: [{
                        data: [monthlyData.currentMonth.income || 0, monthlyData.currentMonth.expense || 0],
                        backgroundColor: ['#4CAF50', '#F44336']
                    }]
                },
                options: { plugins: { legend: { position: 'right' } }, responsive: true }
            });
        }
    }

    function renderOverviewCharts(data) {
        const { incomeExpense, categories, balance } = data;

        const incomeExpenseCtx = document.getElementById('incomeExpenseChart')?.getContext('2d');
        if (incomeExpenseCtx) {
            if (window.incomeExpenseChart && typeof window.incomeExpenseChart.destroy === 'function') {
                window.incomeExpenseChart.destroy();
            }
            window.incomeExpenseChart = new Chart(incomeExpenseCtx, {
                type: 'bar',
                data: {
                    labels: incomeExpense?.labels || [],
                    datasets: [
                        { label: 'Thu', data: cleanData(incomeExpense?.income), backgroundColor: '#4CAF50' },
                        { label: 'Chi', data: cleanData(incomeExpense?.expense), backgroundColor: '#F44336' }
                    ]
                },
                options: { responsive: true, scales: { y: { beginAtZero: true } } }
            });
        }

        const categoryCtx = document.getElementById('categoryChart')?.getContext('2d');
        if (categoryCtx) {
            if (window.categoryChart && typeof window.categoryChart.destroy === 'function') {
                window.categoryChart.destroy();
            }
            window.categoryChart = new Chart(categoryCtx, {
                type: 'pie',
                data: {
                    labels: categories?.labels || [],
                    datasets: [{
                        data: cleanData(categories?.data),
                        backgroundColor: ['#4CAF50', '#FFC107', '#F44336', '#2196F3', '#9C27B0', '#009688']
                    }]
                },
                options: { plugins: { legend: { position: 'right' } }, responsive: true }
            });
        }

        const balanceCtx = document.getElementById('balanceChart')?.getContext('2d');
        if (balanceCtx) {
            if (window.balanceChart && typeof window.balanceChart.destroy === 'function') {
                window.balanceChart.destroy();
            }
            window.balanceChart = new Chart(balanceCtx, {
                type: 'line',
                data: {
                    labels: balance?.labels || [],
                    datasets: [{ label: 'Số Dư', data: cleanData(balance?.data), borderColor: '#2196F3', fill: false }]
                },
                options: { responsive: true, scales: { y: { beginAtZero: true } } }
            });
        }
    }

    // --- Dynamic Time Selectors Logic ---
    function setupTimeSelectors(chart, chartKey, unitId, specificId) {
        const unitSelector = document.getElementById(unitId);
        const specificSelector = document.getElementById(specificId);

        // --- SỬA LỖI: Kiểm tra xem các phần tử có tồn tại không trước khi thêm event listener ---
        if (!unitSelector || !specificSelector) {
            return; // Thoát khỏi hàm nếu không tìm thấy phần tử
        }

        function populateSpecificSelector() {
            const unit = unitSelector.value;
            specificSelector.innerHTML = ''; // Xóa các lựa chọn cũ

            if (unit === 'month') {
                specificSelector.style.display = 'inline-block';
                for (let i = 0; i < 12; i++) {
                    const d = new Date();
                    d.setMonth(d.getMonth() - i);
                    const option = document.createElement('option');
                    const month = d.getMonth() + 1;
                    const year = d.getFullYear();
                    option.value = `month-${year}-${month}`;
                    option.textContent = `Tháng ${month}/${year}`;
                    specificSelector.appendChild(option);
                }
            } else if (unit === 'week') {
                specificSelector.style.display = 'inline-block';
                const now = new Date();
                const year = now.getFullYear();
                const month = now.getMonth(); // 0-11
                const firstDay = new Date(year, month, 1);
                let dayOfWeek = firstDay.getDay();
                if (dayOfWeek === 0) dayOfWeek = 7; // Sunday is 7

                let start = 1;
                let weekNum = 1;
                while(start <= new Date(year, month + 1, 0).getDate()){
                    let end = start + (7 - dayOfWeek);
                    if (end > new Date(year, month + 1, 0).getDate()) end = new Date(year, month + 1, 0).getDate();

                    const option = document.createElement('option');
                    option.value = `week-${year}-${month+1}-${weekNum}`;
                    option.textContent = `Tuần ${weekNum} (${start}/${month+1} - ${end}/${month+1})`;
                    specificSelector.appendChild(option);

                    start = end + 1;
                    dayOfWeek = 1; // Subsequent weeks start on Monday
                    weekNum++;
                }

            } else { // year
                specificSelector.style.display = 'none';
            }
            specificSelector.dispatchEvent(new Event('change'));
        }

        unitSelector.addEventListener('change', populateSpecificSelector);
        specificSelector.addEventListener('change', debounce(() => {
            const period = specificSelector.style.display === 'none' ? unitSelector.value : specificSelector.value;
            // Kiểm tra xem chart có tồn tại không trước khi cập nhật
            if (chart) {
                updateOverviewChart(chart, chartKey, period);
            }
        }, 300));

        populateSpecificSelector();
    }

    // --- Main Initialization ---
    window.initOverview = function () {
        if (!window.contextPath) {
            console.error("❌ contextPath không được định nghĩa.");
            return;
        }

        showLoading('transaction-list');
        showLoading('current-balance');

        fetch(`${window.contextPath}/dashboard-data?period=month`)
            .then(res => res.json())
            .then(data => {
                if(data.error) {
                    throw new Error(data.error);
                }
                renderMonthlyCharts(data);
                loadOverviewData(data);
                renderOverviewCharts(data);

                setupTimeSelectors(window.incomeExpenseChart, "incomeExpense", 'timeUnitIncomeExpense', 'specificPeriodIncomeExpense');
                setupTimeSelectors(window.categoryChart, "categories", 'timeUnitCategory', 'specificPeriodCategory');
                setupTimeSelectors(window.balanceChart, "balance", 'timeUnitBalance', 'specificPeriodBalance');

            }).catch(err => console.error("❌ Lỗi khi fetch dữ liệu ban đầu:", err));
    }

    document.addEventListener('DOMContentLoaded', () => {
        // Chỉ chạy initOverview nếu chúng ta đang ở trên trang tổng quan
        if (document.querySelector('.overview-page')) {
            window.initOverview();
        }
    });
})();

