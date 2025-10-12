<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<h3>Đặt lại mật khẩu</h3>
<c:if test="${not empty error}"><div style="color:red;">${error}</div></c:if>
<c:if test="${not empty success}"><div style="color:green;">${success}</div></c:if>
<form method="post" action="${pageContext.request.contextPath}/auth/reset">
	<input type="email" name="email" placeholder="Email" required />
	<input type="text" name="otp" placeholder="OTP 6 chữ số" required />
	<input type="password" name="newPassword" placeholder="Mật khẩu mới" required />
	<button type="submit">Cập nhật mật khẩu</button>
	<br/>
	<a href="${pageContext.request.contextPath}/views/auth/login.jsp">Quay lại đăng nhập</a>
</form>
