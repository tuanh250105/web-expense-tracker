<%--
  Created by IntelliJ IDEA.
  User: khodo
  Date: 21/9/25
  Time: 01:21
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<style>
    .report-container {<%--
  Created by IntelliJ IDEA.
  User: khodo
  Date: 21/9/25
  Time: 01:21
  To change this template use File | Settings | File Templates.
--%>
    <%@ page contentType="text/html;charset=UTF-8" language="java" %>

    <style>
    .report-container {
        max-width: 800px;
        margin: 40px auto;
        padding: 20px;
    }

        .report-header {
            text-align: center;
            margin-bottom: 30px;
            padding-bottom: 20px;
            border-bottom: 2px solid #e0e0e0;
        }

        .report-header h1 {
            color: #333;
            font-size: 28px;
            margin-bottom: 10px;
        }

        .report-header p {
            color: #666;
            font-size: 14px;
        }

        .report-form {
            background: #fff;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }

        .form-group {
            margin-bottom: 25px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #333;
            font-weight: 600;
            font-size: 14px;
        }

        .form-group input[type="text"],
        .form-group input[type="email"],
        .form-group textarea {
            width: 100%;
            padding: 12px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 14px;
            font-family: inherit;
            transition: border-color 0.3s;
        }

        .form-group input:focus,
        .form-group textarea:focus {
            outline: none;
            border-color: #4CAF50;
        }

        .form-group textarea {
            min-height: 200px;
            resize: vertical;
        }

        .form-group small {
            display: block;
            margin-top: 5px;
            color: #999;
            font-size: 12px;
        }

        .btn-submit {
            background: #4CAF50;
            color: white;
            padding: 12px 30px;
            border: none;
            border-radius: 4px;
            font-size: 16px;
            cursor: pointer;
            transition: background 0.3s;
            width: 100%;
        }

        .btn-submit:hover {
            background: #45a049;
        }

        .btn-submit:disabled {
            background: #ccc;
            cursor: not-allowed;
        }

        .error-message {
            background: #ffebee;
            color: #c62828;
            padding: 12px;
            border-radius: 4px;
            margin-bottom: 20px;
            display: none;
        }

        .error-message.show {
            display: block;
        }

        .required {
            color: #f44336;
        }
</style>

<div class="report-container">
    <div class="report-header">
        <h1>Báo Cáo / Góp Ý</h1>
        <p>Gửi phản hồi, báo cáo lỗi hoặc đóng góp ý kiến của bạn cho chúng tôi</p>
    </div>

    <div class="report-form">
        <div class="error-message" id="errorMessage"></div>

        <form action="${pageContext.request.contextPath}/report" method="POST" id="reportForm">
            <div class="form-group">
                <label for="senderName">Họ và Tên <span class="required">*</span></label>
                <input type="text" id="senderName" name="senderName" required
                       placeholder="Nhập họ tên của bạn">
            </div>

            <div class="form-group">
                <label for="senderEmail">Email <span class="required">*</span></label>
                <input type="email" id="senderEmail" name="senderEmail" required
                       placeholder="email@example.com">
                <small>Chúng tôi sẽ phản hồi qua email này</small>
            </div>

            <div class="form-group">
                <label for="subject">Tiêu Đề <span class="required">*</span></label>
                <input type="text" id="subject" name="subject" required
                       placeholder="Nhập tiêu đề báo cáo">
            </div>

            <div class="form-group">
                <label for="message">Nội Dung <span class="required">*</span></label>
                <textarea id="message" name="message" required
                          placeholder="Nhập nội dung chi tiết báo cáo hoặc góp ý của bạn..."></textarea>
                <small>Tối thiểu 10 ký tự</small>
            </div>

            <button type="submit" class="btn-submit" id="submitBtn">
                Gửi Báo Cáo
            </button>
        </form>
    </div>
</div>

<script>
    document.getElementById('reportForm').addEventListener('submit', function(e) {
        const message = document.getElementById('message').value.trim();
        const errorDiv = document.getElementById('errorMessage');
        const submitBtn = document.getElementById('submitBtn');

        if (message.length < 10) {
            e.preventDefault();
            errorDiv.textContent = 'Nội dung phải có ít nhất 10 ký tự';
            errorDiv.classList.add('show');
            return false;
        }

        // Disable button to prevent double submission
        submitBtn.disabled = true;
        submitBtn.textContent = 'Đang gửi...';
    });

    // Hide error message when user starts typing
    document.getElementById('message').addEventListener('input', function() {
        document.getElementById('errorMessage').classList.remove('show');
    });
</script>

max-width: 800px;
        margin: 40px auto;
        padding: 20px;
    }

    .report-header {
        text-align: center;
        margin-bottom: 30px;
        padding-bottom: 20px;
        border-bottom: 2px solid #e0e0e0;
    }

    .report-header h1 {
        color: #333;
        font-size: 28px;
        margin-bottom: 10px;
    }

    .report-header p {
        color: #666;
        font-size: 14px;
    }

    .report-form {
        background: #fff;
        padding: 30px;
        border-radius: 8px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }

    .form-group {
        margin-bottom: 25px;
    }

    .form-group label {
        display: block;
        margin-bottom: 8px;
        color: #333;
        font-weight: 600;
        font-size: 14px;
    }

    .form-group input[type="text"],
    .form-group input[type="email"],
    .form-group textarea {
        width: 100%;
        padding: 12px;
        border: 1px solid #ddd;
        border-radius: 4px;
        font-size: 14px;
        font-family: inherit;
        transition: border-color 0.3s;
    }

    .form-group input:focus,
    .form-group textarea:focus {
        outline: none;
        border-color: #4CAF50;
    }

    .form-group textarea {
        min-height: 200px;
        resize: vertical;
    }

    .form-group small {
        display: block;
        margin-top: 5px;
        color: #999;
        font-size: 12px;
    }

    .btn-submit {
        background: #4CAF50;
        color: white;
        padding: 12px 30px;
        border: none;
        border-radius: 4px;
        font-size: 16px;
        cursor: pointer;
        transition: background 0.3s;
        width: 100%;
    }

    .btn-submit:hover {
        background: #45a049;
    }

    .btn-submit:disabled {
        background: #ccc;
        cursor: not-allowed;
    }

    .error-message {
        background: #ffebee;
        color: #c62828;
        padding: 12px;
        border-radius: 4px;
        margin-bottom: 20px;
        display: none;
    }

    .error-message.show {
        display: block;
    }

    .required {
        color: #f44336;
    }
</style>

<div class="report-container">
    <div class="report-header">
        <h1>Báo Cáo / Góp Ý</h1>
        <p>Gửi phản hồi, báo cáo lỗi hoặc đóng góp ý kiến của bạn cho chúng tôi</p>
    </div>

    <div class="report-form">
        <div class="error-message" id="errorMessage"></div>

        <form action="${pageContext.request.contextPath}/report" method="POST" id="reportForm">
            <div class="form-group">
                <label for="senderName">Họ và Tên <span class="required">*</span></label>
                <input type="text" id="senderName" name="senderName" required
                       placeholder="Nhập họ tên của bạn">
            </div>

            <div class="form-group">
                <label for="senderEmail">Email <span class="required">*</span></label>
                <input type="email" id="senderEmail" name="senderEmail" required
                       placeholder="email@example.com">
                <small>Chúng tôi sẽ phản hồi qua email này</small>
            </div>

            <div class="form-group">
                <label for="subject">Tiêu Đề <span class="required">*</span></label>
                <input type="text" id="subject" name="subject" required
                       placeholder="Nhập tiêu đề báo cáo">
            </div>

            <div class="form-group">
                <label for="message">Nội Dung <span class="required">*</span></label>
                <textarea id="message" name="message" required
                          placeholder="Nhập nội dung chi tiết báo cáo hoặc góp ý của bạn..."></textarea>
                <small>Tối thiểu 10 ký tự</small>
            </div>

            <button type="submit" class="btn-submit" id="submitBtn">
                Gửi Báo Cáo
            </button>
        </form>
    </div>
</div>

<script>
    document.getElementById('reportForm').addEventListener('submit', function(e) {
        const message = document.getElementById('message').value.trim();
        const errorDiv = document.getElementById('errorMessage');
        const submitBtn = document.getElementById('submitBtn');

        if (message.length < 10) {
            e.preventDefault();
            errorDiv.textContent = 'Nội dung phải có ít nhất 10 ký tự';
            errorDiv.classList.add('show');
            return false;
        }

        // Disable button to prevent double submission
        submitBtn.disabled = true;
        submitBtn.textContent = 'Đang gửi...';
    });

    // Hide error message when user starts typing
    document.getElementById('message').addEventListener('input', function() {
        document.getElementById('errorMessage').classList.remove('show');
    });
</script>
