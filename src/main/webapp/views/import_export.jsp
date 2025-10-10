<%-- src/main/webapp/views/import_export.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Import & Export Dữ Liệu</title>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">

    <style>
        :root {
            --primary-color: #4a90e2;
            --secondary-color: #50e3c2;
            --background-color: #f4f7f9;
            --text-color: #333;
            --card-bg: #ffffff;
            --border-color: #e0e0e0;
        }

        /* General styling from categories.jsp */
        body { font-family: Arial, sans-serif; background-color: var(--background-color); color: var(--text-color); margin: 0; padding: 20px; }
        .container { max-width: 900px; margin: auto; }
        h1, h2 { color: var(--primary-color); border-bottom: 2px solid var(--primary-color); padding-bottom: 10px; }
        .card { background-color: var(--card-bg); border-radius: 8px; padding: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 20px; }

        /* Specific styles for Import/Export */
        .tab-container {
            display: flex;
            border-bottom: 1px solid var(--border-color);
            margin-bottom: 20px;
        }
        .tab-button {
            padding: 15px 25px;
            cursor: pointer;
            border: none;
            background-color: transparent;
            font-size: 18px;
            position: relative;
        }
        .tab-button.active {
            color: var(--primary-color);
            font-weight: bold;
        }
        .tab-button.active::after {
            content: '';
            position: absolute;
            bottom: -1px;
            left: 0;
            width: 100%;
            height: 3px;
            background-color: var(--primary-color);
        }

        .tab-content { display: none; }
        .tab-content.active { display: block; }

        .step {
            margin-bottom: 25px;
            padding-left: 30px;
            position: relative;
            border-left: 2px solid var(--secondary-color);
        }
        .step-number {
            position: absolute;
            left: -15px;
            top: 0;
            width: 30px;
            height: 30px;
            border-radius: 50%;
            background-color: var(--secondary-color);
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
        }
        .step h3 { margin-top: 0; }

        /* Upload area */
        .upload-box {
            border: 2px dashed var(--border-color);
            border-radius: 8px;
            padding: 40px;
            text-align: center;
            cursor: pointer;
            background-color: #fafafa;
        }
        .upload-box:hover { border-color: var(--primary-color); }
        .upload-box i { font-size: 40px; color: var(--primary-color); }
        .upload-box p { font-size: 16px; color: #777; }
        #file-upload { display: none; }

        /* Form elements */
        select, input[type="date"] { padding: 10px; border: 1px solid var(--border-color); border-radius: 4px; width: 100%; box-sizing: border-box; }
        .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
        button { background-color: var(--primary-color); color: white; padding: 12px 20px; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; width: 100%; }

        /* Preview Table */
        table { width: 100%; border-collapse: collapse; }
        th, td { border-bottom: 1px solid var(--border-color); padding: 12px; text-align: left; }
        th { background-color: #f8f9fa; }
    </style>
</head>
<body>
<div class="container">
    <h1><i class="fa-solid fa-file-import"></i> Import / Export Dữ Liệu</h1>
    <div class="card">
        <div class="tab-container">
            <button class="tab-button active" onclick="openTab('import')">
                <i class="fa-solid fa-upload"></i> Import
            </button>
            <button class="tab-button" onclick="openTab('export')">
                <i class="fa-solid fa-download"></i> Export
            </button>
        </div>

        <div id="import" class="tab-content active">
            <!-- Form cho Preview Import -->
            <form action="${pageContext.request.contextPath}/import-export?action=preview_import" method="post" enctype="multipart/form-data">
                <div class="step">
                    <span class="step-number">1</span>
                    <h3>Chọn File sao kê (.csv, .xlsx)</h3>
                    <label for="file-upload" class="upload-box">
                        <i class="fa-solid fa-cloud-arrow-up"></i>
                        <p>Kéo và thả file vào đây, hoặc nhấn để chọn file</p>
                    </label>
                    <input type="file" id="file-upload" name="file-upload" accept=".csv, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet">
                </div>

                <div class="step">
                    <span class="step-number">2</span>
                    <h3>Chọn tài khoản để nhập giao dịch vào</h3>
                    <select id="import-account" name="import-account">
                        <option value="">-- Chọn tài khoản/ví --</option>
                        <c:forEach items="${accounts}" var="acc">
                            <option value="${acc.id}">${acc.name}</option>
                        </c:forEach>
                    </select>
                </div>

                <button type="submit"><i class="fa-solid fa-eye"></i> Xem trước</button>
            </form>

            <!-- Preview và Confirm (Chỉ hiển thị nếu có preview) -->
            <c:if test="${not empty previewTransactions}">
                <form action="${pageContext.request.contextPath}/import-export?action=confirm_import" method="post">
                    <div class="step">
                        <span class="step-number">3</span>
                        <h3>Xem trước và xác nhận</h3>
                        <p>Hệ thống đã đọc file của bạn. Vui lòng kiểm tra lại dữ liệu trước khi xác nhận.</p>
                        <table>
                            <thead>
                            <tr>
                                <th>Ngày</th>
                                <th>Mô tả</th>
                                <th>Số tiền</th>
                                <th>Danh mục (Gợi ý)</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${previewTransactions}" var="tx">
                                <tr>
                                    <td>${tx.transactionDate}</td>
                                    <td>${tx.note}</td>
                                    <td>${tx.amount}đ</td>
                                    <td>${tx.category.name}</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <button type="submit"><i class="fa-solid fa-check"></i> Xác nhận và Import</button>
                </form>
            </c:if>
        </div>

        <div id="export" class="tab-content">
            <!-- Form cho Export -->
            <form action="${pageContext.request.contextPath}/import-export?action=export" method="post">
                <div class="step">
                    <span class="step-number">1</span>
                    <h3>Chọn tài khoản cần xuất dữ liệu</h3>
                    <select id="export-account" name="export-account">
                        <option value="all">Tất cả tài khoản</option>
                        <c:forEach items="${accounts}" var="acc">
                            <option value="${acc.id}">${acc.name}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="step">
                    <span class="step-number">2</span>
                    <h3>Chọn khoảng thời gian</h3>
                    <div class="form-grid">
                        <input type="date" id="start-date" name="start-date" required>
                        <input type="date" id="end-date" name="end-date" required>
                    </div>
                </div>

                <div class="step">
                    <span class="step-number">3</span>
                    <h3>Chọn định dạng file</h3>
                    <select id="file-format" name="file-format">
                        <option value="csv">CSV (Comma-Separated Values)</option>
                        <option value="xlsx">Excel (XLSX)</option>
                    </select>
                </div>

                <button type="submit"><i class="fa-solid fa-download"></i> Tải file về</button>
            </form>
        </div>
    </div>
</div>

<script>
    function openTab(tabName) {
        let i, tabcontent, tablinks;
        tabcontent = document.getElementsByClassName("tab-content");
        for (i = 0; i < tabcontent.length; i++) {
            tabcontent[i].style.display = "none";
        }
        tablinks = document.getElementsByClassName("tab-button");
        for (i = 0; i < tablinks.length; i++) {
            tablinks[i].className = tablinks[i].className.replace(" active", "");
        }
        document.getElementById(tabName).style.display = "block";
        event.currentTarget.className += " active";
    }
</script>
</body>
</html>