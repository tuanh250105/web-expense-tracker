<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty error}"><div style="color:red;">${error}</div></c:if>
<c:if test="${not empty success}"><div style="color:green;">${success}</div></c:if>
<form method="post" action="${pageContext.request.contextPath}/auth/register">
    <input type="text" name="fullName" placeholder="Họ tên" required />
    <input type="email" name="email" placeholder="Email" required />
    <input type="password" name="password" placeholder="Mật khẩu" required />
    <button type="submit">Đăng ký</button>
  </form>
  <a href="${pageContext.request.contextPath}/views/auth/login.jsp">Đã có tài khoản? Đăng nhập</a>
  