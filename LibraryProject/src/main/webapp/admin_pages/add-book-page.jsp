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