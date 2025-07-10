<div class="top-line1">
  <h2>Library Administration Portal</h2>
</div>

<div class="top-line2">
  <div class="library-title">
    <span class="pale-text">Freeuni</span>
    <span class="highlight-text">Library</span>
  </div>

  <div class="nav-container">
    <a href="${pageContext.request.contextPath}/main-page.jsp" class="nav-box">
      <svg class="nav-icon" viewBox="0 0 24 24">
        <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
      </svg>
      Back to Library
    </a>

    <a href="#" class="nav-box active">
      <svg class="nav-icon" viewBox="0 0 24 24">
        <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
      </svg>
      Admin Panel
    </a>

    <!-- Authentication buttons -->
    <% if (session.getAttribute("username") != null) { %>
        <a href="#" class="nav-box auth-btn" onclick="handleLogout(event)">
            <svg class="nav-icon" viewBox="0 0 24 24">
                <path d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.59L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z"></path>
            </svg>
            Logout
        </a>
    <% } else { %>
        <a href="login.jsp" class="nav-box auth-btn">
            <svg class="nav-icon" viewBox="0 0 24 24">
                <path d="M11 7L9.6 8.4l2.6 2.6H2v2h10.2l-2.6 2.6L11 17l5-5-5-5zm9 12h-8v2h8c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2h-8v2h8v12z"></path>
            </svg>
            Login
        </a>
    <% } %>
  </div>
</div>

<div class="main-content">
  <div class="admin-container">
    <div class="admin-header">
      <h1>BookKeeper Administration</h1>
      <p>Manage your library's book collection and user accounts</p>
    </div>

    <div class="admin-tabs">
      <button class="admin-tab active" onclick="showTab('add-book')">Add Book</button>
      <button class="admin-tab" onclick="showTab('manage-orders')">Manage Orders</button>
      <button class="admin-tab" onclick="showTab('manage-users')">User Management</button>
      <button class="admin-tab" onclick="showTab('book-collection')">Book Collection</button>
    </div>