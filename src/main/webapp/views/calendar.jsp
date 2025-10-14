<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Lịch Sự Kiện</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/calendar.css">
    <script type="module" src="${pageContext.request.contextPath}/assets/js/events.js"></script>
</head>
<body>
    <div class="container">
        <div class="notification-panel" id="notificationPanel">
            <!-- Bảng thông báo 7 sự kiện gần nhất sẽ render bằng JS -->
        </div>
        <div class="calendar-panel" id="calendarPanel">
            <!-- Bảng lịch tháng sẽ render bằng JS -->
        </div>
    </div>
</body>
</html>
