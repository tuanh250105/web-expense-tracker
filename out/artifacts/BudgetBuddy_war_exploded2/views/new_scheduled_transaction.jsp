<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="form-section card add-form-container">
    <c:choose>
        <c:when test="${param.type == 'income'}">
            <h3>➕ THU NHẬP MỚI</h3>
        </c:when>
        <c:otherwise>
            <h3>➖ CHI PHÍ MỚI</h3>
        </c:otherwise>
    </c:choose>

    <form method="post" action="${pageContext.request.contextPath}/scheduled_transactions" class="add-form">
        <input type="hidden" name="action" value="create"/>
        <input type="hidden" name="type" value="${param.type != null ? param.type : 'income'}">

        <div class="add-form-group">
            <label>💼 Hạng mục</label>
            <select name="categoryId" id="categorySelect" required>
                <option value="">Chọn hạng mục...</option>
                <c:forEach var="cat" items="${categories}">
                    <option value="${cat.id}">${cat.name}</option>
                </c:forEach>
            </select>
        </div>

        <div class="add-form-group">
            <label>💰 Số tiền</label>
            <input type="number" step="0.01" name="amount" placeholder="Nhập số tiền..." min="0" required>
        </div>

        <div class="add-form-group">
            <label>🏦 Tài khoản</label>
            <select name="accountId" required>
                <option value="">Chọn tài khoản...</option>
                <c:forEach var="acc" items="${accounts}">
                    <option value="${acc.id}">${acc.name}</option>
                </c:forEach>
            </select>
        </div>
        <div class="add-form-group">
            <label>📝 Ghi chú</label>
            <input type="text" name="note" placeholder="Ghi chú thêm...">
        </div>

        <div class="add-form-group">
            <label>📅 Ngày bắt đầu</label>
            <input type="date" name="nextRun" id="nextRun" required>
        </div>

        <div class="add-form-group repeat-section">
            <label>🔁 Lặp lại</label>
            <select name="repeatType" id="repeatType" required>
                <option value="none">Không lặp</option>
                <option value="daily">Hàng ngày</option>
                <option value="weekly">Hàng tuần</option>
                <option value="monthly">Hàng tháng</option>
                <option value="yearly">Hàng năm</option>
            </select>
        </div>

        <input type="hidden" name="scheduleCron" id="scheduleCron" value="">

        <div class="form-buttons">
            <button type="button" id="cancelNew" class="btn-cancel">✖ Hủy</button>
            <button type="submit" class="btn-save">💾 Lưu</button>
        </div>
    </form>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const today = new Date().toISOString().split('T')[0];
        document.getElementById('nextRun').value = today;

        const repeatType = document.getElementById('repeatType');
        const scheduleCron = document.getElementById('scheduleCron');
        repeatType.addEventListener('change', function() {
            let cron = '';
            switch (this.value) {
                case 'none': cron = ''; break;
                case 'daily': cron = '0 0 * * * ?'; break;
                case 'weekly': cron = '0 0 * * 1'; break;
                case 'monthly': cron = '0 0 1 * ?'; break;
                case 'yearly': cron = '0 0 1 1 ?'; break;
                default: cron = '';
            }
            scheduleCron.value = cron;
        });
        repeatType.dispatchEvent(new Event('change'));
    });
</script>