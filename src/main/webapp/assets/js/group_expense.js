// Biến toàn cục để lưu trữ toàn bộ dữ liệu, giúp truy cập dễ dàng hơn
let dashboardData = {
    groupExpenses: { details: [] },
    transactions: []
};

// Danh sách người dùng "thật" giả lập để tìm kiếm và thêm vào nhóm
const MOCK_ALL_USERS = [
    { id: "user-an", name: "An Nguyễn" },
    { id: "user-binh", name: "Bình Trần" },
    { id: "user-chau", name: "Châu Lê" },
    { id: "user-dung", name: "Dũng Phạm" },
    { id: "user-ha", name: "Hà Mai" }
];

/**
 * Khởi tạo trang chi tiêu nhóm, lấy dữ liệu từ server và hiển thị.
 */
function initGroupPage() {
    // Thêm CSS cho modal vào trang một lần duy nhất
    injectModalCss();

    const ctxPath = window.contextPath || '';
    console.log("contextPath in initGroupPage:", ctxPath);

    const listEl = document.getElementById("groupList");
    if (listEl) {
        listEl.innerHTML = '<div class="loading">Đang tải dữ liệu nhóm...</div>';
    }

    fetch(`${ctxPath}/dashboard-data?period=month`)
        .then(res => {
            if (!res.ok) throw new Error(`Lỗi mạng! Trạng thái: ${res.status}`);
            return res.json();
        })
        .then(json => {
            if (json.error) throw new Error(`Lỗi từ server: ${json.error}`);
            dashboardData = json; // Lưu toàn bộ dữ liệu
            console.log("Dữ liệu nhóm nhận được:", dashboardData.groupExpenses);
            renderGroupPage(dashboardData.groupExpenses);
            // Thêm sự kiện cho nút "Tạo Nhóm Mới" sau khi đã có dữ liệu
            document.querySelector('.btn-add-group').addEventListener('click', handleCreateGroup);
        })
        .catch(err => {
            console.error("❌ Lỗi khi tải dữ liệu nhóm:", err);
            if (listEl) {
                listEl.innerHTML = `<div class="error">Không thể tải dữ liệu: ${err.message}</div>`;
            }
        });
}

/**
 * Hiển thị dữ liệu các nhóm lên giao diện.
 * @param {object} groupData - Dữ liệu chi tiêu nhóm từ server.
 */
function renderGroupPage(groupData) {
    const listEl = document.getElementById("groupList");
    const totalEl = document.getElementById("groupTotal");

    if (!listEl) return;
    listEl.innerHTML = '';

    if (!groupData || !groupData.details || groupData.details.length === 0) {
        listEl.innerHTML = '<div class="info">Bạn chưa tham gia nhóm nào.</div>';
        if (totalEl) totalEl.textContent = formatCurrency(0);
        return;
    }

    if (totalEl) {
        totalEl.textContent = formatCurrency(groupData.total || 0);
    }

    groupData.details.forEach(group => {
        const card = document.createElement("div");
        card.className = "group-card";
        card.setAttribute('data-group-id', group.id); // Thêm ID để dễ dàng cập nhật

        const membersHtml = group.members.map(member =>
            `<span class="member-badge" title="${member.name}">${member.name.charAt(0)}</span>`
        ).join('');

        // Cải tiến logic hiển thị nút: Owner có cả nút Xem và Quản lý
        let actionsHtml = `<button class="btn-view" data-group-id="${group.id}">👁️ Xem Chi Tiết</button>`;
        if (group.isCurrentUserOwner) {
            actionsHtml += `
               <button class="btn-manage" data-group-id="${group.id}">+ Quản lý thành viên</button>
               <button class="btn-delete" data-group-id="${group.id}">🗑️ Xóa Nhóm</button>`;
        }

        card.innerHTML = `
            <div class="group-card-header">
                <h3>${group.name}</h3>
                <div class="group-total">${formatCurrency(group.totalAmount)}</div>
            </div>
            <p class="group-description">${group.description}</p>
            <div class="group-members-container">
                <h4>Thành viên:</h4>
                <div class="group-members">${membersHtml}</div>
            </div>
            <div class="group-actions">${actionsHtml}</div>
        `;

        listEl.appendChild(card);
    });

    addGroupEventListeners();
}

/**
 * Thêm các trình xử lý sự kiện cho các nút trên thẻ nhóm.
 */
function addGroupEventListeners() {
    document.querySelectorAll('.btn-manage').forEach(button => {
        button.addEventListener('click', (e) => handleManageMembers(e.target.dataset.groupId));
    });

    document.querySelectorAll('.btn-delete').forEach(button => {
        button.addEventListener('click', (e) => handleDeleteGroup(e.target.dataset.groupId));
    });

    document.querySelectorAll('.btn-view').forEach(button => {
        button.addEventListener('click', (e) => handleViewGroup(e.target.dataset.groupId));
    });
}


// --- HỆ THỐNG MODAL VÀ XỬ LÝ SỰ KIỆN ---

/**
 * Mở modal để tạo nhóm mới.
 */
function handleCreateGroup() {
    const content = `
        <div class="modal-form">
            <label for="groupName">Tên nhóm</label>
            <input type="text" id="groupName" placeholder="Ví dụ: Tiền ăn trưa văn phòng">
            <label for="groupDesc">Mô tả</label>
            <textarea id="groupDesc" placeholder="Mô tả ngắn về mục đích của nhóm"></textarea>
        </div>
    `;
    const footer = `
        <button class="modal-btn-cancel">Hủy</button>
        <button class="modal-btn-confirm" id="confirmCreateGroup">Tạo Nhóm</button>
    `;
    showModal("Tạo Nhóm Mới", content, footer);

    document.getElementById('confirmCreateGroup').addEventListener('click', () => {
        const name = document.getElementById('groupName').value.trim();
        const description = document.getElementById('groupDesc').value.trim();
        if (!name) {
            alert("Vui lòng nhập tên nhóm.");
            return;
        }

        // --- Logic giả lập thêm nhóm mới ---
        const newGroup = {
            id: `group-${Date.now()}`,
            name,
            description,
            totalAmount: 0,
            members: [{ id: 'user-an', name: 'An Nguyễn' }], // Người tạo tự động là thành viên
            isCurrentUserOwner: true
        };
        dashboardData.groupExpenses.details.push(newGroup);
        console.log("Đã tạo nhóm mới:", newGroup);
        // --- Kết thúc logic giả lập ---

        closeModal();
        renderGroupPage(dashboardData.groupExpenses); // Vẽ lại toàn bộ danh sách
    });
}


/**
 * Mở modal để xem lịch sử giao dịch và thành viên của nhóm.
 */
function handleViewGroup(groupId) {
    const group = dashboardData.groupExpenses.details.find(g => g.id === groupId);
    if (!group) return;

    // Lọc giao dịch của nhóm từ dữ liệu tổng
    const groupTransactions = (dashboardData.transactions || []).filter(t => t.groupId === groupId);

    let transactionsHtml = '<div class="info">Không có giao dịch nào.</div>';
    if (groupTransactions.length > 0) {
        transactionsHtml = groupTransactions.map(t => `
            <li>
                <span class="tx-date">${t.date}</span>
                <span class="tx-category">${t.category}</span>
                <span class="tx-amount">${formatCurrency(t.amount / 1_000_000, true)}</span>
            </li>
        `).join('');
    }

    // Lấy danh sách thành viên
    const membersListHtml = group.members.map(m => `<li>${m.name}</li>`).join('');

    const content = `
        <h2>${group.name}</h2>
        <p>${group.description}</p>
        <hr>
        <h4>Thành viên</h4>
        <ul class="modal-member-list">${membersListHtml}</ul>
        <hr>
        <h4>Lịch sử giao dịch</h4>
        <ul class="modal-transaction-list">${transactionsHtml}</ul>
    `;
    showModal("Chi tiết nhóm", content);
}


/**
 * Mở modal để quản lý thành viên (thêm/xóa).
 */
function handleManageMembers(groupId) {
    const group = dashboardData.groupExpenses.details.find(g => g.id === groupId);
    if (!group) return;

    const membersList = group.members.map(m => `
        <li data-user-id="${m.id}">
            <span>${m.name}</span>
            ${group.members.length > 1 ? `<button class="modal-btn-delete-user" data-user-id="${m.id}" data-group-id="${groupId}">Xóa</button>` : ''}
        </li>
    `).join('');

    const content = `
        <h2>Quản lý: ${group.name}</h2>
        <h4>Thành viên hiện tại</h4>
        <ul class="modal-member-list interactive">${membersList}</ul>
        <hr>
        <h4>Thêm thành viên mới</h4>
        <div class="modal-add-member">
            <input type="text" id="newMemberName" placeholder="Nhập tên người dùng mới..." />
            <button class="modal-btn-confirm" id="confirmAddMember" data-group-id="${groupId}">Thêm</button>
        </div>
    `;
    showModal("Quản lý thành viên", content);

    // Thêm sự kiện cho các nút trong modal vừa tạo
    addModalManageEventListeners();
}

/**
 * Mở modal xác nhận xóa nhóm.
 */
function handleDeleteGroup(groupId) {
    const group = dashboardData.groupExpenses.details.find(g => g.id === groupId);
    if (!group) return;

    const content = `<p>Bạn có chắc chắn muốn xóa nhóm <strong>"${group.name}"</strong> không?</p><p>Hành động này không thể hoàn tác.</p>`;
    const footer = `<button class="modal-btn-cancel">Hủy bỏ</button><button class="modal-btn-confirm-delete" id="confirmDeleteGroup">Xác nhận Xóa</button>`;
    showModal("Xác nhận xóa nhóm", content, footer);

    document.getElementById('confirmDeleteGroup').addEventListener('click', () => {
        console.log(`Đã xác nhận xóa nhóm ${groupId}`);
        // --- Logic giả lập xóa nhóm ---
        dashboardData.groupExpenses.details = dashboardData.groupExpenses.details.filter(g => g.id !== groupId);
        // --- Kết thúc logic giả lập ---
        closeModal();
        renderGroupPage(dashboardData.groupExpenses); // Vẽ lại danh sách
    });
}

/**
 * Thêm các trình xử lý sự kiện cho các nút bên trong modal quản lý thành viên.
 */
function addModalManageEventListeners() {
    // Sự kiện xóa thành viên
    document.querySelectorAll('.modal-btn-delete-user').forEach(button => {
        button.addEventListener('click', (e) => {
            const userId = e.target.dataset.userId;
            const groupId = e.target.dataset.groupId;

            // --- Logic giả lập xóa thành viên ---
            const group = dashboardData.groupExpenses.details.find(g => g.id === groupId);
            if (group) {
                group.members = group.members.filter(m => m.id !== userId);
            }
            // --- Kết thúc logic giả lập ---

            closeModal();
            handleManageMembers(groupId); // Mở lại modal để cập nhật
            renderGroupPage(dashboardData.groupExpenses); // Cập nhật cả trang chính
        });
    });

    // Sự kiện thêm thành viên
    document.getElementById('confirmAddMember').addEventListener('click', (e) => {
        const groupId = e.target.dataset.groupId;
        const input = document.getElementById('newMemberName');
        const newName = input.value.trim();
        if (!newName) {
            alert("Vui lòng nhập tên thành viên.");
            return;
        }

        // --- Logic mới: Tìm user thật trong danh sách giả lập ---
        const group = dashboardData.groupExpenses.details.find(g => g.id === groupId);
        if (!group) return;

        // 1. Kiểm tra xem user có tồn tại trong danh sách "thật" không
        const userToAdd = MOCK_ALL_USERS.find(u => u.name.toLowerCase() === newName.toLowerCase());

        if (!userToAdd) {
            alert(`Không tìm thấy người dùng có tên "${newName}".`);
            return;
        }

        // 2. Kiểm tra xem user đã là thành viên chưa
        if (group.members.some(m => m.id === userToAdd.id)) {
            alert(`${userToAdd.name} đã là thành viên của nhóm.`);
            return;
        }

        // 3. Thêm user vào nhóm
        group.members.push(userToAdd);
        console.log(`Đã thêm "${userToAdd.name}" vào nhóm.`);
        // --- Kết thúc logic mới ---

        closeModal();
        handleManageMembers(groupId); // Mở lại modal để cập nhật
        renderGroupPage(dashboardData.groupExpenses); // Cập nhật cả trang chính
    });
}


/**
 * Hiển thị modal.
 */
function showModal(title, contentHtml, footerHtml = null) {
    closeModal();
    const modal = document.createElement('div');
    modal.className = 'group-modal-backdrop';
    modal.id = 'groupModal';
    const defaultFooter = '<button class="modal-btn-close">Đóng</button>';

    modal.innerHTML = `
        <div class="group-modal-content">
            <div class="group-modal-header">
                <h3>${title}</h3>
                <span class="group-modal-close">&times;</span>
            </div>
            <div class="group-modal-body">${contentHtml}</div>
            <div class="group-modal-footer">${footerHtml !== null ? footerHtml : defaultFooter}</div>
        </div>
    `;
    document.body.appendChild(modal);
    document.body.style.overflow = 'hidden';

    // Thêm sự kiện để đóng modal
    modal.querySelector('.group-modal-close').addEventListener('click', closeModal);
    modal.addEventListener('click', (e) => {
        if (e.target === modal) closeModal();
    });
    const defaultCloseBtn = modal.querySelector('.modal-btn-close');
    if (defaultCloseBtn) defaultCloseBtn.addEventListener('click', closeModal);
    const cancelBtn = modal.querySelector('.modal-btn-cancel');
    if(cancelBtn) cancelBtn.addEventListener('click', closeModal);
}

/**
 * Đóng modal.
 */
function closeModal() {
    const modal = document.getElementById('groupModal');
    if (modal) {
        modal.remove();
        document.body.style.overflow = 'auto';
    }
}

/**
 * Định dạng tiền tệ.
 */
function formatCurrency(amount, isRawAmount = false) {
    const value = isRawAmount ? amount : (amount || 0) * 1000000;
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(value);
}

/**
 * Thêm CSS cho modal.
 */
function injectModalCss() {
    if (document.getElementById('group-modal-styles')) return;
    const style = document.createElement('style');
    style.id = 'group-modal-styles';
    style.innerHTML = `
        .group-modal-backdrop {
            position: fixed; z-index: 1000; left: 0; top: 0;
            width: 100%; height: 100%;
            background-color: rgba(0,0,0,0.5);
            display: flex; align-items: center; justify-content: center;
        }
        .group-modal-content {
            background-color: #fff; border-radius: 8px;
            width: 90%; max-width: 500px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.3);
            animation: slide-down 0.3s ease-out;
        }
        .group-modal-header { padding: 16px; border-bottom: 1px solid #dee2e6; display: flex; justify-content: space-between; align-items: center; }
        .group-modal-header h3 { margin: 0; font-size: 1.25rem; }
        .group-modal-close { font-size: 1.5rem; cursor: pointer; color: #6c757d; }
        .group-modal-body { padding: 20px; max-height: 60vh; overflow-y: auto; }
        .group-modal-footer { padding: 16px; border-top: 1px solid #dee2e6; display: flex; justify-content: flex-end; gap: 10px; }
        .modal-btn-close, .modal-btn-cancel, .modal-btn-confirm, .modal-btn-confirm-delete { padding: 8px 16px; border-radius: 6px; border: 1px solid #ccc; cursor: pointer; font-size: 0.9rem; }
        .modal-btn-confirm { background-color: #2f9e44; color: white; border-color: #2f9e44; }
        .modal-btn-confirm-delete { background-color: #fa5252; color: white; border-color: #fa5252; }
        .modal-member-list, .modal-transaction-list { list-style: none; padding: 0; }
        .modal-member-list li, .modal-transaction-list li { padding: 10px 8px; border-bottom: 1px solid #f1f3f5; display: flex; justify-content: space-between; align-items: center; }
        .modal-member-list.interactive li:hover { background-color: #f8f9fa; }
        .modal-btn-delete-user { background: none; border: none; color: #fa5252; cursor: pointer; font-size: 0.8rem; }
        .modal-add-member { display: flex; gap: 10px; margin-top: 1rem; }
        .modal-add-member input, .modal-form input, .modal-form textarea { width: 100%; flex-grow: 1; padding: 8px; border: 1px solid #ccc; border-radius: 4px; }
        .modal-form label { display: block; margin-top: 10px; margin-bottom: 5px; font-weight: 500; }
        .modal-transaction-list .tx-date { color: #868e96; font-size: 0.9rem; width: 100px; }
        .modal-transaction-list .tx-category { flex-grow: 1; }
        .modal-transaction-list .tx-amount { font-weight: 500; color: #343a40; }
        @keyframes slide-down { from { transform: translateY(-30px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
    `;
    document.head.appendChild(style);
}

document.addEventListener('DOMContentLoaded', initGroupPage);

