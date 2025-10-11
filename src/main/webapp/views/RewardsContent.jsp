<%@ page contentType="text/html; charset=UTF-8" %>
<h1 style="margin:0 0 14px;font-size:34px;font-family:Poppins,system-ui">🎉 <b>Vòng quay may mắn</b></h1>
<p style="margin:0 0 16px;color:#475569;font-family:Poppins,system-ui">
    Mỗi <b>20 điểm</b> = 1 lượt quay. Đạt budget (đã hết hạn & chi ≤ hạn mức) sẽ <b>+5 điểm</b>.
</p>

<style>
    .rw-hero{display:flex;justify-content:space-between;align-items:center;color:#fff;
        background:linear-gradient(135deg,#00c6ff,#6a11cb,#2575fc); border-radius:16px;padding:14px 16px;margin-bottom:14px;
        box-shadow:0 12px 28px rgba(37,117,252,.25); font-family:Poppins,system-ui}
    .rw-right{display:flex;gap:12px;align-items:center;font-size:18px}
    .rw-grid{display:grid;grid-template-columns:1fr 1fr;gap:14px; font-family:Poppins,system-ui}
    @media(max-width:1000px){.rw-grid{grid-template-columns:1fr}}
    .rw-card{background:#fff;border-radius:16px;padding:16px;box-shadow:0 10px 22px rgba(0,0,0,.06)}
    .center{text-align:center}
    .rw-btn{padding:14px 22px;border:none;border-radius:12px;font-weight:800;cursor:pointer;font-family:Poppins,system-ui;font-size:16px}
    .rw-btn.primary{color:#fff;background:linear-gradient(135deg,#00c6ff,#6a11cb)}
    .rw-btn.ghost{background:#eef2ff;color:#1e293b;border:1px solid #c7d2fe}
    #rw-wheel{display:block;margin:10px auto 14px auto;background:radial-gradient(circle at 50% 50%,#fff 0,#f8fafc 60%,#e2e8f0 100%);
        border-radius:50%;box-shadow:0 12px 24px rgba(0,0,0,.12)}
    /* mũi tên đặt phía TRÊN, chĩa xuống đúng “đỉnh Bắc” (-90deg) */
    .pointer{width:0;height:0;border-left:14px solid transparent;border-right:14px solid transparent;border-top:18px solid #1d4ed8;margin:0 auto}
    .result{font-weight:800;text-align:center}
</style>

<div class="rw-hero">
    <div><b>Điểm hiện có:</b> <span id="rw-points" style="font-size:22px">0</span></div>
    <div class="rw-right"><button id="rw-award" class="rw-btn ghost">Tính điểm từ Budgets</button></div>
</div>

<div class="rw-grid">
    <div class="rw-card">
        <div class="pointer"></div>
        <canvas id="rw-wheel" width="360" height="360"></canvas>
        <div class="center"><button id="rw-spin" class="rw-btn primary">Quay ngay (−20 điểm)</button></div>
        <p id="rw-result" class="result"></p>
    </div>
    <div class="rw-card">
        <h3 style="margin-top:0">Lịch sử quay</h3>
        <ul id="rw-history" style="padding-left:18px;margin:0"></ul>
    </div>
</div>

<script>
    (() => {
        const ctxPath = '<%=request.getContextPath()%>';
        // ===== Wheel drawing =====
        const wheel = document.getElementById('rw-wheel');
        const ctx = wheel.getContext('2d'); const R=wheel.width/2, CX=R, CY=R;
        const prizes=[
            {code:"VOUCHER5", label:"🎟️ Voucher 5%", color:"#A0C4FF", weight:30},
            {code:"THEME",    label:"📈 Theme",       color:"#FFC6FF", weight:20},
            {code:"BADGE",    label:"🌟 Badge",       color:"#BDB2FF", weight:20},
            {code:"STICKER",  label:"🎁 Sticker",     color:"#CAFFBF", weight:20},
            {code:"EXTRA",    label:"🍀 Lượt quay +", color:"#FDFFB6", weight:10}
        ];
        const SEG = 2*Math.PI/prizes.length;
        let currentAngle = 0;   // góc offset hiện tại (rad)

        function draw(angle=currentAngle){
            currentAngle = angle;
            ctx.clearRect(0,0,wheel.width,wheel.height);
            for(let i=0;i<prizes.length;i++){
                ctx.beginPath(); ctx.moveTo(CX,CY);
                ctx.fillStyle=prizes[i].color;
                ctx.arc(CX,CY,R, angle + i*SEG, angle + (i+1)*SEG);
                ctx.fill();

                // label trên cung
                ctx.save();
                ctx.translate(CX,CY);
                ctx.rotate(angle + i*SEG + SEG/2);
                ctx.textAlign="right"; ctx.fillStyle="#111827"; ctx.font="16px Poppins";
                ctx.fillText(prizes[i].label, R-18, 6);
                ctx.restore();
            }
        }
        draw(0);

        // ===== Storage & API fallbacks =====
        const LS = {
            get(k,def){ try{return JSON.parse(localStorage.getItem(k)??JSON.stringify(def))}catch(e){return def}},
            set(k,v){ localStorage.setItem(k, JSON.stringify(v)) }
        };
        async function getPoints(){
            try{ const r=await fetch(ctxPath+'/api/rewards/points'); if(r.ok){const j=await r.json(); return j.points|0;} }catch(e){}
            return LS.get('rw_points', 40);
        }
        async function setPoints(v){ LS.set('rw_points', Math.max(0,v)); }
        function addHistory(item){ const h=LS.get('rw_hist',[]); h.unshift(item); LS.set('rw_hist',h); }

        async function refresh(){
            document.getElementById('rw-points').textContent = await getPoints();
            const ul=document.getElementById('rw-history'); ul.innerHTML="";
            (LS.get('rw_hist',[])).slice(0,10).forEach(x=>{
                const li=document.createElement('li'); li.textContent=new Date(x.time).toLocaleString()+" – "+x.label; ul.appendChild(li);
            });
        }
        refresh();

        // Weighted bag
        const bag=[]; prizes.forEach((p,idx)=>{for(let i=0;i<Math.max(1,p.weight);i++) bag.push(idx)});

        // Easing animation
        function easeOutCubic(t){ return 1- Math.pow(1-t,3); }

        // Spin to EXACT segment under the top pointer (-90deg)
        async function spin() {
            let pts = await getPoints();
            if (pts < 20){ alert("Chưa đủ 20 điểm."); return; }

            // 1) pick chỉ số theo trọng số
            const pickIndex = bag[Math.floor(Math.random()*bag.length)];

            // 2) tính góc dừng: muốn “trung tâm segment pick” nằm tại -90°
            const ANG_TOP = -Math.PI/2;
            const targetAngleBase = ANG_TOP - (pickIndex + 0.5)*SEG; // offset cần đạt
            // thêm 4 vòng quay để mượt
            const finalAngle = targetAngleBase + 4*2*Math.PI;

            // 3) animate từ currentAngle -> finalAngle (thời gian ~2.2s)
            const startAngle = currentAngle;
            const delta = finalAngle - startAngle;
            const dur = 2200;
            const t0 = performance.now();
            document.getElementById('rw-spin').disabled = true;
            document.getElementById('rw-result').textContent = ""; // KHÔNG hiển thị sớm nữa

            function frame(t){
                const p = Math.min(1, (t - t0) / dur);
                const eased = easeOutCubic(p);
                draw(startAngle + delta * eased);
                if (p < 1) requestAnimationFrame(frame);
                else afterStop(pickIndex);
            }
            requestAnimationFrame(frame);

            async function afterStop(idx){
                // 4) chỉ khi dừng mới trừ điểm, cập nhật lịch sử & hiển thị kết quả
                await setPoints(pts - 20);
                const priz = prizes[idx];
                addHistory({time:Date.now(), label:priz.label});
                document.getElementById('rw-result').textContent = "Bạn nhận: " + priz.label;
                document.getElementById('rw-spin').disabled = false;
                refresh();
            }
        }

        document.getElementById('rw-spin').onclick = spin;
        document.getElementById('rw-award').onclick = async ()=>{
            await setPoints((await getPoints()) + 5);  // mock (+5 nếu chưa có API)
            refresh();
        };
    })();
</script>
