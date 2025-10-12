<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty error}"><div style="color:red;">${error}</div></c:if>
<c:if test="${not empty success}"><div style="color:green;">${success}</div></c:if>
<form method="post" action="${pageContext.request.contextPath}/auth/login">
  <input type="email" name="email" placeholder="Email" required />
  <input type="password" name="password" placeholder="Mật khẩu" required />
  <button type="submit">Đăng nhập</button>
</form>
<a href="${pageContext.request.contextPath}/auth/google/start">Đăng nhập với Google</a>
<br/>
<a href="${pageContext.request.contextPath}/views/auth/register.jsp">Đăng ký</a>
<br/>
<a href="${pageContext.request.contextPath}/views/auth/forgot.jsp">Quên mật khẩu</a>
  