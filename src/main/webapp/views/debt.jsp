<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!-- Fonts & Icons -->
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700;800&display=swap" rel="stylesheet">
<script src="https://kit.fontawesome.com/a2e0ad5fc7.js" crossorigin="anonymous"></script>

<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">

<style>
    :root{
        --accent-primary: #0d6efd;
        --accent-2: #6c63ff;
        --accent-success: #20c997;
        --accent-danger: #dc3545;
        --muted: #6c757d;
        --card-bg: #ffffff;
        --page-bg: #f4f7fb;
        --glass: rgba(255,255,255,0.6);
    }

    body {
        font-family: "Inter", system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial;
        background: linear-gradient(180deg, var(--page-bg), #eef4ff 120%);
        color: #1f2933;
    }

    .app-header {
        display:flex;
        gap:1rem;
        align-items:center;
        justify-content:space-between;
        margin-bottom:1rem;
        flex-wrap:wrap;
    }

    .brand {
        display:flex;
        gap:.9rem;
        align-items:center;
    }

    .brand .logo {
        width:46px;
        height:46px;
        border-radius:10px;
        background: linear-gradient(135deg, var(--accent-primary), var(--accent-2));
        display:flex;
        align-items:center;
        justify-content:center;
        color:#fff;
        font-weight:700;
        box-shadow: 0 6px 18px rgba(12, 63, 255, 0.14);
    }

    .brand h1 {
        margin:0;
        font-size:1.25rem;
        font-weight:700;
        letter-spacing:-0.2px;
    }
    .brand small { color:var(--muted); display:block; margin-top:2px; }

    .stats-row { display:flex; gap:.9rem; align-items:center; flex-wrap:wrap; }

    .stat-card {
        min-width: 170px;
        background: linear-gradient(180deg, rgba(255,255,255,0.8), var(--card-bg));
        border-radius:12px;
        padding:0.8rem 1rem;
        box-shadow: 0 8px 30px rgba(16,24,40,0.06);
        border: 1px solid rgba(13,110,253,0.04);
    }

    .stat-card .value { font-size:1.05rem; font-weight:700; }
    .stat-card .label { color:var(--muted); font-size:0.85rem; }

    .controls {
        display:flex;
        gap:.6rem;
        align-items:center;
        flex-wrap:wrap;
    }

    .search-wrapper {
        position:relative;
    }
    .search-wrapper input {
        padding-left:40px;
        min-width:320px;
        max-width:420px;
    }
    .search-wrapper .fa-search {
        position:absolute;
        left:12px;
        top:50%;
        transform:translateY(-50%);
        color:var(--muted);
    }

    .card-debts {
        border-radius:14px;
        background: linear-gradient(180deg, rgba(255,255,255,0.9), #ffffff);
        padding:1rem;
        box-shadow: 0 10px 34px rgba(15,23,42,0.06);
        border: 1px solid rgba(12,63,255,0.04);
    }

    .table-responsive {
        max-height: 62vh;
        overflow-x: auto;
        overflow-y: auto;
        -webkit-overflow-scrolling: touch;
        border-radius: 8px;
        background: transparent;
    }

    .table {
        white-space: nowrap;
        min-width: 1200px;
        border-collapse: separate;
        border-spacing: 0;
    }

    .table thead th, .table tbody td {
        white-space: nowrap;
        vertical-align: middle;
        text-overflow: ellipsis;
        overflow: hidden;
    }

    .table thead th {
        background: linear-gradient(180deg, rgba(13,110,253,0.95), rgba(6,78,196,0.95));
        color:#fff;
        border: none;
        font-size:0.85rem;
        letter-spacing:0.6px;
    }

    .table tbody tr {
        transition: transform .12s ease, box-shadow .12s ease, background-color .12s ease;
        background: linear-gradient(180deg, rgba(255,255,255,0.5), rgba(248,250,252,0.5));
    }
    .table tbody tr:hover {
        transform: translateY(-6px);
        box-shadow: 0 18px 40px rgba(15,23,42,0.06);
    }

    .small-id {
        font-family: Menlo, Monaco, "Courier New", monospace;
        font-size:0.85rem;
        color:#495057;
        display: inline-block;
        max-width: 180px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .col-note {
        max-width: 360px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .btn-action {
        border-radius:8px;
        font-weight:600;
        padding:.3rem .6rem;
        box-shadow:none;
    }

    .btn-edit {
        background: rgba(13,110,253,0.06);
        color: var(--accent-primary);
        border: 1px solid rgba(13,110,253,0.12);
    }
    .btn-edit:hover { background: var(--accent-primary); color:#fff; transform:translateY(-3px); box-shadow: 0 10px 26px rgba(13,110,253,0.12); }

    .btn-delete {
        background: rgba(220,53,69,0.05);
        color: var(--accent-danger);
        border: 1px solid rgba(220,53,69,0.08);
    }
    .btn-delete:hover { background: var(--accent-danger); color:#fff; transform:translateY(-3px); box-shadow: 0 10px 26px rgba(220,53,69,0.12); }

    .badge-status {
        padding:.35rem .6rem;
        border-radius:10px;
        font-weight:700;
        font-size:.8rem;
    }

    @media (max-width: 1200px) {
        .table { min-width: 1000px; }
    }
    @media (max-width: 900px) {
        .search-wrapper input { min-width:180px; }
        .stats-row { gap:.5rem; }
    }
</style>

<div class="container mt-4 mb-5">
    <div class="app-header">
        <div class="brand">
            <div class="logo">BB</div>
            <div>
                <h1>Quản lý Công nợ</h1>
                <small>Theo dõi các khoản nợ</small>
            </div>
        </div>

        <div class="stats-row">
            <div class="stat-card d-flex flex-column">
                <div class="label">Tổng nợ chưa trả</div>
                <div class="value text-primary">
                    <c:out value="${totalUnpaidStr != null ? totalUnpaidStr : '₫0'}"/>
                </div>
            </div>

            <div class="stat-card d-flex flex-column">
                <div class="label">Tổng nợ quá hạn</div>
                <div class="value text-danger">
                    <c:out value="${totalOverdueStr != null ? totalOverdueStr : '₫0'}"/>
                </div>
            </div>

            <div class="stat-card d-flex flex-column">
                <div class="label">Sắp đến hạn (≤3 ngày)</div>
                <div class="value text-info">
                    <c:out value="${nearDueCount != null ? nearDueCount : 0}"/>
                </div>
            </div>
        </div>
    </div>

    <c:if test="${hasOverdue}">
        <div class="alert alert-danger d-flex align-items-center mb-3" role="alert">
            <i class="fa-solid fa-triangle-exclamation fa-lg me-3"></i>
            <div>
                <strong>Cảnh báo:</strong> Có khoản nợ <strong>quá hạn</strong>. Vui lòng kiểm tra ngay.
            </div>
        </div>
    </c:if>

    <c:if test="${nearDueCount != null && nearDueCount > 0}">
        <div class="alert alert-info d-flex align-items-center mb-3" role="alert">
            <i class="fa-solid fa-clock fa-lg me-3"></i>
            <div>
                <strong>Nhắc nhở:</strong> Có <strong>${nearDueCount}</strong> khoản sắp đến hạn (trong 3 ngày).
            </div>
        </div>
    </c:if>

    <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
        <div class="controls">
            <div class="search-wrapper">
                <i class="fa fa-search"></i>
                <input id="searchBox" class="form-control form-control-sm" placeholder="Tìm theo chủ nợ / trạng thái / ID..." />
            </div>

            <button class="btn btn-outline-secondary btn-sm" id="btnStats" data-bs-toggle="modal" data-bs-target="#statsModal" title="Xem chi tiết thống kê">
                <i class="fa fa-chart-simple me-1"></i> Thống kê
            </button>
        </div>

        <div>
            <div class="badges-inline d-inline-flex align-items-center me-2">
                <div class="me-2 position-relative">
                    <button class="btn btn-outline-danger btn-sm" title="Số khoản quá hạn"><i class="fa fa-exclamation-circle"></i></button>
                    <c:if test="${overdueList != null && !overdueList.isEmpty()}">
                        <span class="badge bg-danger rounded-circle position-absolute" style="top:-8px; right:-8px;">${overdueList.size()}</span>
                    </c:if>
                </div>

                <div class="me-2 position-relative">
                    <button class="btn btn-outline-info btn-sm" title="Số khoản sắp đến hạn"><i class="fa fa-clock"></i></button>
                    <c:if test="${nearDueCount != null && nearDueCount > 0}">
                        <span class="badge bg-info rounded-circle position-absolute" style="top:-8px; right:-8px;">${nearDueCount}</span>
                    </c:if>
                </div>
            </div>

            <button class="btn btn-primary btn-sm" id="btnAddDebt" data-bs-toggle="offcanvas" data-bs-target="#debtOffcanvas">
                <i class="fa fa-plus me-1"></i> Thêm khoản nợ
            </button>
        </div>
    </div>

    <div class="card-debts">
        <div class="table-responsive">
            <table class="table table-hover table-bordered align-middle">
                <thead class="text-uppercase small">
                <tr>
                    <th style="width:48px">#</th>
                    <th style="min-width:180px">ID khoản nợ</th>
                    <th style="min-width:160px">User ID</th>
                    <th>Chủ nợ</th>
                    <th style="width:140px">Số tiền</th>
                    <th style="width:150px">Ngày đáo hạn</th>
                    <th style="width:140px">Trạng thái</th>
                    <th>Ghi chú</th>
                    <th style="width:160px">Hành động</th>
                </tr>
                </thead>
                <tbody id="debtTableBody">
                <c:forEach var="debt" items="${debts}" varStatus="st">
                    <tr
                            data-id="${debt.id}"
                            data-userid="${debt.userId}"
                            data-creditor="${debt.creditorName}"
                            data-note="${debt.note}"
                            data-status="${debt.status}"
                            data-days="${debt.daysUntilDue}"
                            class="${(debt.daysUntilDue >= 0 && debt.daysUntilDue <= 3) ? 'near-due-row' : ''}">
                        <td><c:out value="${st.index + 1}"/></td>
                        <td><span class="small-id" title="${debt.id}"><c:out value="${debt.id}"/></span></td>
                        <td><span class="small-id" title="${debt.userId}"><c:out value="${debt.userId}"/></span></td>
                        <td class="text-start"><c:out value="${debt.creditorName}"/></td>
                        <td class="text-end"><c:out value="${debt.formattedAmount}"/></td>
                        <td><c:out value="${debt.dueDate}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${debt.status == 'PENDING'}">
                                    <span class="badge badge-status bg-warning text-dark">Chưa trả</span>
                                </c:when>
                                <c:when test="${debt.status == 'PAID'}">
                                    <span class="badge badge-status bg-success">Đã trả</span>
                                </c:when>
                                <c:when test="${debt.status == 'OVERDUE'}">
                                    <span class="badge badge-status bg-danger">Quá hạn</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge badge-status bg-secondary"><c:out value="${debt.status}"/></span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-start col-note"><small class="text-muted"><c:out value="${debt.note}"/></small></td>
                        <td>
                            <div class="d-flex justify-content-center gap-2">
                                <button class="btn btn-sm btn-action btn-edit js-edit-btn"
                                        data-id="${debt.id}"
                                        data-creditor="<c:out value='${debt.creditorName}'/>"
                                        data-amount="<c:out value='${debt.amount}'/>"
                                        data-duedate="<c:out value='${debt.dueDate}'/>"
                                        data-status="<c:out value='${debt.status}'/>"
                                        data-note="<c:out value='${debt.note}'/>"
                                        data-userid="<c:out value='${debt.userId}'/>"
                                        data-bs-toggle="offcanvas" data-bs-target="#debtOffcanvas"
                                        title="Sửa khoản nợ">
                                    <i class="fa fa-pen"></i>
                                </button>

                                <form action="${pageContext.request.contextPath}/debt" method="post" style="display:inline-block;">
                                    <input type="hidden" name="action" value="delete"/>
                                    <input type="hidden" name="id" value="${debt.id}"/>
                                    <button type="submit" class="btn btn-sm btn-action btn-delete" onclick="return confirm('Bạn có chắc chắn muốn xoá khoản nợ này?')" title="Xóa khoản nợ">
                                        <i class="fa fa-trash"></i>
                                    </button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<div class="offcanvas offcanvas-end" tabindex="-1" id="debtOffcanvas" aria-labelledby="debtOffcanvasLabel">
    <div class="offcanvas-header">
        <h5 id="debtOffcanvasLabel">Thêm khoản nợ</h5>
        <button type="button" class="btn-close text-reset" data-bs-dismiss="offcanvas"></button>
    </div>
    <div class="offcanvas-body">
        <form id="debtForm" action="${pageContext.request.contextPath}/debt" method="post">
            <input type="hidden" name="action" id="formAction" value="add"/>
            <input type="hidden" name="id" id="debtId"/>

            <c:set var="sessionUserId" value="${sessionScope.userId}" />

            <c:choose>
                <c:when test="${not empty users}">
                    <div class="mb-3">
                        <label class="form-label">Người dùng</label>
                        <select name="userId" id="userId" class="form-select" required>
                            <c:forEach var="u" items="${users}">
                                <option value="${u.id}">
                                    <c:out value="${u.username}"/> - <c:out value="${u.email}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <script>
                        // server-side default selection: try to set selected option matching sessionUserId
                        (function(){
                            const s = "${sessionUserId != null ? sessionUserId : ''}";
                            if (s) {
                                // will be applied by client on DOMContentLoaded (below)
                                window.__debt_defaultUserId = s;
                            }
                        })();
                    </script>
                </c:when>
                <c:otherwise>
                    <input type="hidden" name="userId" id="userId" value="${sessionScope.userId != null ? sessionScope.userId : ''}" />
                </c:otherwise>
            </c:choose>

            <div class="mb-3">
                <label class="form-label">Chủ nợ</label>
                <input type="text" class="form-control" name="creditorName" id="creditorName" required placeholder="Nhập tên ngân hàng / cá nhân..." />
            </div>
            <div class="mb-3">
                <label class="form-label">Số tiền</label>
                <input type="number" class="form-control" name="amount" id="amount" required min="0" step="0.01" placeholder="vd: 1500000" />
            </div>
            <div class="mb-3">
                <label class="form-label">Ngày đáo hạn</label>
                <input type="date" class="form-control" name="dueDate" id="dueDate" required />
            </div>
            <div class="mb-3">
                <label class="form-label">Trạng thái</label>
                <select name="status" id="status" class="form-select">
                    <option value="PENDING">Chưa trả</option>
                    <option value="PAID">Đã trả</option>
                    <option value="OVERDUE">Quá hạn</option>
                </select>
            </div>
            <div class="mb-3">
                <label class="form-label">Ghi chú</label>
                <textarea class="form-control" name="note" id="note" rows="4" placeholder="Ghi chú thêm (nếu cần)"></textarea>
            </div>

            <div class="d-flex justify-content-end gap-2">
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="offcanvas">Huỷ</button>
                <button type="submit" class="btn btn-primary">Lưu</button>
            </div>
        </form>
    </div>
</div>

<!-- Stats modal -->
<div class="modal fade" id="statsModal" tabindex="-1" aria-labelledby="statsModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title"><i class="fa fa-chart-simple me-2"></i>Thống kê nợ</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <p class="mb-1"><strong>Tổng nợ chưa trả:</strong> <c:out value="${totalUnpaidStr}"/></p>
                <p class="mb-3"><strong>Tổng nợ quá hạn:</strong> <c:out value="${totalOverdueStr}"/></p>

                <div class="row">
                    <div class="col-md-6">
                        <h6>Danh sách khoản quá hạn</h6>
                        <c:choose>
                            <c:when test="${empty overdueList}">
                                <div class="text-muted">Không có khoản nợ quá hạn.</div>
                            </c:when>
                            <c:otherwise>
                                <ul class="list-group mb-3">
                                    <c:forEach var="od" items="${overdueList}">
                                        <li class="list-group-item d-flex justify-content-between align-items-center">
                                            <div>
                                                <strong><c:out value="${od.creditorName}"/></strong>
                                                <div class="small text-muted">Ngày đáo hạn: <c:out value="${od.dueDate}"/></div>
                                            </div>
                                            <span><c:out value="${od.formattedAmount}"/></span>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="col-md-6">
                        <h6>Danh sách sắp đến hạn (≤ 3 ngày)</h6>
                        <c:choose>
                            <c:when test="${empty nearDueList}">
                                <div class="text-muted">Không có khoản sắp đến hạn.</div>
                            </c:when>
                            <c:otherwise>
                                <ul class="list-group">
                                    <c:forEach var="nd" items="${nearDueList}">
                                        <li class="list-group-item d-flex justify-content-between align-items-center">
                                            <div>
                                                <strong><c:out value="${nd.creditorName}"/></strong>
                                                <div class="small text-muted">Ngày đáo hạn: <c:out value="${nd.dueDate}"/> — Còn <strong><c:out value="${nd.daysUntilDue}"/></strong> ngày</div>
                                            </div>
                                            <span><c:out value="${nd.formattedAmount}"/></span>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[title]'))
        tooltipTriggerList.forEach(function (el) {
            new bootstrap.Tooltip(el, {delay: { "show": 150, "hide": 50 }});
        });

        const form = document.getElementById('debtForm');
        const formAction = document.getElementById('formAction');
        const debtId = document.getElementById('debtId');
        const userIdEl = document.getElementById('userId');
        let defaultUserId = '';

        if (userIdEl) {
            defaultUserId = userIdEl.value || (window.__debt_defaultUserId || '');
            // if select exists and we have a server-provided default, try to select it
            if (userIdEl.tagName === 'SELECT' && window.__debt_defaultUserId) {
                try { userIdEl.value = window.__debt_defaultUserId; } catch(e){ }
            }
        }

        const searchBox = document.getElementById("searchBox");
        if (searchBox) {
            searchBox.addEventListener("input", function() {
                const query = this.value.trim().toLowerCase();
                document.querySelectorAll("#debtTableBody tr").forEach(row => {
                    const creditor = (row.dataset.creditor || '').toLowerCase();
                    const status = (row.dataset.status || '').toLowerCase();
                    const id = (row.dataset.id || '').toLowerCase();
                    const userid = (row.dataset.userid || '').toLowerCase();
                    const visible = !query || creditor.includes(query) || status.includes(query) || id.includes(query) || userid.includes(query);
                    row.style.display = visible ? "" : "none";
                });
            });
        }

        document.querySelectorAll(".js-edit-btn").forEach(btn => {
            btn.addEventListener("click", function() {
                formAction.value = "edit";
                debtId.value = this.dataset.id || "";
                document.getElementById("creditorName").value = this.dataset.creditor || "";
                document.getElementById("amount").value = this.dataset.amount || "";
                document.getElementById("dueDate").value = this.dataset.duedate || "";
                document.getElementById("status").value = this.dataset.status || "PENDING";
                document.getElementById("note").value = this.dataset.note || "";
                const uid = this.dataset.userid || defaultUserId;
                if (userIdEl) userIdEl.value = uid;
                document.getElementById("debtOffcanvasLabel").innerText = "Chỉnh sửa khoản nợ";
            });
        });

        const btnAdd = document.getElementById("btnAddDebt");
        if (btnAdd) {
            btnAdd.addEventListener("click", function () {
                formAction.value = "add";
                form.reset();
                debtId.value = "";
                // restore default user selection after reset
                if (userIdEl) {
                    try { userIdEl.value = defaultUserId; } catch(e){}
                }
                document.getElementById("debtOffcanvasLabel").innerText = "Thêm khoản nợ";
            });
        }

        const offcanvasEl = document.getElementById('debtOffcanvas');
        if (offcanvasEl) {
            offcanvasEl.addEventListener('hidden.bs.offcanvas', function () {
                formAction.value = "add";
                if (userIdEl) {
                    try { userIdEl.value = defaultUserId; } catch(e){}
                }
            });
        }
    });
</script>
