package com.expensemanager.controller;

import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.model.Transaction;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import jakarta.servlet.annotation.WebServlet;


@WebServlet("/transaction")
public class TransactionController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        TransactionDAO transDao = new TransactionDAO();
        List<Transaction> transList = transDao.getAllTransaction();

        request.setAttribute("transList", transList);
        request.setAttribute("view", "/views/transaction.jsp");
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }
}
