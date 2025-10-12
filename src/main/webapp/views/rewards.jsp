<%@ page contentType="text/html; charset=UTF-8" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/rewards.css"/>

<div class="bb-rewards">
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

    <div class="rw-history">
        <h3>🕒 Lịch sử gần đây</h3>
        <ul id="rw-history"></ul>
    </div>

    <div id="rw-toast"></div>
</div>

<script>
    window.BB_CTX = "${pageContext.request.contextPath}";
    window.BB_USER_ID = "67b78d51-4eec-491c-bbf0-30e982def9e0";
    console.log("Reward Page Loaded:", window.BB_CTX, "User:", window.BB_USER_ID);
</script>
<script src="${pageContext.request.contextPath}/assets/js/rewards.js"></script>
