<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<style>
    /* Các style chung được giữ lại */
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
        align-items: end;
    }
    .form-group {
        display: flex;
        flex-direction: column;
        position: relative;
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
        height: 42px;
    }
    .form-group input[type="color"] {
        width: 100%;
        height: 42px;
        border: 1px solid #ccc;
        border-radius: 6px;
        cursor: pointer;
        padding: 0;
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
        vertical-align: middle;
    }
    th {
        background-color: #f8f9fa;
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

    /* CSS CHO TRÌNH CHỌN ẢNH */
    .image-picker-group {
        display: flex;
        align-items: center;
        gap: 15px;
    }
    #previewImage {
        width: 42px;
        height: 42px;
        border-radius: 8px;
        border: 1px solid #ddd;
        object-fit: cover;
    }
    #chooseImageBtn {
        background-color: var(--primary-color, #4a90e2);
        color: #fff;
        border: none;
        padding: 8px 14px;
        border-radius: 6px;
        cursor: pointer;
        font-weight: 500;
        height: 42px;
    }
    #imagePickerModal {
        display: none;
        position: absolute;
        top: 100%;
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
    #imageGrid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(50px, 1fr));
        gap: 10px;
    }
    #imageGrid img {
        width: 100%;
        height: 50px;
        object-fit: cover;
        cursor: pointer;
        padding: 4px;
        border-radius: 8px;
        border: 2px solid transparent;
        transition: border-color 0.2s;
    }
    #imageGrid img:hover {
        border-color: var(--primary-color, #4a90e2);
    }
    .category-display {
        display: flex;
        align-items: center;
        gap: 10px;
    }
    .category-display img {
        width: 28px;
        height: 28px;
        border-radius: 6px;
    }
    .color-swatch {
        display: inline-block;
        width: 18px;
        height: 18px;
        border-radius: 4px;
        margin-right: 6px;
    }

    /* CSS CHO THÔNG BÁO LỖI */
    .alert {
        padding: 15px;
        margin-bottom: 20px;
        border: 1px solid transparent;
        border-radius: 4px;
    }
    .alert-danger {
        color: #a94442;
        background-color: #f2dede;
        border-color: #ebccd1;
    }
</style>

<div class="categories-container">
    <h1>Quản Lý Danh Mục</h1>

    <%-- HIỂN THỊ THÔNG BÁO LỖI (NẾU CÓ) --%>
    <c:if test="${not empty error}">
        <div class="alert alert-danger">
                ${error}
        </div>
    </c:if>

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
                    <label>Chọn biểu tượng:</label>
                    <div class="image-picker-group">
                        <c:set var="iconToShow" value="${pageContext.request.contextPath}/assets/images/categories/food.png" />
                        <c:if test="${not empty editCategory.iconPath}">
                            <c:set var="iconToShow" value="${pageContext.request.contextPath}${editCategory.iconPath}" />
                        </c:if>
                        <img id="previewImage" src="${iconToShow}" alt="Preview">

                        <input type="hidden" id="iconPathInput" name="icon" value="${not empty editCategory.iconPath ? editCategory.iconPath : '/assets/images/categories/food.png'}">

                        <c:if test="${!readonly}">
                            <button type="button" id="chooseImageBtn">Chọn Ảnh</button>
                        </c:if>
                    </div>

                    <div id="imagePickerModal">
                        <div id="imageGrid"></div>
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
                            <div class="category-display">
                                <span class="color-swatch" style="background-color:${parentCat.color};"></span>
                                <img src="${pageContext.request.contextPath}${parentCat.iconPath}" alt="">
                                <strong>${parentCat.name}</strong>
                            </div>
                        </td>
                        <td>${parentCat.type}</td>
                        <td class="actions">
                            <c:if test="${!readonly}">
                                <a href="${pageContext.request.contextPath}/categories?action=edit&id=${parentCat.id}">Sửa</a>
                                <a href="${pageContext.request.contextPath}/categories?action=delete&id=${parentCat.id}"
                                   onclick="return confirm('Bạn có chắc muốn xóa danh mục này không?');">Xóa</a>
                            </c:if>
                        </td>
                    </tr>

                    <c:forEach var="childCat" items="${categories}">
                        <c:if test="${not empty childCat.parent && childCat.parent.id == parentCat.id}">
                            <tr>
                                <td class="sub-category">
                                    <div class="category-display">
                                        <span class="color-swatch" style="background-color:${childCat.color};"></span>
                                        <img src="${pageContext.request.contextPath}${childCat.iconPath}" alt="">
                                            ${childCat.name}
                                    </div>
                                </td>
                                <td>${childCat.type}</td>
                                <td class="actions">
                                    <c:if test="${!readonly}">
                                        <a href="${pageContext.request.contextPath}/categories?action=edit&id=${childCat.id}">Sửa</a>
                                        <a href="${pageContext.request.contextPath}/categories?action=delete&id=${childCat.id}"
                                           onclick="return confirm('Bạn có chắc muốn xóa danh mục này không?');">Xóa</a>
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
        const chooseBtn = document.getElementById("chooseImageBtn");
        const modal = document.getElementById("imagePickerModal");
        const grid = document.getElementById("imageGrid");
        const hiddenInput = document.getElementById("iconPathInput");
        const previewImg = document.getElementById("previewImage");

        const imageFiles = ["pet.png", "food.png", "gift.png", "home.png", "loan.png", "phone.png", "beauty.png", "drinks.png", "health.png", "salary.png", "travel.png", "charity.png", "concert.png", "shopping.png", "childcare.png", "education.png", "insurance.png", "investment.png", "entertainment.png", "transportation.png"];

        const basePath = "${pageContext.request.contextPath}/assets/images/categories/";

        function renderImageGrid() {
            grid.innerHTML = "";
            imageFiles.forEach(file => {
                const img = document.createElement("img");
                img.src = basePath + file;
                img.dataset.path = "/assets/images/categories/" + file;
                img.addEventListener("click", selectImage);
                grid.appendChild(img);
            });
        }

        function selectImage(event) {
            const selectedRelativePath = event.target.dataset.path;
            const selectedFullPath = event.target.src;

            hiddenInput.value = selectedRelativePath;
            previewImg.src = selectedFullPath;

            modal.style.display = "none";
        }

        if (chooseBtn) {
            chooseBtn.addEventListener("click", () => {
                const isHidden = modal.style.display === "none" || modal.style.display === "";
                modal.style.display = isHidden ? "block" : "none";
                if (isHidden) {
                    renderImageGrid();
                }
            });
        }

        document.addEventListener("click", (event) => {
            if (modal && !modal.contains(event.target) && event.target !== chooseBtn) {
                modal.style.display = "none";
            }
        });
    });
</script>
