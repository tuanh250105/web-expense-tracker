<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý Ngân sách - BudgetBuddy</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        h1 {
            color: #333;
            border-bottom: 3px solid #4CAF50;
            padding-bottom: 10px;
        }
        .message {
            padding: 15px;
            margin-bottom: 20px;
            border-radius: 5px;
        }
        .success {
            background-color: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .error {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        .budget-card {
            border: 1px solid #ddd;
            border-radius: 8px;
            padding: 20px;
            margin-bottom: 15px;
            background: #fafafa;
            transition: box-shadow 0.3s;
        }
        .budget-card:hover {
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        }
        .budget-card h3 {
            margin-top: 0;
            color: #2196F3;
        }
        .budget-date {
            color: #666;
            font-size: 14px;
            margin: 10px 0;
        }
        .budget-actions {
            margin-top: 15px;
        }
        .btn {
            padding: 8px 16px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 14px;
            margin-right: 10px;
            text-decoration: none;
            display: inline-block;
        }
        .btn-primary {
            background-color: #4CAF50;
            color: white;
        }
        .btn-danger {
            background-color: #f44336;
            color: white;
        }
        .btn-warning {
            background-color: #ff9800;
            color: white;
        }
        .btn:hover {
            opacity: 0.8;
        }
        .add-form {
            background: #f9f9f9;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 30px;
        }
        .form-group {
            margin-bottom: 15px;
        }
        .form-group label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
            color: #555;
        }
        .form-group input,
        .form-group select,
        .form-group textarea {
            width: 100%;
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 4px;
            box-sizing: border-box;
        }
        .progress-bar {
            background: #eee;
            border-radius: 5px;
            height: 20px;
            margin: 10px 0;
        }
        .progress-fill {
            background: #4CAF50;
            height: 100%;
            border-radius: 5px;
            transition: width 0.3s;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>Quản lý Ngân sách</h1>

    <c:if test="${not empty successMessage}">
        <div class="message success">${successMessage}</div>
        <% session.removeAttribute("successMessage"); %>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="message error">${errorMessage}</div>
        <% session.removeAttribute("errorMessage"); %>
    </c:if>

    <!-- Add/Edit Form -->
    <div class="add-form">
        <form method="post" action="${pageContext.request.contextPath}/budget">
            <input type="hidden" name="action" value="${not empty editBudget ? 'update' : 'add'}">
            <c:if test="${not empty editBudget}">
                <input type="hidden" name="id" value="${editBudget.id}">
            </c:if>

            <div class="form-group">
                <label for="categoryId">Danh mục *</label>
                <select id="categoryId" name="categoryId" required>
                    <!-- Giả sử list categories từ request attribute "categories" -->
                    <c:forEach items="${categories}" var="cat">
                        <option value="${cat.id}" ${editBudget.category.id == cat.id ? 'selected' : ''}>${cat.name}</option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-group">
                <label for="limitAmount">Hạn mức (VNĐ) *</label>
                <input type="number" id="limitAmount" name="limitAmount" min="0"
                       value="${editBudget.limitAmount}" required>
            </div>

            <div class="form-group">
                <label for="startDate">Ngày bắt đầu *</label>
                <input type="date" id="startDate" name="startDate"
                       value="${editBudget.startDate}" required>
            </div>

            <div class="form-group">
                <label for="endDate">Ngày kết thúc *</label>
                <input type="date" id="endDate" name="endDate"
                       value="${editBudget.endDate}" required>
            </div>

            <button type="submit" class="btn btn-primary">
                ${not empty editBudget ? 'Cập nhật' : 'Thêm mới'}
            </button>
            <c:if test="${not empty editBudget}">
                <a href="${pageContext.request.contextPath}/budget" class="btn btn-warning">Hủy</a>
            </c:if>
        </form>
    </div>

    <!-- Budget List -->
    <h2>Danh sách ngân sách</h2>

    <c:choose>
        <c:when test="${empty budgets}">
            <p style="text-align: center; color: #999; padding: 40px;">
                Chưa có ngân sách nào. Hãy thêm ngân sách đầu tiên! 📊
            </p>
        </c:when>
        <c:otherwise>
            <c:forEach items="${budgets}" var="budget">
                <div class="budget-card">
                    <h3>${budget.category.name}</h3>
                    <p class="budget-date">
                        <strong>📅 Từ:</strong> ${budget.startDate}
                        <strong>đến:</strong> ${budget.endDate}
                    </p>
                    <p>
                        <strong>💰 Hạn mức:</strong> ${budget.limitAmount} VNĐ
                    </p>
                    <p>
                        <strong>Chi tiêu hiện tại:</strong> ${budget.spentAmount} VNĐ
                    </p>
                    <div class="progress-bar">
                        <div class="progress-fill" style="width: ${budgetService.calculateSpentPercentage(budget.id)}%;"></div>
                    </div>
                    <p>${budgetService.calculateSpentPercentage(budget.id)}% đã chi</p>

                    <div class="budget-actions">
                        <a href="${pageContext.request.contextPath}/budget?action=edit&budgetId=${budget.id}"
                           class="btn btn-warning">✏️ Sửa</a>

                        <form method="post" action="${pageContext.request.contextPath}/budget"
                              style="display: inline;"
                              onsubmit="return confirm('Bạn có chắc muốn xóa ngân sách này?');">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="${budget.id}">
                            <button type="submit" class="btn btn-danger">🗑️ Xóa</button>
                        </form>
                    </div>
                </div>
            </c:forEach>
        </c:otherwise>
    </c:choose>

    <br>
    <a href="${pageContext.request.contextPath}/" class="btn btn-primary">← Quay lại trang chủ</a>
</div>
</body>
</html>