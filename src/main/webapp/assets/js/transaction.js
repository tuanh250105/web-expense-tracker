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

    // Hiển thị form khi nhấn 2 nút FAB income vs expense buttons
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
        const categoryIcon = formContainer.querySelector('.category-icon');
        if (categoryIcon) {
            categoryIcon.src = `${window.location.origin}/assets/images/categories/salary.png`;
            categoryIcon.alt = 'Icon';
        }
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

        // Remove id input
        const idInput = formContainer.querySelector('input[name="id"]');
        if (idInput) {
            idInput.remove();
        }
    }


    //Chọn category con
    let currentParentCategory = null; // Lưu category cha đang được chọn

    // Mở Category Modal
    document.querySelectorAll('.open-category-modal').forEach(input => {
        input.addEventListener('click', (e) => {
            console.log('=== Opening Category Modal ===');
            const currentForm = e.target.closest('.container-addForm');
            const isFilterPanel = e.target.closest('.filter-panel');

            // Reset về cấp 1 mỗi khi mở modal
            currentParentCategory = null;
            updateModalHeader();

            if (currentForm) {
                categoryModal.dataset.activeForm = currentForm.id;
                categoryModal.dataset.isFilter = 'false';
                const type = currentForm.id.includes('income') ? 'income' : 'expense';
                console.log('Form type:', type);
                showParentCategories(type);
            } else if (isFilterPanel) {
                categoryModal.dataset.activeForm = 'filter';
                categoryModal.dataset.isFilter = 'true';
                console.log('Filter panel - showing all types');
                showParentCategories('all');
            }

            categoryModal.classList.add('active');
        });
    });

    // Hiển thị category cha
    function showParentCategories(type) {
        const buttons = document.querySelectorAll('#categoryList button');

        buttons.forEach(btn => {
            const btnType = btn.getAttribute('data-type');
            const parentId = btn.getAttribute('data-parent');

            // Kiểm tra parent = null (category cha)
            const isParentCategory = !parentId || parentId === '';

            // Chỉ hiển thị category cha
            if (isParentCategory && (type === 'all' || btnType === type)) {
                btn.parentElement.style.display = '';
            } else {
                btn.parentElement.style.display = 'none';
            }
        });
    }

    // Hiển thị category con của một parent
    function showChildCategories(parentId, type) {
        const buttons = document.querySelectorAll('#categoryList button');
        buttons.forEach(btn => {
            const btnParent = btn.getAttribute('data-parent');
            const btnType = btn.getAttribute('data-type');

            if (btnParent === parentId && (type === 'all' || btnType === type)) {
                btn.parentElement.style.display = '';
            } else {
                btn.parentElement.style.display = 'none';
            }
        });
    }

    // Cập nhật header của modal
    function updateModalHeader() {
        const modalTitle = categoryModal.querySelector('h2');
        if (currentParentCategory) {
            const backBtn = '<span class="back-to-parent" style="cursor: pointer; color: #1976d2; margin-right: 10px;">← Back</span>';
            modalTitle.innerHTML = backBtn + currentParentCategory.name;

            // Thêm event listener cho nút Back
            const backButton = modalTitle.querySelector('.back-to-parent');
            if (backButton) {
                backButton.addEventListener('click', () => {
                    currentParentCategory = null;
                    updateModalHeader();

                    const type = getActiveFormType();
                    showParentCategories(type);
                });
            }
        } else {
            modalTitle.textContent = 'Select Category';
        }
    }

    // Lấy type của form đang active
    function getActiveFormType() {
        if (categoryModal.dataset.isFilter === 'true') {
            return 'all';
        }
        const activeFormId = categoryModal.dataset.activeForm;
        return activeFormId && activeFormId.includes('income') ? 'income' : 'expense';
    }

    // Đóng modal
    closeCategoryModalBtn.addEventListener('click', () => {
        categoryModal.classList.remove('active');
        currentParentCategory = null;
    });

    categoryModal.addEventListener('click', (e) => {
        if (e.target === categoryModal) {
            categoryModal.classList.remove('active');
            currentParentCategory = null;
        }
    });

    // Xử lý chọn Category
    categoryList.addEventListener('click', (e) => {
        const button = e.target.closest('button');
        if (!button) return;

        const categoryId = button.getAttribute('data-category');
        const categoryName = button.textContent.trim();
        const categoryIconImg = button.querySelector('img');
        const categoryIcon = categoryIconImg ? categoryIconImg.src : button.getAttribute('data-icon');
        const parentId = button.getAttribute('data-parent');
        if (parentId === 'null' || parentId === '' || !parentId) {
            // Lưu category cha và hiển thị các category con
            currentParentCategory = {
                id: categoryId,
                name: categoryName
            };
            updateModalHeader();

            const type = getActiveFormType();
            showChildCategories(categoryId, type);
        }
        else {
            // Gán category vào form
            if (categoryModal.dataset.isFilter === 'true') {
                // Filter panel
                filterPanel.querySelector('#select_new_category').value = categoryName;
                filterPanel.querySelector('#hidden_new_category').value = categoryId;
            } else {
                // Add/Edit form
                const activeFormContainer = document.querySelector(`#${categoryModal.dataset.activeForm}`);
                if (activeFormContainer) {
                    activeFormContainer.querySelector('.select_new_category').value = categoryName;
                    activeFormContainer.querySelector('.hidden_new_category').value = categoryId;

                    const categoryIconImg = activeFormContainer.querySelector('.category-icon');
                    if (categoryIconImg && categoryIcon) {
                        categoryIconImg.src = categoryIcon;
                        categoryIconImg.alt = categoryName;
                    }
                }
            }
            categoryModal.classList.remove('active');
            currentParentCategory = null;
        }
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

        const formContainer = t.type.toLowerCase() === "income" ? formIncomeContainer : formExpenseContainer;

        formContainer.classList.add("active");
        initFlatpickr(formContainer);

        // Set category
        formContainer.querySelector('.hidden_new_category').value = t.categoryId;
        formContainer.querySelector('.select_new_category').value = t.categoryName;
        // Tìm icon
        const categoryButton = document.querySelector(`#categoryList button[data-category="${t.categoryId}"]`);
        if (categoryButton) {
            const iconImg = categoryButton.querySelector('img');
            const iconPath = iconImg ? iconImg.src : null;
            const categoryIconImg = formContainer.querySelector('.category-icon');
            if (categoryIconImg && iconPath) {
                categoryIconImg.src = iconPath;
                categoryIconImg.alt = t.categoryName;
            }
        }
        // Set other fields
        formContainer.querySelector('.type_value').value = t.value;
        formContainer.querySelector('.select_new_account').value = t.accountId;
        formContainer.querySelector('.select_new_date').value = t.date;
        formContainer.querySelector('.select_new_time').value = t.time;
        formContainer.querySelector('.type_new_notes').value = t.notes;
        // Set header
        const header = formContainer.querySelector('.addForm-header h2');
        if (header) header.textContent = "Edit " + t.type;
        //Set action
        const actionInput = formContainer.querySelector('input[name="action"]');
        actionInput.value = t.type.toLowerCase() === "income" ? "update_income" : "update_expense";
        const typeInput = formContainer.querySelector('input[name="type"]');
        if (typeInput) {
            typeInput.value = t.type.toLowerCase();
        }

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
                console.warn("Không tìm thấy dropdown-menu");
                return;
            }
            document.querySelectorAll(".dropdown-menu.active").forEach(m => {
                if (m !== menu) m.classList.remove("active");
            });
            menu.classList.toggle("active");
            return;
        }
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
                this.submit();
            }
            const dropdown = this.closest('.dropdown-menu');
            if (dropdown) {
                dropdown.classList.remove('active');
            }
        });
    });
});