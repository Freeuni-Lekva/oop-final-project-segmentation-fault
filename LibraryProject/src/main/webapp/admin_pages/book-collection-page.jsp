<div class="form-container">
  <h2 class="form-section-title">Book Collection Management</h2>
  <div class="book-management-container">
    <div class="search-filters-row">
      <input type="text" id="bookSearchInput" name="bookSearch" placeholder="Search by name..." class="book-search-input" autocomplete="off">
      <div class="filter-buttons">
        <select id="genreFilter" class="filter-select">
          <option value="">All Genres</option>
          <option value="Fiction">Fiction</option>
          <option value="Non-Fiction">Non-Fiction</option>
          <option value="Mystery">Mystery</option>
          <option value="Romance">Romance</option>
          <option value="Sci-Fi">Science Fiction</option>
          <option value="Fantasy">Fantasy</option>
          <option value="Biography">Biography</option>
          <option value="History">History</option>
        </select>
      </div>
    </div>
    <div id="bookManagementMessage" class="message-area" style="display: none; margin-bottom: 10px;"></div>
    <div class="books-list-section">
      <div id="booksList" class="books-list">
      </div>
    </div>
  </div>
</div> 