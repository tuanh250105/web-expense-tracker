package com.expensemanager.controller;

import com.expensemanager.model.Transaction;
import com.expensemanager.shared.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "Test", urlPatterns = {"/test-db"})
public class Test extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            EntityManager em = JpaUtil.getEntityManager();
            out.println("✅ EntityManager created successfully!");
            em.getTransaction().begin();

            List<Transaction> txs = em.createQuery("SELECT t FROM Transaction t", Transaction.class)
                    .setMaxResults(5)
                    .getResultList();

            em.getTransaction().commit();
            em.close();

            out.println("✅ Query executed successfully, found " + txs.size() + " transactions.");
            if (!txs.isEmpty()) {
                out.println("👉 First transaction: " + txs.get(0).getType() + " - " + txs.get(0).getAmount());
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            out.println("❌ Error while connecting to database:");
            out.println(e.toString());
        }
    }
}
