<div class="form-container">
  <h2 class="form-section-title">User Management</h2>
  <div class="user-management-container">
    <div class="username-action-row">
      <input type="text" id="usernameInput" name="username" placeholder="Search by username or email to ban/unban users" class="username-search-input" autocomplete="off" required>
      <button type="button" class="btn btn-danger" id="banUserBtn" style="margin-left: 10px;">
        <svg style="width: 16px; height: 16px;" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm5 11H7v-2h10v2z"/>
        </svg>
        Ban User
      </button>
      <button type="button" class="btn btn-success" id="unbanUserBtn" style="margin-left: 8px;">
        <svg style="width: 16px; height: 16px;" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
        </svg>
        Unban User
      </button>
    </div>
    <div id="userManagementMessage" class="message-area" style="display: none; margin-bottom: 10px;"></div>
    <div class="users-list-section">
      <div id="usersList" class="users-list">
      </div>
    </div>
  </div>
</div> 