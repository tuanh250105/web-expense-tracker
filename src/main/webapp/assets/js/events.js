
import { apiFetch } from './modules/apiService.js';

const userId = window.currentUserId || '';
const calendarPanel = document.getElementById('calendarPanel');
const notificationPanel = document.getElementById('notificationPanel');

function getCurrentMonthRange() {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    return {
        startDate: firstDay.toISOString().slice(0, 10),
        endDate: lastDay.toISOString().slice(0, 10)
    };
}

async function fetchEvents() {
    const { startDate, endDate } = getCurrentMonthRange();
    if (!userId) return [];
    return await apiFetch(`/api/events?startDate=${startDate}&endDate=${endDate}&user_id=${userId}`);
}

async function fetchNotifications() {
    if (!userId) return [];
    return await apiFetch(`/api/events/notifications?user_id=${userId}`);
}

function showModal({ mode, event, date }) {
    let modal = document.getElementById('eventModal');
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'eventModal';
        modal.style.position = 'fixed';
        modal.style.top = '0';
        modal.style.left = '0';
        modal.style.width = '100vw';
        modal.style.height = '100vh';
        modal.style.background = 'rgba(0,0,0,0.2)';
        modal.style.display = 'flex';
        modal.style.alignItems = 'center';
        modal.style.justifyContent = 'center';
        modal.style.zIndex = '9999';
        document.body.appendChild(modal);
    }
    modal.innerHTML = `
      <div style="background:#fff;padding:24px;border-radius:8px;min-width:320px;max-width:90vw;">
        <h3>${mode === 'edit' ? 'Sửa sự kiện' : 'Thêm sự kiện'}</h3>
        <form id="eventForm">
          <input type="hidden" name="id" value="${event?.id || ''}">
          <label>Tên sự kiện:<br><input name="name" required value="${event?.name || ''}"></label><br>
          <label>Mô tả:<br><textarea name="description">${event?.description || ''}</textarea></label><br>
          <label>Ngày bắt đầu:<br><input type="date" name="startDate" required value="${event?.startDate || date || ''}"></label><br>
          <label>Ngày kết thúc:<br><input type="date" name="endDate" required value="${event?.endDate || date || ''}"></label><br>
          <div style="margin-top:12px;display:flex;gap:12px;">
            <button type="submit">${mode === 'edit' ? 'Lưu' : 'Thêm'}</button>
            <button type="button" id="closeModalBtn">Đóng</button>
            ${mode === 'edit' ? '<button type="button" id="deleteEventBtn" style="color:red">Xóa</button>' : ''}
          </div>
        </form>
      </div>
    `;
    modal.style.display = 'flex';
    document.getElementById('closeModalBtn').onclick = () => { modal.style.display = 'none'; };
    document.getElementById('eventForm').onsubmit = async function(e) {
        e.preventDefault();
        const form = e.target;
        const data = {
            id: form.id.value,
            name: form.name.value,
            description: form.description.value,
            startDate: form.startDate.value,
            endDate: form.endDate.value,
            userId: userId
        };
        if (mode === 'edit') {
            await apiFetch(`/api/events/${data.id}`, {
                method: 'PUT',
                body: JSON.stringify(data)
            });
        } else {
            await apiFetch('/api/events', {
                method: 'POST',
                body: JSON.stringify(data)
            });
        }
        modal.style.display = 'none';
        await reloadAll();
    };
    if (mode === 'edit') {
        document.getElementById('deleteEventBtn').onclick = async function() {
            await apiFetch(`/api/events/${event.id}?user_id=${userId}`, {
                method: 'DELETE'
            });
            modal.style.display = 'none';
            await reloadAll();
        };
    }
}

function renderCalendar(events) {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    let html = `<table class="calendar-table"><thead><tr><th>CN</th><th>T2</th><th>T3</th><th>T4</th><th>T5</th><th>T6</th><th>T7</th></tr></thead><tbody>`;
    let day = 1;
    for (let row = 0; row < 6; row++) {
        html += '<tr>';
        for (let col = 0; col < 7; col++) {
            const cellDate = new Date(year, month, day);
            if ((row === 0 && col < firstDay.getDay()) || day > lastDay.getDate()) {
                html += '<td></td>';
            } else {
                const dateStr = cellDate.toISOString().slice(0, 10);
                const dayEvents = events.filter(e => e.startDate === dateStr || e.endDate === dateStr);
                html += `<td data-date="${dateStr}"><div class="date-num">${day}</div>`;
                dayEvents.forEach(ev => {
                    html += `<div class="event-name" data-event-id="${ev.id}">${ev.name}</div>`;
                });
                html += '</td>';
                day++;
            }
        }
        html += '</tr>';
    }
    html += '</tbody></table>';
    calendarPanel.innerHTML = html;

    // Thêm sự kiện click vào ngày để thêm mới
    document.querySelectorAll('.calendar-table td[data-date]').forEach(td => {
        td.addEventListener('click', function(e) {
            // Nếu click vào event-name thì không mở modal thêm
            if (e.target.classList.contains('event-name')) return;
            showModal({ mode: 'add', date: td.getAttribute('data-date') });
        });
    });
    // Sửa/xóa sự kiện
    document.querySelectorAll('.event-name').forEach(div => {
        div.addEventListener('click', function(e) {
            e.stopPropagation();
            const eventId = div.getAttribute('data-event-id');
            const event = events.find(ev => ev.id === eventId);
            showModal({ mode: 'edit', event });
        });
    });
}

function renderNotifications(events) {
    let html = '<h3>Thông báo sự kiện</h3><ul class="notification-list">';
    const now = new Date();
    events.forEach(ev => {
        const start = new Date(ev.startDate);
        let color = '#ccc'; // xám
        if (start > now) {
            const diff = Math.ceil((start - now) / (1000 * 60 * 60 * 24));
            if (diff <= 7) color = 'red'; // sắp tới <=7 ngày
            else color = 'green'; // sắp tới >7 ngày
        }
        html += `<li style="color:${color}">${ev.name} (${ev.startDate})</li>`;
    });
    html += '</ul>';
    notificationPanel.innerHTML = html;
}

async function reloadAll() {
    const [events, notifications] = await Promise.all([
        fetchEvents(),
        fetchNotifications()
    ]);
    renderCalendar(events);
    renderNotifications(notifications);
}

document.addEventListener('DOMContentLoaded', reloadAll);
