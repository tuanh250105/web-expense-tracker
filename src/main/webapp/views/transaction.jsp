<%--
  Created by IntelliJ IDEA.
  User: khodo
  Date: 6/10/25
  Time: 22:47
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Title</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/layout/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/transaction.css">
</head>
<body>
    <div class="transaction-container">
        <!-- Filter -->
        <aside class="filter-panel">
            <h2>FILTER</h2>
            <label>Category</label>
            <input type="text" placeholder="Category">

            <label>From / To</label>
            <div class="range-inputs">
                <input type="date">
                <input type="date">
            </div>

            <label>Notes</label>
            <input type="text" placeholder="Notes">

            <label>Checked / Not checked</label>
            <div class="checkbox-group">
                <label><input type="checkbox" checked> Checked</label>
                <label><input type="checkbox" checked> Not checked</label>
            </div>

            <label>Type</label>
            <div class="checkbox-group">
                <label><input type="checkbox" checked> Expenses</label>
                <label><input type="checkbox" checked> Income</label>
                <label><input type="checkbox"> Transfer between accounts</label>
            </div>

            <button class="cancel-btn">CANCEL</button>
        </aside>

        <!-- Transactions -->
        <section class="transaction-list">
            <div class="transaction-header">
                <button class="back-btn">&lt;</button>
                <h2>October 2025</h2>
                <span class="total">Total: <span class="negative">- $143.50</span></span>
            </div>

            <ul class="transaction-items">
                <c:forEach var="t" items="${transList}">
                    <li class="transaction-item">
                        <img src="${t.iconUrl}" alt=""List
                        <div class="details">
                            <h3>${t.name}</h3>
                            <p>${t.source}</p>
                        </div>
                        <div class="amount ${t.positive ? 'positive' : 'negative'}">
                            <c:choose>
                                <c:when test="${t.positive}">+ $${t.amount}</c:when>
                                <c:otherwise>- $${t.amount}</c:otherwise>
                            </c:choose>
                        </div>
                        <div class="date">${t.date}</div>
                    </li>
                </c:forEach>
            </ul>
            </div>
        </section>

        <!-- Floating buttons -->
        <div class="fab-buttons">
            <button class="fab add">+</button>
            <button class="fab remove">−</button>
        </div>
    </div>

    <script defer src="${pageContext.request.contextPath}/assets/js/transaction.js"></script>
</body>
</html>
