const toggleBtn = document.querySelector('.menu-toggle');
const sidebar = document.querySelector('.sidebar');
const darkToggle = document.getElementById('darkModeToggle');

// Toggle sidebar mobile
toggleBtn.addEventListener('click', () => {
    sidebar.classList.toggle('active');
});

// Dark Mode switch
darkToggle.addEventListener('change', () => {
    document.body.classList.toggle('dark', darkToggle.checked);
});