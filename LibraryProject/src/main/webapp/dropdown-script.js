function toggleDropdown(event) {
    event.preventDefault();
    const dropdown = document.getElementById('browseDropdown');
    dropdown.classList.toggle('show');
}

window.onclick = function(event) {
    if (!event.target.matches('.nav-box') && !event.target.closest('.browse-dropdown')) {
        const dropdown = document.getElementById('browseDropdown');
        if (dropdown.classList.contains('show')) {
            dropdown.classList.remove('show');
        }
    }

    if (event.target.classList.contains('dropdown-item')) {
        window.location.href = event.target.getAttribute('href');
    }
}