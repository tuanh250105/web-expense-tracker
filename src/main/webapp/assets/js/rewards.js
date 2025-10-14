(() => {
    const CTX = window.BB_CTX || (window.location.pathname.split('/')[1] ? `/${window.location.pathname.split('/')[1]}` : "");
    const USER_ID = window.BB_USER_ID; // fallback test user

    const wheelEl = document.getElementById("rw-wheel");
    const spinBtn = document.getElementById("rw-spin");
    const awardBtn = document.getElementById("rw-award");
    const pointsEl = document.getElementById("rw-points");
    const historyEl = document.getElementById("rw-history");
    const resultEl = document.getElementById("rw-result");
    if (!wheelEl || !spinBtn || !awardBtn || !pointsEl || !historyEl || !resultEl) return;

    wheelEl.width = 420;
    wheelEl.height = 420;
    const ctx = wheelEl.getContext("2d");
    const r = wheelEl.width / 2, cx = r, cy = r;

    const prizes = [
        { code: "VOUCHER5", label: "🎁", color: "#A0C4FF", weight: 25 },
        { code: "THEME", label: "🎁", color: "#FFC6FF", weight: 20 },
        { code: "BADGE", label: "🎁", color: "#BDB2FF", weight: 20 },
        { code: "STICKER", label: "🎁", color: "#CAFFBF", weight: 15 },
        { code: "EXTRA", label: "🍀", color: "#FDFFB6", weight: 20 }
    ];
    const segRad = 2 * Math.PI / prizes.length;
    const bag = [];
    prizes.forEach(p => { for (let i = 0; i < Math.max(1, p.weight); i++) bag.push(p); });

    function draw(angle = 0) {
        ctx.clearRect(0, 0, wheelEl.width, wheelEl.height);
        prizes.forEach((p, i) => {
            ctx.beginPath();
            ctx.moveTo(cx, cy);
            ctx.fillStyle = p.color;
            ctx.arc(cx, cy, r, angle + i * segRad - Math.PI / 2, angle + (i + 1) * segRad - Math.PI / 2);
            ctx.fill();

            ctx.save();
            ctx.translate(cx, cy);
            ctx.rotate(angle + i * segRad + segRad / 2 - Math.PI / 2);
            ctx.textAlign = "center";
            ctx.fillStyle = "#111";
            ctx.font = "28px Poppins, system-ui";
            ctx.fillText(p.label, r - 40, 10);
            ctx.restore();
        });

        // kim chỉ
        ctx.save();
        ctx.translate(cx, cy);
        ctx.rotate(-Math.PI / 2);
        ctx.beginPath();
        ctx.moveTo(0, -r - 5);
        ctx.lineTo(0, -r + 25);
        ctx.strokeStyle = "#0ea5e9";
        ctx.lineWidth = 6;
        ctx.lineCap = "round";
        ctx.shadowColor = "rgba(14,165,233,0.4)";
        ctx.shadowBlur = 4;
        ctx.stroke();
        ctx.restore();
    }

    draw();

    function showToast(msg, color = "#16a34a") {
        const t = document.getElementById("rw-toast");
        if (!t) return;
        t.textContent = msg;
        t.style.background = color;
        t.classList.add("show");
        setTimeout(() => t.classList.remove("show"), 1500);
    }

    // ======================== API ========================
    async function apiGetPoints() {
        try {
            const res = await fetch(`${CTX}/api/rewards/points?userId=${USER_ID}`);
            if (res.ok) {
                const j = await res.json();
                return (j.points | 0);
            }
        } catch (err) {
            console.error(" API /points error:", err);
        }
        return ls.get("rw_points", 40);
    }

    async function apiRecent(limit = 10) {
        try {
            const res = await fetch(`${CTX}/api/rewards?userId=${USER_ID}`);
            if (res.ok) {
                const j = await res.json();
                return j.recent || [];
            }
        } catch (err) {
            console.error("❌ API /rewards error:", err);
        }
        return [];
    }


    async function apiClaimable() {
        try {
            const res = await fetch(`${CTX}/api/rewards/claimable?userId=${USER_ID}`);
            if (res.ok) return await res.json();
        } catch (err) {
            console.error(" API /claimable error:", err);
        }
        return null;
    }

    async function apiClaimOne() {
        try {
            const res = await fetch(`${CTX}/api/rewards/claim-one?userId=${USER_ID}`, { method: "POST" });
            if (res.ok) return await res.json();
        } catch { }
        return { added: 0, points: 0, remaining: 0 };
    }

    async function apiSpin() {
        try {
            const res = await fetch(`${CTX}/api/rewards/spin?userId=${USER_ID}`, { method: "POST" });
            if (res.ok) return await res.json();
            if (res.status === 400) return { error: "not_enough_points" };
        } catch { }
        return { error: "network_error" };
    }
    // ====================================================

    async function refreshPointsAndHistory() {
        try {
            pointsEl.textContent = await apiGetPoints();
            const items = await apiRecent(10);
            console.log("📜 Reward history from API:", items);

            historyEl.innerHTML = "";

            if (!Array.isArray(items) || items.length === 0) {
                const li = document.createElement("li");
                li.textContent = "Đạt budget để được 5 điểm thưởng";
                li.style.color = "#9ca3af";
                li.style.fontStyle = "italic";
                historyEl.appendChild(li);
                return;
            }

            items.forEach(x => {
                const when = x.createdAt ? new Date(x.createdAt).toLocaleString("vi-VN") : "";
                const label = x.prizeLabel || x.prizeCode || "(Không xác định)";
                const cost = x.pointsSpent > 0 ? `(-${x.pointsSpent} điểm)` : `(+${Math.abs(x.pointsSpent)} điểm)`;

                const li = document.createElement("li");
                li.innerHTML = `
                <div style="display:flex; justify-content:space-between; align-items:center; gap:8px;">
                    <span>${when}</span>
                    <span>${label}</span>
                    <span style="color:#6b7280; font-size:13px;">${cost}</span>
                </div>
            `;
                historyEl.appendChild(li);
            });
        } catch (err) {
            console.error(" Lỗi khi tải lịch sử:", err);
            historyEl.innerHTML = "<li style='color:red'>Không tải được lịch sử</li>";
        }
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

    awardBtn.addEventListener("click", async () => {
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

    // 🎯 Khi quay
    spinBtn.addEventListener("click", async () => {
        const resp = await apiSpin();
        if (resp.error === "not_enough_points") {
            alert("Chưa đủ 20 điểm.");
            return;
        }
        if (resp.error === "network_error") {
            alert("Không kết nối được máy chủ.");
            return;
        }

        const code = resp.prizeCode;
        const idx = prizes.findIndex(p => p.code === code);
        if (idx < 0) {
            alert("PRIZE_NOT_FOUND_ON_WHEEL");
            return;
        }

        const segDeg = 360 / prizes.length;
        const stopAt = 6 * 360 + (360 - (idx * segDeg + segDeg / 2)) - 90;

        const tips = {
            "VOUCHER5": "💡 Mẹo: Tiết kiệm ít nhất 20% thu nhập mỗi tháng!",
            "THEME": "📊 Ghi chép chi tiêu mỗi ngày giúp bạn kiểm soát tốt hơn.",
            "BADGE": "🧘‍♀️ Cẩn thận khi mua sắm lúc đang buồn, dễ vượt kế hoạch!",
            "STICKER": "🔐 Nhớ bảo mật tài khoản, không chia sẻ mật khẩu nhé!",
            "EXTRA": "🍀 Bạn nhận được thêm 1 lượt quay miễn phí!"
        };

        animateTo(stopAt, 4000, async () => {
            const msg = tips[code] || "🎁 Chúc mừng! Bạn đã nhận được phần thưởng!";
            resultEl.innerHTML = `<div style="
                background: #eef6ff;
                color: #0c4a6e;
                border-radius: 12px;
                padding: 10px 16px;
                margin-top: 12px;
                font-size: 16px;
                font-weight: 600;
                text-align: center;
                box-shadow: 0 2px 6px rgba(0,0,0,0.1);
            ">${msg}</div>`;
            showToast(msg);
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

    document.addEventListener("DOMContentLoaded", async () => {
        await refreshPointsAndHistory();
        await updateAwardButton();
    });
})();
