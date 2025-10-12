
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
  <title>Đăng nhập - BudgetBuddy</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;600&display=swap" rel="stylesheet">
  <style>
    body {
      font-family: 'Poppins', sans-serif;
      background: linear-gradient(120deg, #f8fafc 0%, #e0e7ff 100%);
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .login-container {
      background: #fff;
      border-radius: 16px;
      box-shadow: 0 4px 24px rgba(0,0,0,0.08);
      padding: 32px 28px;
      width: 100%;
      max-width: 400px;
    }
    .logo {
      text-align: center;
      margin-bottom: 16px;
    }
    .logo i {
      font-size: 2.5rem;
      color: #6366f1;
    }
    h2 {
      text-align: center;
      font-weight: 600;
      color: #374151;
      margin-bottom: 8px;
    }
    .msg {
      text-align: center;
      margin-bottom: 12px;
    }
    .msg.error { color: #ef4444; }
    .msg.success { color: #22c55e; }
    form {
      display: flex;
      flex-direction: column;
      gap: 14px;
    }
    input {
      padding: 10px 12px;
      border-radius: 8px;
      border: 1px solid #d1d5db;
      font-size: 1rem;
    }
    button {
      padding: 10px;
      border-radius: 8px;
      border: none;
      background: #6366f1;
      color: #fff;
      font-weight: 600;
      font-size: 1rem;
      cursor: pointer;
      transition: background 0.2s;
    }
    button:hover {
      background: #4f46e5;
    }
    .google-btn {
      background: #fff;
      color: #374151;
      border: 1px solid #d1d5db;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
    }
    .google-btn img {
      width: 20px;
      height: 20px;
    }
    .links {
      text-align: center;
      margin-top: 10px;
    }
    .links a {
      color: #6366f1;
      text-decoration: none;
      margin: 0 8px;
      font-size: 0.98rem;
    }
    @media (max-width: 500px) {
      .login-container { padding: 18px 8px; }
    }
  </style>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
<div class="login-container">
  <div class="logo">
    <i class="fa-solid fa-wallet"></i>
  </div>
  <h2>Đăng nhập BudgetBuddy</h2>
  <c:if test="${not empty error}"><div class="msg error">${error}</div></c:if>
  <c:if test="${not empty success}"><div class="msg success">${success}</div></c:if>
  <form method="post" action="${pageContext.request.contextPath}/auth/login">
    <input type="email" name="email" placeholder="Email" required />
    <input type="password" name="password" placeholder="Mật khẩu" required />
    <button type="submit">Đăng nhập</button>
  </form>
  <form method="get" action="${pageContext.request.contextPath}/auth/google/start" style="margin-top:10px;">
    <button type="submit" class="google-btn">
      <span style="display:flex;align-items:center;gap:6px;">
        <i class="fa-brands fa-google" style="color:#4285F4;font-size:20px;"></i>
        <span>Đăng nhập với Google</span>
      </span>
    </button>
  </form>
  <div class="links">
    <a href="${pageContext.request.contextPath}/views/auth/register.jsp">Đăng ký tài khoản</a>
    <a href="${pageContext.request.contextPath}/views/auth/forgot.jsp">Quên mật khẩu?</a>
  </div>
</div>
</body>
</html>
  