<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<h3>Quên mật khẩu</h3>
<c:if test="${not empty error}"><div style="color:red;">${error}</div></c:if>
<c:if test="${not empty success}"><div style="color:green;">${success}</div></c:if>
<form method="post" action="${pageContext.request.contextPath}/auth/forgot">
	<input type="email" name="email" placeholder="Email" required />
	<button type="submit">Gửi OTP</button>
	<p>Sau khi nhận OTP qua email, hãy truy cập trang đặt lại mật khẩu.</p>
	<a href="${pageContext.request.contextPath}/views/auth/reset.jsp">Đặt lại mật khẩu</a>
	<br/>
	<a href="${pageContext.request.contextPath}/views/auth/login.jsp">Quay lại đăng nhập</a>
</form>
