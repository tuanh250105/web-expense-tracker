const toggleBtn = document.querySelector('.menu-toggle');
const sidebar = document.querySelector('.sidebar');
const darkToggle = document.getElementById('darkModeToggle');

toggleBtn.addEventListener('click', () => {
    sidebar.classList.toggle('active');
});

function loadPage(page) {
    console.log("Loading page:", page);
    fetch(`${window.contextPath}/views/${page}.jsp`)
        .then(res => {
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.text();
        })
        .then(html => {
            document.getElementById("content-area").innerHTML = html;
            // Tải script tương ứng với trang
            if (page === 'overview') {
                const script = document.createElement('script');
                script.src = `${window.contextPath}/assets/js/overview.js?v=${Date.now()}`;
                script.onload = () => {
                    console.log("✅ overview.js đã được tải");
                    if (typeof initOverview === 'function') {
                        initOverview();
                    } else {
                        console.error("❌ Hàm initOverview không tồn tại");
                    }
                };
                script.onerror = () => console.error("❌ Lỗi khi tải overview.js");
                document.body.appendChild(script);
            } else if (page === 'group_expense') {
                const script = document.createElement('script');
                script.src = `${window.contextPath}/assets/js/group_expense.js?v=${Date.now()}`;
                script.onload = () => {
                    console.log("✅ group_expense.js đã được tải");
                    if (typeof initGroupPage === 'function') {
                        initGroupPage();
                    } else {
                        console.error("❌ Hàm initGroupPage không tồn tại");
                    }
                };
                script.onerror = () => console.error("❌ Lỗi khi tải group_expense.js");
                document.body.appendChild(script);
            }
        })
        .catch(err => console.error("❌ Lỗi khi tải trang:", err));
}

// Gắn sự kiện sidebar
document.querySelectorAll('.sidebar a[data-page]').forEach(link => {
    link.addEventListener('click', function(e) {
        e.preventDefault();
        loadPage(this.getAttribute('data-page'));
    });
});

// Mặc định load trang Overview khi vào
window.addEventListener("DOMContentLoaded", () => {
    loadPage("overview");
});