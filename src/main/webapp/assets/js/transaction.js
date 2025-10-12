document.addEventListener("DOMContentLoaded", () => {
    const addIncomeBtn = document.querySelector('.fab.add-income');
    const addExpenseBtn = document.querySelector('.fab.add-expense');
    const formIncomeContainer = document.querySelector('#incomeForm');
    const formExpenseContainer = document.querySelector('#expenseForm');
    const categoryModal = document.getElementById('categoryModal');
    const closeCategoryModalBtn = document.querySelector('.close-category-modal');
    const categoryList = document.getElementById('categoryList');

    // ===============================
    // Khởi tạo Flatpickr cho Add/Edit Form
    // ===============================
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

    // ===============================
    // Khởi tạo Flatpickr cho Filter Panel
    // ===============================
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

    // ===============================
    // Hiển thị form khi nhấn FAB buttons
    // ===============================
    addIncomeBtn.addEventListener('click', () => {
        formIncomeContainer.classList.add('active');
        initFlatpickr(formIncomeContainer);
    });

    addExpenseBtn.addEventListener('click', () => {
        formExpenseContainer.classList.add('active');
        initFlatpickr(formExpenseContainer);
    });

    // ===============================
    // Logic cho Category Modal (bao gồm filter)
    // ===============================
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

    // Xử lý chọn Category (cho cả Add/Edit Form và Filter)
    categoryList.addEventListener('click', (e) => {
        const button = e.target.closest('button');
        if (!button) return;

        const selectedCategoryName = button.textContent.trim();
        const selectedCategoryValue = button.getAttribute('data-category');

        if (categoryModal.dataset.isFilter === 'true') {
            // Nếu filter panel
            filterPanel.querySelector('#select_new_category').value = selectedCategoryName;
            filterPanel.querySelector('#hidden_new_category').value = selectedCategoryValue;
        } else {
            // Nếu add/edit form
            const activeFormContainer = document.querySelector(`#${categoryModal.dataset.activeForm}`);
            if (activeFormContainer) {
                activeFormContainer.querySelector('.select_new_category').value = selectedCategoryName;
                activeFormContainer.querySelector('.hidden_new_category').value = selectedCategoryValue;
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

    // ===============================
    // Logic Edit Transaction
    // ===============================
    if (window.editTransactionData) {
        const t = window.editTransactionData;
        const formContainer = t.type === "Income" ? formIncomeContainer : formExpenseContainer;
        formContainer.classList.add("active");
        initFlatpickr(formContainer);

        formContainer.querySelector('.hidden_new_category').value = t.categoryId;
        formContainer.querySelector('.select_new_category').value = t.categoryName;
        formContainer.querySelector('.type_value').value = t.value;
        formContainer.querySelector('.select_new_account').value = t.accountId;
        formContainer.querySelector('.select_new_date').value = t.date;
        formContainer.querySelector('.select_new_time').value = t.time;
        formContainer.querySelector('.type_new_notes').value = t.notes;

        const header = formContainer.querySelector('.addForm-header h2');
        if (header) header.textContent = "Edit " + t.type;

        const actionInput = formContainer.querySelector('input[name="action"]');
        actionInput.value = t.type.toLowerCase() === "income" ? "update_income" : "update_expense";

        let idInput = formContainer.querySelector('input[name="id"]');
        if (!idInput) {
            idInput = document.createElement("input");
            idInput.type = "hidden";
            idInput.name = "id";
            formContainer.querySelector("form").appendChild(idInput);
        }
        idInput.value = t.id;
    }

    // ===============================
    // Logic expand notes khi click vào transaction-item
    // ===============================
    const items = document.querySelectorAll(".transaction-item");
    items.forEach(item => {
        item.addEventListener("click", function(e) {
            if (e.target.closest(".more-btn") || e.target.closest(".dropdown-menu")) {
                return;
            }
            item.classList.toggle("expanded");
        });
    });

    // ===============================
    // Logic dropdown menu (More button)
    // ===============================
    document.addEventListener("click", (e) => {
        const moreBtn = e.target.closest(".more-btn");
        const dropdownMenu = e.target.closest(".dropdown-menu");

        if (moreBtn) {
            e.stopPropagation();
            e.preventDefault();

            const menu = moreBtn.parentElement.querySelector(".dropdown-menu");

            if (!menu) {
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

    // ===============================
    // Xử lý Delete Transaction
    // ===============================
    function confirmDelete(id) {
        if (confirm("Are you sure you want to delete this transaction?")) {
            fetch(`${window.location.origin}/transaction?action=delete&id=${id}`, { method: 'POST' })
                .then(() => window.location.reload());
        }
    }

    document.querySelectorAll(".dropdown-item.delete").forEach(btn => {
        btn.addEventListener("click", e => {
            e.stopPropagation();
            const item = btn.closest(".transaction-item");

            // Tìm ID từ form Edit bên trong dropdown
            const editForm = item.querySelector('form[action*="transaction"]');
            const idInput = editForm ? editForm.querySelector('input[name="id"]') : null;
            const id = idInput ? idInput.value : null;

            if (id) {
                confirmDelete(id);
            } else {
                console.error("Không tìm thấy transaction ID");
            }
        });
    });
});