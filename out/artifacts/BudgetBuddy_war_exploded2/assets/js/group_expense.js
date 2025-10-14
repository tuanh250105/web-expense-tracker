// /assets/js/group_expense.js

// --- Import các module ---
import { apiFetch } from './modules/apiService.js';
import { renderGroupPage, showModal, closeModal } from './modules/ui.js';
import { formatCurrency } from './modules/utils.js';

// --- State của trang ---
let dashboardData = {
    groupExpenses: { details: [], total: 0 },
    transactions: []
};

// --- Khởi tạo ---
//document.addEventListener('DOMContentLoaded', initGroupPage);

function initGroupPage() {
    const listEl = document.getElementById("groupList");
    if (listEl) {
        listEl.innerHTML = '<div class="loading">Đang tải dữ liệu nhóm...</div>';
    }

    apiFetch('/dashboard-data?period=month')
        .then(json => {
            dashboardData = json;
            renderGroupPage(dashboardData.groupExpenses);
            addGroupEventListeners();

            document.querySelector('.btn-add-group').addEventListener('click', handleCreateGroup);
        })
        .catch(err => {
            console.error("❌ Lỗi khi tải dữ liệu nhóm:", err);
            if (listEl) {
                listEl.innerHTML = `<div class="error">Không thể tải dữ liệu: ${err.message}</div>`;
            }
        });
}

// --- Xử lý sự kiện ---

function addGroupEventListeners() {
    document.querySelectorAll('.btn-manage').forEach(button =>
        button.addEventListener('click', (e) => handleManageMembers(e.target.dataset.groupId))
    );
    document.querySelectorAll('.btn-delete').forEach(button =>
        button.addEventListener('click', (e) => handleDeleteGroup(e.target.dataset.groupId))
    );
    document.querySelectorAll('.btn-view').forEach(button =>
        button.addEventListener('click', (e) => handleViewGroup(e.target.dataset.groupId))
    );
}

function handleCreateGroup() {
    const content = `
        <div class="modal-form">
            <label for="groupName">Tên nhóm</label>
            <input type="text" id="groupName" placeholder="Ví dụ: Tiền ăn trưa văn phòng" required>
            <label for="groupDesc">Mô tả</label>
            <textarea id="groupDesc" placeholder="Mô tả ngắn về mục đích của nhóm"></textarea>
        </div>`;
    const footer = `
        <button class="modal-btn-cancel">Hủy</button>
        <button class="modal-btn-confirm" id="confirmCreateGroup">Tạo Nhóm</button>`;
    showModal("Tạo Nhóm Mới", content, footer);

    document.getElementById('confirmCreateGroup').addEventListener('click', async () => {
        const name = document.getElementById('groupName').value.trim();
        const description = document.getElementById('groupDesc').value.trim();
        if (!name) return alert("Vui lòng nhập tên nhóm.");

        try {
            const newGroup = await apiFetch('/api/groups', {
                method: 'POST',
                body: JSON.stringify({ name, description })
            });

            if (!dashboardData.groupExpenses) dashboardData.groupExpenses = { details: [], total: 0 };
            if (!dashboardData.groupExpenses.details) dashboardData.groupExpenses.details = [];

            dashboardData.groupExpenses.details.push(newGroup);
            dashboardData.groupExpenses.total = (dashboardData.groupExpenses.total || 0) + (newGroup.totalAmount || 0);

            renderGroupPage(dashboardData.groupExpenses);
            addGroupEventListeners(); // Gắn lại sự kiện sau khi render
            closeModal();
        } catch (error) {
            console.error('Lỗi khi tạo nhóm:', error);
            alert(`Không thể tạo nhóm: ${error.message}`);
        }
    });
}

function handleDeleteGroup(groupId) {
    const group = dashboardData.groupExpenses.details.find(g => g.id === groupId);
    if (!group) return;

    const content = `<p>Bạn có chắc chắn muốn xóa nhóm <strong>"${group.name}"</strong> không?</p>`;
    const footer = `<button class="modal-btn-cancel">Hủy</button><button class="modal-btn-confirm-delete" id="confirmDeleteGroup">Xóa</button>`;
    showModal("Xác nhận xóa nhóm", content, footer);

    document.getElementById('confirmDeleteGroup').addEventListener('click', async () => {
        try {
            await apiFetch(`/api/groups/${groupId}`, { method: 'DELETE' });
            dashboardData.groupExpenses.details = dashboardData.groupExpenses.details.filter(g => g.id !== groupId);

            // Cập nhật lại tổng tiền
            dashboardData.groupExpenses.total -= (group.totalAmount || 0);

            renderGroupPage(dashboardData.groupExpenses);
            addGroupEventListeners(); // Gắn lại sự kiện
            closeModal();
        } catch (error) {
            console.error(`Lỗi khi xóa nhóm ${groupId}:`, error);
            alert(`Không thể xóa nhóm: ${error.message}`);
        }
    });
}

function handleManageMembers(groupId) {
    const group = dashboardData.groupExpenses.details.find(g => g.id === groupId);
    if (!group) return;

    const membersList = group.members.map(m => `
        <li data-user-id="${m.id}">
            <span>${m.name}</span>
            ${!m.isOwner && group.isCurrentUserOwner ? `<button class="modal-btn-delete-user" data-user-id="${m.id}">Xóa</button>` : ''}
        </li>`).join('');

    const content = `
        <h2>Quản lý: ${group.name}</h2>
        <h4>Thành viên hiện tại</h4>
        <ul class="modal-member-list interactive">${membersList}</ul><hr>
        <h4>Thêm thành viên mới</h4>
        <div class="modal-add-member">
            <input type="text" id="newMemberSearchInput" placeholder="Nhập tên để tìm kiếm..." autocomplete="off"/>
        </div>
        <div id="userSearchResults"></div>`;
    showModal("Quản lý thành viên", content);
    addModalManageEventListeners(groupId);
}

function addModalManageEventListeners(groupId) {
    // Xóa thành viên
    document.querySelectorAll('.modal-btn-delete-user').forEach(button => {
        button.addEventListener('click', async (e) => {
            const userId = e.target.dataset.userId;
            const group = dashboardData.groupExpenses.details.find(g => g.id === groupId);
            const member = group.members.find(m => m.id === userId);
            if (confirm(`Bạn có chắc muốn xóa "${member.name}" khỏi nhóm?`)) {
                try {
                    await apiFetch(`/api/groups/${groupId}/members/${userId}`, { method: 'DELETE' });
                    group.members = group.members.filter(m => m.id !== userId);
                    closeModal();
                    handleManageMembers(groupId);
                    renderGroupPage(dashboardData.groupExpenses);
                    addGroupEventListeners();
                } catch (error) {
                    alert(`Không thể xóa thành viên: ${error.message}`);
                }
            }
        });
    });

    // Thêm thành viên
    const searchInput = document.getElementById('newMemberSearchInput');
    const searchResultsContainer = document.getElementById('userSearchResults');
    let searchTimeout;

    searchInput.addEventListener('input', () => {
        clearTimeout(searchTimeout);
        const query = searchInput.value.trim();
        if (query.length < 2) {
            searchResultsContainer.innerHTML = '';
            return;
        }
        searchResultsContainer.innerHTML = '<div class="search-result-item">Đang tìm...</div>';

        searchTimeout = setTimeout(async () => {
            try {
                const users = await apiFetch(`/api/users/search?name=${encodeURIComponent(query)}`);
                const group = dashboardData.groupExpenses.details.find(g => g.id === groupId);

                searchResultsContainer.innerHTML = users.length === 0
                    ? '<div class="search-result-item">Không tìm thấy người dùng nào.</div>'
                    : users.map(user => {
                        const isMember = group.members.some(m => m.id === user.id);
                        return `
                        <div class="search-result-item" data-user-id="${user.id}" data-user-name="${user.name}">
                            <span>${user.name}</span>
                            <button class="btn-add-member" ${isMember ? 'disabled' : ''}>${isMember ? 'Đã có' : 'Thêm'}</button>
                        </div>`;
                    }).join('');

                document.querySelectorAll('.btn-add-member:not([disabled])').forEach(button => {
                    button.addEventListener('click', async (e) => {
                        const userItem = e.target.closest('.search-result-item');
                        const userId = userItem.dataset.userId;
                        const userName = userItem.dataset.userName;
                        try {
                            await apiFetch(`/api/groups/${groupId}/members`, {
                                method: 'POST',
                                body: JSON.stringify({ userId })
                            });
                            group.members.push({ id: userId, name: userName });
                            closeModal();
                            handleManageMembers(groupId);
                            renderGroupPage(dashboardData.groupExpenses);
                            addGroupEventListeners();
                        } catch (error) {
                            alert(`Không thể thêm thành viên: ${error.message}`);
                        }
                    });
                });
            } catch (error) {
                searchResultsContainer.innerHTML = `<div class="search-result-item error">Lỗi: ${error.message}</div>`;
            }
        }, 300);
    });
}

function handleViewGroup(groupId) {
    const group = dashboardData.groupExpenses.details.find(g => g.id === groupId);
    if (!group) return;

    // TODO: Cần API để lấy giao dịch theo nhóm
    const groupTransactions = (dashboardData.transactions || []).filter(t => t.groupId === groupId);

    let transactionsHtml = '<div class="info">Không có giao dịch nào.</div>';
    if (groupTransactions.length > 0) {
        transactionsHtml = `<ul>${groupTransactions.map(t => `
            <li>
                <span>${t.date}</span><span>${t.category}</span>
                <span>${formatCurrency(t.amount, true)}</span>
            </li>`).join('')}</ul>`;
    }
    const membersListHtml = `<ul>${group.members.map(m => `<li>${m.name}</li>`).join('')}</ul>`;

    const content = `
        <h2>${group.name}</h2><p>${group.description}</p><hr>
        <h4>Thành viên</h4>${membersListHtml}<hr>
        <h4>Lịch sử giao dịch</h4>${transactionsHtml}`;
    showModal("Chi tiết nhóm", content);
}
window.initGroupPage = initGroupPage;
// ✅ Gọi tự động khi trang được load hoàn tất
document.addEventListener('DOMContentLoaded', () => {
    console.log("🔹 Layout loaded, context:", window.contextPath);
    const groupContainer = document.querySelector('.group-expense-container');
    if (groupContainer) {
        // Chỉ chạy nếu phần group tồn tại
        initGroupPage();
    } else {
        console.warn("⚠️ Không tìm thấy phần group-expense-container trên trang này");
    }
});
