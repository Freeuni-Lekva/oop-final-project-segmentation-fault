let ordersSearchTimeout;
let currentOrders = [];

function showTab(tabName) {
  const contents = document.querySelectorAll('.tab-content');
  contents.forEach(content => content.classList.remove('active'));

  const tabs = document.querySelectorAll('.admin-tab');
  tabs.forEach(tab => tab.classList.remove('active'));

  document.getElementById(tabName).classList.add('active');
  event.target.classList.add('active');

  if (tabName === 'manage-orders') {
    setTimeout(() => {
      loadOrders();
    }, 100);
  }
}

function handleLogout(event) {
  event.preventDefault();
  
  const contextPath = window.CONTEXT_PATH;
  
  fetch(contextPath + '/api/authorization/logout', {
      method: 'POST',
      headers: {
          'Content-Type': 'application/json',
      }
  })
  .then(response => response.json())
  .then(data => {
      if (data.redirect) {
          window.location.href = data.redirect;
      } else {
          window.location.reload();
      }
  })
  .catch(error => {
      console.error('Logout error:', error);
      window.location.reload();
  });
} 