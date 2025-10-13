# 📊 BudgetBuddy

## Budgets & Events Modules

Xem `CHANGES_BUDGET_EVENT.md` để biết file mới, URLs test và hướng dẫn deploy.

BudgetBuddy là ứng dụng web quản lý chi tiêu cá nhân, được xây dựng bằng **Java (Servlet/JSP)**, chạy trên **Tomcat 10 + JDK 21**, sử dụng **PostgreSQL 15** làm cơ sở dữ liệu.  
Dự án hỗ trợ Docker để đảm bảo tất cả thành viên phát triển trong cùng một môi trường đồng bộ.

---

## 🚀 Công nghệ sử dụng
- **Backend**: Java Servlet/JSP (Jakarta EE 10)
- **Build tool**: Maven (đóng gói WAR)
- **Server**: Tomcat 10.1
- **JDK**: 21 (LTS)
- **Database**: PostgreSQL 15
- **DB Admin**: Adminer (quản lý database qua giao diện web)
- **Docker**: quản lý môi trường phát triển đồng bộ cho cả team

---

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

---

## ⚙️ Chuẩn bị
1. Cài **Docker** và **Docker Compose**.
2. Cài **IntelliJ IDEA** (hoặc IDE khác hỗ trợ Maven).
3. Lấy file `.env` từ leader

Ví dụ file `.env`:
```env
POSTGRES_USER=budget_user
POSTGRES_PASSWORD=budget_pass
POSTGRES_DB=budget_db

DB_USER=budget_user
DB_PASS=budget_pass
DB_URL=jdbc:postgresql://db:5432/budget_db
```
---

## **▶️ Cách chạy dự án**
1. Chạy với Docker (đồng bộ toàn bộ stack)
docker compose up -d --build
App chạy tại: http://localhost:8080/BudgetBuddy
Adminer: http://localhost:8081 (đăng nhập với budget_user / budget_pass)
2. Chạy trong IntelliJ (dev & debug)
Start DB trước:
docker compose up -d db adminer
Import project vào IntelliJ (Maven project).
Run app bằng Tomcat local trong IntelliJ.
App kết nối DB qua localhost:5432.

## **👨‍💻 Quy trình phát triển**

**Clone repo:**
git clone <repo>
cd BudgetBuddy
Copy file .env
**Start database:**
docker compose up -d db adminer
Code feature riêng → tạo branch:
git checkout -b feature/login
**Build WAR để test:**
mvn clean package
**com.expensemanager.controller.Test app trên local Tomcat hoặc bằng Docker** (docker compose up app).
Push branch → tạo Pull Request → Leader review & merge vào dev.

