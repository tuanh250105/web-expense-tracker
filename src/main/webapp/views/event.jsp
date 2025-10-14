<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:if test="${not empty editEvent}">
    <script>
        window.editEventData = {
            id: "${editEvent.id}",
            name: "${editEvent.name}",
            description: "${editEvent.description}",
            startDate: "${editEvent.startDate}",
            endDate: "${editEvent.endDate}"
        };
    </script>
</c:if>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/event.css">

<div class="event-wrapper">
    <div class="event-container">
        <!-- Bộ lọc -->
        <form action="${pageContext.request.contextPath}/event" method="POST" class="filter-form">
            <aside class="filter-panel">
                <input type="hidden" name="action" value="filter">

                <h2>BỘ LỌC SỰ KIỆN</h2>

                <label>Từ ngày / Đến ngày</label>
                <div class="range-inputs">
                    <input type="text" class="select_from_date" name="fromDate" placeholder="Chọn ngày bắt đầu" readonly>
                    <input type="text" class="select_to_date" name="toDate" placeholder="Chọn ngày kết thúc" readonly>
                </div>

                <button type="submit" class="apply-btn">ÁP DỤNG</button>
                <button type="reset" class="cancel-btn">HỦY</button>
            </aside>
        </form>

        <!-- Danh sách sự kiện -->
        <section class="event-list">
            <div class="event-header">
                <h2>Quản lý sự kiện</h2>
            </div>

            <div class="event-items">
                <c:forEach var="e" items="${events}">
                    <div class="event-item" data-id="${e.id}">
                        <div class="details">
                            <h3>${e.name}</h3>
                            <p><strong>Mô tả:</strong> ${e.description}</p>
                            <p><strong>Thời gian:</strong> ${e.startDate} - ${e.endDate}</p>
                            <p><strong>Tổng chi:</strong>
                                <fmt:formatNumber value="${eventService.calculateTotalSpent(e.id)}" type="currency" currencySymbol="₫" groupingUsed="true"/>
                            </p>
                        </div>

                        <div class="progress-bar">
                            <c:set var="percentage" value="${eventService.calculateSpentPercentage(e.id)}" />
                            <div class="progress ${percentage > 80 ? 'danger' : (percentage > 60 ? 'warning' : '')}"
                                 style="width: ${percentage}%">
                            </div>
                        </div>

                        <button class="more-btn" onclick="toggleDropdown(this)">⋮</button>

                        <div class="dropdown-menu">
                            <a href="${pageContext.request.contextPath}/event?action=edit&eventId=${e.id}">✏️ Sửa</a>
                            <form class="delete-form" method="POST" action="${pageContext.request.contextPath}/event"
                                  onsubmit="return confirm('Bạn có chắc muốn xóa sự kiện này?')">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="id" value="${e.id}">
                                <button type="submit">🗑️ Xóa</button>
                            </form>
                            <a href="#" class="view-chart" data-event="${e.id}" onclick="openChartModal('${e.id}'); return false;">📊 Xem biểu đồ</a>
                            <a href="#" class="view-history" data-event="${e.id}" onclick="openHistoryModal('${e.id}'); return false;">📜 Xem lịch sử</a>
                            <a href="#" class="link-transaction" data-event="${e.id}" onclick="openLinkModal('${e.id}'); return false;">🔗 Liên kết giao dịch</a>
                        </div>
                    </div>
                </c:forEach>

                <c:if test="${empty events}">
                    <div style="text-align: center; padding: 40px; color: #999;">
                        <p>Chưa có sự kiện nào. Nhấn nút + để thêm sự kiện mới!</p>
                    </div>
                </c:if>
            </div>
        </section>
    </div>

    <!-- Form Thêm/Sửa Sự kiện -->
    <div id="eventForm" class="addForm-container">
        <form method="POST" action="${pageContext.request.contextPath}/event">
            <input type="hidden" name="action" value="add" id="formAction">
            <input type="hidden" name="id" id="eventId">

            <div class="addForm-header">
                <h2 id="formTitle">Thêm Sự kiện</h2>
                <button type="button" class="closeFormBtn" onclick="closeEventForm()">×</button>
            </div>

            <div class="addForm-body">
                <!-- Tên -->
                <div class="input-group">
                    <div class="input-img">
                        <img src="${pageContext.request.contextPath}/assets/images/icon-base/name.png"
                             alt="Icon" onerror="this.style.display='none'">
                    </div>
                    <div class="input-wrap">
                        <label>Tên sự kiện <span style="color: red;">*</span></label>
                        <input type="text" name="name" id="eventName" placeholder="Nhập tên sự kiện" required>
                    </div>
                </div>

                <!-- Mô tả -->
                <div class="input-group">
                    <div class="input-img">
                        <img src="${pageContext.request.contextPath}/assets/images/icon-base/notes.png"
                             alt="Icon" onerror="this.style.display='none'">
                    </div>
                    <div class="input-wrap">
                        <label>Mô tả</label>
                        <textarea name="description" id="eventDescription" placeholder="Nhập mô tả sự kiện"></textarea>
                    </div>
                </div>

                <!-- Ngày bắt đầu -->
                <div class="input-group">
                    <div class="input-img">
                        <img src="${pageContext.request.contextPath}/assets/images/icon-base/date.png"
                             alt="Icon" onerror="this.style.display='none'">
                    </div>
                    <div class="input-wrap">
                        <label>Ngày bắt đầu <span style="color: red;">*</span></label>
                        <input type="text" class="select_start_date" name="startDate" id="eventStartDate"
                               placeholder="Chọn ngày bắt đầu" readonly required>
                    </div>
                </div>

                <!-- Ngày kết thúc -->
                <div class="input-group">
                    <div class="input-img">
                        <img src="${pageContext.request.contextPath}/assets/images/icon-base/date.png"
                             alt="Icon" onerror="this.style.display='none'">
                    </div>
                    <div class="input-wrap">
                        <label>Ngày kết thúc <span style="color: red;">*</span></label>
                        <input type="text" class="select_end_date" name="endDate" id="eventEndDate"
                               placeholder="Chọn ngày kết thúc" readonly required>
                    </div>
                </div>
            </div>

            <div class="addForm-footer">
                <button type="button" class="cancelEventBtn" onclick="closeEventForm()">HỦY</button>
                <button type="submit" class="saveEventBtn">LƯU</button>
            </div>
        </form>
    </div>

    <!-- Modal Liên kết Giao dịch -->
    <div id="linkTransactionModal">
        <div class="modal-content">
            <span class="close-link-modal" onclick="closeLinkModal()">&times;</span>
            <h2>Liên kết Giao dịch</h2>
            <ul id="transactionList">
                <c:forEach var="t" items="${transactionList}">
                    <li>
                        <button type="button" data-transaction="${t.id}" onclick="linkTransaction('${t.id}')">
                                ${t.note} -
                            <fmt:formatNumber value="${t.amount}" type="currency" currencySymbol="₫" groupingUsed="true"/>
                        </button>
                    </li>
                </c:forEach>

                <c:if test="${empty transactionList}">
                    <li style="text-align: center; padding: 20px; color: #999;">
                        Không có giao dịch nào để liên kết
                    </li>
                </c:if>
            </ul>
        </div>
    </div>

    <!-- Modal Biểu đồ -->
    <div id="chartModal">
        <div class="modal-content">
            <span class="close-chart-modal" onclick="closeChartModal()">&times;</span>
            <h2>Biểu đồ chi tiêu</h2>
            <canvas id="spentChart"></canvas>
        </div>
    </div>

    <!-- Modal Lịch sử -->
    <div id="historyModal">
        <div class="modal-content">
            <span class="close-history-modal" onclick="closeHistoryModal()">&times;</span>
            <h2>Lịch sử Sự kiện</h2>
            <ul id="historyList">
                <!-- Sẽ được load bằng JS -->
            </ul>
        </div>
    </div>

    <!-- Nút FAB -->
    <div class="fab-buttons">
        <button class="fab add-event" onclick="openEventForm()" title="Thêm sự kiện mới">+</button>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/Chart.min.js"></script>
<script defer src="${pageContext.request.contextPath}/assets/js/event.js"></script>

<script>
    // Hàm toggle dropdown
    function toggleDropdown(btn) {
        const dropdown = btn.nextElementSibling;
        // Đóng tất cả dropdown khác
        document.querySelectorAll('.dropdown-menu').forEach(menu => {
            if (menu !== dropdown) {
                menu.classList.remove('show');
            }
        });
        dropdown.classList.toggle('show');
    }

    // Đóng dropdown khi click ra ngoài
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.more-btn') && !e.target.closest('.dropdown-menu')) {
            document.querySelectorAll('.dropdown-menu').forEach(menu => {
                menu.classList.remove('show');
            });
        }
    });

    // Mở form thêm sự kiện
    function openEventForm() {
        document.getElementById('eventForm').classList.add('show');
        document.getElementById('formTitle').textContent = 'Thêm Sự kiện';
        document.getElementById('formAction').value = 'add';
        document.querySelector('#eventForm form').reset();
    }

    // Đóng form
    function closeEventForm() {
        document.getElementById('eventForm').classList.remove('show');
    }

    // Mở/đóng các modal
    function openLinkModal(eventId) {
        document.getElementById('linkTransactionModal').classList.add('show');
        document.getElementById('linkTransactionModal').dataset.eventId = eventId;
    }

    function closeLinkModal() {
        document.getElementById('linkTransactionModal').classList.remove('show');
    }

    function openChartModal(eventId) {
        document.getElementById('chartModal').classList.add('show');
        // Load chart data here
    }

    function closeChartModal() {
        document.getElementById('chartModal').classList.remove('show');
    }

    function openHistoryModal(eventId) {
        document.getElementById('historyModal').classList.add('show');
        // Load history data here
    }

    function closeHistoryModal() {
        document.getElementById('historyModal').classList.remove('show');
    }

    // Link transaction
    function linkTransaction(transactionId) {
        const eventId = document.getElementById('linkTransactionModal').dataset.eventId;
        // Gọi API để liên kết transaction với event
        console.log('Linking transaction', transactionId, 'to event', eventId);
        closeLinkModal();
    }

    // Load edit data nếu có
    if (window.editEventData) {
        document.getElementById('eventForm').classList.add('show');
        document.getElementById('formTitle').textContent = 'Sửa Sự kiện';
        document.getElementById('formAction').value = 'edit';
        document.getElementById('eventId').value = window.editEventData.id;
        document.getElementById('eventName').value = window.editEventData.name;
        document.getElementById('eventDescription').value = window.editEventData.description;
        document.getElementById('eventStartDate').value = window.editEventData.startDate;
        document.getElementById('eventEndDate').value = window.editEventData.endDate;
    }
</script>