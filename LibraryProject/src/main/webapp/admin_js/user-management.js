let messageTimeout;

document.getElementById('banUserBtn').addEventListener('click', function () {
  const username = document.getElementById('usernameInput').value.trim();
  const button = this;
  const buttonText = button.textContent;

  button.disabled = true;
  button.textContent = 'Banning...';

  const msgBox = document.getElementById('userManagementMessage');
  if (messageTimeout) {
    clearTimeout(messageTimeout);
  }
  msgBox.style.display = 'none';

  const payload = {
    username: username
  };

  fetch(window.CONTEXT_PATH + '/api/bookkeeper/ban-user', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload),
    credentials: "include"

  })
          .then(function (response) {
            return response.json().then(data => {
              console.log('Ban response:', response.status, data);
              return {
                ok: response.ok,
                data: data
              };
            }).catch(jsonError => {
              console.error('JSON parsing error:', jsonError);
              return {
                ok: response.ok,
                data: { message: response.ok ? 'Operation completed' : 'Server error occurred' }
              };
            });
          })
          .then(function (result) {
            if (result.ok) {
              msgBox.textContent = result.data.message || 'User banned successfully';
              msgBox.className = 'message-area success';
              msgBox.style.display = 'block';
              document.getElementById('usernameInput').value = '';
              refreshUsersList();
              messageTimeout = setTimeout(function () {
                msgBox.style.display = 'none';
              }, 2000);
            } else {
              msgBox.textContent = result.data.message || 'Failed to ban user';
              msgBox.className = 'message-area error';
              msgBox.style.display = 'block';
              messageTimeout = setTimeout(function () {
                msgBox.style.display = 'none';
              }, 2000);
            }
          })
          .catch(function (error) {
            console.error(error);
            msgBox.textContent = 'Error: Check your connection';
            msgBox.className = 'message-area error';
            msgBox.style.display = 'block';
            messageTimeout = setTimeout(function () {
              msgBox.style.display = 'none';
            }, 2000);
          })
          .finally(function () {
            button.textContent = buttonText;
            button.disabled = false;
          });
});

document.getElementById('unbanUserBtn').addEventListener('click', function () {
  const username = document.getElementById('usernameInput').value.trim();
  const button = this;
  const buttonText = button.textContent;

  button.disabled = true;
  button.textContent = 'Unbanning...';

  const msgBox = document.getElementById('userManagementMessage');
  //delete old message when new activity happens
  if (messageTimeout) {
    clearTimeout(messageTimeout);
  }
  msgBox.style.display = 'none';

  const payload = {
    username: username
  };

  fetch(window.CONTEXT_PATH + '/api/bookkeeper/unban-user', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload),
    credentials: "include"
  })
          .then(function (response) {
            return response.json().then(data => {
              console.log('Unban response:', response.status, data);
              return {
                ok: response.ok,
                data: data
              };
            }).catch(jsonError => {
              console.error('JSON parsing error:', jsonError);
              return {
                ok: response.ok,
                data: { message: response.ok ? 'Operation completed' : 'Server error occurred' }
              };
            });
          })
          .then(function (result) {
            if (result.ok) {
              msgBox.textContent = result.data.message || 'User unbanned successfully';
              msgBox.className = 'message-area success';
              msgBox.style.display = 'block';
              document.getElementById('usernameInput').value = '';
              refreshUsersList();
              messageTimeout = setTimeout(function () {
                msgBox.style.display = 'none';
              }, 2000);
            } else {
              msgBox.textContent = result.data.message || 'Failed to unban user';
              msgBox.className = 'message-area error';
              msgBox.style.display = 'block';
              messageTimeout = setTimeout(function () {
                msgBox.style.display = 'none';
              }, 2000);
            }
          })
          .catch(function (error) {
            console.error(error);
            msgBox.textContent = 'Error: Check your connection';
            msgBox.className = 'message-area error';
            msgBox.style.display = 'block';
            messageTimeout = setTimeout(function () {
              msgBox.style.display = 'none';
            }, 2000);
          })
          .finally(function () {
            button.textContent = buttonText;
            button.disabled = false;
          });
});

document.getElementById('usernameInput').addEventListener('input', function(e) {
  const searchText = e.target.value.toLowerCase();
  const allUsers = document.querySelectorAll('.user-item');
  allUsers.forEach(function(item) {
    const username = item.querySelector('.username').textContent.toLowerCase();
    const email = item.querySelector('.email').textContent.toLowerCase();
    if (username.includes(searchText) || email.includes(searchText)) {
      item.style.display = 'block';
    } else {
      item.style.display = 'none';
    }
  });
});

function refreshUsersList() {
  setTimeout(function() {
    loadUsersList();
  }, 800);
}

function loadUsersList() {
  const list = document.getElementById('usersList');
  fetch(window.CONTEXT_PATH + '/api/bookkeeper/users', {
    credentials: "include"
  })
          .then(function(response) {
            if (response.ok) {
              return response.json();
            } else {
              throw new Error('Error loading users');
            }
          })
          .then(function(users) {
            list.textContent = '';
            users.forEach(function(user) {
              const listMember = document.createElement('div');
              listMember.className = 'user-item ' + user.status.toLowerCase();

              const usernameElement = document.createElement('span');
              const emailElement = document.createElement('span');
              const statusElement = document.createElement('span');
              
              usernameElement.className = 'username';
              emailElement.className = 'email';
              statusElement.className = 'status';
              
              usernameElement.textContent = user.username;
              emailElement.textContent = user.mail || 'No email';
              statusElement.textContent = user.status;

              listMember.appendChild(usernameElement);
              listMember.appendChild(emailElement);
              listMember.appendChild(statusElement);
              
              listMember.onclick = function() {
                document.getElementById('usernameInput').value = user.username;
              };
              list.appendChild(listMember);
            });
          })
          .catch(function(error) {
            console.error(error);
            list.textContent = 'Error loading users';
          });
} 