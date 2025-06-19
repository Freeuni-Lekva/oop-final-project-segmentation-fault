<%--
  Created by IntelliJ IDEA.
  User: elenejobava
  Date: 6/17/25
  Time: 22:49
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
  <%@ include file="main-page-styles.jsp" %>
</head>
<body>
<div class="top-line1">
  <h2>Explore the world's knowledge, cultures and ideas</h2>
</div>

<div class="top-line2">
  <div class="library-title">
    <span class="pale-text">Freeuni</span>
    <span class="highlight-text">Library</span>
    <img src="images/cropped_circle_image.png" alt="owl" class="owl-icon">
  </div>

  <div class="nav-container">
    <a href="#home" class="nav-box active">
      <svg class="nav-icon" viewBox="0 0 24 24">
        <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
      </svg>
      Home
    </a>

    <a href="#my-books" class="nav-box">
      <svg class="nav-icon" viewBox="0 0 24 24">
        <path d="M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 4h5v8l-2.5-1.5L6 12V4z"/>
      </svg>
      My Books
    </a>

    <div class="nav-box browse-dropdown" id="browseDropdown">
      <svg class="nav-icon" viewBox="0 0 24 24">
        <path d="M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z"/>
      </svg>
      Browse
      <svg class="dropdown-arrow" viewBox="0 0 24 24">
        <path d="M7 10l5 5 5-5z"/>
      </svg>

      <!-- Browse Dropdown Menu -->
      <div class="browse-menu" id="browseMenu">
        <div class="browse-section">
          <h4>Recommendations</h4>
          <a href="#recommendations" class="browse-item featured">Recommendations</a>
        </div>

        <div class="browse-section">
          <h4>Administration</h4>
          <a href="#banned-users" class="browse-item featured">Banned Users</a>
        </div>

        <div class="browse-section">
          <h4>Favorite Genres</h4>
          <a href="#fiction" class="browse-item">Fiction</a>
          <a href="#classics" class="browse-item">Classics</a>
          <a href="#mystery" class="browse-item">Mystery & Thriller</a>
          <a href="#romance" class="browse-item">Romance</a>
          <a href="#fantasy" class="browse-item">Fantasy</a>
          <a href="#science-fiction" class="browse-item">Science Fiction</a>
          <a href="#non-fiction" class="browse-item">Non-Fiction</a>
          <a href="#biography" class="browse-item">Biography</a>
          <a href="#history" class="browse-item">History</a>
          <a href="#poetry" class="browse-item">Poetry</a>
          <a href="#psychology" class="browse-item">Psychology</a>
          <a href="#all-genres" class="browse-item featured">All Genres</a>
        </div>
      </div>
    </div>

    <a href="#search" class="nav-box">
      <svg class="nav-icon" viewBox="0 0 24 24">
        <path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
      </svg>
      Search
    </a>

    <a href="#profile" class="nav-box">
      <svg class="nav-icon" viewBox="0 0 24 24">
        <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
      </svg>
      Profile
    </a>
  </div>
</div>

<div class="main-content">
  <h3>Welcome to Freeuni Library</h3>
</div>

<script>
  const browseDropdown = document.getElementById('browseDropdown');
  const browseMenu = document.getElementById('browseMenu');

  browseDropdown.addEventListener('click', function(e) {
    e.preventDefault();
    const isOpen = browseMenu.classList.contains('show');

    if (isOpen) {
      browseMenu.classList.remove('show');
      browseDropdown.classList.remove('open');
    } else {
      browseMenu.classList.add('show');
      browseDropdown.classList.add('open');
    }
  });

  document.addEventListener('click', function(e) {
    if (!browseDropdown.contains(e.target) && !browseMenu.contains(e.target)) {
      browseMenu.classList.remove('show');
      browseDropdown.classList.remove('open');
    }
  });
</script>

</body>
</html>