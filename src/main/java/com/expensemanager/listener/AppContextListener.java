package com.expensemanager.listener;

import com.expensemanager.service.TransactionScheduler;
import com.expensemanager.util.JpaUtil;
import jakarta.servlet.annotation.WebListener;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
@WebListener
public class AppContextListener implements ServletContextListener {

    private Scheduler scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            // Khởi tạo Quartz Scheduler
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            JobDetail job = JobBuilder.newJob(TransactionScheduler.class)
                    .withIdentity("scheduledTransactionJob", "expenseManager")
                    .build();

            // Trigger: Hàng ngày lúc 00:05
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("dailyTrigger", "expenseManager")
                    .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(0, 5))
                    .build();

            scheduler.scheduleJob(job, trigger);
            //scheduler.triggerJob(job.getKey()); // Chạy ngay lập tức khi khởi động

            scheduler.start();
            System.out.println("AQuartz Scheduler khởi tạo thành công cho TransactionScheduler!");

            /*
            try {
                com.expensemanager.service.TransactionScheduler testRun = new com.expensemanager.service.TransactionScheduler();
                testRun.execute(null); // Gọi trực tiếp job để test
                System.out.println("Đã chạy thử TransactionScheduler ngay khi khởi động!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            */

        } catch (SchedulerException e) {
            System.err.println("Lỗi khởi tạo Quartz Scheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            // Shutdown Quartz Scheduler trước
            if (scheduler != null) {
                scheduler.shutdown(true);  // true: chờ jobs hoàn thành
                System.out.println("Quartz Scheduler đã shutdown.");
            }

            // Shutdown JPA EntityManagerFactory (quan trọng để đóng connection pool)
            JpaUtil.shutdown();
            System.out.println("JPA EntityManagerFactory đã shutdown.");
        } catch (SchedulerException e) {
            System.err.println("Lỗi shutdown Quartz: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Lỗi shutdown JPA: " + e.getMessage());
            e.printStackTrace();
        }
    }
}