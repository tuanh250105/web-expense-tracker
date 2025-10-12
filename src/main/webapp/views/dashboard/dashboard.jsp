<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<h2>Dashboard</h2>
<p>Xin chào, <c:out value="${sessionScope.user.fullName}"/></p>
<a href="${pageContext.request.contextPath}/auth/logout">Đăng xuất</a>
