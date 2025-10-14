document.addEventListener("DOMContentLoaded", () => {
    const addIncomeBtn = document.querySelector('.fab.add-income');
    const addExpenseBtn = document.querySelector('.fab.add-expense');
    const formIncomeContainer = document.querySelector('#incomeForm');
    const formExpenseContainer = document.querySelector('#expenseForm');
    const categoryModal = document.getElementById('categoryModal');
    const closeCategoryModalBtn = document.querySelector('.close-category-modal');
    const categoryList = document.getElementById('categoryList');


    // 2 nut Prev va Next
    const navButtons = document.querySelectorAll('.nav-btn');
    navButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            this.classList.add('loading');
            this.style.opacity = '0.5';
            this.style.pointerEvents = 'none';
        });
    });


    // Khởi tạo Flatpickr cho 2 cái Add/Edit Form
    function initFlatpickr(formElement) {
        const dateInputs = formElement.querySelectorAll('.select_new_date');
        const timeInputs = formElement.querySelectorAll('.select_new_time');

        dateInputs.forEach(input => {
            flatpickr(input, {
                dateFormat: "Y-m-d",
                allowInput: true,
                defaultDate: new Date()
            });
        });

        timeInputs.forEach(input => {
            flatpickr(input, {
                enableTime: true,
                noCalendar: true,
                dateFormat: "H:i",
                time_24hr: true,
                defaultDate: new Date()
            });
        });
    }


    // Khởi tạo Flatpickr cho FilterPanel
    const filterPanel = document.querySelector('.filter-panel');
    if (filterPanel) {
        const filterDateInputs = filterPanel.querySelectorAll('.select_filter_from_date, .select_filter_to_date');

        filterDateInputs.forEach(input => {
            flatpickr(input, {
                dateFormat: "Y-m-d",
                allowInput: true
            });
        });
    }

    // Hiển thị form khi nhấn 2 nút FAB incomv vs expense buttons
    addIncomeBtn.addEventListener('click', () => {
        formIncomeContainer.classList.add('active');
        initFlatpickr(formIncomeContainer);
        resetForm(formIncomeContainer);
    });

    addExpenseBtn.addEventListener('click', () => {
        formExpenseContainer.classList.add('active');
        initFlatpickr(formExpenseContainer);
        resetForm(formExpenseContainer);
    });

    // Function reset form về default
    function resetForm(formContainer) {
        // Reset icon về default
        const categoryIcon = formContainer.querySelector('.category-icon');
        if (categoryIcon) {
            categoryIcon.src = `${window.location.origin}/assets/images/categories/salary.png`;
            categoryIcon.alt = 'Icon';
        }

        // Reset các field khác
        formContainer.querySelector('.select_new_category').value = '';
        formContainer.querySelector('.hidden_new_category').value = '';
        formContainer.querySelector('.type_value').value = '';
        formContainer.querySelector('.type_new_notes').value = '';

        // Reset action về add
        const actionInput = formContainer.querySelector('input[name="action"]');
        if (formContainer.id === 'incomeForm') {
            actionInput.value = 'add_income';
            formContainer.querySelector('.addForm-header h2').textContent = 'New Income';
        } else {
            actionInput.value = 'add_expense';
            formContainer.querySelector('.addForm-header h2').textContent = 'New Expense';
        }

        // Remove id input nếu có
        const idInput = formContainer.querySelector('input[name="id"]');
        if (idInput) {
            idInput.remove();
        }
    }


    // Logic cho Category Modal
    document.querySelectorAll('.open-category-modal').forEach(input => {
        input.addEventListener('click', (e) => {
            const currentForm = e.target.closest('.container-addForm');
            const isFilterPanel = e.target.closest('.filter-panel');

            if (currentForm) {
                categoryModal.dataset.activeForm = currentForm.id;
                categoryModal.dataset.isFilter = 'false';
            } else if (isFilterPanel) {
                categoryModal.dataset.activeForm = 'filter';
                categoryModal.dataset.isFilter = 'true';
            }

            categoryModal.classList.add('active');
        });
    });

    closeCategoryModalBtn.addEventListener('click', () => {
        categoryModal.classList.remove('active');
    });

    categoryModal.addEventListener('click', (e) => {
        if (e.target === categoryModal) categoryModal.classList.remove('active');
    });

    // Xử lý chọn Category
    categoryList.addEventListener('click', (e) => {
        const button = e.target.closest('button');
        if (!button) return;

        const selectedCategoryName = button.textContent.trim();
        const selectedCategoryValue = button.getAttribute('data-category');
        const selectedCategoryIcon = button.getAttribute('data-icon');

        if (categoryModal.dataset.isFilter === 'true') {
            // Filter panel
            filterPanel.querySelector('#select_new_category').value = selectedCategoryName;
            filterPanel.querySelector('#hidden_new_category').value = selectedCategoryValue;
        } else {
            // Add/Edit form
            const activeFormContainer = document.querySelector(`#${categoryModal.dataset.activeForm}`);
            if (activeFormContainer) {
                // Cập nhật tên category
                activeFormContainer.querySelector('.select_new_category').value = selectedCategoryName;
                activeFormContainer.querySelector('.hidden_new_category').value = selectedCategoryValue;

                // Cập nhật icon category
                const categoryIconImg = activeFormContainer.querySelector('.category-icon');
                if (categoryIconImg && selectedCategoryIcon) {
                    categoryIconImg.src = selectedCategoryIcon;
                    categoryIconImg.alt = selectedCategoryName;
                }
            }
        }

        categoryModal.classList.remove('active');
    });

    // Nút cancel trong mỗi form
    document.querySelectorAll('.cancelTransactionBtn').forEach(btn => {
        btn.addEventListener('click', () => {
            formIncomeContainer.classList.remove('active');
            formExpenseContainer.classList.remove('active');
        });
    });


    // Logic Edit Transaction
    if (window.editTransactionData) {
        const t = window.editTransactionData;

        // ✅ Sửa: So sánh không phân biệt hoa/thường
        const formContainer = t.type.toLowerCase() === "income" ? formIncomeContainer : formExpenseContainer;

        formContainer.classList.add("active");
        initFlatpickr(formContainer);

        // ✅ Set category
        formContainer.querySelector('.hidden_new_category').value = t.categoryId;
        formContainer.querySelector('.select_new_category').value = t.categoryName;

        // ✅ Set category icon - Tìm icon từ categoryList
        const categoryButton = document.querySelector(`#categoryList button[data-category="${t.categoryId}"]`);
        if (categoryButton) {
            const iconPath = categoryButton.getAttribute('data-icon');
            const categoryIconImg = formContainer.querySelector('.category-icon');
            if (categoryIconImg && iconPath) {
                categoryIconImg.src = iconPath;
                categoryIconImg.alt = t.categoryName;
            }
        }

        // ✅ Set other fields
        formContainer.querySelector('.type_value').value = t.value;
        formContainer.querySelector('.select_new_account').value = t.accountId;
        formContainer.querySelector('.select_new_date').value = t.date;
        formContainer.querySelector('.select_new_time').value = t.time;
        formContainer.querySelector('.type_new_notes').value = t.notes;

        // ✅ Set header
        const header = formContainer.querySelector('.addForm-header h2');
        if (header) header.textContent = "Edit " + t.type;

        // ✅ Set action
        const actionInput = formContainer.querySelector('input[name="action"]');
        actionInput.value = t.type.toLowerCase() === "income" ? "update_income" : "update_expense";

        // ✅ Đảm bảo type được set đúng
        const typeInput = formContainer.querySelector('input[name="type"]');
        if (typeInput) {
            typeInput.value = t.type.toLowerCase();
        }

        // ✅ Set ID
        let idInput = formContainer.querySelector('input[name="id"]');
        if (!idInput) {
            idInput = document.createElement("input");
            idInput.type = "hidden";
            idInput.name = "id";
            formContainer.querySelector("form").appendChild(idInput);
        }
        idInput.value = t.id;
    }


    // Logic expand notes khi click vào transaction-item
    const items = document.querySelectorAll(".transaction-item");
    items.forEach(item => {
        item.addEventListener("click", function(e) {
            // Không toggle nếu click vào button hoặc dropdown
            if (e.target.closest(".more-btn") || e.target.closest(".dropdown-menu")) {
                return;
            }
            item.classList.toggle("expanded");
        });
    });


    // Logic dropdown menu (more-butn)
    document.addEventListener("click", (e) => {
        const moreBtn = e.target.closest(".more-btn");
        const dropdownMenu = e.target.closest(".dropdown-menu");

        if (moreBtn) {
            e.stopPropagation();
            e.preventDefault();

            const menu = moreBtn.nextElementSibling;

            if (!menu || !menu.classList.contains("dropdown-menu")) {
                console.warn("⚠️ Không tìm thấy dropdown-menu");
                return;
            }

            // Đóng menu khác
            document.querySelectorAll(".dropdown-menu.active").forEach(m => {
                if (m !== menu) m.classList.remove("active");
            });

            // Toggle menu hiện tại
            menu.classList.toggle("active");
            return;
        }

        // Click ra ngoài thì đóng menu
        if (!dropdownMenu) {
            document.querySelectorAll(".dropdown-menu.active").forEach(m => m.classList.remove("active"));
        }
    });


    // Xử lý Delete Transaction với Confirm
    document.querySelectorAll(".delete-form").forEach(form => {
        form.addEventListener("submit", function(e) {
            e.preventDefault();

            // Lấy tên category để hiển thị trong confirm
            const transactionItem = this.closest('.transaction-item');
            const categoryName = transactionItem ?
                transactionItem.querySelector('.details h3').textContent :
                'this transaction';

            if (confirm(`Are you sure you want to delete "${categoryName}"?`)) {
                // Submit form nếu user confirm
                this.submit();
            }

            // Đóng dropdown sau khi xử lý
            const dropdown = this.closest('.dropdown-menu');
            if (dropdown) {
                dropdown.classList.remove('active');
            }
        });
    });
});