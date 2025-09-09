
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
- **Frontend:** HTML, CSS, Chart.js  
- **Backend:** Java 17, Severlet 
- **Database:** PostgreSQL 16  
- **Khác:** Docker, Docker Compose, Flyway (migration), Lombok, JUnit 5  
- **Dev tools:** PgAdmin, MailHog (test email)

## ⚙️ Các chức năng chính
### 1. Đăng nhập / Đăng ký
- Hỗ trợ đăng nhập bằng Email hoặc Google Account.
- Quên mật khẩu: gửi mã xác nhận qua Email, Google hoặc SMS.
- Lưu phiên đăng nhập.

### 2. Overview
- Biểu đồ lịch sử thu – chi theo tuần, tháng, năm.
- Biểu đồ phân loại thu – chi và biến động số dư.
- Xem số dư từng ví/ngân hàng.
- Thống kê chi tiêu theo danh mục, ví, ngân hàng.
- Hỗ trợ click để xem chi tiết biểu đồ.
- Tùy chỉnh biểu đồ: sắp xếp, thêm loại biểu đồ khác.

### 3. Giao dịch (Transaction)
- Xem danh sách giao dịch theo ngày hoặc tháng.
- Lọc giao dịch theo thời gian, danh mục, loại chi tiêu, trạng thái kiểm tra.
- Chỉnh sửa giao dịch: Duplicate, Edit, Delete.
- Thêm giao dịch mới với form chi tiết.
- Giao dịch định kỳ: tự động lập các khoản thu – chi lặp lại hằng tháng hoặc ngày.

### 4. Tài khoản (Accounts)
- Quản lý ví/ngân hàng: thêm, sửa, xóa.
- Hiển thị danh sách tài khoản và số dư chi tiết.
- Hiển thị tổng số dư (loại trừ tài khoản ẩn).
- Xem chi tiết chi tiêu của từng tài khoản.

### 5. Ngân sách (Budgets)
- Đặt ngân sách theo tuần hoặc tháng.
- Chia theo mục chi tiêu từ Transaction.
- Cập nhật hạn mức, thời gian, thanh tiến độ chi tiêu.
- Xem biểu đồ chi tiêu từng ngày cho mỗi mục.
- Xem lịch sử ngân sách các tuần trước.

### 6. Quản lý nợ (Debt)
- Thêm, sửa, xóa nợ.
- Phân loại: Mượn nợ hoặc Mắc nợ.
- Theo dõi thông tin nợ: người nợ, số tiền, tài khoản, thời gian mượn/trả.

### 7. Notes & Dashboard
- Trực quan hóa dữ liệu bằng các loại biểu đồ.
- Tuỳ chỉnh: thời gian, loại thu/chi, loại biểu đồ, màu sắc.
- Xuất biểu đồ dưới dạng file.

### 8. Lịch
- Hiển thị chi tiêu theo ngày với ký hiệu trực quan.
- Click vào ngày cụ thể để xem chi tiết giao dịch.

### 9. Chi tiêu nhóm
- Tạo quỹ nhóm với nhiều thành viên.
- Thành viên thêm giao dịch cá nhân vào quỹ.
- Tự động tính toán số tiền mỗi thành viên phải đóng.
- Thay thế quản lý Excel truyền thống.

### 10. Import / Export
- Hỗ trợ file CSV hoặc PDF.
- Chọn lọc cột và chuyển vào Transaction hoặc Debt.
- Tăng khả năng nhập liệu nhanh chóng từ sao kê ngân hàng.

### 11. Giao diện & Thống kê
- Dark Mode / Light Mode.
- Thống kê Top Categories: chi tiết theo danh mục, giao dịch, nợ.
- Quản lý danh mục: thêm, sửa, xóa, sub-category.
- Hỗ trợ export báo cáo và tự động gửi sao kê qua Email.

### 12. Giải trí & Sự kiện tài chính
- Tích điểm khi hoàn thành Budget hoặc Debt đúng hạn.
- Chơi minigame với điểm tích lũy.
- Quản lý Events: các quỹ cá nhân như “Đi du lịch”, tổng hợp giao dịch theo sự kiện.
- Biểu đồ, thống kê đơn giản để tiện theo dõi.

### 13. Liên hệ & Help
- Form góp ý trực tiếp trên web.
- Câu hỏi thường gặp (FAQ) được trả lời sẵn.
- Tự động gửi sao kê hàng tháng qua email dưới dạng CSV.


## 📂 Cấu trúc chính của dự án
```
BudgetBuddy/
│
├── docker-compose.yml        # Chạy PostgreSQL (local dev, có thể thêm backend)
├── Dockerfile                # Đóng gói backend (Tomcat + war) khi deploy
├── init.sql                  # Script tạo bảng mẫu cho DB (chỉ khởi tạo)
├── .env                      # Lưu biến môi trường (DB_URL, DB_USER, DB_PASS)
│
├── db/                       # Quản lý migration (Flyway)
│   └── migration/
│       ├── V1__init.sql
│       └── V2__add_budget_table.sql
│
├── pom.xml                   # File Maven quản lý dependency, build war
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/expensemanager/
│   │   │       ├── controller/       
│   │   │       │   ├── AuthController.java
│   │   │       │   ├── TransactionController.java
│   │   │       │   ├── BudgetController.java
│   │   │       │   └── AccountController.java
│   │   │       │
│   │   │       ├── dao/              
│   │   │       │   ├── UserDAO.java
│   │   │       │   ├── TransactionDAO.java
│   │   │       │   └── BudgetDAO.java
│   │   │       │
│   │   │       ├── model/            
│   │   │       │   ├── User.java
│   │   │       │   ├── Transaction.java
│   │   │       │   ├── Budget.java
│   │   │       │   └── Account.java
│   │   │       │
│   │   │       ├── service/          
│   │   │       │   ├── AuthService.java
│   │   │       │   ├── TransactionService.java
│   │   │       │   ├── BudgetService.java
│   │   │       │   └── AccountService.java
│   │   │       │
│   │   │       ├── filter/           # Servlet Filters
│   │   │       │   ├── AuthFilter.java
│   │   │       │   └── EncodingFilter.java
│   │   │       │
│   │   │       ├── listener/         # Servlet Listeners
│   │   │       │   └── AppContextListener.java
│   │   │       │
│   │   │       ├── scheduler/        # Quartz Jobs cho schedule transaction/email
│   │   │       │   └── TransactionScheduler.java
│   │   │       │
│   │   │       └── util/             
│   │   │           ├── DatabaseConnection.java
│   │   │           ├── PasswordUtil.java
│   │   │           ├── EmailUtil.java
│   │   │           └── JwtUtil.java
│   │   │
│   │   ├── resources/                
│   │   │   ├── application.properties
│   │   │   ├── logback.xml           # Logging cấu hình
│   │   │   └── mail-template/        # Email templates
│   │   │       └── reset-password.html
│   │   │
│   │   └── webapp/                   
│   │       ├── WEB-INF/
│   │       │   └── web.xml           # Cấu hình servlet, filter, listener
│   │       │
│   │       ├── views/                
│   │       │   ├── index.jsp
│   │       │   ├── login.jsp
│   │       │   ├── dashboard.jsp
│   │       │   ├── transactions.jsp
│   │       │   ├── budget.jsp
│   │       │   └── accounts.jsp
│   │       │
│   │       ├── assets/               
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   └── images/
│   │       │
│   │       └── charts/               # Script vẽ biểu đồ
│   │
│   └── test/                         
│       └── com/expensemanager/
│           ├── dao/
│           │   └── UserDAOTest.java
│           ├── service/
│           │   └── AuthServiceTest.java
│           └── controller/
│               └── TransactionControllerTest.java
│
├── .github/                          # CI/CD pipeline (GitHub Actions)
│   └── workflows/
│       └── ci.yml
│
└── README.md                         # Hướng dẫn cài đặt, run project

```

