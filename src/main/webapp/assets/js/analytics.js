(() => {
    const $ = (s) => document.querySelector(s);

    const elChart   = $('#bb-analytics-chart');
    const elType    = $('#bb-chart-type');   // Cột / Tròn
    const elApp     = $('#bb-app');          // Ứng dụng (dataset)
    const elTop     = $('#bb-top');          // Top N
    const elFrom    = $('#bb-from');
    const elTo      = $('#bb-to');
    const elGroup   = $('#bb-group');        // Ngày/Tuần/Tháng/Năm
    const elColor   = $('#bb-color');
    const elApply   = $('#bb-apply');
    const sumInEl   = $('#bb-sum-in');
    const sumOutEl  = $('#bb-sum-out');
    const sumBalEl  = $('#bb-sum-bal');

    // ====== 1) Khởi tạo danh sách Ứng dụng theo loại biểu đồ ======
    function populateApps() {
        const type = elType.value; // 'bar' or 'pie'
        elApp.innerHTML = '';
        const opts = [];
        if (type === 'bar') {
            opts.push({ v: 'timeseries', t: 'Theo chuỗi thời gian' });
            opts.push({ v: 'top-category', t: 'Top danh mục' });
            // bar: có "Nhóm" (Ngày/Tuần/Tháng/Năm)
            elGroup.disabled = false;
        } else {
            opts.push({ v: 'top-category', t: 'Cơ cấu theo danh mục' });
            // pie: không có "Nhóm"
            elGroup.disabled = true;
        }
        opts.forEach(o => {
            const op = document.createElement('option');
            op.value = o.v;
            op.textContent = o.t;
            elApp.appendChild(op);
        });

        // với timeseries: Top vô nghĩa -> disable
        toggleTop();
    }

    function toggleTop() {
        const app = elApp.value;
        const isTop = (app === 'top-category');
        elTop.disabled = !isTop;
    }

    elType.addEventListener('change', populateApps);
    elApp.addEventListener('change', toggleTop);

    // init lần đầu
    populateApps();

    // ====== 2) Vẽ chart ======
    let chart;
    function renderBar(labels, values, color) {
        if (chart) chart.destroy();
        chart = new Chart(elChart.getContext('2d'), {
            type: 'bar',
            data: {
                labels,
                datasets: [{
                    label: '',
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
            options: { responsive: true, maintainAspectRatio: false }
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
        a.href = url; a.download = `analytics-${Date.now()}.png`; a.click();
    });

    const CTX = window.location.pathname.split('/')[1];
    const BASE = `/${CTX}/api/analytics`;

    // ====== 3) Áp dụng: đọc UI -> gọi mock -> vẽ ======
    elApply.addEventListener('click', () => {
        const chartType = elType.value;         // 'bar' | 'pie'
        const app = elApp.value;                // 'timeseries' | 'top-category'
        const from = elFrom.value;
        const to   = elTo.value;
        const group= elGroup.value;             // day|week|month|year
        const color= elColor.value;
        const topN = parseInt(elTop.value || '10', 10);
        const type = $('#bb-kind')?.value || 'all'; // nếu có combobox Income/Expense/All

        if (app === 'timeseries') {
            fetch(`${BASE}?type=${type}`)
                .then(res => res.ok ? res.json() : [])
                .then(r => {
                    // nếu API trả về danh sách transaction => tự nhóm theo ngày
                    const grouped = groupByDate(r);
                    sumInEl.textContent  = grouped.sumIn.toLocaleString('vi-VN');
                    sumOutEl.textContent = grouped.sumOut.toLocaleString('vi-VN');
                    sumBalEl.textContent = grouped.balance.toLocaleString('vi-VN');
                    renderBar(grouped.labels, grouped.values, color);
                })
                .catch(() => {
                    // fallback mock nếu API lỗi
                    const r = window.BB_MOCK.timeSeries({ from, to, group, type });
                    sumInEl.textContent  = (r.sumIn  || 0).toLocaleString('vi-VN');
                    sumOutEl.textContent = (r.sumOut || 0).toLocaleString('vi-VN');
                    sumBalEl.textContent = (r.balance|| 0).toLocaleString('vi-VN');
                    const labels = r.points.map(p => p.label);
                    const values = r.points.map(p => p.value);
                    renderBar(labels, values, color);
                });
            sumInEl.textContent  = (r.sumIn  || 0).toLocaleString('vi-VN');
            sumOutEl.textContent = (r.sumOut || 0).toLocaleString('vi-VN');
            sumBalEl.textContent = (r.balance|| 0).toLocaleString('vi-VN');
            const labels = r.points.map(p => p.label);
            const values = r.points.map(p => p.value);
            renderBar(labels, values, color);
        } else {
            const r = window.BB_MOCK.topCategory({ top: topN, type });
            sumInEl.textContent  = (r.sumIn  || 0).toLocaleString('vi-VN');
            sumOutEl.textContent = (r.sumOut || 0).toLocaleString('vi-VN');
            sumBalEl.textContent = (r.balance|| 0).toLocaleString('vi-VN');
            const labels = r.parts.map(p => p.label);
            const values = r.parts.map(p => p.value);
            if (chartType === 'bar') renderBar(labels, values, color);
            else renderPie(labels, values);
        }
    });

    function groupByDate(rows) {
        const map = {};
        let sumIn = 0, sumOut = 0;
        rows.forEach(t => {
            const date = t.transactionDate?.substring(0, 10);
            const amt = Number(t.amount);
            if (t.type === 'income') sumIn += amt; else sumOut += amt;
            map[date] = (map[date] || 0) + amt * (t.type === 'income' ? 1 : -1);
        });
        const labels = Object.keys(map).sort();
        const values = labels.map(l => map[l]);
        return { labels, values, sumIn, sumOut, balance: sumIn - sumOut };
    }

    // Vẽ lần đầu để không “trắng trang”
    elApply.click();
})();
