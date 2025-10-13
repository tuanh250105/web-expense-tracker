const toggleBtn = document.querySelector('.menu-toggle');
const sidebar = document.querySelector('.sidebar');
const darkToggle = document.getElementById('darkModeToggle');

// Toggle sidebar mobile
if (toggleBtn && sidebar) {
    toggleBtn.addEventListener('click', () => {
        sidebar.classList.toggle('active');
    });
}

// Dark Mode switch
if (darkToggle) {
    darkToggle.addEventListener('change', () => {
        document.body.classList.toggle('dark', darkToggle.checked);
    });
}

// Sidebar active state handling
function setActiveItem(targetLi) {
    const prev = document.querySelector('.sidebar ul li.active');
    if (prev && prev !== targetLi) prev.classList.remove('active');
    if (targetLi) targetLi.classList.add('active');
}

// When clicking a sidebar link, mark its parent <li> as active immediately
document.addEventListener('click', (e) => {
    const a = e.target.closest('.sidebar ul li a');
    if (a) {
        const li = a.closest('li');
        setActiveItem(li);
    }
});

// On load, try to set active based on data-page attribute or current pathname
window.addEventListener('DOMContentLoaded', () => {
    // Priority: elements with data-page matching body/content or link href
    const pageAttr = document.querySelector('[data-page]');
    if (pageAttr) {
        const page = pageAttr.getAttribute('data-page');
        const match = document.querySelector(`.sidebar ul li[a][data-page="${page}"]`);
        // Fallback: find a link whose href contains the page name
    }

    // Fallback: mark link matching current path
    const path = window.location.pathname.replace(/\/+$/, '');
    const links = document.querySelectorAll('.sidebar ul li a');
    for (const link of links) {
        try {
            const href = link.getAttribute('href') || '';
            if (href && path.endsWith(href.replace(/\/+$/, '')) || (href !== '/' && path === href)) {
                setActiveItem(link.closest('li'));
                return;
            }
        } catch (err) {
            // ignore malformed hrefs
        }
    }
});
