<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Library BookKeeper - Administration</title>
  <%@ include file="bookkeeper-styles.jsp" %>
</head>
<body>
<div class="top-line1">
  <h2>Library Administration Portal</h2>
</div>

<div class="top-line2">
  <div class="library-title">
    <span class="pale-text">Freeuni</span>
    <span class="highlight-text">Library</span>
  </div>

  <div class="nav-container">
    <a href="#" class="nav-box">
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

    <a href="#" class="nav-box">
      <svg class="nav-icon" viewBox="0 0 24 24">
        <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
      </svg>
      Profile
    </a>
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

    <div id="add-book" class="tab-content active">
      <div class="form-container">
        <h2 class="form-section-title">Add New Book to Collection</h2>
        <form id="addBookForm" enctype="multipart/form-data">
          <div class="form-grid">
            <div class="form-group">
              <label for="title">Book Title *</label>
              <input type="text" id="title" name="title" placeholder="Enter book title" required>
            </div>
            <div class="form-group">
              <label for="author">Author *</label>
              <input type="text" id="author" name="author" placeholder="Enter author name" required>
            </div>
            <div class="form-group">
              <label for="genre">Genre</label>
              <select id="genre" name="genre">
                <option value="">Select Genre</option>
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
            <div class="form-group">
              <label for="volume">Volume</label>
              <input type="text" id="volume" name="volume" placeholder="e.g., 1st Edition, Volume 2">
            </div>
          </div>
          <div class="form-group">
            <label for="description">Description</label>
            <textarea id="description" name="description" placeholder="Brief description of the book..."></textarea>
          </div>
          <div class="form-group">
            <label for="bookImage">Book Cover Image</label>
            <div class="file-upload-area" onclick="document.getElementById('bookImage').click()">
              <input type="file" id="bookImage" name="bookImage" accept="image/*" class="file-input">
              <svg class="file-upload-icon" viewBox="0 0 24 24">
                <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z"/>
              </svg>
              <div class="file-upload-text">Click to upload book cover</div>
              <div class="file-upload-subtext">PNG, JPG, JPEG up to 5MB</div>
            </div>
            <div class="file-preview" id="imagePreview">
              <div class="file-preview-name" id="imagePreviewName"></div>
            </div>
          </div>
          <div class="action-buttons">
            <button type="submit" class="btn" id="addBookBtn">
              <svg style="width: 16px; height: 16px;" viewBox="0 0 24 24" fill="currentColor">
                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
              </svg>
              Add Book
            </button>
          </div>
          <div id="addBookMessage" class="message-area" style="display: none;"></div>
        </form>
      </div>
    </div>

    <div id="manage-orders" class="tab-content">
      <div class="form-container">
        <h2 class="form-section-title">Manage Book Orders</h2>
        <form id="markBorrowedForm">
          <div class="form-group">
            <label for="orderPublicId">Order Public ID</label>
            <input type="text" id="orderPublicId" name="orderPublicId" placeholder="Enter order public ID" required>
          </div>
          <div class="action-buttons">
            <button type="submit" class="btn btn-success" id="markBorrowedBtn">
              <svg style="width: 16px; height: 16px;" viewBox="0 0 24 24" fill="currentColor">
                <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
              </svg>
              Mark as Borrowed
            </button>
          </div>
          <div id="markBorrowedMessage" class="message-area" style="display: none;"></div>
        </form>
      </div>
    </div>

    <div id="manage-users" class="tab-content">
      <div class="form-container">
        <h2 class="form-section-title">User Management</h2>
        <div class="user-management-container">
          <div class="username-action-row">
            <input type="text" id="usernameInput" name="username" placeholder="Type username to search, ban, or unban" class="username-search-input" autocomplete="off" required>
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
    </div>

    <div id="book-collection" class="tab-content">
      <div class="form-container">
        <h2 class="form-section-title">Book Collection Management</h2>
        <form id="deleteBookForm">
          <div class="form-group">
            <label for="bookPublicId">Delete Book by Public ID</label>
            <input type="text" id="bookPublicId" name="bookPublicId" placeholder="Enter book public ID" required>
          </div>
          <div class="action-buttons">
            <button type="submit" class="btn btn-danger" id="deleteBookBtn">
              <svg style="width: 16px; height: 16px;" viewBox="0 0 24 24" fill="currentColor">
                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
              </svg>
              Delete Book
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</div>

<script>
  function showTab(tabName) {
    const contents = document.querySelectorAll('.tab-content');
    contents.forEach(content => content.classList.remove('active'));

    const tabs = document.querySelectorAll('.admin-tab');
    tabs.forEach(tab => tab.classList.remove('active'));

    document.getElementById(tabName).classList.add('active');
    event.target.classList.add('active');
  }


  document.getElementById('bookImage').addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (file) {
      const preview = document.getElementById('imagePreview');
      const previewName = document.getElementById('imagePreviewName');

      previewName.textContent = file.name;
      preview.classList.add('show');
    }
  });


  document.getElementById('addBookForm').addEventListener('submit', async function (e) {
    e.preventDefault();

    const form = this;
    const button = document.getElementById('addBookBtn');
    const buttonText = button.textContent;
    const msgBox = document.getElementById('addBookMessage');
    const preview = document.getElementById('imagePreview');

    button.disabled = true;
    button.textContent = 'Adding...';
    msgBox.style.display = 'none';

    try {
      const title = document.getElementById('title').value.trim();
      const author = document.getElementById('author').value.trim();
      const genre = document.getElementById('genre').value.trim();
      const volume = document.getElementById('volume').value.trim();
      const description = document.getElementById('description').value.trim();
      const fileInput = document.getElementById('bookImage');
      const imageFile = fileInput.files[0];

      let imageUrl = null;
      if (imageFile) {
        const imageFormData = new FormData();
        imageFormData.append('image', imageFile);

        const imageUploadResponse = await fetch('${pageContext.request.contextPath}/api/bookkeeper/upload-image', {
          method: 'POST',
          body: imageFormData
        });

        if (!imageUploadResponse.ok) throw new Error('Image upload failed');
        const imageData = await imageUploadResponse.json();
        imageUrl = imageData.url || imageData.imageUrl || imageData.path;
      }

      const bookData = {
        title,
        author,
        genre,
        volume,
        description,
      };

      const bookCreateResponse = await fetch('${pageContext.request.contextPath}/api/bookkeeper/add-book', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(bookData)
      });

      if (bookCreateResponse.ok) {
        msgBox.textContent = 'Book added successfully';
        msgBox.className = 'message-area success';
        msgBox.style.display = 'block';
        form.reset();
        if (preview && preview.classList.contains('show')) {
          preview.classList.remove('show');
        }
        setTimeout(() => (msgBox.style.display = 'none'), 5000);
      } else {
        msgBox.textContent = 'Failed to add book';
        msgBox.className = 'message-area error';
        msgBox.style.display = 'block';
      }
    } catch (error) {
      console.error(error);
      msgBox.textContent = 'Error: Check your connection or input';
      msgBox.className = 'message-area error';
      msgBox.style.display = 'block';
    } finally {
      button.textContent = buttonText;
      button.disabled = false;
    }
  });


  document.getElementById('markBorrowedForm').addEventListener('submit', function (e) {
    e.preventDefault();

    const form = this;
    const orderPublicId = document.getElementById('orderPublicId').value.trim();
    const button = document.getElementById('markBorrowedBtn');
    const buttonText = button.textContent;

    button.disabled = true;
    button.textContent = 'Processing...';

    const msgBox = document.getElementById('markBorrowedMessage');
    msgBox.style.display = 'none';

    const payload = {
      orderPublicId: orderPublicId
    };

    fetch('${pageContext.request.contextPath}/api/bookkeeper/mark-borrowed', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    })
            .then(function (response) {
              if (response.ok) {
                msgBox.textContent = 'Order marked as borrowed';
                msgBox.className = 'message-area success';
                msgBox.style.display = 'block';
                form.reset();

                setTimeout(function () {
                  msgBox.style.display = 'none';
                }, 5000);
              } else {
                msgBox.textContent = 'Order not found';
                msgBox.className = 'message-area error';
                msgBox.style.display = 'block';
              }
            })
            .catch(function (error) {
              console.error(error);
              msgBox.textContent = 'Error: Check your connection';
              msgBox.className = 'message-area error';
              msgBox.style.display = 'block';
            })
            .finally(function () {
              button.textContent = buttonText;
              button.disabled = false;
            });
  });
  ;

  document.getElementById('banUserBtn').addEventListener('click', function () {
    const username = document.getElementById('usernameInput').value.trim();
    const button = this;
    const buttonText = button.textContent;

    button.disabled = true;
    button.textContent = 'Banning...';

    const msgBox = document.getElementById('userManagementMessage');
    msgBox.style.display = 'none';

    const payload = {
      username: username
    };

    fetch('${pageContext.request.contextPath}/api/bookkeeper/ban-user', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    })
            .then(function (response) {
              if (response.ok) {
                msgBox.textContent = 'User banned';
                msgBox.className = 'message-area success';
                msgBox.style.display = 'block';
                document.getElementById('usernameInput').value = '';
                refreshUsersList();
                setTimeout(function () {
                  msgBox.style.display = 'none';
                }, 5000);
              } else {
                msgBox.textContent = 'Failed to ban user';
                msgBox.className = 'message-area error';
                msgBox.style.display = 'block';
              }
            })
            .catch(function (error) {
              console.error(error);
              msgBox.textContent = 'Error: Check your connection';
              msgBox.className = 'message-area error';
              msgBox.style.display = 'block';
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
    msgBox.style.display = 'none';

    const payload = {
      username: username
    };

    fetch('${pageContext.request.contextPath}/api/bookkeeper/unban-user', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    })
            .then(function (response) {
              if (response.ok) {
                msgBox.textContent = 'User unbanned';
                msgBox.className = 'message-area success';
                msgBox.style.display = 'block';
                document.getElementById('usernameInput').value = '';
                refreshUsersList();
                setTimeout(function () {
                  msgBox.style.display = 'none';
                }, 5000);
              } else {
                msgBox.textContent = 'Failed to unban user';
                msgBox.className = 'message-area error';
                msgBox.style.display = 'block';
              }
            })
            .catch(function (error) {
              console.error(error);
              msgBox.textContent = 'Error: Check your connection';
              msgBox.className = 'message-area error';
              msgBox.style.display = 'block';
            })
            .finally(function () {
              button.textContent = buttonText;
              button.disabled = false;
            });
  });


  document.getElementById('deleteBookForm').addEventListener('submit', function(e) {
    e.preventDefault();
  });


  const uploadArea = document.querySelector('.file-upload-area');

  uploadArea.addEventListener('dragover', (e) => {
    e.preventDefault();
    uploadArea.classList.add('drag-over');
  });

  uploadArea.addEventListener('dragleave', () => {
    uploadArea.classList.remove('drag-over');
  });

  uploadArea.addEventListener('drop', (e) => {
    e.preventDefault();
    uploadArea.classList.remove('drag-over');

    const file = e.dataTransfer.files[0];
    if (file) {
      document.getElementById('bookImage').files = e.dataTransfer.files;
      const event = new Event('change');
      document.getElementById('bookImage').dispatchEvent(event);
    }
  });

  document.getElementById('usernameInput').addEventListener('input', function(e) {
    const nameInSearch = e.target.value.toLowerCase();
    const allUsers = document.querySelectorAll('.user-item');
    allUsers.forEach(function(item) {
      const username = item.querySelector('.username').textContent.toLowerCase();
      if (username.includes(nameInSearch)) {
        item.style.display = 'block';
      } else {
        item.style.display = 'none';
      }
    });
  });

  document.addEventListener('DOMContentLoaded', function() {
    loadUsersList();
  });

  function refreshUsersList() {
    setTimeout(function() {
      loadUsersList();
    }, 800);
  }

  function loadUsersList() {
    const list = document.getElementById('usersList');
    fetch('${pageContext.request.contextPath}/api/bookkeeper/users')
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
                const statusElement = document.createElement('span');
                usernameElement.className = 'username';
                statusElement.className = 'status';
                usernameElement.textContent = user.username;
                statusElement.textContent = user.status;

                listMember.appendChild(usernameElement);
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

</script>
</body>
</html>