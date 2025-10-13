// /assets/js/modules/apiService.js

/**
 * Hàm fetch API chung để xử lý lỗi và JSON.
 * Tự động thêm contextPath vào mỗi yêu cầu.
 * @param {string} url - URL tương đối của API (ví dụ: '/api/groups').
 * @param {object} options - Tùy chọn cho fetch (method, headers, body).
 * @returns {Promise<any>} - Dữ liệu JSON từ response.
 */
export async function apiFetch(url, options = {}) {
    const ctxPath = window.contextPath || '';
    const defaultHeaders = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    };

    options.headers = { ...defaultHeaders, ...options.headers };

    const response = await fetch(`${ctxPath}${url}`, options);

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({ error: `Lỗi không xác định: ${response.status}` }));
        throw new Error(errorData.error);
    }

    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
        return response.json();
    }
    return { success: true };
}