


<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
  <title>Đặt lại mật khẩu - BudgetBuddy</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;600&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
  <style>
    body {
      font-family: 'Poppins', sans-serif;
      background: linear-gradient(120deg, #6366f1 0%, #60a5fa 40%, #84addf 70%, #b6b6d4 100%);
      background-size: 300% 300%;
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      animation: gradientMove 12s ease-in-out infinite alternate;
    }
    @keyframes gradientMove {
      0% {
        background-position: 0% 50%;
      }
      50% {
        background-position: 100% 50%;
      }
      100% {
        background-position: 0% 50%;
      }
    }
    .reset-container {
      background: rgba(255,255,255,0.08);
      border-radius: 24px;
      box-shadow: 0 15px 50px 0 rgba(0, 0, 0, 0.45);
      padding: 40px 32px;
      width: 100%;
      max-width: 400px;
      position: relative;
      overflow: hidden;
      animation: fadeIn 1s;
      backdrop-filter: blur(12px);
      -webkit-backdrop-filter: blur(12px);
      border: 1px solid rgba(255,255,255,0.12);
    }
    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(40px); }
      to { opacity: 1; transform: none; }
    }
    .logo {
      text-align: center;
      margin-bottom: 18px;
    }
    .logo i {
      font-size: 3rem;
      background: linear-gradient(90deg, #6366f1 0%, #60a5fa 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      color: transparent;
      animation: bounce 1.2s infinite alternate;
    }
    @keyframes bounce {
      from { transform: translateY(0); }
      to { transform: translateY(-10px); }
    }
    h2 {
      text-align: center;
      font-weight: 700;
      color: #374151;
      margin-bottom: 12px;
      letter-spacing: 1px;
    }
    .msg {
      text-align: center;
      margin-bottom: 14px;
      font-size: 1.05rem;
      transition: all 0.3s;
    }
    .msg.error { color: #ef4444; animation: shake 0.4s; }
    .msg.success { color: #22c55e; animation: fadeIn 0.7s; }
    @keyframes shake {
      0% { transform: translateX(0); }
      25% { transform: translateX(-6px); }
      50% { transform: translateX(6px); }
      75% { transform: translateX(-6px); }
      100% { transform: translateX(0); }
    }
    form {
      display: flex;
      flex-direction: column;
      gap: 18px;
      margin-bottom: 10px;
    }
    .input-group {
      position: relative;
      display: flex;
      align-items: center;
      width: 100%;
      background: rgba(255, 255, 255, 0.18);
      border-radius: 16px;
      box-shadow: 0 4px 30px rgba(0, 0, 0, 0.1);
      backdrop-filter: blur(5.1px);
      -webkit-backdrop-filter: blur(5.1px);
    }
    .input-group i {
      position: absolute;
      left: 12px;
      color: #4a4de6;
      font-size: 1.1rem;
      opacity: 0.7;
      transition: color 0.2s;
    }
    input {
      padding: 10px 12px 10px 36px;
      border-radius: 10px;
      border: 1px solid #6099f0;
      font-size: 1rem;
      background: #f3f4f6;
      color: #374151;
      transition: border 0.2s, box-shadow 0.2s;
      width: 100%;
      box-sizing: border-box;
      display: block;
    }
    input:focus {
      border: 1.5px solid #6366f1;
      box-shadow: 0 0 0 2px #6366f133;
      outline: none;
    }
    button {
      padding: 12px;
      border-radius: 10px;
      border: none;
      background: linear-gradient(90deg, #6366f1 0%, #60a5fa 100%);
      color: #fff;
      font-weight: 700;
      font-size: 1.08rem;
      cursor: pointer;
      box-shadow: 0 2px 8px rgba(99,102,241,0.10);
      transition: background 0.2s, transform 0.2s;
      letter-spacing: 0.5px;
      width: 100%;
      display: block;
    }
    button:hover {
      background: linear-gradient(90deg, #60a5fa 0%, #6366f1 100%);
      transform: translateY(-2px) scale(1.03);
    }
    .links {
      text-align: center;
      margin-top: 16px;
      animation: fadeIn 1.2s;
    }
    .links a {
      color: #6366f1;
      text-decoration: none;
      margin: 0 10px;
      font-size: 1rem;
      font-weight: 500;
      transition: color 0.2s;
    }
    .links a:hover {
      color: #60a5fa;
      text-decoration: underline;
    }
    @media (max-width: 500px) {
      .reset-container { padding: 18px 6px; }
      h2 { font-size: 1.2rem; }
    }
  </style>
</head>
<body>
<div class="reset-container">
  <div class="logo">
    <i class="fa-solid fa-wallet"></i>
  </div>
  <h2>Đặt lại mật khẩu</h2>
  <c:if test="${not empty error}"><div class="msg error">${error}</div></c:if>
  <c:if test="${not empty success}"><div class="msg success">${success}</div></c:if>
  <form method="post" action="${pageContext.request.contextPath}/auth/reset">
    <div class="input-group">
      <i class="fa-solid fa-envelope"></i>
      <input type="email" name="email" placeholder="Email" required />
    </div>
    <div class="input-group">
      <i class="fa-solid fa-key"></i>
      <input type="text" name="otp" placeholder="Mã OTP" required />
    </div>
    <div class="input-group">
      <i class="fa-solid fa-lock"></i>
      <input type="password" name="newPassword" placeholder="Mật khẩu mới" required />
    </div>
    <button type="submit">Đặt lại mật khẩu</button>
  </form>
  <div class="links">
    <a href="${pageContext.request.contextPath}/views/auth/login.jsp">Quay lại đăng nhập</a>
    <a href="${pageContext.request.contextPath}/views/auth/register.jsp">Đăng ký mới</a>
  </div>
</div>
</body>
</html>
