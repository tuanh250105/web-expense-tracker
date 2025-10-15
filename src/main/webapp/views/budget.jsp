<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/budget.css">
<div class="budget-container">
    <h1 class="budget-title">Quản lý Ngân sách</h1>

    <c:if test="${not empty successMessage}">
        <div class="message success">${successMessage}</div>
        <% session.removeAttribute("successMessage"); %>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="message error">${errorMessage}</div>
        <% session.removeAttribute("errorMessage"); %>
    </c:if>

    <!-- Add/Edit Form -->
    <div class="budget-form">
        <form method="post" action="${pageContext.request.contextPath}/budget" class="form-wrapper">
            <input type="hidden" name="action" value="${not empty editBudget ? 'update' : 'add'}">
            <c:if test="${not empty editBudget}">
                <input type="hidden" name="id" value="${editBudget.id}">
            </c:if>

            <div class="form-group">
                <label for="categoryId" class="form-label"><strong>Danh mục *</strong></label>
                <select id="categoryId" name="categoryId" class="form-select" required>
                    <c:forEach items="${categories}" var="cat">
                        <option value="${cat.id}" ${editBudget.category.id == cat.id ? 'selected' : ''}>${cat.name}</option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-group">
                <label for="limitAmount" class="form-label"><strong>Hạn mức (VNĐ) *</strong></label>
                <input type="number" id="limitAmount" name="limitAmount" min="0" class="form-input" value="${editBudget.limitAmount}" required>
            </div>

            <div class="form-group">
                <label for="startDate" class="form-label"><strong>Ngày bắt đầu *</strong></label>
                <input type="date" id="startDate" name="startDate" class="form-input" value="${editBudget.startDate}" required>
            </div>

            <div class="form-group">
                <label for="endDate" class="form-label"><strong>Ngày kết thúc *</strong></label>
                <input type="date" id="endDate" name="endDate" class="form-input" value="${editBudget.endDate}" required>
            </div>

            <button type="submit" class="btn btn-primary">
                <strong>${not empty editBudget ? 'Cập nhật' : 'Thêm mới'}</strong>
            </button>
        </form>
    </div>

    <!-- Budget List -->
    <h2 class="budget-subtitle">Danh sách ngân sách</h2>

    <c:choose>
        <c:when test="${empty budgets}">
            <div class="no-budget-message">
                <p><strong>Chưa có ngân sách nào.</strong> Hãy thêm ngân sách đầu tiên! 📊</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="budget-list">
                <c:forEach items="${budgets}" var="budget">
                    <div class="budget-card">
                        <h3 class="budget-card-title"><strong>${budget.category.name}</strong></h3>
                        <p class="budget-date">
                            <strong>📅 Từ:</strong> ${budget.startDate} <strong>đến:</strong> ${budget.endDate}
                        </p>
                        <p class="budget-amount">
                            <strong>💰 Hạn mức:</strong> <strong>${budget.limitAmount}</strong> VNĐ
                        </p>
                        <p class="budget-spent">
                            <strong>Chi tiêu hiện tại:</strong> <strong><span class="spent-value">${budget.spentAmount}</span></strong> VNĐ
                        </p>
                        <p class="budget-remaining">
                            <strong>Số dư còn lại:</strong> <strong><span class="remaining-value">${budgetService.calculateRemaining(budget.id)}</span></strong> VNĐ
                        </p>
                        <div class="progress-bar">
                            <div class="progress-fill" style="width: ${budgetService.calculateSpentPercentage(budget.id)}%;"></div>
                        </div>
                        <p class="progress-text">${budgetService.calculateSpentPercentage(budget.id)}% đã chi</p>

                        <div class="transactions-section">
                            <h4 class="transactions-title"><strong>Danh sách giao dịch chi tiêu:</strong></h4>
                            <ul class="transactions-list">
                                <c:forEach items="${budgetService.getTransactionsForBudget(budget.id)}" var="transaction">
                                    <li class="transaction-item">
                                        <strong>Ngày:</strong> ${transaction.transactionDate} -
                                        <strong>Số tiền:</strong> <strong>${transaction.amount}</strong> VNĐ -
                                        <strong>Ghi chú:</strong> ${transaction.description}
                                    </li>
                                </c:forEach>
                                <c:if test="${empty budgetService.getTransactionsForBudget(budget.id)}">
                                    <li class="transaction-item"><strong>Chưa có giao dịch chi tiêu nào</strong> trong khoảng thời gian này.</li>
                                </c:if>
                            </ul>
                        </div>

                        <div class="budget-actions">
                            <a href="${pageContext.request.contextPath}/budget?action=edit&budgetId=${budget.id}" class="btn btn-warning">
                                <i class="fas fa-edit"></i> <strong>Sửa</strong>
                            </a>
                            <form method="post" action="${pageContext.request.contextPath}/budget" style="display: inline-block;"
                                  onsubmit="return confirm('Bạn có chắc muốn xóa ngân sách này?');">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="id" value="${budget.id}">
                                <button type="submit" class="btn btn-danger">
                                    <i class="fas fa-trash"></i> <strong>Xóa</strong>
                                </button>
                            </form>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>

    <br>
    <a href="${pageContext.request.contextPath}/" class="btn btn-back"><strong>← Quay lại trang chủ</strong></a>
</div>