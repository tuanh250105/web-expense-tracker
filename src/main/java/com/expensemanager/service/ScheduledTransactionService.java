package com.expensemanager.service;

import com.expensemanager.dao.ScheduledTransactionDAO;
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

    public void processAllScheduled() {
        // Xử lý due (đã quá hạn hoặc đúng hạn)
        List<ScheduledTransaction> due = dao.getDueTransactions();
        for (ScheduledTransaction st : due) {
            UUID userId = getUserIdFromAccount(st);
            if (st.isActive() && userId != null) {
                processSingle(st, userId);
                dao.update(st);
            }
        }

        // Xử lý upcoming (sắp tới trong 1 ngày tới)
        List<ScheduledTransaction> upcoming = dao.getUpcomingTransactions(1); // 1 ngày tới
        for (ScheduledTransaction st : upcoming) {
            UUID userId = getUserIdFromAccount(st);
            if (st.isActive() && userId != null) {
                processUpcoming(st, userId);
                dao.update(st);
            }
        }
    }

    private void processSingle(ScheduledTransaction st, UUID userId) {
      /*  System.out.println("Đang xử lý schedule " + st.getId()
                + " | Category=" + st.getCategoryId()
                + " | Amount=" + st.getAmount()
                + " | Type=" + st.getType()
                + " | NextRun=" + st.getNextRun());
        */
        boolean paid = dao.hasTransactionNearDue(
                st.getCategoryId(),
                st.getAmount(),
                st.getType(),
                st.getNextRun(),
                7,  // Kiểm tra trước 7 ngày
                userId
        );

        if (paid) {
            updateNextRun(st);
            System.out.println("Giao dịch định kỳ đã được thanh toán (có thể trước hạn), cập nhật nextRun → " + st.getNextRun());
        } else {
            // Nếu gần đến hạn thì gửi nhắc nhở
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime dueDate = st.getNextRun().toLocalDateTime();
            if (now.isAfter(dueDate.minusDays(3))) {
                sendReminderIfNeeded(st, userId);
            }
            System.out.println("Chưa có giao dịch tương ứng cho " + st.getId());
        }
    }

    private void processUpcoming(ScheduledTransaction st, UUID userId) {
        System.out.println("Đang kiểm tra upcoming schedule " + st.getId() + " | NextRun=" + st.getNextRun());

        boolean paid = dao.hasTransactionNearDue(
                st.getCategoryId(),
                st.getAmount(),
                st.getType(),
                st.getNextRun(),
                7,
                userId
        );

        if (paid) {
            updateNextRun(st);
            System.out.println("Đã paid (có thể trước hạn), cập nhật nextRun → " + st.getNextRun());
        } else {
            // Kiểm tra nếu đúng thời điểm gửi trước 1 ngày
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime dueDate = st.getNextRun().toLocalDateTime();
            LocalDateTime reminderTime = dueDate.minusDays(1); // Trước 1 ngày
            if (now.isAfter(reminderTime) && now.isBefore(dueDate)) {
                sendReminderIfNeeded(st, userId);
                System.out.println("Đã gửi nhắc nhở trước cho " + st.getId());
            } else {
                System.out.println("Chưa đến lúc gửi nhắc nhở cho " + st.getId());
            }
        }
    }

    private void sendReminderIfNeeded(ScheduledTransaction st, UUID userId) {
        String email = dao.getUserEmailByAccount(st.getAccountId());
        String subject = "Nhắc nhở thanh toán: " + st.getCategoryName();
        String body = "Vui lòng thanh toán " + st.getAmount() + " VND " +
                " trước hạn " + st.getNextRun().toLocalDateTime().toLocalDate() +
                ". (Giao dịch định kỳ ID: " + st.getId() + ")";
        EmailService.sendReminder(email, subject, body);
    }

    private UUID getUserIdFromAccount(ScheduledTransaction st) {
        if (st.getAccount() != null && st.getAccount().getUser() != null) {
            return st.getAccount().getUser().getId();
        }
        return UUID.fromString("67b78d51-4eec-491c-bbf0-30e982def9e0"); // fallback
    }

    //BỎ QUA GIAO DỊCH
    public void skipTransaction(UUID id, UUID userId) {
        ScheduledTransaction st = dao.getById(id, userId);
        if (st == null || !st.isActive()) return;

        try {
            updateNextRun(st);
            dao.update(st);
            System.out.println("Đã bỏ qua: cập nhật next_run mới = " + st.getNextRun());
        } catch (Exception e) {
            e.printStackTrace();
            LocalDateTime next = st.getNextRun().toLocalDateTime().plusDays(1);
            st.setNextRun(Timestamp.valueOf(next));
            dao.update(st);
        }
    }

    //CẬP NHẬT NEXT_RUN
    private void updateNextRun(ScheduledTransaction st) {
        try {
            String cronExpr = st.getScheduleCron();
            // Nếu cron hợp lệ (Quartz)
            if (cronExpr != null && CronExpression.isValidExpression(cronExpr)) {
                CronExpression cron = new CronExpression(cronExpr);
                Date current = new Date(st.getNextRun().getTime());
                Date next = cron.getNextValidTimeAfter(current);
                if (next != null) {
                    st.setNextRun(new Timestamp(next.getTime()));
                    return;
                }
            }
            // Nếu Quartz không hiểu thì fallback
            LocalDateTime current = st.getNextRun().toLocalDateTime();
            LocalDateTime nextTime = detectNextRun(cronExpr, current);
            st.setNextRun(Timestamp.valueOf(nextTime));
        } catch (Exception e) {
            e.printStackTrace();
            LocalDateTime next = st.getNextRun().toLocalDateTime().plusDays(1);
            st.setNextRun(Timestamp.valueOf(next));
        }
    }

    // XÁC ĐỊNH NEXT_RUN (FALLBACK)
    private LocalDateTime detectNextRun(String cron, LocalDateTime current) {
        if (cron == null || cron.isEmpty()) {
            return current.plusDays(1);
        }
        cron = cron.trim();

        // Hàng ngày +1 ngày
        if (cron.equals("0 0 * * * ?")) {
            return current.plusDays(1);
        }
        // Hàng tuần +7 ngày
        if (cron.matches("0 0 \\* \\* [1-7]\\s?\\??") || cron.matches("0 0 \\* \\* \\? [1-7]")) {
            return current.plusWeeks(1);
        }

        // Hàng tháng +1 tháng
        if (cron.matches("^0 0 (\\d{1,2}) \\* \\?$")) {
            return current.plusMonths(1);
        }

        // Hàng năm +1 năm
        if (cron.matches("^0 0 (\\d{1,2}) (\\d{1,2}) \\?$")) {
            return current.plusYears(1);
        }

        // Trường hợp chọn 1 ngày cụ thể +1 tháng
        if (cron.matches("^0 0 \\d{1,2} \\* \\?$")) {
            return current.plusMonths(1);
        }

        // Mặc định: +1 ngày
        return current.plusDays(1);
    }

    public List<Account> getAccounts(UUID userId) {
        return dao.getAccountsByUserId(userId);
    }

    // Lấy account theo ID
    public Account findAccountById(UUID id, UUID userId) {
        return dao.findAccountById(id, userId);
    }

    public Category findCategoryById(UUID id) {
        return dao.findCategoryById(id);
    }

    // Wrapper cho getAllCategories
    public List<Category> getAllCategories() {
        return dao.getAllCategories();
    }

    // Wrapper cho getByType
    public List<Category> getCategoriesByType(String type) {
        return dao.getByType(type);
    }
}