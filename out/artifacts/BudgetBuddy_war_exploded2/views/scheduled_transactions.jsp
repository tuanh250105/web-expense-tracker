<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/scheduled_transactions.css" />
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">

<div id="content" class="container">
    <div class="filter-section card">
        <div class="filter-header">
            <h3>Bộ lọc</h3>
        </div>
        <form id="filterForm" method="get" action="${pageContext.request.contextPath}/scheduled_transactions">
            <input type="hidden" name="action" value="list"/>

            <div class="filter-grid">
                <div>
                    <label>Hạng mục</label>
                    <select name="categorySelect" id="categorySelect" style="width:100%; margin-bottom:5px;">
                        <option value="">Tất cả</option>
                        <c:forEach var="cat" items="${allCategories}">
                            <option value="${cat.name}" ${param.categorySelect == cat.name ? 'selected' : ''}>${cat.name} (${cat.type == 'income' ? 'Thu' : 'Chi'})</option>
                        </c:forEach>
                    </select>
                    <input type="text" name="category" placeholder="Tìm kiếm chi tiết..." value="${param.category}" style="width:100%;">
                </div>

                <div class="filter-group">
                    <label>Tài khoản</label>
                    <select name="account">
                        <option value="">Tất cả tài khoản...</option>
                        <c:forEach var="acc" items="${allAccounts}">
                            <option value="${acc.name}">${acc.name}</option>
                        </c:forEach>
                    </select>
                </div>
                <div>
                    <label>Từ ngày</label>
                    <input type="date" name="from" value="${param.from}">
                </div>
                <div>
                    <label>Đến ngày</label>
                    <input type="date" name="to" value="${param.to}">
                </div>
                <div style="grid-column: span 2;">
                    <label>Ghi chú</label>
                    <input type="text" name="note" placeholder="Nhập ghi chú..." value="${param.note}">
                </div>
            </div>

            <div class="filter-checkboxes">
                <label><input type="checkbox" name="types" value="expense" ${fn:contains(paramValues.types, 'expense') ? 'checked' : ''}/> Chi phí</label>
                <label><input type="checkbox" name="types" value="income" ${fn:contains(paramValues.types, 'income') ? 'checked' : ''}/> Thu nhập</label>
                <label><input type="checkbox" name="types" value="transfer" ${fn:contains(paramValues.types, 'transfer') ? 'checked' : ''}/> Chuyển khoản</label>
                <label><input type="checkbox" name="types" value="loan" ${fn:contains(paramValues.types, 'loan') ? 'checked' : ''}/> Vay / Cho vay</label>
            </div>

            <div class="filter-buttons">
                <button type="submit" class="btn-primary">Lọc</button>
                <button type="button" id="resetFilter" class="btn-secondary">Hủy</button>
            </div>
        </form>
    </div>

    <div class="list-section card">
        <h3>Danh sách giao dịch định kỳ</h3>

        <c:choose>
            <c:when test="${empty transactions}">
                <div class="no-data">
                    <p>Chưa có giao dịch định kỳ nào. <a href="#" id="addIncomeBtn">Thêm thu nhập</a> hoặc <a href="#" id="addExpenseBtn">thêm chi phí</a>.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="transaction-cards">
                    <c:forEach var="transaction" items="${transactions}">
                        <div class="transaction-card">
                            <div class="card-header">
                                <span class="category-icon">
                                    <c:choose>
                                        <c:when test="${transaction.type == 'income'}">
                                            <i class="fas fa-arrow-up"></i>
                                        </c:when>
                                        <c:otherwise>
                                            <i class="fas fa-arrow-down"></i>
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                                <h4>${transaction.categoryName}</h4>
                                <span class="badge ${transaction.type}">
                                    <c:choose>
                                        <c:when test="${transaction.type == 'income'}">Thu nhập</c:when>
                                        <c:otherwise>Chi phí</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <div class="card-body">
                                <p><strong>Số tiền:</strong>
                                    <span class="${transaction.type == 'income' ? 'text-success' : 'text-danger'}">
                                        <c:choose>
                                            <c:when test="${transaction.type == 'income'}">
                                                +<fmt:formatNumber value="${transaction.amount}" type="number" pattern="#,###" /> VND
                                            </c:when>
                                            <c:otherwise>
                                                -<fmt:formatNumber value="${transaction.amount}" type="number" pattern="#,###" /> VND
                                            </c:otherwise>
                                        </c:choose>
                                    </span>
                                </p>
                                <p><strong>Ngày chạy tiếp:</strong>
                                    <fmt:formatDate value="${transaction.nextRun}" pattern="dd/MM/yyyy" />
                                </p>
                                <p><strong>Ghi chú:</strong> ${transaction.note != null ? transaction.note : 'Không có'}</p>
                                <p><strong>Lặp lịch:</strong> ${transaction.repeatLabel}</p>

                            </div>
                            <div class="card-actions">
                                <button class="btn btn-danger" onclick="deleteTransaction('${transaction.id}')">
                                    <i class="fas fa-trash"></i> Xóa giao dịch
                                </button>
                                <button class="btn btn-warning" onclick="skipTransaction('${transaction.id}')">
                                    <i class="fas fa-forward"></i> Bỏ qua lặp lại
                                </button>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="add-buttons">
        <button id="addIncomeBtn" class="add-btn income"></button>
        <button id="addExpenseBtn" class="add-btn expense"></button>
    </div>

    <script>
        var contextPath = '${pageContext.request.contextPath}';
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/scheduled_transactions.js"></script>
</div>