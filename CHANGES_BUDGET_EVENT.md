# Budget & Events Modules (BudgetBuddy)

Các file thêm/sửa (chỉ trong 2 module mới):

Backend (Java):
- `src/main/java/com/expensemanager/model/Budget.java`
- `src/main/java/com/expensemanager/dao/BudgetDAO.java`
- `src/main/java/com/expensemanager/service/BudgetService.java`
- `src/main/java/com/expensemanager/controller/BudgetController.java`
- `src/main/java/com/expensemanager/model/Event.java`
- `src/main/java/com/expensemanager/model/Point.java`
- `src/main/java/com/expensemanager/dao/EventDAO.java`
- `src/main/java/com/expensemanager/dao/PointDAO.java`
- `src/main/java/com/expensemanager/service/EventService.java`
- `src/main/java/com/expensemanager/controller/EventController.java`

Views & Charts (JSP/JS):
- `src/main/webapp/views/budget.jsp`
- `src/main/webapp/charts/budgetChart.js`
- `src/main/webapp/views/events.jsp`
- `src/main/webapp/charts/eventChart.js`

Migration (Flyway):
- `db/migration/V2__budgets.sql`
- `db/migration/V3__events_points.sql`
- `db/migration/V4__seed_budget_event.sql`

web.xml:
- `src/main/webapp/WEB-INF/web.xml` (giữ @WebServlet, không thêm mapping thủ công)

URLs test:
- Budgets UI: `/budgets`
- Budget daily series (Chart.js JSON): `/budgets/{id}`
- Events UI: `/events`
- Event spending series (Chart.js JSON): `/events/{id}/series`
- Attach transaction to event (POST): `/events/{id}/attach-tx` (form field `transactionId`)
- Redeem points (POST): `/points/redeem` (form field `amount`)

SQL chính (rút gọn):
```sql
-- budgets
CREATE TABLE IF NOT EXISTS budgets (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  period_type VARCHAR(16) NOT NULL CHECK (period_type IN ('WEEK','MONTH')),
  period_start DATE NOT NULL,
  period_end   DATE NOT NULL,
  category_id  BIGINT,
  limit_amount NUMERIC(18,2) NOT NULL,
  spent_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
  note TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- events, points, event_transactions
CREATE TABLE IF NOT EXISTS events (...);
CREATE TABLE IF NOT EXISTS points (...);
CREATE TABLE IF NOT EXISTS event_transactions (...);
```

Hướng dẫn deploy WAR lên Tomcat (Supabase):
- Đặt biến môi trường trên server: `DB_URL`, `DB_USER`, `DB_PASS` (sslmode=require). Không commit `.env`.
- Build: `mvn -DskipTests package` tạo WAR trong `target/`.
- Deploy: copy WAR vào `TOMCAT/webapps/` (Tomcat 10, Jakarta EE 10).
- Truy cập: `http://localhost:8080/<context>/budgets` và `/events` để kiểm tra.

Lưu ý:
- Không sửa các module khác; chỉ thêm file/logic cho 2 module mới.
- Truy vấn chi tiêu ngày và theo sự kiện đọc bảng `transactions` (read-only). Nếu schema khác, cần điều chỉnh câu lệnh SELECT cho phù hợp.


