<%@ page contentType="text/html; charset=UTF-8" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/rewards.css"/>

<div class="bb-rewards">
    <!-- Vòng quay + thao tác -->
    <div class="rw-left">
        <h3>🎉 Vòng quay may mắn</h3>

        <div class="rw-wheel-wrap">
            <canvas id="rw-wheel" width="380" height="380"></canvas>
        </div>

        <div class="rw-btns">
            <button id="rw-spin" class="rw-btn">Quay (−20)</button>
            <button id="rw-award" class="rw-btn">Nhận thưởng (+5)</button>
        </div>

        <p class="rw-points-line">Điểm hiện có: <b id="rw-points">0</b></p>
        <p id="rw-result"></p>
    </div>

    <!-- Lịch sử -->
    <div class="rw-history">
        <h3>🕒 Lịch sử gần đây</h3>
        <ul id="rw-history"></ul>
    </div>

    <!-- Popup cộng điểm -->
    <div id="rw-toast"></div>
</div>

<script>
    window.BB_CTX = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/assets/js/rewards.js"></script>
