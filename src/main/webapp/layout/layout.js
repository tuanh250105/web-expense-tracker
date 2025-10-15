document.addEventListener('DOMContentLoaded', () => {
    const toggleBtn = document.querySelector('.menu-toggle');
    const sidebar = document.querySelector('.sidebar');
    const darkToggle = document.getElementById('darkModeToggle');

    // Toggle sidebar mobile
    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            sidebar.classList.toggle('active');
        });
    }

    // Dark Mode switch
    if (darkToggle) {
        // Kiểm tra dark mode đã lưu từ trước
        const savedDarkMode = localStorage.getItem('darkMode');
        if (savedDarkMode === 'true') {
            document.body.classList.add('dark');
            darkToggle.checked = true;
        }

        darkToggle.addEventListener('change', () => {
            document.body.classList.toggle('dark', darkToggle.checked);
            // Lưu trạng thái dark mode
            localStorage.setItem('darkMode', darkToggle.checked);
        });
    }

    // ========================================================================
    // AUTO ACTIVE MENU ITEM - Đánh dấu menu đang active dựa trên URL
    // ========================================================================

    function setActiveMenuItem() {
        // Lấy path hiện tại
        const currentPath = window.location.pathname;

        console.log('=== Active Menu Detection ===');
        console.log('Current path:', currentPath);

        // Lấy tất cả menu items
        const menuItems = document.querySelectorAll('.sidebar ul li');

        // Xóa active từ tất cả items trước
        menuItems.forEach(item => item.classList.remove('active'));

        let bestMatch = null;
        let bestMatchLength = 0;

        menuItems.forEach(item => {
            const link = item.querySelector('a');
            if (!link) return;

            const linkHref = link.getAttribute('href');
            if (!linkHref) return;

            // Lấy pathname từ href
            let linkPath;
            try {
                if (linkHref.startsWith('http')) {
                    linkPath = new URL(linkHref).pathname;
                } else {
                    linkPath = linkHref;
                }
            } catch (e) {
                linkPath = linkHref;
            }

            // Tìm match tốt nhất (path dài nhất)
            if (currentPath.startsWith(linkPath) && linkPath.length > bestMatchLength) {
                bestMatch = item;
                bestMatchLength = linkPath.length;
            }
        });

        if (bestMatch) {
            bestMatch.classList.add('active');
            const linkText = bestMatch.querySelector('a').textContent.trim();
            console.log('✓ Active set for:', linkText);

            // Scroll item vào view nếu cần
            bestMatch.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
        } else {
            console.log('✗ No matching menu item found');
        }
    }

    // Gọi function khi trang load
    setActiveMenuItem();

    // Thêm event listener cho click để cập nhật active ngay lập tức
    const menuLinks = document.querySelectorAll('.sidebar ul li a');
    menuLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            // Xóa active từ tất cả
            document.querySelectorAll('.sidebar ul li').forEach(item => {
                item.classList.remove('active');
            });

            // Thêm active vào item được click
            this.parentElement.classList.add('active');

            console.log('Menu clicked:', this.textContent.trim());
        });
    });
});