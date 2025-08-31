
📌 GitHub Repository: [web-expense-tracker](https://github.com/tuanh250105/web-expense-tracker)

## 🚀 Hướng dẫn clone dự án

Để tải dự án này về máy, bạn làm theo các bước sau:

### 1. Clone repository
Mở terminal và chạy lệnh:
```bash
git clone https://github.com/tuanh250105/web-expense-tracker.git
```

### 2. Di chuyển và mở dự án
Mở terminal và chạy lệnh:
```bash 
cd web-expense-tracker
code .
```
## 3. Chuyển sang branch cần làm việc
```bash
git checkout "tên branch"
```
=======

## 🚀 Công nghệ
- **Frontend:** HTML, CSS, Thymeleaf (Spring Boot render), Chart.js  
- **Backend:** Java 17, Spring Boot 3 (Spring Web, Spring Security, Spring Data JPA, OAuth2 Client, Validation, Actuator, Mail)  
- **Database:** PostgreSQL 16  
- **Khác:** Docker, Docker Compose, Flyway (migration), Lombok, JUnit 5  
- **Dev tools:** PgAdmin, MailHog (test email)

## ⚙️ Các chức năng chính
- **Xác thực:** Đăng nhập/Đăng ký (Email/Google), quên mật khẩu (OTP qua email), lưu phiên.  
- **Dashboard:** Biểu đồ thu–chi theo tuần/tháng/năm, phân loại chi tiêu, biến động số dư.  
- **Giao dịch (Transactions):** Thêm/sửa/xóa, filter nâng cao, nhân bản nhanh.  
- **Tài khoản (Accounts):** Quản lý ví/ngân hàng, hiển thị số dư, ẩn/hiện tài khoản.  
- **Ngân sách (Budgets):** Đặt hạn mức chi tiêu theo tuần/tháng, cảnh báo khi gần vượt.  
- **Nợ (Debts):** Quản lý khoản vay/mượn, trạng thái, ngày trả.  
- **Giao dịch định kỳ (Scheduled):** Tự động thêm giao dịch lặp lại.  
- **Chi tiêu nhóm:** Chia sẻ chi phí, tính toán công nợ.  
- **Import/Export:** Nhập file CSV, xuất CSV/PDF báo cáo.

## 📂 Cấu trúc chính của dự án
```
web-expense-tracker/
├── backend/ # Spring Boot (Java)
│ ├── src/main/java/com/example/expense/
│ │ ├── auth/ # Đăng nhập, đăng ký, JWT, OAuth2
│ │ ├── account/ # Quản lý tài khoản người dùng
│ │ ├── transaction/ # Quản lý giao dịch (CRUD)
│ │ ├── budget/ # Quản lý ngân sách
│ │ ├── debt/ # Quản lý nợ
│ │ ├── group/ # Chi tiêu nhóm (chia sẻ, phân bổ)
│ │ ├── scheduler/ # Giao dịch định kỳ, nhắc nhở
│ │ └── common/ # Utils, exceptions, constants
│ │
│ ├── src/main/resources/
│ │ ├── application.yml # Config Spring Boot + PostgreSQL
│ │ └── db/migration/ # Flyway scripts (tạo bảng, seed data)
│ │
│ └── Dockerfile # Docker build cho backend
│
├── frontend/ # HTML/CSS (hoặc thêm JS/React nếu mở rộng)
│ ├── index.html # Dashboard chính
│ ├── transactions.html # Quản lý giao dịch
│ ├── budget.html # Quản lý ngân sách
│ ├── debt.html # Quản lý nợ
│ ├── group.html # Quản lý nhóm
│ ├── login.html # Đăng nhập / đăng ký
│ └── assets/ # CSS, JS, hình ảnh
│
├── docker-compose.yml # Chạy PostgreSQL + backend bằng Docker
├── README.md # Tài liệu dự án
```

