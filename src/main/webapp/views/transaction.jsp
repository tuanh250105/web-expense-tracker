<%--
  Created by IntelliJ IDEA.
  User: khodo
  Date: 6/10/25
  Time: 22:47
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="transaction-wrapper">
    <div class="transaction-container">
        <!-- Filter -->
        <form action="${pageContext.request.contextPath}/transaction" method="POST" class="filter-form">
            <aside class="filter-panel">
                <input type="hidden" name="action" value="filter">

                <h2>FILTER</h2>

                <label>Category</label>
                <input type="text" id="select_new_category" placeholder="Select category" readonly class="open-category-modal">
                <input type="hidden" name="category" id="hidden_new_category">

                <label>From / To</label>
                <div class="range-inputs">
                    <input type="text" id="select_new_date" name="fromDate" placeholder="Select Date" readonly>
                    <input type="text" id="select_new_date" name="toDate" placeholder="Select Date" readonly>
                </div>

                <label>Notes</label>
                <input type="text" name="notes" placeholder="Notes">


                <label>Type</label>
                <div class="checkbox-group">
                    <label><input type="checkbox" name="type" value="Expense" checked> Expenses</label>
                    <label><input type="checkbox" name="type" value="Income" checked> Income</label>
                    <label><input type="checkbox" name="type" value="Transfer"> Transfer between accounts</label>
                </div>

                <button type="submit" class="apply-btn">OK</button>
                <button type="reset" class="cancel-btn">CANCEL</button>
            </aside>
        </form>


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
<%--                        <img src="${t.iconUrl}" alt=List">--%>
                        <div class="details">
                            <h3>${t.category.name}</h3>
                            <p>${t.account.name}</p>
                        </div>
                        <div class="amount ${t.type ? "income" : "expense"}">
                            <c:choose>
                                <c:when test="${t.type}">+ $${t.amount}</c:when>
                                <c:otherwise>- $${t.amount}</c:otherwise>
                            </c:choose>
                        </div>
                        <div class="date">${t.transactionDate}</div>
                    </li>
                </c:forEach>
            </ul>
        </section>

        <!-- Form Income khi nhấp + (Add Transaction Income Form) -->
        <div id="container-addForm">
            <form id="addForm" class="modal-addForm"
                  action="${pageContext.request.contextPath}/transaction"
                  method="POST">

                <!-- Ẩn hành động để Controller nhận biết -->
                <input type="hidden" name="action" value="add_income">

                <div class="addForm-header">
                    <h2>New Income</h2>
                    <button type="button" class="exchange-btn">Exchange</button>
                </div>

                <div class="addForm-body">
                    <!-- Category -->
                    <div class="input-group">
                        <div class="input-img">
                            <img src="assets/icons/salary.png" alt="Icon">
                        </div>
                        <div class="input-wrap">
                            <label for="select_new_category">Category</label>
                            <input type="text" id="select_new_category"
                                   placeholder="Select category" readonly
                                   class="open-category-modal">
                            <input type="hidden" name="category" id="hidden_new_category">
                        </div>

                        <!-- Value -->
                        <div class="input-group-2">
                            <div class="input-wrap">
                                <label for="type_value">Value</label>
                                <input type="number" step="0.01" id="type_value"
                                       name="value" placeholder="0.00" required>
                            </div>
                        </div>
                    </div>

                    <!-- Account -->
                    <div class="input-group">
                        <div class="input-img">
                            <img src="assets/icons/salary.png" alt="Icon">
                        </div>
                        <div class="input-wrap">
                            <label for="select_new_account">Account</label>
                            <select id="select_new_account" name="account" required>
                                <option value="" disabled selected>Select account</option>
                                <c:forEach var="a" items="${accountList}">
                                    <option value="${a.id}">${a.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <!-- Date & Time -->
                    <div class="input-group">
                        <div class="input-img">
                            <img src="assets/icons/salary.png" alt="Icon">
                        </div>
                        <div class="input-wrap">
                            <label for="select_new_date">Date</label>
                            <input type="text" id="select_new_date"
                                   name="date" placeholder="Select Date" readonly required>
                        </div>
                        <div class="input-group-2">
                            <div class="input-wrap">
                                <label for="select_new_time">Time</label>
                                <input type="text" id="select_new_time"
                                       name="time" placeholder="00:00" required>
                            </div>
                        </div>
                    </div>

                    <!-- Source -->
                    <div class="input-group">
                        <div class="input-img">
                            <img src="assets/icons/salary.png" alt="Icon">
                        </div>
                        <div class="input-wrap">
                            <label for="select_new_from">From</label>
                            <select id="select_new_from" name="source" required>
                                <option value="" disabled selected>Select source</option>
                                <option value="work">Công việc</option>
                                <option value="side_hustle">Việc phụ</option>
                            </select>
                        </div>
                    </div>

                    <!-- Notes -->
                    <div class="input-group">
                        <div class="input-img">
                            <img src="assets/icons/salary.png" alt="Icon">
                        </div>
                        <div class="input-wrap">
                            <label for="type_new_notes">Note</label>
                            <textarea id="type_new_notes" name="notes"></textarea>
                        </div>
                    </div>
                </div>

                <div class="addForm-footer">
                    <button type="button" id="cancelTransactionBtn" class="save-btn">CANCEL</button>
                    <!-- Nút SAVE gửi form -->
                    <button type="submit" id="saveTransactionBtn" class="save-btn">SAVE</button>
                </div>
            </form>
        </div>


        <!-- Form Expense khi nhấp + (Add Transaction Expense Form) -->
        <div id="container-addForm-expense">
            <form id="addForm" class="modal-addForm" action="newTransaction" method="POST">
                <div class="addForm-header">
                    <h2>New Expense</h2>
                    <button type="button" class="exchange-btn">Exchange</button>
                </div>
                <div class="addForm-body">

                    <div class="input-group">
                        <div class="input-img">
                            <img src="assets/icons/salary.png" alt="Icon">
                        </div>
                        <div class="input-wrap">
                            <label for="select_new_category">Category</label>
                            <input type="text" id="select_new_category" placeholder="Select category" readonly class="open-category-modal">
                            <input type="hidden" name="category" id="hidden_new_category">
                        </div>
                        <div class="group"></div>
                        <div class="input-group-2">
                            <div class="input-wrap">
                                <label for="type_value">Value</label>
                                <input type="text" id="type_value" name="value" placeholder="0.00">
                            </div>
                        </div>
                    </div>

                    <div class="input-group">
                        <div class="input-img">
                            <img src="assets/icons/salary.png" alt="Icon">
                        </div>
                        <div class="input-wrap">
                            <label for="select_new_account">Account</label>
                            <select id="select_new_account" name="account">
                                <option value="" disabled selected>Select account</option>
                                <c:forEach var="a" items="${accountList}">
                                    <option value=${a.id}>${a.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <div class="input-group">
                        <div class="input-img">
                            <img src="assets/icons/salary.png" alt="Icon">
                        </div>
                        <div class="input-wrap">
                            <label for="select_new_date">Date</label>
                            <input type="text" id="select_new_date" name="date" placeholder="Select Date" readonly>
                        </div>
                        <div class="group"></div>
                        <div class="input-group-2">
                            <div class="input-wrap">
                                <label for="select_new_time">Time</label>
                                <input type="text" id="select_new_time" name="time" placeholder="00:00">
                            </div>
                        </div>
                    </div>

                    <div class="input-group">
                        <div class="input-img">
                            <img src="assets/icons/salary.png" alt="Icon">
                        </div>
                        <div class="input-wrap" id="input-wrap-from">
                            <label for="select_new_from">To</label>
                            <select id="select_new_from" name="source">
                                <option value="" disabled selected>Select source</option>
                                <option value="work">Công việc</option>
                                <option value="side_hustle">Việc phụ</option>
                            </select>
                        </div>
                    </div>

                    <div class="input-group">
                        <div class="input-img">
                            <img src="assets/icons/salary.png" alt="Icon">
                        </div>
                        <div class="input-wrap" id="input-wrap-note">
                            <label for="type_new_notes">Note</label>
                            <textarea id="type_new_notes" name="notes"></textarea>
                        </div>
                    </div>
                </div>

                <div class="addForm-footer">
                    <button type="submit" id="cancelTransactionBtn" class="save-btn">CANCEL</button>
                    <button type="submit" id="saveTransactionBtn" class="save-btn">SAVE</button>
                </div>
            </form> </div>

        <!-- Modal chọn Category -->
        <div id="categoryModal" class="modal" style="display: none;">
            <div class="modal-content">
                <span class="close-category-modal">&times;</span>
                <h2>Select Category</h2>
                <ul id="categoryList">
                    <c:forEach var="c" items="${categoryList}">
                        <li>
                            <button type="button" data-category="${c.id}">
                                    ${c.name}
                            </button>
                        </li>
                    </c:forEach>
                </ul>
            </div>
        </div>


        <div class="fab-buttons">
            <button class="fab add-income">+</button>
            <button class="fab add-expense">−</button>
        </div>
    </div>
</div>

