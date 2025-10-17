<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:if test="${not empty editTransaction}">
    <script>
        window.editTransactionData = {
            id: "${editTransaction.id}",
            type: "${editTransaction.type}",
            categoryName: "${editTransaction.category.name}",
            categoryId: "${editTransaction.category.id}",
            accountId: "${editTransaction.account.id}",
            accountName: "${editTransaction.account.name}",
            value: "${editTransaction.amount}",
            date: "${editTransaction.transactionDate.toLocalDate()}",
            time: "${editTransaction.transactionDate.toLocalTime()}",
            notes: "${editTransaction.note}"
        };
    </script>
</c:if>
<div class="transaction-wrapper">
    <div class="transaction-container">
        <!-- Filter Panel -->
        <form action="${pageContext.request.contextPath}/transaction" method="POST" class="filter-form">
            <aside class="filter-panel">
                <input type="hidden" name="action" value="filter">

                <h2>FILTER</h2>

                <label>Category</label>
                <input type="text" id="select_new_category" placeholder="Select category" readonly class="open-category-modal">
                <input type="hidden" name="category" id="hidden_new_category">

                <label>From / To</label>
                <div class="range-inputs">
                    <input type="text" class="select_filter_from_date" name="fromDate" placeholder="Select Date" readonly>
                    <input type="text" class="select_filter_to_date" name="toDate" placeholder="Select Date" readonly>
                </div>

                <label>Notes</label>
                <input type="text" name="notes" placeholder="Notes">

                <label>Type</label>
                <div class="checkbox-group">
                    <label><input type="checkbox" name="type" value="Expense" checked> Expenses</label>
                    <label><input type="checkbox" name="type" value="Income" checked> Income</label>
                </div>

                <button type="submit" class="apply-btn">OK</button>
                <button type="reset" class="cancel-btn">CANCEL</button>
            </aside>
        </form>

        <!-- Transaction List -->
        <section class="transaction-list">
            <div class="transaction-header">
                <!-- Prev btn -->
                <a href="${pageContext.request.contextPath}/transaction?navigate=prev" class="nav-btn back-btn" title="Previous month"> &lt; </a>

                <!-- Current month -->
                <div class="month-display">
                    <h2>${dateRangeLabel}</h2>
                    <c:if test="${!isCurrentMonth and !isFiltered}">
                        <a href="${pageContext.request.contextPath}/transaction" class="today-link">(Go to current month)</a>
                    </c:if>
                </div>

                <!-- Next btn -->
                <a href="${pageContext.request.contextPath}/transaction?navigate=next" class="nav-btn forward-btn" title="Next month"> &gt; </a>

                <!-- Total  -->
                <span class="total">
                    Total:
                    <c:choose>
                        <c:when test="${totalBalance >= 0}">
                            <span class="positive">+ VNĐ <fmt:formatNumber value="${totalBalance}" pattern="#,##0"/></span>
                        </c:when>
                        <c:otherwise>
                            <span class="negative">- VNĐ<fmt:formatNumber value="${-totalBalance}" pattern="#,##0"/></span>
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>

            <ul class="transaction-items">
                <c:choose>
                    <c:when test="${empty transList}">
                        <li class="no-transactions">
                            <p>No transactions found for this period</p>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="t" items="${transList}">
                            <li class="transaction-item">
                                <img src="${pageContext.request.contextPath}${t.category.iconPath}" alt="Category Icon">
                                <div class="details">
                                    <h3>${t.category.name}</h3>
                                    <p>${t.account.name}</p>
                                </div>
                                <div class="amount ${t.type eq 'income' ? 'income' : 'expense'}">
                                    <c:choose>
                                        <c:when test="${t.type eq 'income'}">
                                            + đ<fmt:formatNumber value="${t.amount}" pattern="#,##0"/>
                                        </c:when>
                                        <c:otherwise>
                                            - đ<fmt:formatNumber value="${t.amount}" pattern="#,##0"/>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="date">
                                        ${t.transactionDate != null ? t.transactionDate.toLocalDate().toString() : ''}
                                </div>
                                <button class="more-btn" type="button">
                                    <img src="${pageContext.request.contextPath}/assets/images/icon-base/more.png" alt="More">
                                </button>

                                <!-- Dropdown Menu -->
                                <div class="dropdown-menu">
                                    <form action="${pageContext.request.contextPath}/transaction" method="get" style="display:inline;">
                                        <input type="hidden" name="action" value="edit">
                                        <input type="hidden" name="transactionId" value="${t.id}">
                                        <button type="submit" class="dropdown-item edit">Edit transaction</button>
                                    </form>
                                    <form action="${pageContext.request.contextPath}/transaction" method="post" class="delete-form" style="display:inline;">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${t.id}">
                                        <button type="submit" class="dropdown-item delete">
                                            Delete transaction
                                        </button>
                                    </form>
                                </div>

                                <!-- Notes -->
                                <div class="notes">
                                    <c:choose>
                                        <c:when test="${not empty t.note}">
                                            ${t.note}
                                        </c:when>
                                        <c:otherwise>
                                            <em>No notes for this transaction.</em>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </li>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </ul>
        </section>

        <!-- Form Income -->
        <div class="container-addForm" id="incomeForm">
            <form class="addForm modal-addForm" action="${pageContext.request.contextPath}/transaction" method="POST">
                <input type="hidden" name="action" value="add_income">
                <input type="hidden" name="type" value="income">

                <div class="addForm-header">
                    <h2>New Income</h2>
                    <button type="button" class="exchange-btn">Exchange</button>
                </div>

                <div class="addForm-body">
                    <!-- Category -->
                    <div class="input-group">
                        <div class="input-img"><img src="${pageContext.request.contextPath}/assets/images/categories/salary.png" alt="Icon"></div>
                        <div class="input-wrap">
                            <label>Category</label>
                            <input type="text" class="select_new_category open-category-modal" placeholder="Select category" readonly>
                            <input type="hidden" name="category" class="hidden_new_category">
                        </div>
                        <div class="input-group-2">
                            <div class="input-wrap">
                                <label>Value</label>
                                <input type="number" step="0.01" class="type_value" name="value" placeholder="0.00 VND" required>
                            </div>
                        </div>
                    </div>

                    <!-- Account -->
                    <div class="input-group">
                        <div class="input-img"><img src="${pageContext.request.contextPath}/assets/images/icon-base/account.png" alt="Icon"></div>
                        <div class="input-wrap">
                            <label>Account</label>
                            <select class="select_new_account" name="account" required>
                                <option value="" disabled selected>Select account</option>
                                <c:forEach var="a" items="${accountList}">
                                    <option value="${a.id}">${a.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <!-- Date & Time -->
                    <div class="input-group">
                        <div class="input-img"><img src="${pageContext.request.contextPath}/assets/images/icon-base/date.png" alt="Icon"></div>
                        <div class="input-wrap">
                            <label>Date</label>
                            <input type="text" class="select_new_date" name="date" placeholder="Select Date" readonly required>
                        </div>
                        <div class="input-group-2">
                            <div class="input-wrap">
                                <label>Time</label>
                                <input type="text" class="select_new_time" name="time" placeholder="00:00" required>
                            </div>
                        </div>
                    </div>

                    <!-- Note -->
                    <div class="input-group">
                        <div class="input-img"><img src="${pageContext.request.contextPath}/assets/images/icon-base/notes.png" alt="Icon"></div>
                        <div class="input-wrap">
                            <label>Note</label>
                            <textarea class="type_new_notes" name="notes"></textarea>
                        </div>
                    </div>
                </div>

                <div class="addForm-footer">
                    <button type="button" class="cancelTransactionBtn" id="cancelIncome">CANCEL</button>
                    <button type="submit" class="saveTransactionBtn save-income">SAVE</button>
                </div>
            </form>
        </div>

        <!-- Form Expense -->
        <div class="container-addForm" id="expenseForm">
            <form class="addForm modal-addForm" action="${pageContext.request.contextPath}/transaction" method="POST">
                <input type="hidden" name="action" value="add_expense">
                <input type="hidden" name="type" value="expense">

                <div class="addForm-header">
                    <h2>New Expense</h2>
                    <button type="button" class="exchange-btn">Exchange</button>
                </div>

                <div class="addForm-body">
                    <!-- Category -->
                    <div class="input-group">
                        <div class="input-img"><img src="${pageContext.request.contextPath}/assets/images/categories/salary.png" alt="Icon"></div>
                        <div class="input-wrap">
                            <label>Category</label>
                            <input type="text" class="select_new_category open-category-modal" placeholder="Select category" readonly>
                            <input type="hidden" name="category" class="hidden_new_category">
                        </div>
                        <div class="input-group-2">
                            <div class="input-wrap">
                                <label>Value</label>
                                <input type="number" step="0.01" class="type_value" name="value" placeholder="0.00 VND" required>
                            </div>
                        </div>
                    </div>

                    <!-- Account -->
                    <div class="input-group">
                        <div class="input-img"><img src="${pageContext.request.contextPath}/assets/images/icon-base/account.png" alt="Icon"></div>
                        <div class="input-wrap">
                            <label>Account</label>
                            <select class="select_new_account" name="account" required>
                                <option value="" disabled selected>Select account</option>
                                <c:forEach var="a" items="${accountList}">
                                    <option value="${a.id}">${a.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <!-- Date & Time -->
                    <div class="input-group">
                        <div class="input-img"><img src="${pageContext.request.contextPath}/assets/images/icon-base/date.png" alt="Icon"></div>
                        <div class="input-wrap">
                            <label>Date</label>
                            <input type="text" class="select_new_date" name="date" placeholder="Select Date" readonly required>
                        </div>
                        <div class="input-group-2">
                            <div class="input-wrap">
                                <label>Time</label>
                                <input type="text" class="select_new_time" name="time" placeholder="00:00" required>
                            </div>
                        </div>
                    </div>

                    <!-- Note -->
                    <div class="input-group">
                        <div class="input-img"><img src="${pageContext.request.contextPath}/assets/images/icon-base/notes.png" alt="Icon"></div>
                        <div class="input-wrap">
                            <label>Note</label>
                            <textarea class="type_new_notes" name="notes"></textarea>
                        </div>
                    </div>
                </div>

                <div class="addForm-footer">
                    <button type="button" class="cancelTransactionBtn" id="cancelExpense">CANCEL</button>
                    <button type="submit" class="saveTransactionBtn save-expense">SAVE</button>
                </div>
            </form>
        </div>

        <!-- Modal chọn Category cho filter-->
        <div id="categoryModal">
            <div class="modal-content">
                <span class="close-category-modal">&times;</span>
                <h2>Select Category</h2>
                <ul id="categoryList">
                    <c:forEach var="c" items="${categoryList}">
                        <li>
                            <button type="button" data-category="${c.id}" data-type="${c.type}" data-parent="${empty c.parent.id ? '' : c.parent.id}" >
                                <img src="${pageContext.request.contextPath}${c.iconPath}" alt="Icon">
                                    ${c.name}
                            </button>
                        </li>
                    </c:forEach>
                </ul>
            </div>
        </div>

        <!-- FAB Buttons -->
        <div class="fab-buttons">
            <button class="fab add-income">+</button>
            <button class="fab add-expense">−</button>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const navButtons = document.querySelectorAll('.nav-btn');

        navButtons.forEach(btn => {
            btn.addEventListener('click', function(e) {
                // Add loading class
                this.classList.add('loading');
                this.style.opacity = '0.5';
                this.style.pointerEvents = 'none';
            });
        });
    });
</script>
