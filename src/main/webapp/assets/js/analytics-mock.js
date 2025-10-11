// window.BB_MOCK: trả về data “giống thật” cho 2 use-case
window.BB_MOCK = {
    timeSeries({ from, to, group, type }) {
        // ví dụ trả 1 cột duy nhất trong tháng
        return {
            points: [{ label: '2025-09', value: 2155000 }],
            sumIn: 2500000,
            sumOut: 345000,
            balance: 2500000 - 345000,
        };
    },
    topCategory({ top, type }) {
        const base = [
            { label: 'Ăn uống', value: 950000 },
            { label: 'Đi lại', value: 450000 },
            { label: 'Giáo dục', value: 320000 },
            { label: 'Mua sắm', value: 280000 },
            { label: 'Sức khoẻ', value: 150000 },
            { label: 'Giải trí', value: 90000 },
        ];
        const pick = base.slice(0, top);
        const sumOut = pick.reduce((s, x) => s + x.value, 0);
        return {
            parts: pick,
            sumIn: 0,
            sumOut,
            balance: 0 - sumOut,
        };
    }
};
