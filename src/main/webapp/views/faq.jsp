<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="faq-container">
    <h2>Hỏi đáp thường gặp với Budgetbuddy</h2>
    <p>Chào mừng bạn đến với ứng dụng Quản lý chi tiêu. Dưới đây là thông tin cơ bản về ứng dụng của chúng tôi. </p>

    <c:forEach var="faq" items="${faqs}">
        <div class="faq-item">
            <div class="faq-question" onclick="toggleAnswer(this)">
                    ${faq.question}
                <span class="faq-toggle">?</span>
            </div>
            <div class="faq-answer" style="display: none;">
                <c:out value="${faq.answer}" escapeXml="false" />
            </div>
        </div>
    </c:forEach>
</div>

<script>
    function toggleAnswer(element) {
        var answer = element.nextElementSibling;
        var toggle = element.querySelector('.faq-toggle');
        if (answer.style.display === "none") {
            answer.style.display = "block";
            toggle.textContent = '-';
        } else {
            answer.style.display = "none";
            toggle.textContent = '?';
        }
    }
</script>

<style>
    .faq-container { max-width: 800px; margin: 20px auto; padding: 20px; background: #f9f9f9; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
    .faq-item { margin-bottom: 15px; border: 1px solid #ddd; border-radius: 5px; overflow: hidden; }
    .faq-question { background: #e0e0e0; padding: 15px; cursor: pointer; font-weight: bold; display: flex; justify-content: space-between; align-items: center; }
    .faq-toggle { font-size: 20px; color: #333; }
    .faq-answer { padding: 15px; background: #fff; line-height: 1.6; }
    .faq-answer a { color: #007bff; text-decoration: underline; cursor: pointer; }
    .faq-answer a:hover { color: #0056b3; }
</style>