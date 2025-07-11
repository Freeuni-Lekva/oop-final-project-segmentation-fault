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

  <!-- Manual Book Entry Section -->
  <div class="manual-add-section">
    <h3 class="subsection-title">Manual Book Entry</h3>
    <form id="addBookForm" enctype="multipart/form-data">
      
      <!-- Primary Book Information -->
    <div class="form-grid">
      <div class="form-group">
          <label for="title">Title *</label>
          <input type="text" id="title" name="title" placeholder="Book title" required>
      </div>
        
      <div class="form-group">
        <label for="author">Author *</label>
          <input type="text" id="author" name="author" placeholder="Author name" required>
        </div>
        
        <div class="form-group">
          <label for="copies">Number of Copies *</label>
          <input type="number" id="copies" name="copies" placeholder="1" min="1" value="1" required>
        </div>
        
        <div class="form-group">
          <label for="volume">Pages</label>
          <input type="number" id="volume" name="volume" placeholder="Number of pages" min="1" value="1" step="1">
      </div>
        
      <div class="form-group">
          <label for="genre">Genre *</label>
          <select id="genre" name="genre" required>
            <option value="">Choose a genre</option>
            <option value="classics">Classics</option>
            <option value="fiction">Fiction</option>
            <option value="crime">Crime</option>
            <option value="mystery">Mystery</option>
            <option value="romance">Romance</option>
            <option value="memoir">Memoir</option>
            <option value="fantasy">Fantasy</option>
            <option value="horror">Horror</option>
            <option value="history">History</option>
            <option value="poetry">Poetry</option>
            <option value="adventure">Adventure</option>
            <option value="comedy">Comedy</option>
            <option value="philosophy">Philosophy</option>
            <option value="psychology">Psychology</option>
            <option value="art">Art</option>
            <option value="religion">Religion</option>
            <option value="politics">Politics</option>
        </select>
      </div>
        
      <div class="form-group">
          <label for="publicationDate">Release Date *</label>
          <input type="date" id="publicationDate" name="publicationDate" required>
        </div>
      </div>

      <!-- Book Description -->
      <div class="form-group">
        <label for="description">Description</label>
        <textarea id="description" name="description" placeholder="Optional book description..." rows="3"></textarea>
      </div>

      <!-- Image Upload Section -->
      <div class="form-group">
        <label for="bookImage">Book Cover Photo</label>
        <div class="file-upload-container">
          <input type="file" id="bookImage" name="bookImage" accept="image/png,image/jpg,image/jpeg" class="file-input" style="display: none;">
          <div class="file-upload-area" id="fileUploadClickArea">
            <div class="upload-content">
              <svg class="file-upload-icon" viewBox="0 0 24 24" fill="currentColor">
                <path d="M9 16h6v-6h4l-7-7-7 7h4zm-4 2h14v2H5z"/>
              </svg>
              <div class="file-upload-text">Upload Book Cover</div>
              <div class="file-upload-subtext">JPG, PNG up to 5MB</div>
      </div>
    </div>
          <div class="file-preview" id="imagePreview" style="display: none;">
            <img id="imagePreviewImg" src="" alt="Preview" style="max-width: 100px; max-height: 150px; border-radius: 4px;">
        <div class="file-preview-name" id="imagePreviewName"></div>
      </div>
    </div>
      </div>

      <!-- Submit Button -->
    <div class="action-buttons">
      <button type="submit" class="btn" id="addBookBtn">
        <svg style="width: 16px; height: 16px;" viewBox="0 0 24 24" fill="currentColor">
          <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
        </svg>
          Add Book to Collection
      </button>
    </div>
      
      <!-- Message Area -->
    <div id="addBookMessage" class="message-area" style="display: none;"></div>
  </form>
  </div>
</div> 