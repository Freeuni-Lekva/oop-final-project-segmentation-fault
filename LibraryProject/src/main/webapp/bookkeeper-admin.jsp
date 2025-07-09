<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Library BookKeeper - Administration</title>
  <%@ include file="bookkeeper-styles.jsp" %>
</head>
<body>

<script>
  // Check if user is authorized to access admin page
  async function checkAdminAccess() {
    try {
      // Try to access a bookkeeper-only endpoint to verify permissions
      const response = await fetch("${pageContext.request.contextPath}/api/bookkeeper/users", {
        method: "GET",
        credentials: "include"
      });
      
      if (!response.ok) {
        // User is not authorized, silently redirect
        window.location.href = "${pageContext.request.contextPath}/main-page.jsp";
        return;
      }
    } catch (error) {
      // If there's any error, redirect to main page
      window.location.href = "${pageContext.request.contextPath}/main-page.jsp";
    }
  }
  
  // Run the check when page loads
  checkAdminAccess();
</script>

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

    <div id="add-book" class="tab-content active">
      <div class="form-container">
        <h2 class="form-section-title">Add New Book to Collection</h2>
        
        <!-- Google Books Quick Add Section -->
        <div class="google-books-section">
          <h3 class="subsection-title">Quick Add from Google Books</h3>
          <form id="googleBooksForm">
            <div class="form-grid-simple">
              <div class="form-group">
                <label for="googleTitle">Book Title *</label>
                <input type="text" id="googleTitle" name="googleTitle" placeholder="Enter book title" required>
              </div>
              <div class="form-group">
                <label for="googleAuthor">Author *</label>
                <input type="text" id="googleAuthor" name="googleAuthor" placeholder="Enter author name" required>
              </div>
              <div class="form-group">
                <label for="googleCopies">Number of Copies *</label>
                <input type="number" id="googleCopies" name="googleCopies" placeholder="Enter number of copies" min="1" value="1" required>
              </div>
            </div>
            <div class="action-buttons">
              <button type="submit" class="btn btn-google" id="addFromGoogleBtn">
                <svg style="width: 16px; height: 16px;" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                </svg>
                Add from Google Books
              </button>
            </div>
            <div id="googleBooksMessage" class="message-area" style="display: none;"></div>
          </form>
        </div>

        <!-- Manual Add Section -->
        <div class="manual-add-section">
          <h3 class="subsection-title">Manual Add (Custom Book)</h3>
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
            <div class="form-group">
              <label for="copies">Number of Copies *</label>
              <input type="number" id="copies" name="copies" placeholder="Enter number of copies" min="1" value="1" required>
            </div>
            <div class="form-group">
              <label for="publicationDate">Publication Date *</label>
              <input type="date" id="publicationDate" name="publicationDate" required>
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
        </div> <!-- End manual-add-section -->
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
      const copies = parseInt(document.getElementById('copies').value) || 1;
      const publicationDate = document.getElementById('publicationDate').value;
      const fileInput = document.getElementById('bookImage');
      const imageFile = fileInput.files[0];

      let imageUrl = null;
      if (imageFile) {
        const imageFormData = new FormData();
        imageFormData.append('image', imageFile);

        const imageUploadResponse = await fetch('${pageContext.request.contextPath}/api/bookkeeper/upload-image', {
          method: 'POST',
          body: imageFormData,
          credentials: "include"
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
        copies,
        publicationDate
      };

      const bookCreateResponse = await fetch('${pageContext.request.contextPath}/api/bookkeeper/add-book', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(bookData),
        credentials: "include"
      });

      if (bookCreateResponse.ok) {
        const responseData = await bookCreateResponse.json().catch(() => null);
        const successMessage = responseData ? responseData.message : 'Book added successfully';
        msgBox.textContent = successMessage;
        msgBox.className = 'message-area success';
        msgBox.style.display = 'block';
        form.reset();
        if (preview && preview.classList.contains('show')) {
          preview.classList.remove('show');
        }
        
        // Always refresh book collection list
        loadBooksList();
        
        setTimeout(() => (msgBox.style.display = 'none'), 5000);
      } else {
        const responseData = await bookCreateResponse.json().catch(() => null);
        const errorMessage = responseData ? responseData.message : 'Failed to add book';
        msgBox.textContent = errorMessage;
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

  // Google Books Form Handler
  document.getElementById('googleBooksForm').addEventListener('submit', async function (e) {
    e.preventDefault();

    const form = this;
    const button = document.getElementById('addFromGoogleBtn');
    const buttonText = button.textContent;
    const msgBox = document.getElementById('googleBooksMessage');

    button.disabled = true;
    button.textContent = 'Fetching from Google Books...';
    msgBox.style.display = 'none';

    try {
      const title = document.getElementById('googleTitle').value.trim();
      const author = document.getElementById('googleAuthor').value.trim();
      const copies = parseInt(document.getElementById('googleCopies').value) || 1;

      if (!title) {
        throw new Error('Book title is required');
      }

      if (!author) {
        throw new Error('Author is required for accurate Google Books search');
      }

      if (copies < 1) {
        throw new Error('Number of copies must be at least 1');
      }

      const payload = {
        title: title,
        author: author,
        copies: copies
      };

      const response = await fetch('${pageContext.request.contextPath}/api/bookkeeper/add-book-from-google', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload),
        credentials: "include"
      });

      if (response.ok) {
        const responseData = await response.json();
        msgBox.textContent = responseData.message || 'Book successfully added from Google Books!';
        msgBox.className = 'message-area success';
        msgBox.style.display = 'block';
        form.reset();
        
        // Always refresh book collection list
        loadBooksList();
        
        setTimeout(() => (msgBox.style.display = 'none'), 5000);
      } else {
        const responseData = await response.json().catch(() => null);
        const errorMessage = responseData ? responseData.message : 'Failed to fetch book from Google Books';
        msgBox.textContent = errorMessage;
        msgBox.className = 'message-area error';
        msgBox.style.display = 'block';
      }
    } catch (error) {
      console.error(error);
      msgBox.textContent = 'Error: ' + error.message;
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
      body: JSON.stringify(payload),
      credentials: "include"
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
      body: JSON.stringify(payload),
      credentials: "include"

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
      body: JSON.stringify(payload),
      credentials: "include"
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


  document.getElementById('bookSearchInput').addEventListener('input', function(e) {
    const search = e.target.value.toLowerCase();
    const genre = document.getElementById('genreFilter').value;
    filterBooks(search, genre);
  });

  document.getElementById('genreFilter').addEventListener('change', function(e) {
    const search = document.getElementById('bookSearchInput').value.toLowerCase();
    const genre = e.target.value;
    filterBooks(search, genre);
  });

  function filterBooks(searchTerm, genreFilter) {
    const allBooks = document.querySelectorAll('.book-item');
    console.log('Filtering with search:', searchTerm, 'genre:', genreFilter, 'Total books:', allBooks.length);
    
    let count = 0;
    
    allBooks.forEach(function(item) {
      const nameTemp = item.querySelector('.book-name');
      const authorTemp = item.querySelector('.book-author');
      const genreTemp = item.querySelector('.book-genre');
      const volumeTemp = item.querySelector('.book-volume');
      const yearTemp = item.querySelector('.book-year');

      const name = nameTemp ? nameTemp.textContent.toLowerCase() : '';
      const author = authorTemp ? authorTemp.textContent.toLowerCase() : '';
      const genre = genreTemp ? genreTemp.textContent.trim() : '';
      const volume = volumeTemp ? volumeTemp.textContent.toLowerCase() : '';
      const year = yearTemp ? yearTemp.textContent.toLowerCase() : '';

      const searchMatch = !searchTerm ||
                           name.includes(searchTerm) || 
                           author.includes(searchTerm) || 
                           volume.includes(searchTerm) || 
                           year.includes(searchTerm);
      const genreMatch = !genreFilter || genre === genreFilter;
      const show = searchMatch && genreMatch;
      
      if (show) {
        item.style.display = 'flex';
        count++;
      } else {
        item.style.display = 'none';
      }
    });
    
    console.log('books after filter:', count);
  }


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
    loadBooksList();
  });

  // Auto-refresh book list when returning to admin panel (e.g., after deleting a book in another tab)
  document.addEventListener('visibilitychange', function() {
    if (!document.hidden) {
      // Page became visible again - refresh the book list in case books were deleted
      if (document.getElementById('book-collection').classList.contains('active')) {
        console.log('Admin panel became visible - refreshing book list');
        loadBooksList();
      }
    }
  });

  // Also refresh when window gains focus
  window.addEventListener('focus', function() {
    if (document.getElementById('book-collection').classList.contains('active')) {
      console.log('Admin panel gained focus - refreshing book list');
      loadBooksList();
    }
  });

  function refreshUsersList() {
    setTimeout(function() {
      loadUsersList();
    }, 800);
  }

  function loadUsersList() {
    const list = document.getElementById('usersList');
    fetch('${pageContext.request.contextPath}/api/bookkeeper/users', {
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

  function getGenreColor(genre) {
    const genreColors = {
      'Fiction': '#4a90e2',
      'Non-Fiction': '#7ed321',
      'Mystery': '#bd10e0',
      'Romance': '#f5a623',
      'Science Fiction': '#50e3c2',
      'Sci-Fi': '#50e3c2',
      'Fantasy': '#9013fe',
      'Biography': '#8e44ad',
      'History': '#e67e22',
      'Philosophy': '#34495e',
      'Psychology': '#e74c3c',
      'Poetry': '#2ecc71',
      'Comedy': '#f39c12',
      'Horror': '#2c3e50',
      'Crime': '#c0392b',
      'Adventure': '#27ae60',
      'Classics': '#95a5a6'
    };
    return genreColors[genre] || '#95a5a6';
  }

  function createBookPlaceholder(book) {
    const placeholder = document.createElement('div');
    placeholder.style.width = '100%';
    placeholder.style.height = '100%';
    placeholder.style.backgroundColor = getGenreColor(book.genre);
    placeholder.style.display = 'flex';
    placeholder.style.flexDirection = 'column';
    placeholder.style.alignItems = 'center';
    placeholder.style.justifyContent = 'center';
    placeholder.style.fontSize = '24px';
    placeholder.style.color = '#fff';
    placeholder.style.textAlign = 'center';
    placeholder.style.borderRadius = '3px';
    placeholder.style.border = '2px solid rgba(255,255,255,0.3)';
    placeholder.style.boxShadow = 'inset 0 0 10px rgba(0,0,0,0.2)';
    

    const bookNameDiv = document.createElement('div');
    bookNameDiv.textContent = book.name || 'Unknown Title';
    bookNameDiv.style.fontSize = '10px';
    bookNameDiv.style.fontWeight = 'bold';
    bookNameDiv.style.textShadow = '1px 1px 2px rgba(0,0,0,0.7)';
    bookNameDiv.style.marginBottom = '3px';
    bookNameDiv.style.padding = '0 4px';
    bookNameDiv.style.textAlign = 'center';
    bookNameDiv.style.lineHeight = '1.2';
    bookNameDiv.style.maxHeight = '36px';
    bookNameDiv.style.overflow = 'hidden';
    bookNameDiv.style.display = '-webkit-box';
    bookNameDiv.style.webkitLineClamp = '3';
    bookNameDiv.style.webkitBoxOrient = 'vertical';

    const genreDiv = document.createElement('div');
    genreDiv.style.fontSize = '8px';
    genreDiv.style.opacity = '0.8';
    genreDiv.style.fontWeight = 'normal';
    genreDiv.style.textShadow = '1px 1px 2px rgba(0,0,0,0.5)';
    genreDiv.textContent = book.genre || 'Book';
    
    placeholder.appendChild(bookNameDiv);
    placeholder.appendChild(genreDiv);
    return placeholder;
  }

  function refreshBooksList() {
    setTimeout(function() {
      loadBooksList();
    }, 800);
  }

  function loadBooksList() {
    const list = document.getElementById('booksList');
    fetch('${pageContext.request.contextPath}/api/bookkeeper/books', {
      credentials: "include"
    })
            .then(function(response) {
              if (response.ok) {
                return response.json();
              } else {
                console.error('Response status:', response.status);
                throw new Error('Error loading books: ' + response.status);
              }
            })
                        .then(function(books) {
              list.textContent = '';
              console.log('Loaded books:', books.length);

              const genres = new Set();
              books.forEach(book => {
                if (book.genre) genres.add(book.genre);
              });
              console.log('Available genres:', Array.from(genres));
              

              const genreFilter = document.getElementById('genreFilter');
              const currentValue = genreFilter.value;
              genreFilter.innerHTML = '<option value="">All Genres</option>';
              Array.from(genres).sort().forEach(genre => {
                const option = document.createElement('option');
                option.value = genre;
                option.textContent = genre;
                genreFilter.appendChild(option);
              });

              if (currentValue && genres.has(currentValue)) {
                genreFilter.value = currentValue;
              }
              
              books.forEach(function(book) {
                const bookElement = document.createElement('div');
                bookElement.className = 'book-item';
                bookElement.setAttribute('data-genre', book.genre || '');
                bookElement.setAttribute('data-name', (book.name || '').toLowerCase());
                bookElement.setAttribute('data-author', (book.author || '').toLowerCase());
                bookElement.setAttribute('data-year', book.date ? book.date.substring(0, 4) : '');
                bookElement.setAttribute('data-volume', book.volume || '');

                const bookInfo = document.createElement('div');
                bookInfo.className = 'book-info';

                const bookImage = document.createElement('div');
                bookImage.className = 'book-image';


                if (book.imageUrl && book.imageUrl.trim() !== '') {
                  const img = document.createElement('img');
                  img.style.width = '100%';
                  img.style.height = '100%';
                  img.style.objectFit = 'cover';
                  img.style.borderRadius = '3px';

                  if (book.imageUrl.startsWith('http://') || book.imageUrl.startsWith('https://')) {
                    img.src = book.imageUrl;
                  } else {
                    img.src = '${pageContext.request.contextPath}/images/' + book.imageUrl;
                  }
                  
                  img.alt = book.name || 'Book Cover';


                  img.onerror = function() {
                    console.log('Failed to load image:', this.src);
                    this.style.display = 'none';
                    const placeholder = createBookPlaceholder(book);
                    bookImage.appendChild(placeholder);
                  };
                  
                  //success
                  img.onload = function() {
                    console.log('Successfully loaded image:', this.src);
                  };
                  
                  bookImage.appendChild(img);
                } else {
                  // default cover
                  const placeholder = createBookPlaceholder(book);
                  bookImage.appendChild(placeholder);
                }

                const bookDetails = document.createElement('div');
                bookDetails.className = 'book-details';

                const bookName = document.createElement('div');
                bookName.className = 'book-name';
                bookName.textContent = book.name || 'Unknown Title';

                const bookAuthor = document.createElement('div');
                bookAuthor.className = 'book-author';
                bookAuthor.textContent = 'by ' + (book.author || 'Unknown Author');

                const bookData = document.createElement('div');
                bookData.className = 'book-meta';

                const bookGenre = document.createElement('span');
                bookGenre.className = 'book-genre';
                bookGenre.textContent = book.genre || 'Unknown';

                const bookVolume = document.createElement('span');
                bookVolume.className = 'book-volume';
                bookVolume.textContent = book.volume && book.volume !== 0 ? 'Vol. ' + book.volume : 'N/A';

                const bookYear = document.createElement('span');
                bookYear.className = 'book-year';
                bookYear.textContent = book.date ? book.date.substring(0, 4) : 'Unknown';

                const bookAmount = document.createElement('span');
                bookAmount.className = 'book-amount';
                bookAmount.textContent = (book.currentAmount || 0) + '/' + (book.totalAmount || 0) + ' available';

                bookData.appendChild(bookGenre);
                bookData.appendChild(document.createTextNode(' | '));
                bookData.appendChild(bookVolume);
                bookData.appendChild(document.createTextNode(' | '));
                bookData.appendChild(bookYear);
                bookData.appendChild(document.createTextNode(' | '));
                bookData.appendChild(bookAmount);

                bookDetails.appendChild(bookName);
                bookDetails.appendChild(bookAuthor);
                bookDetails.appendChild(bookData);

                const bookActions = document.createElement('div');
                bookActions.className = 'book-actions';

                const deleteBtn = document.createElement('button');
                deleteBtn.className = 'btn btn-danger btn-small';
                deleteBtn.innerHTML = '<svg style="width: 14px; height: 14px;" viewBox="0 0 24 24" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg> Delete';
                deleteBtn.onclick = function() {
                  deleteBook(book.publicId, bookElement);
                };

                bookActions.appendChild(deleteBtn);

                bookInfo.appendChild(bookImage);
                bookInfo.appendChild(bookDetails);
                
                // Make the book info clickable to go to book details
                bookInfo.style.cursor = 'pointer';
                bookInfo.onclick = function() {
                  window.open('${pageContext.request.contextPath}/book-details.jsp?bookId=' + encodeURIComponent(book.publicId) + '&admin=true', '_blank');
                };
                
                bookElement.appendChild(bookInfo);
                bookElement.appendChild(bookActions);

                list.appendChild(bookElement);
              });
            })
            .catch(function(error) {
              console.error('Error loading books:', error);
              list.innerHTML = '<div style="padding: 20px; text-align: center; color: #e74c3c;">Error loading books: ' + error.message + '</div>';
            });
  }

  function deleteBook(bookPublicId, bookElement) {
    if (!confirm('Are you sure you want to delete this book?')) {
      return;
    }
    const msgBox = document.getElementById('bookManagementMessage');
    msgBox.style.display = 'none';

    const payload = {
      bookPublicId: bookPublicId
    };

    fetch('${pageContext.request.contextPath}/api/bookkeeper/delete-book', {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload),
      credentials: "include"
    })
            .then(function(response) {
              return response.json().then(function(data) {
                if (response.ok && data.status === 'success') {
                  msgBox.textContent = data.message || 'Book deleted successfully';
                  msgBox.className = 'message-area success';
                  msgBox.style.display = 'block';
                  bookElement.remove();
                  setTimeout(function() {
                    msgBox.style.display = 'none';
                  }, 5000);
                } else {
                  msgBox.textContent = data.message || 'Failed to delete book';
                  msgBox.className = 'message-area error';
                  msgBox.style.display = 'block';
                }
              });
            })
            .catch(function(error) {
              console.error(error);
              msgBox.textContent = 'Error: Check your connection';
              msgBox.className = 'message-area error';
              msgBox.style.display = 'block';
            });
  }

  // Logout function for authentication buttons
  function handleLogout(event) {
    event.preventDefault();
    
    const contextPath = '${pageContext.request.contextPath}';
    
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
        // Fallback: just reload the page
        window.location.reload();
    });
  }

</script>
</body>
</html>