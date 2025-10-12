<%-- src/main/webapp/views/import_export.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Import & Export Dữ Liệu</title>
    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">
    <style>
        :root {
            --primary: #4a90e2;
            --bg: #f4f7f9;
            --card: #ffffff;
            --border: #e0e0e0;
            --text: #333;
        }
        body { background: var(--bg); color: var(--text); margin: 0; }
        .page { padding: 20px; }
        .row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
        .card { background: var(--card); border-radius: 12px; border: 1px solid var(--border); padding: 16px; }
        h2 { margin-top: 0; }
        label { font-weight: 500; }
        select, input[type="date"], input[type="file"] {
            width: 100%; padding: 8px; border-radius: 6px; border: 1px solid var(--border);
        }
        button { background: var(--primary); border: none; color: white; padding: 10px 16px;
            border-radius: 6px; cursor: pointer; font-weight: 500; }
        button:hover { background: #3579c8; }
        .msg { padding: 10px; margin-bottom: 10px; border-radius: 6px; }
        .msg.error { background: #ffe3e3; color: #b10000; border: 1px solid #ffaaaa; }
        .msg.success { background: #e9fff0; color: #007a2e; border: 1px solid #a8ffc6; }
        table { width: 100%; border-collapse: collapse; margin-top: 12px; font-size: 14px; }
        th, td { border: 1px solid var(--border); padding: 6px 8px; text-align: left; }
        th { background: #f3f3f3; }
    </style>
</head>
<body>
<div class="page">

    <!-- Thông báo -->
    <c:if test="${not empty error}">
        <div style="background-color:#ffe6e6; color:#b30000; padding:10px; border-radius:6px; margin-bottom:10px;">
            ${error}
        </div>
    </c:if>
    <c:if test="${not empty success}">
        <div class="msg success"><i class="fa-solid fa-circle-check"></i> ${success}</div>
    </c:if>

    <c:if test="${readonly}">
        <div style="background-color:#fff3cd; color:#856404; padding:10px; border-radius:6px; margin-bottom:10px;">
            🔒 Bạn đang ở chế độ khách — chỉ có thể xem và xem trước file.
        </div>
    </c:if>

    <c:if test="${!readonly}">
        <div class="row">

            <!-- IMPORT -->
            <div class="card">
                <h2><i class="fa-solid fa-file-import"></i> Import dữ liệu</h2>

                <!-- FORM UPLOAD FILE -->
                <form action="${pageContext.request.contextPath}/import-export" method="post"
                      enctype="multipart/form-data">
                    <input type="hidden" name="action" value="preview">

                    <label for="account_import">Tài khoản</label>
                    <select id="account_import" name="account" required>
                        <option value="">-- Chọn tài khoản --</option>
                        <c:forEach var="a" items="${accounts}">
                            <option value="${a.id}" <c:if test="${a.id == selectedAccountId}">selected</c:if>>
                                    ${a.name}
                            </option>
                        </c:forEach>
                    </select>

                    <label for="file">Chọn file (CSV hoặc XLSX)</label>
                    <input id="file" type="file" name="file" accept=".csv,.xlsx" required>

                    <br>
                    <button type="submit"><i class="fa-solid fa-eye"></i> Xem trước</button>
                </form>

                <!-- HIỂN THỊ BẢNG XEM TRƯỚC -->
                <c:if test="${not empty previewTransactions}">
                    <hr>
                    <h3><i class="fa-solid fa-table"></i> Bảng xem trước</h3>

                    <div style="max-height:350px; overflow:auto; border:1px solid var(--border); border-radius:8px;">
                        <table>
                            <thead>
                            <tr>
                                <th>Type</th>
                                <th>Amount</th>
                                <th>Note</th>
                                <th>Transaction Date</th>
                                <th>Created At</th>
                                <th>Updated At</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="t" items="${previewTransactions}">
                                <tr>
                                    <td>${t.type}</td>
                                    <td>${t.amount}</td>
                                    <td>${t.note}</td>
                                    <td>${t.transactionDate}</td>
                                    <td>${t.create_at}</td>
                                    <td>${t.update_at}</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <!-- FORM XÁC NHẬN IMPORT -->
                    <form action="${pageContext.request.contextPath}/import-export" method="post" style="margin-top:10px;">
                        <input type="hidden" name="action" value="import">
                        <button type="submit"><i class="fa-solid fa-circle-check"></i> Xác nhận Import</button>
                    </form>
                </c:if>
            </div>

            <!-- EXPORT -->
            <div class="card">
                <h2><i class="fa-solid fa-file-export"></i> Export dữ liệu</h2>
                <form action="${pageContext.request.contextPath}/import-export" method="post">
                    <input type="hidden" name="action" value="export">

                    <label for="account_export">Tài khoản</label>
                    <select id="account_export" name="account" required>
                        <option value="">-- Chọn tài khoản --</option>
                        <c:forEach var="a" items="${accounts}">
                            <option value="${a.id}">${a.name}</option>
                        </c:forEach>
                    </select>

                    <label for="startDate">Từ ngày</label>
                    <input id="startDate" type="date" name="startDate">

                    <label for="endDate">Đến ngày</label>
                    <input id="endDate" type="date" name="endDate">

                    <label for="format">Định dạng</label>
                    <select id="format" name="format">
                        <option value="csv">CSV</option>
                        <option value="xlsx">XLSX</option>
                        <option value="pdf">PDF</option>
                    </select>

                    <br><br>
                    <button type="submit"><i class="fa-solid fa-download"></i> Xuất file</button>
                </form>
            </div>
        </div>
    </c:if>
</div>
</body>
</html>
