<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">

<style>
    .categories-container {
        max-width: 900px;
        margin: 40px auto;
        font-family: "Poppins", sans-serif;
    }

    h1, h2 {
        color: var(--primary-color, #4a90e2);
        border-bottom: 2px solid var(--primary-color, #4a90e2);
        padding-bottom: 10px;
        margin-bottom: 20px;
    }

    .form-container, .table-container {
        background-color: #fff;
        border-radius: 10px;
        padding: 25px 30px;
        box-shadow: 0 3px 6px rgba(0,0,0,0.1);
        margin-bottom: 25px;
    }

    .form-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
        gap: 20px;
        align-items: end; /* ✅ giúp các ô input nằm cùng đường chân */
    }

    .form-group {
        display: flex;
        flex-direction: column;
        position: relative; /* For icon picker positioning */
    }

    .form-group label {
        margin-bottom: 6px;
        font-weight: 600;
        color: #333;
    }

    .form-group input[type="text"],
    .form-group select {
        padding: 10px 12px;
        border: 1px solid #ccc;
        border-radius: 6px;
        font-size: 15px;
        height: 42px; /* ✅ chiều cao đồng nhất */
    }

    .form-group input[type="color"] {
        width: 100%;
        height: 42px;
        border: 1px solid #ccc;
        border-radius: 6px;
        cursor: pointer;
        padding: 0;
    }

    .icon-input {
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .icon-input input {
        flex: 1;
        background: #fafafa;
        height: 42px;
    }

    .icon-input i {
        font-size: 22px;
        color: #333; /* icon mặc định đen */
    }

    .icon-input button {
        background-color: var(--primary-color, #4a90e2);
        color: #fff;
        border: none;
        padding: 8px 14px;
        border-radius: 6px;
        cursor: pointer;
        font-weight: 500;
        height: 42px;
    }

    .form-container button[type="submit"] {
        grid-column: 1 / -1;
        margin-top: 15px;
        background-color: var(--primary-color, #4a90e2);
        color: white;
        padding: 12px 20px;
        border: none;
        border-radius: 6px;
        cursor: pointer;
        font-size: 16px;
    }

    .form-container button[type="submit"]:hover {
        opacity: 0.9;
    }

    table {
        width: 100%;
        border-collapse: collapse;
    }

    th, td {
        border-bottom: 1px solid #e0e0e0;
        padding: 12px;
        text-align: left;
    }

    th {
        background-color: #f8f9fa;
    }

    .color-swatch {
        display: inline-block;
        width: 18px;
        height: 18px;
        border-radius: 4px;
        margin-right: 6px;
    }

    .actions a {
        color: var(--primary-color, #4a90e2);
        text-decoration: none;
        margin-right: 10px;
    }

    .sub-category {
        padding-left: 24px;
    }

    .sub-category::before {
        content: '↳ ';
    }

    /* Popup icon */
    #iconPicker {
        display: none;
        position: absolute;
        top: 100%; /* Position below the input group */
        left: 0;
        right: 0;
        z-index: 10;
        margin-top: 2px;
        padding: 15px;
        border: 1px solid #ddd;
        border-radius: 8px;
        background: #fff;
        box-shadow: 0 2px 6px rgba(0,0,0,0.1);
        max-height: 300px;
        overflow-y: auto;
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
</style>

<div class="categories-container">
    <h1><i class="fa-solid fa-tags"></i> Quản Lý Danh Mục</h1>

    <div class="form-container">
        <h2>
            <c:choose>
                <c:when test="${not empty editCategory}">Chỉnh Sửa Danh Mục</c:when>
                <c:otherwise>Thêm Danh Mục Mới</c:otherwise>
            </c:choose>
        </h2>

        <c:if test="${readonly}">
            <p style="color:red; font-weight:bold;">⚠️ Bạn đang ở chế độ khách — không thể chỉnh sửa!</p>
        </c:if>

        <form action="${pageContext.request.contextPath}/categories" method="post">
            <c:if test="${not empty editCategory}">
                <input type="hidden" name="id" value="${editCategory.id}">
            </c:if>

            <div class="form-grid">
                <div class="form-group">
                    <label for="name">Tên danh mục:</label>
                    <input type="text" id="name" name="name"
                           value="${editCategory.name}" required placeholder="Ví dụ: Ăn uống" ${readonly ? 'disabled' : ''}>
                </div>

                <div class="form-group">
                    <label for="type">Loại:</label>
                    <select id="type" name="type" ${readonly ? 'disabled' : ''}>
                        <option value="expense" ${editCategory.type == 'expense' ? 'selected' : ''}>Chi tiêu</option>
                        <option value="income" ${editCategory.type == 'income' ? 'selected' : ''}>Thu nhập</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="parentId">Danh mục cha:</label>
                    <select id="parentId" name="parentId" ${readonly ? 'disabled' : ''}>
                        <option value="">-- Không có (Là danh mục cha) --</option>
                        <c:forEach var="cat" items="${categories}">
                            <c:if test="${empty cat.parent}">
                                <option value="${cat.id}" ${not empty editCategory.parent && editCategory.parent.id == cat.id ? 'selected' : ''}>${cat.name}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>

                <div class="form-group" style="grid-column: 1 / -1;">
                    <label for="icon">Chọn biểu tượng:</label>
                    <div class="icon-input">
                        <input type="text" id="icon" name="icon"
                               value="${editCategory.iconPath}"
                               placeholder="fa-solid fa-utensils" readonly>
                        <i id="previewIcon"
                           class="${empty editCategory.iconPath ? 'fa-solid fa-tag' : editCategory.iconPath}"></i>
                        <c:if test="${!readonly}">
                            <button type="button" id="chooseIconBtn">Chọn</button>
                        </c:if>
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
                           value="${empty editCategory.color ? '#4a90e2' : editCategory.color}" ${readonly ? 'disabled' : ''}>
                </div>
            </div>

            <c:if test="${!readonly}">
                <button type="submit">
                    <i class="fa-solid fa-plus"></i>
                    <c:choose>
                        <c:when test="${not empty editCategory}">Cập nhật</c:when>
                        <c:otherwise>Thêm Mới</c:otherwise>
                    </c:choose>
                </button>
            </c:if>
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
                <c:if test="${empty parentCat.parent}">
                    <tr>
                        <td>
                            <span class="color-swatch" style="background-color:${parentCat.color};"></span>
                            <i class="${parentCat.iconPath}" style="color:#333;"></i>
                            <strong>${parentCat.name}</strong>
                        </td>
                        <td>${parentCat.type}</td>
                        <td class="actions">
                            <c:if test="${!readonly}">
                                <a href="${pageContext.request.contextPath}/categories?action=edit&id=${parentCat.id}">
                                    <i class="fa-solid fa-pen"></i> Sửa
                                </a>
                                <a href="${pageContext.request.contextPath}/categories?action=delete&id=${parentCat.id}"
                                   onclick="return confirm('Bạn có chắc muốn xóa danh mục này không?');">
                                    <i class="fa-solid fa-trash"></i> Xóa
                                </a>
                            </c:if>
                        </td>
                    </tr>

                    <c:forEach var="childCat" items="${categories}">
                        <c:if test="${not empty childCat.parent && childCat.parent.id == parentCat.id}">
                            <tr>
                                <td class="sub-category">
                                    <span class="color-swatch" style="background-color:${childCat.color};"></span>
                                    <i class="${childCat.iconPath}" style="color:#333;"></i>
                                        ${childCat.name}
                                </td>
                                <td>${childCat.type}</td>
                                <td class="actions">
                                    <c:if test="${!readonly}">
                                        <a href="${pageContext.request.contextPath}/categories?action=edit&id=${childCat.id}">
                                            <i class="fa-solid fa-pen"></i> Sửa
                                        </a>
                                        <a href="${pageContext.request.contextPath}/categories?action=delete&id=${childCat.id}"
                                           onclick="return confirm('Bạn có chắc muốn xóa danh mục này không?');">
                                            <i class="fa-solid fa-trash"></i> Xóa
                                        </a>
                                    </c:if>
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

<script>
    document.addEventListener("DOMContentLoaded", () => {
        const btn = document.getElementById("chooseIconBtn");
        const picker = document.getElementById("iconPicker");
        const input = document.getElementById("icon");
        const preview = document.getElementById("previewIcon");
        const search = document.getElementById("iconSearch");
        const grid = document.getElementById("iconGrid");
        const loading = document.getElementById("loadingIcons");
        let allIcons = [];

        if (btn) {
            btn.addEventListener("click", () => {
                picker.style.display = picker.style.display === "none" ? "block" : "none";
                if (picker.style.display === "block") search.focus();
            });
        }

        document.addEventListener("click", (e) => {
            if (picker && !picker.contains(e.target) && e.target !== btn) {
                picker.style.display = "none";
            }
        });

        search.addEventListener("input", () => {
            const term = search.value.toLowerCase();
            renderIcons(allIcons.filter(i => i.includes(term)));
        });

        function selectIcon(iconClass) {
            input.value = "fa-solid fa-" + iconClass;
            preview.className = "fa-solid fa-" + iconClass;
            picker.style.display = "none";
        }

        function renderIcons(list) {
            grid.innerHTML = "";
            list.forEach(cls => {
                const el = document.createElement("i");
                el.className = "fa-solid fa-" + cls;
                el.title = cls;
                el.addEventListener("click", () => selectIcon(cls));
                grid.appendChild(el);
            });
            loading.style.display = list.length === 0 ? "block" : "none";
            loading.innerText = list.length === 0 ? "Không tìm thấy biểu tượng nào..." : "";
        }

        async function loadIcons() {
            try {
                const res = await fetch("${pageContext.request.contextPath}/assets/icons.json");
                const data = await res.json();
                allIcons = Object.keys(data);
                renderIcons(allIcons);
                loading.style.display = "none";
            } catch {
                loading.innerText = "Không thể tải danh sách icon 😢";
            }
        }

        loadIcons();
    });
</script>
