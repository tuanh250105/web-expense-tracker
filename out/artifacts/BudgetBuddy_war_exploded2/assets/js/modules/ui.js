// /assets/js/modules/ui.js

import { formatCurrency } from './utils.js';

/**
 * Hiển thị dữ liệu các nhóm lên giao diện.
 * @param {object} groupData - Dữ liệu chi tiêu nhóm.
 */
export function renderGroupPage(groupData) {
    const listEl = document.getElementById("groupList");
    const totalEl = document.getElementById("groupTotal");

    if (!listEl) return;
    listEl.innerHTML = '';

    if (!groupData || !groupData.details || groupData.details.length === 0) {
        listEl.innerHTML = '<div class="info">Bạn chưa tham gia nhóm nào.</div>';
        if (totalEl) totalEl.textContent = formatCurrency(0, true); // Dùng formatCurrency từ utils
        return;
    }

    if (totalEl) {
        totalEl.textContent = formatCurrency(groupData.total || 0, true);
    }

    groupData.details.forEach(group => {
        const card = document.createElement("div");
        card.className = "group-card";
        card.setAttribute('data-group-id', group.id);

        const membersHtml = group.members.map(member =>
            `<span class="member-badge" title="${member.name}">${member.name.charAt(0)}</span>`
        ).join('');

        let actionsHtml = `<button class="btn-view" data-group-id="${group.id}">👁️ Xem Chi Tiết</button>`;
        if (group.isCurrentUserOwner) {
            actionsHtml += `
               <button class="btn-manage" data-group-id="${group.id}">+ Quản lý</button>
               <button class="btn-delete" data-group-id="${group.id}">🗑️ Xóa</button>`;
        }

        card.innerHTML = `
            <div class="group-card-header">
                <h3>${group.name}</h3>
                <div class="group-total">${formatCurrency(group.totalAmount, true)}</div>
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
}


/**
 * Hiển thị modal.
 * @param {string} title - Tiêu đề của modal.
 * @param {string} contentHtml - Nội dung HTML bên trong modal.
 * @param {string|null} footerHtml - Nội dung HTML cho phần footer.
 */
export function showModal(title, contentHtml, footerHtml = null) {
    closeModal(); // Đảm bảo đóng modal cũ trước khi mở modal mới
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
 * Đóng modal đang mở.
 */
export function closeModal() {
    const modal = document.getElementById('groupModal');
    if (modal) {
        modal.remove();
        document.body.style.overflow = 'auto';
    }
}