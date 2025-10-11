const addIncomeBtn = document.querySelector('.fab.add-income');
const addExpenseBtn = document.querySelector('.fab.add-expense');
const closeBtn = document.querySelector('.close-btn');
const container = document.getElementById('container-addForm');
const containerExpense = document.getElementById('container-addForm-expense');

// --- Element cho Category Modal ---
const categoryInput = document.getElementById('select_new_category'); // Input hiển thị Category đã chọn
const categoryModal = document.getElementById('categoryModal');       // Modal chính
const closeCategoryModalBtn = document.querySelector('.close-category-modal');
const categoryList = document.getElementById('categoryList');         // Danh sách các nút Category

//
// 1. Logic cho Form chính (giữ nguyên)
//

// Hiển thị form khi nhấn +
addIncomeBtn.addEventListener('click', () => {
    container.classList.add('active');


    flatpickr("#select_new_date", {
        dateFormat: "Y-m-d",
        allowInput: true,
        defaultDate: new Date()
    });

    flatpickr("#select_new_time", {
        enableTime: true,
        noCalendar: true,
        dateFormat: "H:i",
        time_24hr: true,
        defaultDate: new Date()
    });
});

addExpenseBtn.addEventListener('click', () => {
    containerExpense.classList.add('active');

    flatpickr("#select_new_date", {
        dateFormat: "Y-m-d",
        allowInput: true,
        defaultDate: new Date()
    });

    flatpickr("#select_new_time", {
        enableTime: true,
        noCalendar: true,
        dateFormat: "H:i",
        time_24hr: true,
        defaultDate: new Date()
    });
});


// Ẩn khi nhấn × hoặc click ra ngoài
closeBtn.addEventListener('click', () => {
    container.classList.remove('active');
});

container.addEventListener('click', (e) => {
    // Chỉ đóng form chính nếu click chính xác vào backdrop
    if (e.target === container) {
        container.classList.remove('active');
    }
});


// ==========================================================
// 2. Logic cho Category Modal (BỔ SUNG)
// ==========================================================

// Mở Category Modal khi click vào Input Category
categoryInput.addEventListener('click', () => {
    // Đảm bảo Form chính đang hiển thị trước khi mở Modal
    if (container.classList.contains('active')) {
        categoryModal.style.display = 'flex'; // Hiển thị modal
    }
});

// Đóng Category Modal khi nhấn nút ×
closeCategoryModalBtn.addEventListener('click', () => {
    categoryModal.style.display = 'none';
});

// Đóng Category Modal khi click ra ngoài (backdrop)
categoryModal.addEventListener('click', (e) => {
    if (e.target === categoryModal) {
        categoryModal.style.display = 'none';
    }
});


// Xử lý khi chọn một Category trong danh sách
categoryList.addEventListener('click', (e) => {
    const button = e.target.closest('button');

    if (button && categoryList.contains(button)) {
        const selectedCategoryName = button.textContent.trim();
        const selectedCategoryValue = button.getAttribute('data-category'); // 👈 lấy ID

        // Gán giá trị vào 2 input:
        categoryInput.value = selectedCategoryName;              // hiển thị tên
        document.getElementById('hidden_new_category').value = selectedCategoryValue; // 👈 gán ID thật

        // Đóng modal
        categoryModal.style.display = 'none';
    }
});
