(function () {
    // --- Helper Functions ---
    function cleanData(data) {
        return Array.isArray(data) ? data.map(val => (val == null || isNaN(val) ? 0 : Number(val))) : [];
    }

    function showLoading(elementId) {
        const element = document.getElementById(elementId);
        if (element) {
            element.innerHTML = '<p class="text-gray-500">Đang tải...</p>';
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
        // SỬA LỖI: dùng key `recentTransactions` từ backend
        const txList = document.getElementById('transaction-list');
        if (txList) {
            txList.innerHTML = '';
            const transactions = data.recentTransactions || [];
            if (transactions.length === 0) {
                txList.innerHTML = '<p class="text-gray-500">Không có giao dịch nào.</p>';
            } else {
                transactions.forEach(tx => {
                    const li = document.createElement('li');
                    li.className = 'flex justify-between items-center py-2 border-b';
                    li.innerHTML = `<span class="flex items-center"><span class="text-blue-500 mr-2">🔹</span><span>${tx.category || 'Không xác định'}</span></span><span class="text-gray-500 text-sm">${tx.date || 'N/A'}</span><span class="font-semibold">${(tx.amount / 1).toLocaleString('vi-VN')} đ</span>`;
                    txList.appendChild(li);
                });
            }
        }

        // SỬA LỖI: dùng key `currentBalance` từ backend
        const currentBalanceElem = document.getElementById('current-balance');
        if (currentBalanceElem) {
            const currentBalance = data.currentBalance || 0;
            currentBalanceElem.textContent = Number(currentBalance).toLocaleString('vi-VN') + ' triệu đ';
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

                // SỬA LỖI: Đọc dữ liệu từ các key đúng do backend trả về
                const balanceChangesData = json.balanceChanges || {};
                const categoriesData = json.categories || {};

                if (key === "incomeExpense") {
                    chart.data.labels = balanceChangesData.labels || [];
                    chart.data.datasets[0].data = cleanData(balanceChangesData.income || []);
                    chart.data.datasets[1].data = cleanData(balanceChangesData.expense || []);
                } else if (key === "categories") {
                    chart.data.labels = categoriesData.labels || [];
                    chart.data.datasets[0].data = cleanData(categoriesData.data || []);
                } else if (key === "balance") {
                    chart.data.labels = balanceChangesData.labels || [];
                    // SỬA LỖI: dùng `runningBalance` cho biểu đồ số dư
                    chart.data.datasets[0].data = cleanData(balanceChangesData.runningBalance || []);
                }

                chart.update();
            })
            .catch(err => console.error("❌ Lỗi cập nhật biểu đồ:", err));
    }

    // --- Chart Rendering ---
    function renderMonthlyCharts(data) {
        // SỬA LỖI: dùng key `monthlySummary` từ backend
        const monthlyData = data.monthlySummary || { previousMonth: {}, currentMonth: {} };

        const prevCtx = document.getElementById('previousMonthChart')?.getContext('2d');
        if (prevCtx) {
            if (window.previousMonthChart && typeof window.previousMonthChart.destroy === 'function') window.previousMonthChart.destroy();
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
            if (window.currentMonthChart && typeof window.currentMonthChart.destroy === 'function') window.currentMonthChart.destroy();
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
        // SỬA LỖI: Đọc từ các key đúng là `balanceChanges` và `categories`
        const { balanceChanges, categories } = data;

        const incomeExpenseCtx = document.getElementById('incomeExpenseChart')?.getContext('2d');
        if (incomeExpenseCtx) {
            if (window.incomeExpenseChart && typeof window.incomeExpenseChart.destroy === 'function') window.incomeExpenseChart.destroy();
            window.incomeExpenseChart = new Chart(incomeExpenseCtx, {
                type: 'bar',
                data: {
                    labels: balanceChanges?.labels || [],
                    datasets: [
                        { label: 'Thu', data: cleanData(balanceChanges?.income), backgroundColor: '#4CAF50' },
                        { label: 'Chi', data: cleanData(balanceChanges?.expense), backgroundColor: '#F44336' }
                    ]
                },
                options: { responsive: true, scales: { y: { beginAtZero: true } } }
            });
        }

        const categoryCtx = document.getElementById('categoryChart')?.getContext('2d');
        if (categoryCtx) {
            if (window.categoryChart && typeof window.categoryChart.destroy === 'function') window.categoryChart.destroy();
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
            if (window.balanceChart && typeof window.balanceChart.destroy === 'function') window.balanceChart.destroy();
            window.balanceChart = new Chart(balanceCtx, {
                type: 'line',
                data: {
                    labels: balanceChanges?.labels || [],
                    // SỬA LỖI: Dùng `runningBalance` cho dữ liệu
                    datasets: [{ label: 'Số Dư', data: cleanData(balanceChanges?.runningBalance), borderColor: '#2196F3', fill: false }]
                },
                options: { responsive: true, scales: { y: { beginAtZero: true } } }
            });
        }
    }

    // --- Dynamic Time Selectors Logic ---
    function setupTimeSelectors(chart, chartKey, unitId, specificId) {
        const unitSelector = document.getElementById(unitId);
        const specificSelector = document.getElementById(specificId);

        if (!unitSelector || !specificSelector) return;

        function populateSpecificSelector() {
            const unit = unitSelector.value;
            specificSelector.innerHTML = '';
            specificSelector.style.display = 'inline-block';

            if (unit === 'month') {
                for (let i = 0; i < 12; i++) {
                    const d = new Date();
                    d.setMonth(d.getMonth() - i);
                    const month = d.getMonth() + 1;
                    const year = d.getFullYear();
                    const option = document.createElement('option');
                    option.value = `month-${year}-${month}`;
                    option.textContent = `Tháng ${month}/${year}`;
                    specificSelector.appendChild(option);
                }
            } else if (unit === 'week') {
                const now = new Date();
                const year = now.getFullYear();
                const month = now.getMonth();
                const firstDay = new Date(year, month, 1);
                let dayOfWeek = firstDay.getDay();
                if (dayOfWeek === 0) dayOfWeek = 7;

                let start = 1;
                let weekNum = 1;
                const lastDay = new Date(year, month + 1, 0).getDate();
                while (start <= lastDay) {
                    let end = start + (7 - dayOfWeek);
                    if (end > lastDay) end = lastDay;
                    const option = document.createElement('option');
                    option.value = `week-${year}-${month + 1}-${weekNum}`;
                    option.textContent = `Tuần ${weekNum} (${start}/${month + 1} - ${end}/${month + 1})`;
                    specificSelector.appendChild(option);
                    start = end + 1;
                    dayOfWeek = 1;
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
            if (chart && period) {
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
                if (data.error) throw new Error(data.error);

                renderMonthlyCharts(data);
                loadOverviewData(data);
                renderOverviewCharts(data);

                setupTimeSelectors(window.incomeExpenseChart, "incomeExpense", 'timeUnitIncomeExpense', 'specificPeriodIncomeExpense');
                setupTimeSelectors(window.categoryChart, "categories", 'timeUnitCategory', 'specificPeriodCategory');
                setupTimeSelectors(window.balanceChart, "balance", 'timeUnitBalance', 'specificPeriodBalance');
            }).catch(err => {
            console.error("❌ Lỗi khi fetch dữ liệu ban đầu:", err)
            document.getElementById('overview-container').innerHTML = `<p class="text-red-500">Không thể tải dữ liệu. Vui lòng kiểm tra console log.</p>`
        });
    }

    document.addEventListener('DOMContentLoaded', () => {
        if (document.querySelector('.overview-page')) {
            window.initOverview();
        }
    });
})();