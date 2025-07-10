<div class="form-container">
  <h2 class="form-section-title">Manage Book Orders</h2>
  <div class="order-management-container">
    <div class="search-filters-row">
      <input type="text" id="orderSearchInput" name="orderSearch" placeholder="Search by username..." class="search-input" autocomplete="off">
      <div class="filter-checkbox">
        <input type="checkbox" id="overdueOnlyCheckbox" name="overdueOnly">
        <label for="overdueOnlyCheckbox">Show only overdue orders</label>
      </div>
    </div>
    <div id="orderManagementMessage" class="message-area" style="display: none; margin-bottom: 10px;"></div>
    <div class="orders-list-section">
      <table class="orders-table">
        <thead>
          <tr>
            <th>User</th>
            <th>Book Name</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody id="ordersTableBody">
        </tbody>
      </table>
    </div>
  </div>
</div> 