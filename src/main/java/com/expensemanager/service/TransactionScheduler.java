package com.expensemanager.service;

import com.expensemanager.dao.ScheduledTransactionDAO;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class TransactionScheduler implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            System.out.println("🚀 [Quartz] TransactionScheduler bắt đầu chạy lúc " + new java.util.Date());

            ScheduledTransactionDAO dao = new ScheduledTransactionDAO();
            ScheduledTransactionService service = new ScheduledTransactionService(dao);

            service.processAllScheduled();

            System.out.println("✅ [Quartz] Đã xử lý xong các giao dịch định kỳ lúc " + new java.util.Date());
        } catch (Exception e) {
            System.err.println("❌ [Quartz] Lỗi khi xử lý TransactionScheduler:");
            e.printStackTrace();
            throw new JobExecutionException(e);
        }
    }

}