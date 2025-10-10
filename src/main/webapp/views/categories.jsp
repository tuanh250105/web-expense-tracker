<%-- src/main/webapp/views/categories.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản Lý Danh Mục</title>

    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">

    <style>
        :root {
            --primary-color: #4a90e2;
            --background-color: #f4f7f9;
            --text-color: #333;
            --card-bg: #ffffff;
            --border-color: #e0e0e0;
        }

        body {
            font-family: Arial, sans-serif;
            background-color: var(--background-color);
            color: var(--text-color);
            margin: 0;
            padding: 20px;
        }

        .container {
            max-width: 900px;
            margin: auto;
        }

        h1, h2 {
            color: var(--primary-color);
            border-bottom: 2px solid var(--primary-color);
            padding-bottom: 10px;
        }

        .form-container, .table-container {
            background-color: var(--card-bg);
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }

        .form-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
        }

        .form-group {
            display: flex;
            flex-direction: column;
        }

        label {
            margin-bottom: 5px;
            font-weight: bold;
        }

        input[type="text"], select {
            padding: 10px;
            border: 1px solid var(--border-color);
            border-radius: 4px;
            width: 100%;
            box-sizing: border-box;
        }

        input[type="color"] {
            padding: 5px;
            height: 40px;
        }

        button {
            background-color: var(--primary-color);
            color: white;
            padding: 12px 20px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
            grid-column: 1 / -1;
        }

        button:hover {
            opacity: 0.9;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th, td {
            border-bottom: 1px solid var(--border-color);
            padding: 12px;
            text-align: left;
        }

        th {
            background-color: #f8f9fa;
        }

        .color-swatch {
            display: inline-block;
            width: 20px;
            height: 20px;
            border-radius: 4px;
            vertical-align: middle;
            margin-right: 5px;
        }

        .actions a {
            color: var(--primary-color);
            text-decoration: none;
            margin-right: 10px;
        }

        .sub-category {
            padding-left: 20px;
        }

        .sub-category::before {
            content: '↳ ';
        }

        /* Popup chọn icon */
        #iconPicker {
            display: none;
            margin-top: 10px;
            padding: 15px;
            border: 1px solid var(--border-color);
            border-radius: 8px;
            background: #fff;
            box-shadow: 0 2px 6px rgba(0,0,0,0.1);
            max-height: 300px;
            overflow-y: auto;
        }

        #iconPicker input {
            width: 100%;
            padding: 10px;
            margin-bottom: 10px;
            border: 1px solid var(--border-color);
            border-radius: 6px;
        }

        #iconGrid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(45px, 1fr));
            gap: 8px;
        }

        #iconGrid i {
            font-size: 20px;
            cursor: pointer;
            padding: 6px;
            border-radius: 6px;
            transition: 0.2s;
            text-align: center;
        }

        #iconGrid i:hover {
            background: #f0f0f0;
        }

        #loadingIcons {
            text-align: center;
            color: gray;
            font-style: italic;
        }
    </style>
</head>
<body>

<div class="container">
    <h1><i class="fa-solid fa-tags"></i> Quản Lý Danh Mục</h1>

    <!-- Form thêm hoặc sửa danh mục -->
    <div class="form-container">
        <h2>
            <c:choose>
                <c:when test="${not empty editCategory}">Chỉnh Sửa Danh Mục</c:when>
                <c:otherwise>Thêm Danh Mục Mới</c:otherwise>
            </c:choose>
        </h2>
        <form action="${pageContext.request.contextPath}/categories" method="post">
            <c:if test="${not empty editCategory}">
                <input type="hidden" name="id" value="${editCategory.id}">
            </c:if>

            <div class="form-grid">
                <div class="form-group">
                    <label for="name">Tên danh mục:</label>
                    <input type="text" id="name" name="name"
                           value="${editCategory.name}" required placeholder="Ví dụ: Ăn uống">
                </div>

                <div class="form-group">
                    <label for="type">Loại:</label>
                    <select id="type" name="type">
                        <option value="expense" ${editCategory.categoryType == 'expense' ? 'selected' : ''}>Chi tiêu</option>
                        <option value="income" ${editCategory.categoryType == 'income' ? 'selected' : ''}>Thu nhập</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="parentId">Danh mục cha:</label>
                    <select id="parentId" name="parentId">
                        <option value="">-- Không có (Là danh mục cha) --</option>
                        <c:forEach var="cat" items="${categories}">
                            <c:if test="${empty cat.parentId}">
                                <option value="${cat.id}" ${editCategory.parentId == cat.id ? 'selected' : ''}>${cat.name}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>

                <!-- Chọn icon -->
                <div class="form-group">
                    <label for="icon">Chọn biểu tượng:</label>
                    <div style="display:flex; align-items:center; gap:10px;">
                        <input type="text" id="icon" name="icon"
                               value="${editCategory.icon}"
                               placeholder="fa-solid fa-utensils" readonly style="flex:1;">
                        <i id="previewIcon" class="${empty editCategory.icon ? 'fa-solid fa-question-circle' : editCategory.icon}"
                           style="font-size:22px; color:var(--primary-color);"></i>
                        <button type="button" id="chooseIconBtn" style="padding:8px 12px; background-color:var(--primary-color); color:white; border:none; border-radius:4px; cursor:pointer;">Chọn</button>
                    </div>

                    <div id="iconPicker">
                        <input type="text" id="iconSearch" placeholder="🔍 Tìm biểu tượng (ví dụ: money, car, food...)">
                        <div id="iconGrid"></div>
                        <p id="loadingIcons">Đang tải danh sách biểu tượng...</p>
                    </div>
                </div>

                <div class="form-group">
                    <label for="color">Màu sắc:</label>
                    <input type="color" id="color" name="color"
                           value="${empty editCategory.color ? '#4a90e2' : editCategory.color}">
                </div>
            </div>

            <button type="submit">
                <i class="fa-solid fa-plus"></i>
                <c:choose>
                    <c:when test="${not empty editCategory}">Cập nhật</c:when>
                    <c:otherwise>Thêm Mới</c:otherwise>
                </c:choose>
            </button>
        </form>
    </div>

    <!-- Bảng danh mục -->
    <div class="table-container">
        <h2>Danh Sách Danh Mục</h2>
        <table>
            <thead>
            <tr>
                <th>Tên Danh Mục</th>
                <th>Loại</th>
                <th style="width: 150px;">Hành động</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="parentCat" items="${categories}">
                <c:if test="${empty parentCat.parentId}">
                    <tr>
                        <td>
                            <span class="color-swatch" style="background-color:${parentCat.color};"></span>
                            <i class="${parentCat.icon}"></i>
                            <strong>${parentCat.name}</strong>
                        </td>
                        <td>${parentCat.categoryType}</td>
                        <td class="actions">
                            <!-- Thêm link sửa / xóa -->
                            <a href="${pageContext.request.contextPath}/categories?action=edit&id=${parentCat.id}">
                                <i class="fa-solid fa-pen"></i> Sửa
                            </a>
                            <a href="${pageContext.request.contextPath}/categories?action=delete&id=${parentCat.id}"
                               onclick="return confirm('Bạn có chắc muốn xóa danh mục này không?');">
                                <i class="fa-solid fa-trash"></i> Xóa
                            </a>
                        </td>
                    </tr>

                    <!-- Danh mục con -->
                    <c:forEach var="childCat" items="${categories}">
                        <c:if test="${childCat.parentId == parentCat.id}">
                            <tr>
                                <td class="sub-category">
                                    <span class="color-swatch" style="background-color:${childCat.color};"></span>
                                    <i class="${childCat.icon}"></i>
                                        ${childCat.name}
                                </td>
                                <td>${childCat.categoryType}</td>
                                <td class="actions">
                                    <a href="${pageContext.request.contextPath}/categories?action=edit&id=${childCat.id}">
                                        <i class="fa-solid fa-pen"></i> Sửa
                                    </a>
                                    <a href="${pageContext.request.contextPath}/categories?action=delete&id=${childCat.id}"
                                       onclick="return confirm('Bạn có chắc muốn xóa danh mục này không?');">
                                        <i class="fa-solid fa-trash"></i> Xóa
                                    </a>
                                </td>
                            </tr>
                        </c:if>
                    </c:forEach>
                </c:if>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<!-- JS: tải icon từ file nội bộ -->
<script>
    const btn = document.getElementById('chooseIconBtn');
    const picker = document.getElementById('iconPicker');
    const input = document.getElementById('icon');
    const preview = document.getElementById('previewIcon');
    const search = document.getElementById('iconSearch');
    const grid = document.getElementById('iconGrid');
    const loading = document.getElementById('loadingIcons');
    let allIcons = [];

    btn.addEventListener('click', () => {
        picker.style.display = picker.style.display === 'none' ? 'block' : 'none';
        search.focus();
    });

    document.addEventListener('click', (e) => {
        if (!picker.contains(e.target) && e.target !== btn) {
            picker.style.display = 'none';
        }
    });

    search.addEventListener('input', () => {
        const term = search.value.toLowerCase();
        renderIcons(allIcons.filter(i => i.includes(term)));
    });

    function selectIcon(iconClass) {
        input.value = 'fa-solid fa-' + iconClass;
        preview.className = 'fa-solid fa-' + iconClass;
        picker.style.display = 'none';
    }

    function renderIcons(list) {
        grid.innerHTML = '';
        list.forEach(cls => {
            const el = document.createElement('i');
            el.className = 'fa-solid fa-' + cls;
            el.title = cls;
            el.style.cursor = 'pointer';
            el.style.fontSize = '20px';
            el.style.padding = '6px';
            el.style.borderRadius = '6px';
            el.addEventListener('click', () => selectIcon(cls));
            el.addEventListener('mouseover', () => el.style.background = '#f0f0f0');
            el.addEventListener('mouseout', () => el.style.background = 'transparent');
            grid.appendChild(el);
        });
        loading.style.display = list.length === 0 ? 'block' : 'none';
        loading.innerText = list.length === 0 ? 'Không tìm thấy biểu tượng nào...' : '';
    }

    async function loadIcons() {
        try {
            const res = await fetch('${pageContext.request.contextPath}/assets/icons.json');
            const data = await res.json();
            allIcons = Object.keys(data);
            renderIcons(allIcons);
            loading.style.display = 'none';
        } catch (err) {
            console.error('Lỗi tải danh sách icon:', err);
            loading.innerText = 'Không thể tải danh sách icon 😢';
        }
    }

    loadIcons();
</script>

</body>
</html>
