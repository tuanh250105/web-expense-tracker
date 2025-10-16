(function () {
    Chart.defaults.font.family = "'Poppins', sans-serif";
    Chart.defaults.font.size = 14;

    // --- Helper Functions ---
    function cleanData(data) {
        console.log("Cleaning data:", data);
        return Array.isArray(data) ? data.map(val => {
            if (val == null || isNaN(val)) return 0;
            // Chuyển BigDecimal thành Number, giữ nguyên giá trị lớn
            return Number(val);
        }) : [];
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

    // --- Load basic info (transactions + balance) ---
    function loadOverviewData(data) {
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
                    li.innerHTML = `
                        <span class="flex items-center">
                            <span class="text-blue-500 mr-2">🔹</span>
                            <span>${tx.category || 'Không xác định'}</span>
                        </span>
                        <span class="text-gray-500 text-sm">${tx.date || 'N/A'}</span>
                        <span class="font-semibold">${Number(tx.amount).toLocaleString('vi-VN')} đ</span>`;
                    txList.appendChild(li);
                });
            }
        }

        const currentBalanceElem = document.getElementById('current-balance');
        if (currentBalanceElem) {
            const currentBalance = data.currentBalance || 0;
            currentBalanceElem.textContent = (Number(currentBalance) / 1000000).toLocaleString('vi-VN') + ' triệu đ';
        }
    }

    // --- Update Chart when user changes time range ---
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
                console.log("Dữ liệu nhận được:", json);
                if (json.error) {
                    console.error("❌ Lỗi từ server:", json.error);
                    return;
                }

                const balanceChangesData = json.balanceChanges || { labels: [], income: [], expense: [], runningBalance: [] };
                const categoriesData = json.categories || { labels: [], data: [] };

                console.log(`Balance changes data for ${key}:`, balanceChangesData);
                console.log(`Categories data for ${key}:`, categoriesData);

                if (key === "incomeExpense") {
                    chart.data.labels = balanceChangesData.labels || [];
                    chart.data.datasets[0].data = cleanData(balanceChangesData.income || []);
                    chart.data.datasets[1].data = cleanData(balanceChangesData.expense || []);
                } else if (key === "categories") {
                    chart.data.labels = categoriesData.labels || [];
                    chart.data.datasets[0].data = cleanData(categoriesData.data || []);
                } else if (key === "balance") {
                    chart.data.labels = balanceChangesData.labels || [];
                    chart.data.datasets[0].data = cleanData(balanceChangesData.runningBalance || []);
                }

                console.log(`Cập nhật biểu đồ ${key}:`, chart.data);
                chart.update();
            })
            .catch(err => console.error("❌ Lỗi cập nhật biểu đồ:", err));
    }

    // --- Render monthly pie charts ---
    function renderMonthlyCharts(data) {
        const monthlyData = data.monthlySummary || { previousMonth: {}, currentMonth: {} };

        const prevCtx = document.getElementById('previousMonthChart')?.getContext('2d');
        if (prevCtx) {
            if (window.previousMonthChart?.destroy) window.previousMonthChart.destroy();
            window.previousMonthChart = new Chart(prevCtx, {
                type: 'pie',
                data: {
                    labels: ['Thu nhập', 'Chi tiêu'],
                    datasets: [{
                        data: [
                            Number(monthlyData.previousMonth.income || 0) / 1000000,
                            Number(monthlyData.previousMonth.expense || 0) / 1000000
                        ],
                        backgroundColor: ['#4CAF50', '#F44336']
                    }]
                },
                options: {
                    plugins: {
                        legend: { position: 'right' },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    return `${context.label}: ${context.raw.toLocaleString('vi-VN')} triệu đ`;
                                }
                            }
                        }
                    },
                    responsive: true
                }
            });
        }

        const currCtx = document.getElementById('currentMonthChart')?.getContext('2d');
        if (currCtx) {
            if (window.currentMonthChart?.destroy) window.currentMonthChart.destroy();
            window.currentMonthChart = new Chart(currCtx, {
                type: 'pie',
                data: {
                    labels: ['Thu nhập', 'Chi tiêu'],
                    datasets: [{
                        data: [
                            Number(monthlyData.currentMonth.income || 0) / 1000000,
                            Number(monthlyData.currentMonth.expense || 0) / 1000000
                        ],
                        backgroundColor: ['#4CAF50', '#F44336']
                    }]
                },
                options: {
                    plugins: {
                        legend: { position: 'right' },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    return `${context.label}: ${context.raw.toLocaleString('vi-VN')} triệu đ`;
                                }
                            }
                        }
                    },
                    responsive: true
                }
            });
        }
    }

    // --- Render main charts (income/expense, category, balance) ---
    function renderOverviewCharts(data) {
        console.log("Rendering charts with data:", data);
        if (Array.isArray(data.balanceChanges)) {
            console.warn("Converting legacy balanceChanges format");
            const arr = data.balanceChanges;
            data.balanceChanges = {
                labels: arr.map(i => i.date),
                income: arr.map(i => i.income),
                expense: arr.map(i => i.expense),
                runningBalance: arr.map(i => i.balance)
            };
        }

        const { balanceChanges, categories } = data;

        // Income vs Expense
        const incomeExpenseCtx = document.getElementById('incomeExpenseChart')?.getContext('2d');
        if (incomeExpenseCtx) {
            if (window.incomeExpenseChart?.destroy) window.incomeExpenseChart.destroy();
            window.incomeExpenseChart = new Chart(incomeExpenseCtx, {
                type: 'bar',
                data: {
                    labels: balanceChanges?.labels || [],
                    datasets: [
                        { label: 'Thu', data: cleanData(balanceChanges?.income), backgroundColor: '#4CAF50' },
                        { label: 'Chi', data: cleanData(balanceChanges?.expense), backgroundColor: '#F44336' }
                    ]
                },
                options: {
                    responsive: true,
                    scales: {
                        y: {
                            beginAtZero: false, // Không bắt đầu từ 0 để hiển thị tốt hơn
                            ticks: {
                                callback: function(value) {
                                    return (value / 1000000).toLocaleString('vi-VN') + ' triệu';
                                }
                            }
                        }
                    },
                    plugins: {
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    return `${context.dataset.label}: ${(context.raw / 1000000).toLocaleString('vi-VN')} triệu đ`;
                                }
                            }
                        }
                    }
                }
            });
        }

        // Categories
        const categoryCtx = document.getElementById('categoryChart')?.getContext('2d');
        if (categoryCtx) {
            if (window.categoryChart?.destroy) window.categoryChart.destroy();
            window.categoryChart = new Chart(categoryCtx, {
                type: 'pie',
                data: {
                    labels: categories?.labels || [],
                    datasets: [{
                        data: cleanData(categories?.data).map(val => val / 1000000),
                        backgroundColor: ['#4CAF50', '#FFC107', '#F44336', '#2196F3', '#9C27B0', '#009688']
                    }]
                },
                options: {
                    plugins: {
                        legend: { position: 'right' },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    return `${context.label}: ${context.raw.toLocaleString('vi-VN')} triệu đ`;
                                }
                            }
                        }
                    },
                    responsive: true
                }
            });
        }

        // Balance over time
        const balanceCtx = document.getElementById('balanceChart')?.getContext('2d');
        if (balanceCtx) {
            if (window.balanceChart?.destroy) window.balanceChart.destroy();
            window.balanceChart = new Chart(balanceCtx, {
                type: 'line',
                data: {
                    labels: balanceChanges?.labels || [],
                    datasets: [{
                        label: 'Số Dư',
                        data: cleanData(balanceChanges?.runningBalance).map(val => val / 1000000),
                        borderColor: '#2196F3',
                        fill: false
                    }]
                },
                options: {
                    responsive: true,
                    scales: {
                        y: {
                            beginAtZero: false, // Không bắt đầu từ 0
                            ticks: {
                                callback: function(value) {
                                    return value.toLocaleString('vi-VN') + ' triệu';
                                }
                            }
                        }
                    },
                    plugins: {
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    return `Số Dư: ${context.raw.toLocaleString('vi-VN')} triệu đ`;
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    // --- Setup time selectors for charts ---
    function setupTimeSelectors(chart, chartKey, unitId, specificId) {
        const unitSelector = document.getElementById(unitId);
        const specificSelector = document.getElementById(specificId);

        if (!unitSelector || !specificSelector) {
            console.error(`❌ Selector không tìm thấy: ${unitId}, ${specificId}`);
            return;
        }

        function populateSpecificSelector() {
            const unit = unitSelector.value;
            specificSelector.innerHTML = '';
            specificSelector.style.display = 'inline-block';

            console.log(`Populating ${specificId} for unit=${unit}`);

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
            } else if (unit === 'year') {
                const now = new Date();
                const currentYear = now.getFullYear();
                for (let i = 0; i < 5; i++) {
                    const year = currentYear - i;
                    const option = document.createElement('option');
                    option.value = `year-${year}`;
                    option.textContent = `Năm ${year}`;
                    specificSelector.appendChild(option);
                }
            } else {
                specificSelector.style.display = 'none';
            }
        }

        unitSelector.addEventListener('change', () => {
            console.log(`Unit selector changed to: ${unitSelector.value}`);
            populateSpecificSelector();
        });

        specificSelector.addEventListener('change', debounce(() => {
            const period = specificSelector.style.display === 'none' ? unitSelector.value : specificSelector.value;
            console.log(`Specific selector changed, period=${period}`);
            if (chart && period) {
                updateOverviewChart(chart, chartKey, period);
            }
        }, 300));

        populateSpecificSelector();

        const initialPeriod = specificSelector.style.display === 'none' ? unitSelector.value : specificSelector.value;
        console.log(`Initial period for ${chartKey}: ${initialPeriod}`);
        if (chart && initialPeriod) {
            updateOverviewChart(chart, chartKey, initialPeriod);
        }
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
            })
            .catch(err => {
                console.error("❌ Lỗi khi fetch dữ liệu ban đầu:", err);
                const container = document.getElementById('overview-container');
                if (container) {
                    container.innerHTML = `<p class="text-red-500">Không thể tải dữ liệu. Vui lòng kiểm tra console log.</p>`;
                }
            });
    };

    document.addEventListener('DOMContentLoaded', () => {
        if (document.querySelector('.overview-page')) {
            window.initOverview();
        }
    });
})();