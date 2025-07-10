function loadOrders() {
  const usernameInput = document.getElementById('orderSearchInput');
  const overdueCheckbox = document.getElementById('overdueOnlyCheckbox');
  
  if (!usernameInput || !overdueCheckbox) {
    console.error('Order management elements not found');
    return;
  }
  
  const username = usernameInput.value.trim();
  const overdueOnly = overdueCheckbox.checked;
  
  let url = window.CONTEXT_PATH + '/api/bookkeeper/orders';
  const params = new URLSearchParams();
  
  if (username) {
    params.append('username', username);
  }
  if (overdueOnly) {
    params.append('overdue', 'true');
  }
  
  if (params.toString()) {
    url += '?' + params.toString();
  }

  fetch(url, {
    method: 'GET',
    credentials: 'include'
  })
  .then(response => response.json())
  .then(orders => {
    currentOrders = orders || [];
    renderOrdersTable(currentOrders);
  })
  .catch(error => {
    console.error('Error loading orders:', error);
    showOrderMessage('Error loading orders: ' + error.message, 'error');
  });
}

function renderOrdersTable(orders) {
  const tbody = document.getElementById('ordersTableBody');
  if (!tbody) {
    console.error('Orders table body not found');
    return;
  }
  
  tbody.innerHTML = '';

  if (!orders || orders.length === 0) {
    const row = document.createElement('tr');
    row.innerHTML = '<td colspan="4" style="text-align: center; padding: 20px; color: #666;">No orders found</td>';
    tbody.appendChild(row);
    return;
  }

  orders.forEach(order => {
    const row = document.createElement('tr');
    
    // Apply overdue styling
    if (order.isOverdue) {
      row.className = 'overdue-row';
    }

    const userCell = document.createElement('td');
    userCell.textContent = order.username;

    const bookCell = document.createElement('td');
    bookCell.textContent = order.book ? order.book.name : 'Unknown Book';

    const statusCell = document.createElement('td');
    const statusBadge = document.createElement('span');
    statusBadge.className = 'status-badge status-' + order.status.toLowerCase();
    statusBadge.textContent = order.status;
    statusCell.appendChild(statusBadge);

    const actionsCell = document.createElement('td');
    const actionsContainer = document.createElement('div');
    actionsContainer.className = 'order-actions';

    const pickupBtn = document.createElement('button');
    pickupBtn.className = 'btn btn-success btn-small';
    pickupBtn.innerHTML = '<svg style="width: 14px; height: 14px;" viewBox="0 0 24 24" fill="currentColor"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg> Confirm Pickup';
    pickupBtn.disabled = order.status !== 'RESERVED';
    pickupBtn.onclick = () => handleConfirmPickup(order.orderPublicId);

    const returnBtn = document.createElement('button');
    returnBtn.className = 'btn btn-danger btn-small';
    returnBtn.innerHTML = '<svg style="width: 14px; height: 14px;" viewBox="0 0 24 24" fill="currentColor"><path d="M19 7v4H5.83l3.58-3.59L8 6l-6 6 6 6 1.41-1.41L5.83 13H21V7z"/></svg> Confirm Return';
    returnBtn.disabled = order.status !== 'BORROWED';
    returnBtn.onclick = () => handleConfirmReturn(order.orderPublicId);

    actionsContainer.appendChild(pickupBtn);
    actionsContainer.appendChild(returnBtn);
    actionsCell.appendChild(actionsContainer);

    row.appendChild(userCell);
    row.appendChild(bookCell);
    row.appendChild(statusCell);
    row.appendChild(actionsCell);

    tbody.appendChild(row);
  });
}

function handleConfirmPickup(orderPublicId) {
  if (!confirm('Confirm that the user has picked up this book?')) {
    return;
  }

  const payload = { orderPublicId: orderPublicId };

  fetch(window.CONTEXT_PATH + '/api/bookkeeper/mark-borrowed', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload),
    credentials: 'include'
  })
  .then(response => {
    if (response.ok) {
      showOrderMessage('Order confirmed - book marked as borrowed', 'success');
      loadOrders();
    } else {
      showOrderMessage('Failed to confirm pickup', 'error');
    }
  })
  .catch(error => {
    console.error('Error confirming pickup:', error);
    showOrderMessage('Error: Check your connection', 'error');
  });
}

function handleConfirmReturn(orderPublicId) {
  if (!confirm('Confirm that the user has returned this book?')) {
    return;
  }

  const payload = { orderPublicId: orderPublicId };

  fetch(window.CONTEXT_PATH + '/api/bookkeeper/return-book', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload),
    credentials: 'include'
  })
  .then(response => {
    if (response.ok) {
      showOrderMessage('Book return confirmed - added to user\'s read books', 'success');
      loadOrders();
    } else {
      showOrderMessage('Failed to confirm return', 'error');
    }
  })
  .catch(error => {
    console.error('Error confirming return:', error);
    showOrderMessage('Error: Check your connection', 'error');
  });
}

function showOrderMessage(message, type) {
  const msgBox = document.getElementById('orderManagementMessage');
  if (!msgBox) {
    console.error('Order management message element not found');
    return;
  }
  
  msgBox.textContent = message;
  msgBox.className = 'message-area ' + type;
  msgBox.style.display = 'block';
  
  setTimeout(() => {
    msgBox.style.display = 'none';
  }, 5000);
}

function initializeOrderManagement() {
  const orderSearchInput = document.getElementById('orderSearchInput');
  const overdueCheckbox = document.getElementById('overdueOnlyCheckbox');
  
  if (orderSearchInput) {
    orderSearchInput.addEventListener('input', function() {
      clearTimeout(ordersSearchTimeout);
      ordersSearchTimeout = setTimeout(() => {
        loadOrders();
      }, 300);
    });
  }

  if (overdueCheckbox) {
    overdueCheckbox.addEventListener('change', function() {
      loadOrders();
    });
  }

  setInterval(() => {
    const manageOrdersTab = document.getElementById('manage-orders');
    if (manageOrdersTab && manageOrdersTab.classList.contains('active')) {
      loadOrders();
    }
  }, 30000);
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initializeOrderManagement);
} else {
  initializeOrderManagement();
} 