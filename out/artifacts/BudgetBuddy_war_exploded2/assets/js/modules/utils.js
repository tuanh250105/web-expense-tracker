// /assets/js/modules/utils.js

/**
 * Định dạng một số thành chuỗi tiền tệ VND.
 * @param {number | string} amount - Số tiền.
 * @param {boolean} [isRawAmount=false] - Nếu true, không nhân với 1,000,000.
 * @returns {string} Chuỗi tiền tệ đã định dạng.
 */
export function formatCurrency(amount, isRawAmount = false) {
    const numericAmount = Number(amount) || 0;
    const value = isRawAmount ? numericAmount : numericAmount * 1000000;
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(value);
}