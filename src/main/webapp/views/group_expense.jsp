<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- Nạp file CSS cho trang và modal --%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/group.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modal.css">

<div class="group-expense-page">
    <div class="group-expense-container">
        <div class="group-expense-header">
            <h2>Chi Tiêu Nhóm</h2>
            <button class="btn-add-group">+ Tạo Nhóm Mới</button>
        </div>

        <%-- Hiển thị tổng chi tiêu từ dữ liệu server cung cấp --%>
        <p><strong>Tổng chi tiêu nhóm:</strong>
            <span id="groupTotal">
                <fmt:formatNumber value="${groupData.total}" type="currency" currencyCode="VND" maxFractionDigits="0"/>
            </span>
        </p>

        <div id="groupList" class="group-list">
            <%-- Dùng JSTL để render danh sách nhóm ban đầu --%>
            <c:if test="${empty groupData.details}">
                <div class="empty-state">Chưa có nhóm nào. Hãy tạo một nhóm mới!</div>
            </c:if>

            <c:forEach var="group" items="${groupData.details}">
                <div class="group-card" data-group-id="${group.id}">
                    <div class="group-card-header">
                        <h3><c:out value="${group.name}"/></h3>
                        <div class="group-card-total">
                            <fmt:formatNumber value="${group.totalAmount}" type="currency" currencyCode="VND" maxFractionDigits="0"/>
                        </div>
                    </div>
                    <div class="group-card-body">
                        <p><c:out value="${group.description}"/></p>
                        <div class="group-members">
                            <c:forEach var="member" items="${group.members}" varStatus="loop">
                                <span class="member-avatar" title="${member.name}">${fn:substring(member.name, 0, 1)}</span>
                            </c:forEach>
                        </div>
                    </div>
                    <div class="group-card-actions">
                        <button class="btn-view" data-group-id="${group.id}"><i class="fas fa-eye"></i> Xem</button>
                        <button class="btn-manage" data-group-id="${group.id}"><i class="fas fa-users"></i> Quản lý</button>
                        <button class="btn-delete" data-group-id="${group.id}"><i class="fas fa-trash"></i> Xóa</button>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
</div>

<%--
  Quan trọng: Truyền dữ liệu ban đầu từ Servlet sang JavaScript.
  JavaScript sẽ đọc biến window.initialGroupData để khởi tạo state.
--%>
<script>
    window.initialGroupData = JSON.parse('${initialGroupDataJson}');
</script>

<%-- Nạp file JS với type="module" --%>
<script type="module" src="${pageContext.request.contextPath}/assets/js/group_expense.js"></script>