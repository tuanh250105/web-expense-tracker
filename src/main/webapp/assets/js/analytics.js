(() => {
    const $ = (s) => document.querySelector(s);
    const USER_ID = window.BB_USER_ID || "67b78d51-4eec-491c-bbf0-30e982def9e0";

    const elChart   = $('#bb-analytics-chart');
    const elType    = $('#bb-chart-type');
    const elApp     = $('#bb-app');
    const elTop     = $('#bb-top');
    const elFrom    = $('#bb-from');
    const elTo      = $('#bb-to');
    const elGroup   = $('#bb-group');
    const elColor   = $('#bb-color');
    const elApply   = $('#bb-apply');
    const sumInEl   = $('#bb-sum-in');
    const sumOutEl  = $('#bb-sum-out');
    const sumBalEl  = $('#bb-sum-bal');

    function populateApps() {
        const type = elType.value;
        elApp.innerHTML = '';
        const opts = [];
        // các loại có time-series
        if (type === 'bar' || type === 'line') {
            opts.push({ v: 'timeseries', t: 'Theo chuỗi thời gian' });
            opts.push({ v: 'top-category', t: 'Top danh mục' });
            elGroup.disabled = false;
        } else {
            opts.push({ v: 'top-category', t: 'Cơ cấu theo danh mục' });
            elGroup.disabled = true;
        }
        opts.forEach(o => {
            const op = document.createElement('option');
            op.value = o.v;
            op.textContent = o.t;
            elApp.appendChild(op);
        });
        toggleTop();
    }

    function toggleTop() {
        const app = elApp.value;
        elTop.disabled = (app !== 'top-category');
    }

    elType.addEventListener('change', populateApps);
    elApp.addEventListener('change', toggleTop);
    populateApps();

    let chart;
    function renderBar(labels, values, color) {
        if (chart) chart.destroy();
        chart = new Chart(elChart.getContext('2d'), {
            type: 'bar',
            data: {
                labels,
                datasets: [{
                    label: 'Thu - Chi',
                    data: values,
                    backgroundColor: makeGradient(elChart, color),
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true, ticks: { callback: v => v.toLocaleString('vi-VN') } }
                }
            }
        });
    }

    function renderPie(labels, values) {
        if (chart) chart.destroy();
        chart = new Chart(elChart.getContext('2d'), {
            type: 'pie',
            data: { labels, datasets: [{ data: values }] },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: 'bottom' } }
            }
        });
    }

    // === Các loại biểu đồ mới ===
    function renderLine(labels, values, color) {
        if (chart) chart.destroy();
        chart = new Chart(elChart.getContext('2d'), {
            type: 'line',
            data: {
                labels,
                datasets: [{
                    label: 'Thu - Chi theo thời gian',
                    data: values,
                    borderColor: color,
                    backgroundColor: 'rgba(124,77,255,0.2)',
                    fill: true,
                    tension: 0.3
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: { y: { beginAtZero: true } }
            }
        });
    }

    function renderDoughnut(labels, values) {
        if (chart) chart.destroy();
        chart = new Chart(elChart.getContext('2d'), {
            type: 'doughnut',
            data: { labels, datasets: [{ data: values }] },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: 'bottom' } }
            }
        });
    }

    function renderRadar(labels, values) {
        if (chart) chart.destroy();
        chart = new Chart(elChart.getContext('2d'), {
            type: 'radar',
            data: {
                labels,
                datasets: [{
                    label: 'So sánh danh mục',
                    data: values,
                    borderColor: '#8a2be2',
                    backgroundColor: 'rgba(138,43,226,0.2)',
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: 'top' } }
            }
        });
    }

    function renderPolar(labels, values) {
        if (chart) chart.destroy();
        chart = new Chart(elChart.getContext('2d'), {
            type: 'polarArea',
            data: { labels, datasets: [{ data: values }] },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: 'bottom' } }
            }
        });
    }

    function makeGradient(canvas, hex) {
        const ctx = canvas.getContext('2d');
        const g = ctx.createLinearGradient(0, 0, 0, canvas.height);
        g.addColorStop(0, hex);
        g.addColorStop(1, '#ede7ff');
        return g;
    }

    document.getElementById('bb-export')?.addEventListener('click', () => {
        if (!chart) return alert('Chưa có biểu đồ để export');
        const url = chart.toBase64Image();
        const a = document.createElement('a');
        a.href = url;
        a.download = `analytics-${Date.now()}.png`;
        a.click();
    });

    const BASE = `${window.BB_CTX}/api/analytics`;

    function groupByDate(rows) {
        const map = {};
        let sumIn = 0, sumOut = 0;
        rows.forEach(t => {
            let dateStr = "";
            if (typeof t.transactionDate === "string") {
                dateStr = t.transactionDate.substring(0, 10);
            } else if (t.transactionDate?.date) {
                dateStr = t.transactionDate.date.substring(0, 10);
            } else {
                dateStr = new Date().toISOString().substring(0, 10);
            }
            const amt = Number(t.amount || 0);
            if (t.type === 'income') sumIn += amt; else sumOut += amt;
            map[dateStr] = (map[dateStr] || 0) + amt * (t.type === 'income' ? 1 : -1);
        });
        const labels = Object.keys(map).sort();
        const values = labels.map(l => map[l]);
        return { labels, values, sumIn, sumOut, balance: sumIn - sumOut };
    }

    elApply.addEventListener('click', async () => {
        const chartType = elType.value;
        const app = elApp.value;
        const from = elFrom.value;
        const to   = elTo.value;
        const color= elColor.value;
        const topN = parseInt(elTop.value || '5', 10);
        const type = $('#bb-kind')?.value || 'all';
        const group = elGroup.value;

        console.log("Fetch:", `${BASE}?userId=${USER_ID}&type=${type}&from=${from}&to=${to}&group=${group}&top=${topN}`);

        try {
            const res = await fetch(`${BASE}?userId=${USER_ID}&type=${type}&from=${from}&to=${to}&group=${group}&top=${topN}`);
            if (!res.ok) throw new Error("Lỗi tải dữ liệu");
            const data = await res.json();
            console.log("API Data:", data);

            // === Cập nhật tổng thu/chi ===
            const summary = data.summary || {};
            sumInEl.textContent  = (summary.income || 0).toLocaleString('vi-VN');
            sumOutEl.textContent = (summary.expense || 0).toLocaleString('vi-VN');
            sumBalEl.textContent = (summary.balance || 0).toLocaleString('vi-VN');

            // === Biểu đồ theo chuỗi thời gian ===
            if (app === 'timeseries') {
                const grouped = data.grouped || [];
                if (grouped.length === 0) {
                    alert("Không có dữ liệu chuỗi thời gian!");
                    return;
                }
                const labels = grouped.map(x => x.label);
                const values = grouped.map(x => x.value);
                if (chartType === 'line') renderLine(labels, values, color);
                else renderBar(labels, values, color);
            }

            // === Biểu đồ Top danh mục ===
            else if (app === 'top-category') {
                const top = data.topCategory || [];
                if (top.length === 0) {
                    alert("Không có dữ liệu danh mục!");
                    return;
                }
                const labels = top.map(t => t.categoryName || `Danh mục #${t.categoryId}`);
                const values = top.map(t => t.total);
                switch (chartType) {
                    case 'bar': renderBar(labels, values, color); break;
                    case 'pie': renderPie(labels, values); break;
                    case 'doughnut': renderDoughnut(labels, values); break;
                    case 'radar': renderRadar(labels, values); break;
                    case 'polar': renderPolar(labels, values); break;
                    default: renderPie(labels, values);
                }
            }

        } catch (err) {
            console.error("Lỗi khi fetch API:", err);
            alert("Không thể tải dữ liệu từ API.");
        }
    });

    window.addEventListener("DOMContentLoaded", () => {
        const now = new Date();
        const firstDay = new Date(now.getFullYear(), now.getMonth(), 1);
        elFrom.value = firstDay.toISOString().slice(0, 10);
        elTo.value = now.toISOString().slice(0, 10);
        elApply.click();
    });
})();
