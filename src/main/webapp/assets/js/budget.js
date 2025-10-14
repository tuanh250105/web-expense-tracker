document.addEventListener("DOMContentLoaded", () => {
    const addBudgetBtn = document.querySelector('.fab.add-budget');
    const formBudgetContainer = document.querySelector('#budgetForm');
    const chartModal = document.getElementById('chartModal');
    const closeChartModalBtn = document.querySelector('.close-chart-modal');
    const historyModal = document.getElementById('historyModal');
    const closeHistoryModalBtn = document.querySelector('.close-history-modal');

    // Hiển thị form khi nhấn FAB
    addBudgetBtn.addEventListener('click', () => {
        formBudgetContainer.classList.add('active');
        initFlatpickr(formBudgetContainer);
        resetForm(formBudgetContainer);
    });

    // Reset form
    function resetForm(formContainer) {
        formContainer.querySelector('form').reset();
    }

    // Khởi tạo Flatpickr
    function initFlatpickr(formElement) {
        const dateInputs = formElement.querySelectorAll('.select_start_date, .select_end_date');
        dateInputs.forEach(input => {
            flatpickr(input, {
                dateFormat: "Y-m-d",
                allowInput: true,
                defaultDate: new Date()
            });
        });
    }

    // Expand items và Dropdown
    const items = document.querySelectorAll(".budget-item");
    items.forEach(item => {
        item.addEventListener("click", function(e) {
            if (e.target.closest(".more-btn") || e.target.closest(".dropdown-menu")) return;
            item.classList.toggle("expanded");
        });
    });

    document.addEventListener("click", (e) => {
        const moreBtn = e.target.closest(".more-btn");
        const dropdownMenu = e.target.closest(".dropdown-menu");

        if (moreBtn) {
            e.stopPropagation();
            e.preventDefault();
            const menu = moreBtn.nextElementSibling;
            document.querySelectorAll(".dropdown-menu.active").forEach(m => {
                if (m !== menu) m.classList.remove("active");
            });
            menu.classList.toggle("active");
            return;
        }

        if (!dropdownMenu) {
            document.querySelectorAll(".dropdown-menu.active").forEach(m => m.classList.remove("active"));
        }
    });

    // Xóa với Confirm
    document.querySelectorAll(".delete-form").forEach(form => {
        form.addEventListener("submit", function(e) {
            e.preventDefault();
            const budgetItem = this.closest('.budget-item');
            const name = budgetItem ? budgetItem.querySelector('.details h3').textContent : 'ngân sách này';
            if (confirm(`Bạn có chắc muốn xóa ngân sách "${name}" không?`)) {
                this.submit();
            }
            const dropdown = this.closest('.dropdown-menu');
            if (dropdown) dropdown.classList.remove('active');
        });
    });

    // Xem biểu đồ
    document.querySelectorAll('.view-chart').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const budgetId = btn.dataset.budget;
            fetch(`${window.BB_CTX}/api/budget/daily?budgetId=${budgetId}`)
                .then(response => response.json())
                .then(data => {
                    const chartData = {
                        labels: data.dates,
                        datasets: [{
                            label: 'Chi tiêu hàng ngày',
                            data: data.amounts,
                            borderColor: '#1976d2',
                            fill: false
                        }]
                    };
                    renderChart(chartData);
                    chartModal.classList.add('active');
                });
        });
    });

    closeChartModalBtn.addEventListener('click', () => {
        chartModal.classList.remove('active');
    });

    function renderChart(data) {
        const ctx = document.getElementById('spentChart').getContext('2d');
        new Chart(ctx, {
            type: 'line',
            data: data,
            options: { responsive: true }
        });
    }

    // Xem lịch sử
    document.querySelectorAll('.view-history').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const categoryId = btn.dataset.category;
            fetch(`${window.BB_CTX}/api/budget/history?categoryId=${categoryId}`)
                .then(response => response.json())
                .then(data => {
                    const historyList = document.getElementById('historyList');
                    historyList.innerHTML = data.map(b => `
                        <li>
                            Từ ${b.startDate} đến ${b.endDate} - Chi: ${b.spentAmount} / Hạn mức: ${b.limitAmount}₫
                        </li>
                    `).join('');
                    historyModal.classList.add('active');
                });
        });
    });

    closeHistoryModalBtn.addEventListener('click', () => {
        historyModal.classList.remove('active');
    });

    // Chế độ chỉnh sửa
    if (window.editBudgetData) {
        const form = document.querySelector('#budgetForm form');
        form.querySelector('[name="action"]').value = 'update';
        form.querySelector('[name="id"]').value = window.editBudgetData.id;
        form.querySelector('[name="categoryId"]').value = window.editBudgetData.categoryId;
        form.querySelector('[name="limitAmount"]').value = window.editBudgetData.limitAmount;
        form.querySelector('.select_start_date').value = window.editBudgetData.startDate;
        form.querySelector('.select_end_date').value = window.editBudgetData.endDate;
        formBudgetContainer.classList.add('active');
    }
});