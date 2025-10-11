<%@ page contentType="text/html; charset=UTF-8" %>
<link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@500;700&display=swap" rel="stylesheet">

<h1 style="margin:0 0 10px;font-size:34px;font-family:'Be Vietnam Pro',system-ui">📊 <b>Tổng hợp biểu đồ</b></h1>
<p style="margin:0 0 16px;color:#475569;font-family:'Be Vietnam Pro',system-ui">
    Chọn <b>loại biểu đồ</b> trước, rồi chọn <b>ứng dụng/dataset</b> phù hợp.
</p>

<style>
    .bar{background:#fff;border:1px solid #e5e7eb;border-radius:14px;box-shadow:0 8px 18px rgba(0,0,0,.05)}
    .toolbar{display:flex;flex-wrap:wrap;gap:10px;align-items:center;padding:12px 14px; font-family:'Be Vietnam Pro',system-ui}
    .toolbar label{font-weight:700;color:#334155;margin-left:8px}
    select,input{height:40px;border-radius:8px;border:1px solid #e5e7eb;padding:0 10px;font-family:'Be Vietnam Pro',system-ui}
    .btn{padding:12px 18px;border:none;border-radius:10px;font-weight:800;cursor:pointer;color:#fff;background:linear-gradient(135deg,#00c6ff,#6a11cb)}
    .btn.ghost{background:#eef2ff;color:#1e293b;border:1px solid #c7d2fe}
    .muted{opacity:.5}
    .stats{display:grid;grid-template-columns:repeat(3,1fr);gap:12px;margin:10px 0}
    .stat{padding:12px 14px} .stat b{font-size:20px}
    .note{color:#64748b;margin:8px 0;font-family:'Be Vietnam Pro',system-ui}
    .plot{height:360px;padding:10px}
    .plot canvas{background:#fff;border-radius:16px;box-shadow:0 10px 22px rgba(0,0,0,.06);width:100%;height:100%}
</style>

<div class="bar toolbar">
    <label>Biểu đồ</label>
    <select id="chartType">
        <option value="pie">Tròn / Donut</option>
        <option value="bar" selected>Cột</option>
    </select>

    <label>Ứng dụng</label>
    <!-- LUÔN có sẵn options để không bao giờ trống nếu JS lỗi -->
    <select id="dataset">
        <option value="net_over_time">Số dư theo thời gian</option>
        <option value="income_over_time">Thu theo thời gian</option>
        <option value="expense_over_time">Chi theo thời gian</option>
        <option value="top_categories_exp">Top danh mục CHI</option>
        <option value="top_categories_inc">Top danh mục THU</option>
    </select>

    <label id="lblTop">Top</label>
    <select id="topN">
        <option value="5">5</option>
        <option value="10" selected>10</option>
        <option value="20">20</option>
        <option value="all">All</option>
    </select>

    <label>Từ</label><input type="date" id="from" value="2025-09-01">
    <label>Đến</label><input type="date" id="to"   value="2025-09-07">

    <!-- Nhóm luôn hiển thị cho cả Tròn & Cột (Pie sẽ bỏ qua khi tính) -->
    <label>Nhóm</label>
    <select id="group">
        <option value="day">Ngày</option>
        <option value="week">Tuần</option>
        <option value="month" selected>Tháng</option>
        <option value="year">Năm</option>
    </select>

    <label>Màu</label><input type="color" id="color" value="#6a11cb">
    <button id="apply"  class="btn">Áp dụng</button>
    <button id="export" class="btn ghost">Export PNG</button>
</div>

<div class="note"><em>* Pie chỉ dành cho “cơ cấu/tỷ trọng”; Bar dùng cho chuỗi thời gian & Top N.</em></div>

<div class="stats">
    <div class="bar stat">Tổng vào<br><b id="sumIn">0</b></div>
    <div class="bar stat">Tổng ra<br><b id="sumOut">0</b></div>
    <div class="bar stat">Số dư<br><b id="sumBal">0</b></div>
</div>

<div class="bar plot"><canvas id="chart"></canvas></div>

<script>
    (function(){
        // --- helper: chờ DOM & đảm bảo Chart.js đã tải ---
        function ready(fn){ if(document.readyState!=='loading') fn(); else document.addEventListener('DOMContentLoaded',fn); }
        function ensureChart(cb){
            if (window.Chart) return cb();
            const s=document.createElement('script');
            s.src='https://cdn.jsdelivr.net/npm/chart.js@4';
            s.onload=cb;
            s.onerror=function(){ console.error('Không tải được Chart.js từ CDN'); cb(); };
            document.head.appendChild(s);
        }

        ready(()=> ensureChart(init));

        function init(){
            const $ = s => document.querySelector(s);
            const el = {
                type: $("#chartType"), ds: $("#dataset"), top: $("#topN"), from: $("#from"),
                to: $("#to"), grp: $("#group"), color: $("#color"), apply: $("#apply"), exp: $("#export")
            };
            const lblTop = $("#lblTop");

            // Nếu Chart có mặt thì set font; nếu không có, vẫn cho UI chạy, chỉ không vẽ chart.
            if (window.Chart) {
                Chart.defaults.font.family = "'Be Vietnam Pro', system-ui, -apple-system, Segoe UI, Roboto, sans-serif";
            }

            // mapping dataset cho mỗi loại biểu đồ
            const SETS = {
                pie: [
                    {v:"expense_by_category", t:"Cơ cấu CHI theo danh mục"},
                    {v:"income_by_category",  t:"Cơ cấu THU theo danh mục"}
                ],
                bar: [
                    {v:"net_over_time",       t:"Số dư theo thời gian"},
                    {v:"income_over_time",    t:"Thu theo thời gian"},
                    {v:"expense_over_time",   t:"Chi theo thời gian"},
                    {v:"top_categories_exp",  t:"Top danh mục CHI"},
                    {v:"top_categories_inc",  t:"Top danh mục THU"}
                ]
            };

            // dữ liệu mẫu (01–07/09/2025)
            const MOCK = [
                {date:"2025-09-01", type:"income",  category:"Salary",    amount:2000000},
                {date:"2025-09-02", type:"expense", category:"Food",      amount: 95000},
                {date:"2025-09-03", type:"expense", category:"Transport", amount: 45000},
                {date:"2025-09-04", type:"expense", category:"Shopping",  amount:120000},
                {date:"2025-09-05", type:"income",  category:"Bonus",     amount:500000},
                {date:"2025-09-06", type:"expense", category:"Food",      amount: 85000},
                {date:"2025-09-07", type:"expense", category:"Bills",     amount:300000}
            ];

            // điền options “Ứng dụng” theo loại biểu đồ (và luôn đảm bảo không rỗng)
            function fillDatasetOptions(){
                const list = SETS[el.type.value] || [];
                if (list.length){
                    el.ds.innerHTML = list.map(x=>`<option value="${x.v}">${x.t}</option>`).join("");
                    el.ds.value = list[0].v;
                }
                updateTopState();
            }
            function updateTopState(){
                const enableTop = (el.ds.value || '').startsWith('top_');
                el.top.disabled = !enableTop;
                el.top.classList.toggle('muted', !enableTop);
                lblTop.classList.toggle('muted', !enableTop);
            }

            function parseISO(s){
                if(!s) return null;
                const [y,m,d]=(s||'').split('-').map(n=>parseInt(n,10));
                if(!y||!m||!d) return null;
                return new Date(y,m-1,d);
            }
            function groupKey(d,mode){
                const t=new Date(d);
                if(mode==='day') return t.toISOString().slice(0,10);
                if(mode==='week'){const a=new Date(Date.UTC(t.getFullYear(),t.getMonth(),t.getDate()));const n=a.getUTCDay()||7;a.setUTCDate(a.getUTCDate()+4-n);const y=new Date(Date.UTC(a.getUTCFullYear(),0,1));return t.getFullYear()+"-W"+Math.ceil((((a-y)/86400000)+1)/7);}
                if(mode==='month') return t.getFullYear()+"-"+String(t.getMonth()+1).padStart(2,'0');
                return String(t.getFullYear());
            }

            function compute(){
                const from=parseISO(el.from.value), to=parseISO(el.to.value);
                const ds = el.ds.value || 'net_over_time';
                let sumIn=0,sumOut=0; const map=new Map();

                MOCK.forEach(r=>{
                    const dt=new Date(r.date);
                    if(from && dt<from) return;
                    if(to   && dt>to)   return;

                    if (ds==='expense_by_category'){ if(r.type!=='expense') return; map.set(r.category,(map.get(r.category)||0)+r.amount); sumOut+=r.amount; }
                    else if (ds==='income_by_category'){ if(r.type!=='income') return; map.set(r.category,(map.get(r.category)||0)+r.amount); sumIn+=r.amount; }
                    else if (ds==='net_over_time'){ const k=groupKey(dt,el.grp.value); map.set(k,(map.get(k)||0)+(r.type==='expense'?-r.amount:r.amount)); if(r.type==='expense') sumOut+=r.amount; else sumIn+=r.amount; }
                    else if (ds==='income_over_time'){ if(r.type!=='income') return; const k=groupKey(dt,el.grp.value); map.set(k,(map.get(k)||0)+r.amount); sumIn+=r.amount; }
                    else if (ds==='expense_over_time'){ if(r.type!=='expense') return; const k=groupKey(dt,el.grp.value); map.set(k,(map.get(k)||0)+r.amount); sumOut+=r.amount; }
                    else if (ds==='top_categories_exp'){ if(r.type!=='expense') return; map.set(r.category,(map.get(r.category)||0)+r.amount); sumOut+=r.amount; }
                    else if (ds==='top_categories_inc'){ if(r.type!=='income') return; map.set(r.category,(map.get(r.category)||0)+r.amount); sumIn+=r.amount; }
                });

                let entries=[...map.entries()];
                if (ds.startsWith('top_')) {
                    const N = el.top.value==='all' ? entries.length : parseInt(el.top.value,10);
                    entries = entries.sort((a,b)=>b[1]-a[1]).slice(0,N);
                } else if (ds.endsWith('_over_time')) {
                    entries = entries.sort((a,b)=>(''+a[0]).localeCompare(''+b[0]));
                } else entries = entries.sort((a,b)=>b[1]-a[1]);

                $("#sumIn").textContent  = sumIn.toLocaleString('vi-VN');
                $("#sumOut").textContent = '-'+sumOut.toLocaleString('vi-VN');
                $("#sumBal").textContent = (sumIn-sumOut).toLocaleString('vi-VN');

                return {
                    labels: entries.length? entries.map(e=>e[0]) : ['Không có dữ liệu'],
                    data:   entries.length? entries.map(e=>e[1]) : [0]
                };
            }

            let chart;
            function render(){
                const ctx = document.getElementById('chart').getContext('2d');
                const r = compute();
                // Nếu Chart không tải được: vẫn cập nhật số tổng, bỏ qua vẽ chart
                if (!window.Chart) { ctx.font="14px sans-serif"; ctx.fillText("Không tải được Chart.js (offline/CDN bị chặn).", 16, 24); return; }

                const type = el.type.value;
                const base = el.color.value;
                const grad = ctx.createLinearGradient(0,0,0,300); grad.addColorStop(0,base); grad.addColorStop(1,"#ffffff");
                const ds  = {label: el.ds.options[el.ds.selectedIndex]?.text || '', data:r.data,
                    borderWidth:2, borderColor:base, backgroundColor: type==='bar'?grad:base, fill:type==='bar'};
                if(chart) chart.destroy();
                chart = new Chart(ctx,{type, data:{labels:r.labels, datasets:[ds]},
                    options:{responsive:true, maintainAspectRatio:false, plugins:{legend:{display:type!=='bar'}}}});
            }

            // events
            el.apply.onclick = render;
            el.exp.onclick   = () => { const c=document.getElementById('chart'); const a=document.createElement('a'); a.href=c.toDataURL('image/png'); a.download='analytics.png'; a.click(); };
            el.type.onchange = () => { fillDatasetOptions(); render(); };
            el.ds.onchange   = () => { updateTopState(); render(); };

            // Khởi tạo lần đầu
            fillDatasetOptions();   // đồng bộ options theo loại biểu đồ hiện tại
            render();               // vẽ ngay
        }
    })();
</script>
