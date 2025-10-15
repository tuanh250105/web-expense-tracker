//package com.expensemanager.controller;
//
//
//import com.expensemanager.util.JpaUtil;
//import jakarta.servlet.ServletContextEvent;
//import jakarta.servlet.ServletContextListener;
//import jakarta.servlet.annotation.WebListener;
//
//@WebListener
//public class JpaShutdownListener implements ServletContextListener {
//
//    @Override
//    public void contextDestroyed(ServletContextEvent sce) {
//        System.out.println("🛑 Đang dọn tài nguyên Hibernate...");
//        JpaUtil.close();
//    }
//
//    @Override
//    public void contextInitialized(ServletContextEvent sce) {
//        // Optional: bạn có thể in ra log khi khởi động app.
//        System.out.println("🚀 App khởi động - JpaUtil đã sẵn sàng.");
//    }
//}
