package com.expensemanager.service;

import com.expensemanager.dao.ScheduledTransactionDAO;
import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.model.Account;
import com.expensemanager.model.Category;
import com.expensemanager.model.ScheduledTransaction;
import org.quartz.CronExpression;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ScheduledTransactionService {
    private ScheduledTransactionDAO dao;
    private TransactionDAO transactionDAO = new TransactionDAO();

    public ScheduledTransactionService() {
        this.dao = new ScheduledTransactionDAO();
    }

    public ScheduledTransactionService(ScheduledTransactionDAO dao) {
        this.dao = dao;
    }

    public List<ScheduledTransaction> listTransactions(String categoryNameFilter, String account, String from, String to, String note, String[] types, UUID userId) {
        return dao.getFiltered(categoryNameFilter, account, from, to, note, types, userId);
    }

    public void createTransaction(ScheduledTransaction t) {
        dao.add(t);
    }

    public void removeTransaction(UUID id, UUID userId) {
        dao.delete(id, userId);
    }


    public void runNow(UUID id, UUID userId) {
        ScheduledTransaction st = dao.getById(id, userId);
        if (st != null && st.isActive()) {
            processSingle(st, userId);
            dao.update(st);
        }
    }

    public void processAllScheduled() {
        // Giữ nguyên xử lý due (đã quá hạn hoặc đúng hạn)
        List<ScheduledTransaction> due = dao.getDueTransactions();
        for (ScheduledTransaction st : due) {
            UUID userId = getUserIdFromAccount(st);
            if (st.isActive() && userId != null) {
                processSingle(st, userId);
                dao.update(st);
            }
        }

        // THÊM: Xử lý upcoming (sắp due trong 1 ngày tới)
        List<ScheduledTransaction> upcoming = dao.getUpcomingTransactions(1); // 1 ngày tới
        for (ScheduledTransaction st : upcoming) {
            UUID userId = getUserIdFromAccount(st);
            if (st.isActive() && userId != null) {
                processUpcoming(st, userId);// Method mới cho upcoming
                dao.update(st); // mới thêm
                // Không update next_run ở đây, vì chưa due
            }
        }
    }

    private void processSingle(ScheduledTransaction st, UUID userId) {
        System.out.println("▶ Đang xử lý schedule " + st.getId()
                + " | Category=" + st.getCategoryId()
                + " | Amount=" + st.getAmount()
                + " | Type=" + st.getType()
                + " | NextRun=" + st.getNextRun());

        // THAY ĐỔI: Sử dụng hasTransactionNearDue thay vì hasTransactionInMonth, để kiểm tra trước hạn (7 ngày)
        boolean paid = transactionDAO.hasTransactionNearDue(
                st.getCategoryId(),
                st.getAmount(),  // Giả sử getAmount() trả BigDecimal
                st.getType(),
                st.getNextRun(),
                7,  // Kiểm tra trước 7 ngày (có thể thay thành 1 nếu chỉ muốn đúng ngày)
                userId
        );

        if (paid) {
            updateNextRun(st);
            System.out.println("✅ Giao dịch định kỳ đã được thanh toán (có thể trước hạn), cập nhật nextRun → " + st.getNextRun());
        } else {
            // Nếu gần đến hạn thì gửi nhắc nhở
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime dueDate = st.getNextRun().toLocalDateTime();
            if (now.isAfter(dueDate.minusDays(3))) {
                sendReminderIfNeeded(st, userId);
            }
            System.out.println("⏳ Chưa có giao dịch tương ứng cho " + st.getId());
        }
    }

    private void processUpcoming(ScheduledTransaction st, UUID userId) {
        System.out.println("▶ Đang kiểm tra upcoming schedule " + st.getId() + " | NextRun=" + st.getNextRun());

        // THAY ĐỔI: Sử dụng hasTransactionNearDue thay vì hasTransactionInMonth
        boolean paid = transactionDAO.hasTransactionNearDue(
                st.getCategoryId(),
                st.getAmount(),  // BigDecimal
                st.getType(),
                st.getNextRun(),
                7,
                userId
        );

        if (paid) {
            updateNextRun(st);
            System.out.println("✅ Đã paid (có thể trước hạn), cập nhật nextRun → " + st.getNextRun());
        } else {
            // Kiểm tra nếu đúng thời điểm gửi trước 1 ngày
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime dueDate = st.getNextRun().toLocalDateTime();
            LocalDateTime reminderTime = dueDate.minusDays(1); // Trước 1 ngày
            if (now.isAfter(reminderTime) && now.isBefore(dueDate)) {
                sendReminderIfNeeded(st, userId);
                System.out.println("📧 Đã gửi nhắc nhở trước cho " + st.getId());
            } else {
                System.out.println("⏳ Chưa đến lúc gửi nhắc nhở cho " + st.getId());
            }
        }
    }

    private void sendReminderIfNeeded(ScheduledTransaction st, UUID userId) {
        String email = transactionDAO.getUserEmailByAccount(st.getAccountId());
        String subject = "Nhắc nhở thanh toán: " + st.getCategoryName();
        String body = "Vui lòng thanh toán " + st.getAmount() + " VND " +
                " trước hạn " + st.getNextRun().toLocalDateTime().toLocalDate() +
                ". (Giao dịch định kỳ ID: " + st.getId() + ")";
        EmailService.sendReminder(email, subject, body);
    }

    private UUID getUserIdFromAccount(ScheduledTransaction st) {
        return st.getAccount() != null ? st.getAccount().getUserId() :
                UUID.fromString("4efcb554-f4c5-442a-b57c-89d213861501"); // Fallback hardcoded
    }
    // =================== BỎ QUA GIAO DỊCH ===================
    public void skipTransaction(UUID id, UUID userId) {
        ScheduledTransaction st = dao.getById(id, userId);
        if (st == null || !st.isActive()) return;

        try {
            updateNextRun(st);
            dao.update(st);
            System.out.println("⏭️ Đã bỏ qua: cập nhật next_run mới = " + st.getNextRun());
        } catch (Exception e) {
            e.printStackTrace();
            // fallback an toàn: +7 ngày
            LocalDateTime next = st.getNextRun().toLocalDateTime().plusDays(1);
            st.setNextRun(Timestamp.valueOf(next));
            dao.update(st);
        }
    }

    // =================== CẬP NHẬT NEXT_RUN ===================
    private void updateNextRun(ScheduledTransaction st) {
        try {
            String cronExpr = st.getScheduleCron();

            // ✅ 1️⃣ Nếu cron hợp lệ (Quartz) → dùng chuẩn
            if (cronExpr != null && CronExpression.isValidExpression(cronExpr)) {
                CronExpression cron = new CronExpression(cronExpr);
                Date current = new Date(st.getNextRun().getTime());
                Date next = cron.getNextValidTimeAfter(current);
                if (next != null) {
                    st.setNextRun(new Timestamp(next.getTime()));
                    return;
                }
            }

            // ✅ 2️⃣ Nếu Quartz không hiểu → fallback logic đơn giản
            LocalDateTime current = st.getNextRun().toLocalDateTime();
            LocalDateTime nextTime = detectNextRun(cronExpr, current);
            st.setNextRun(Timestamp.valueOf(nextTime));

        } catch (Exception e) {
            e.printStackTrace();
            // ✅ 3️⃣ fallback cuối cùng: +7 ngày
            LocalDateTime next = st.getNextRun().toLocalDateTime().plusDays(1);
            st.setNextRun(Timestamp.valueOf(next));
        }
    }

    // =================== XÁC ĐỊNH NEXT_RUN (FALLBACK) ===================
    private LocalDateTime detectNextRun(String cron, LocalDateTime current) {
        if (cron == null || cron.isEmpty()) {
            return current.plusDays(1); // default: mỗi tuần
        }

        cron = cron.trim();

        // 🔹 Hàng ngày → +1 ngày
        if (cron.equals("0 0 * * * ?")) {
            return current.plusDays(1);
        }

        // 🔹 Hàng tuần → +7 ngày (không quan tâm thứ)
        if (cron.matches("0 0 \\* \\* [1-7]\\s?\\??") || cron.matches("0 0 \\* \\* \\? [1-7]")) {
            return current.plusWeeks(1);
        }

        // 🔹 Hàng tháng → +1 tháng
        if (cron.matches("^0 0 (\\d{1,2}) \\* \\?$")) {
            return current.plusMonths(1);
        }

        // 🔹 Hàng năm → +1 năm
        if (cron.matches("^0 0 (\\d{1,2}) (\\d{1,2}) \\?$")) {
            return current.plusYears(1);
        }

        // 🔹 Trường hợp chọn 1 ngày cụ thể → +1 tháng
        if (cron.matches("^0 0 \\d{1,2} \\* \\?$")) {
            return current.plusMonths(1);
        }

        // 🔸 Mặc định: +1 ngày
        return current.plusDays(1);
    }

    public Account findAccountByName(String name, UUID userId) {
        return dao.findAccountByName(name, userId);
    }

    public Category findCategoryById(Integer id) {
        return dao.findCategoryById(id);
    }
}