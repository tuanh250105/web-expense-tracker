(() => {
    const CTX = window.BB_CTX || "";
    const USER_ID = window.BB_USER_ID || "00000000-0000-0000-0000-000000000001";

    // ---- DOM ----
    const wheelEl   = document.getElementById("rw-wheel");
    const spinBtn   = document.getElementById("rw-spin");
    const awardBtn  = document.getElementById("rw-award");
    const pointsEl  = document.getElementById("rw-points");
    const historyEl = document.getElementById("rw-history");
    const resultEl  = document.getElementById("rw-result");
    if (!wheelEl || !spinBtn || !awardBtn || !pointsEl || !historyEl || !resultEl) return;

    // ---- Canvas setup ----
    wheelEl.width = 420;
    wheelEl.height = 420;
    const ctx = wheelEl.getContext("2d");
    const r = wheelEl.width / 2, cx = r, cy = r;

    const prizes = [
        { code: "VOUCHER5", label: "🎟️ Voucher 5%", color: "#A0C4FF", weight: 30 },
        { code: "THEME",    label: "📈 Theme",        color: "#FFC6FF", weight: 20 },
        { code: "BADGE",    label: "🌟 Badge",        color: "#BDB2FF", weight: 20 },
        { code: "STICKER",  label: "🎁 Sticker",      color: "#CAFFBF", weight: 20 },
        { code: "EXTRA",    label: "🍀 Lượt quay +",  color: "#FDFFB6", weight: 10 }
    ];
    const segRad = 2 * Math.PI / prizes.length;
    const bag = [];
    prizes.forEach(p => { for (let i = 0; i < Math.max(1, p.weight); i++) bag.push(p); });

    // ---- Vẽ vòng quay ----
    function draw(angle = 0) {
        ctx.clearRect(0, 0, wheelEl.width, wheelEl.height);

        // ==== Vẽ các lát ====
        prizes.forEach((p, i) => {
            ctx.beginPath();
            ctx.moveTo(cx, cy);
            ctx.fillStyle = p.color;
            // Xoay vòng quay sao cho 0° ở 12h
            ctx.arc(cx, cy, r, angle + i * segRad - Math.PI / 2, angle + (i + 1) * segRad - Math.PI / 2);
            ctx.fill();

            ctx.save();
            ctx.translate(cx, cy);
            ctx.rotate(angle + i * segRad + segRad / 2 - Math.PI / 2);
            ctx.textAlign = "right";
            ctx.fillStyle = "#111827";
            ctx.font = "16px Poppins, system-ui, sans-serif";
            ctx.fillText(p.label, r - 18, 6);
            ctx.restore();
        });

        // ==== Vẽ cây kim chỉa xuống (ở hướng 12h) ====
        ctx.save();
        ctx.translate(cx, cy);
        ctx.rotate(-Math.PI / 2); // 💡 đây là dòng fix quan trọng — xoay kim lên trên
        ctx.beginPath();
        ctx.moveTo(0, -r - 5);   // đỉnh ngoài
        ctx.lineTo(0, -r + 25);  // vào trong bánh
        ctx.strokeStyle = "#0ea5e9";
        ctx.lineWidth = 6;
        ctx.lineCap = "round";
        ctx.shadowColor = "rgba(14,165,233,0.4)";
        ctx.shadowBlur = 4;
        ctx.stroke();
        ctx.restore();
    }

    draw();

    // ---- Hiển thị popup (toast) ----
    function showToast(msg, color = "#16a34a") {
        const t = document.getElementById("rw-toast");
        if (!t) return;
        t.textContent = msg;
        t.style.background = color;
        t.classList.add("show");
        setTimeout(() => t.classList.remove("show"), 1500);
    }

    // ---- LocalStorage fallback ----
    const ls = {
        get(k, def) { try { return JSON.parse(localStorage.getItem(k) ?? JSON.stringify(def)); } catch { return def; } },
        set(k, v)   { localStorage.setItem(k, JSON.stringify(v)); }
    };

    // ---- API wrappers ----
    async function apiGetPoints() {
        try {
            const res = await fetch(`${CTX}/api/rewards/points?userId=${encodeURIComponent(USER_ID)}`);
            if (res.ok) { const j = await res.json(); return (j.points | 0); }
        } catch {}
        return ls.get("rw_points", 40);
    }

    async function apiRecent(limit = 10) {
        try {
            const res = await fetch(`${CTX}/api/rewards/recent?userId=${encodeURIComponent(USER_ID)}&limit=${limit}`);
            if (res.ok) return await res.json();
        } catch {}
        return (ls.get("rw_hist", [])).map(x => ({
            createdAt: new Date(x.time).toISOString(),
            prizeLabel: x.prize_label,
            prizeCode: x.prize_code
        }));
    }

    async function apiClaimable() {
        try {
            const res = await fetch(`${CTX}/api/rewards/claimable?userId=${encodeURIComponent(USER_ID)}`);
            if (res.ok) return await res.json();
        } catch {}
        return null;
    }

    async function apiClaimOne() {
        try {
            const res = await fetch(`${CTX}/api/rewards/claim-one?userId=${encodeURIComponent(USER_ID)}`, { method: "POST" });
            if (res.ok) return await res.json();
        } catch {}
        const cur = await apiGetPoints();
        ls.set("rw_points", cur + 5);
        return { added: 5, points: cur + 5, remaining: 0 };
    }

    async function apiSpin() {
        try {
            const res = await fetch(`${CTX}/api/rewards/spin?userId=${encodeURIComponent(USER_ID)}`, { method: "POST" });
            if (res.ok) return await res.json();
            if (res.status === 400) return { error: "not_enough_points" };
        } catch {}
        let p = await apiGetPoints(); if (p < 20) return { error: "not_enough_points" };
        ls.set("rw_points", p - 20);
        const pick = bag[Math.floor(Math.random() * bag.length)];
        const hist = ls.get("rw_hist", []);
        hist.unshift({ time: Date.now(), prize_code: pick.code, prize_label: pick.label });
        ls.set("rw_hist", hist);
        return { prizeCode: pick.code, prizeLabel: pick.label, spent: 20 };
    }

    // ---- UI helpers ----
    async function refreshPointsAndHistory() {
        pointsEl.textContent = await apiGetPoints();
        const items = await apiRecent(10);
        historyEl.innerHTML = "";
        items.forEach(x => {
            const when = x.createdAt ? new Date(x.createdAt).toLocaleString() :
                (x.time ? new Date(x.time).toLocaleString() : "");
            const li = document.createElement("li");
            li.textContent = `${when} – ${x.prizeLabel || x.prize_label || x.prizeCode || x.prize_code}`;
            historyEl.appendChild(li);
        });
    }

    async function updateAwardButton() {
        const data = await apiClaimable();
        if (data && typeof data.remaining === "number") {
            const left = data.remaining | 0;
            if (left > 0) {
                awardBtn.textContent = `Nhận thưởng (+5) — còn ${left} lượt`;
                awardBtn.disabled = false;
            } else {
                awardBtn.textContent = `Nhận thưởng (+5) — hết lượt`;
                awardBtn.disabled = true;
                resultEl.textContent = "🎯 Bạn đã nhận hết lượt thưởng cho ngân sách đạt mục tiêu.";
            }
        } else {
            awardBtn.textContent = `Nhận thưởng (+5)`;
            awardBtn.disabled = false;
        }
    }

    // ---- Sự kiện nút "Nhận thưởng" ----
    awardBtn.addEventListener("click", async () => {
        const cur = await apiGetPoints();
        if (false) {
            alert("⚠️ Bạn chưa đủ điểm để nhận thưởng. Hãy đạt ngân sách để nhận thêm điểm!");
            return;
        }
        awardBtn.disabled = true;
        try {
            const r = await apiClaimOne();
            if ((r.added | 0) > 0) {
                resultEl.textContent = `+${r.added} điểm`;
                showToast(`+${r.added} điểm thưởng 🎉`);
            }
            if (typeof r.points === "number") pointsEl.textContent = r.points;
        } finally {
            await updateAwardButton();
        }
    });

    // ---- Sự kiện "Quay" ----
    spinBtn.addEventListener("click", async () => {
        const resp = await apiSpin();
        if (resp.error === "not_enough_points") {
            alert("Chưa đủ 20 điểm.");
            return;
        }

        const code  = resp.prizeCode  || resp.prize_code;
        const label = resp.prizeLabel || resp.prize_label || code;
        const idx   = prizes.findIndex(p => p.code === code);
        if (idx < 0) {
            alert("PRIZE_NOT_FOUND_ON_WHEEL");
            return;
        }

        const segDeg = 360 / prizes.length;

        // 🔧 Đây là công thức đã bù hướng canvas + hướng xoay vòng quay
        const stopAt = 6 * 360 + (360 - (idx * segDeg + segDeg / 2)) - 90;

        animateTo(stopAt, 4000, async () => {
            resultEl.textContent = "🎁 Bạn nhận: " + label;
            showToast(`🎉 ${label}`);
            await refreshPointsAndHistory();
        });
    });

    function animateTo(targetDeg, duration, onDone) {
        const start = performance.now();
        const startDeg = 0;

        function frame(t) {
            const p = Math.min(1, (t - start) / duration);
            const ease = 1 - Math.pow(1 - p, 3);
            const deg = startDeg + (targetDeg - startDeg) * ease;
            draw((deg * Math.PI) / 180);
            if (p < 1) requestAnimationFrame(frame);
            else onDone && onDone();
        }

        requestAnimationFrame(frame);
    }


    // ---- Khi DOM sẵn sàng ----
    document.addEventListener("DOMContentLoaded", async () => {
        await refreshPointsAndHistory();
        await updateAwardButton();
    });
})();
