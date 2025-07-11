// Handle file upload area click and file input change
document.addEventListener('DOMContentLoaded', function() {
  console.log('Setting up file upload handlers...');
  
  const bookImageInput = document.getElementById('bookImage');
  const fileUploadClickArea = document.getElementById('fileUploadClickArea');
  const preview = document.getElementById('imagePreview');
  const previewName = document.getElementById('imagePreviewName');
  const previewImg = document.getElementById('imagePreviewImg');

  // Handle click area - open file dialog
  if (fileUploadClickArea && !fileUploadClickArea.hasAttribute('data-click-handler')) {
    fileUploadClickArea.setAttribute('data-click-handler', 'true');
    fileUploadClickArea.addEventListener('click', function(e) {
      console.log('Upload area clicked');
      e.preventDefault();
      e.stopPropagation();
      if (bookImageInput) {
        bookImageInput.click();
      }
    });
  }

  // Handle file selection
  if (bookImageInput && !bookImageInput.hasAttribute('data-change-handler')) {
    bookImageInput.setAttribute('data-change-handler', 'true');
    
    bookImageInput.addEventListener('change', function(e) {
      console.log('File input changed, files count:', e.target.files.length);
      const file = e.target.files[0];

      if (file) {
        console.log('File selected:', file.name, 'Size:', file.size, 'Type:', file.type);
        
        // Validate file type
        if (!file.type.startsWith('image/')) {
          alert('Please select an image file (JPG, PNG, JPEG)');
          bookImageInput.value = '';
          return;
        }
        
        // Validate file size (5MB)
        if (file.size > 5 * 1024 * 1024) {
          alert('File size must be less than 5MB');
          bookImageInput.value = '';
          return;
        }
        
        // Show file name immediately
        if (previewName) {
          previewName.textContent = file.name;
        }
        
        // Create and show image preview
        const reader = new FileReader();
        reader.onload = function(readerEvent) {
          if (previewImg) {
            previewImg.src = readerEvent.target.result;
          }
          console.log('Image preview loaded successfully');
        };
        reader.onerror = function() {
          console.error('Error reading file for preview');
        };
        reader.readAsDataURL(file);
        
        // Show preview container
        if (preview) {
          preview.style.display = 'block';
        }
        
        console.log('File processing completed');
        
      } else {
        console.log('No file selected or file cleared');
        // Hide preview if no file
        if (preview) {
          preview.style.display = 'none';
        }
        if (previewImg) {
          previewImg.src = '';
        }
        if (previewName) {
          previewName.textContent = '';
        }
      }
    });
  }
});

// Ensure single event listener for form submission
const addBookForm = document.getElementById('addBookForm');
if (addBookForm && !addBookForm.hasAttribute('data-listener-added')) {
  addBookForm.setAttribute('data-listener-added', 'true');
  
  addBookForm.addEventListener('submit', async function (e) {
    e.preventDefault();
    console.log('Form submission started');

    const form = this;
    const button = document.getElementById('addBookBtn');
    const buttonText = button.textContent;
    const msgBox = document.getElementById('addBookMessage');

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

      console.log('Form data:', { title, author, genre, copies, publicationDate });
      console.log('Image file:', imageFile ? imageFile.name : 'NO FILE SELECTED');

      // Validate required fields (image is now optional)
      if (!title || !author || !genre || !publicationDate) {
        throw new Error('Please fill in all required fields');
      }
      
      console.log('Validation passed - proceeding with book creation');

      let imageUrl = null;
      
      // Only upload image if one was selected
      if (imageFile) {
        console.log('Starting image upload...');
        const imageFormData = new FormData();
        imageFormData.append('image', imageFile);

        const imageUploadResponse = await fetch(window.CONTEXT_PATH + '/api/bookkeeper/upload-image', {
          method: 'POST',
          body: imageFormData,
          credentials: "include"
        });

        console.log('Image upload response status:', imageUploadResponse.status);

        if (!imageUploadResponse.ok) {
          const errorData = await imageUploadResponse.json().catch(() => null);
          const errorMessage = errorData?.message || 'Image upload failed';
          console.error('Image upload failed:', errorMessage);
          throw new Error(errorMessage);
        }
        
        const imageData = await imageUploadResponse.json();
        console.log('Image upload response:', imageData);
        
        if (imageData.status !== 'success') {
          throw new Error(imageData.message || 'Image upload failed');
        }
        imageUrl = imageData.imageUrl;
        console.log('Image uploaded successfully:', imageUrl);
      } else {
        console.log('No image file selected - proceeding without image');
      }

            const bookData = {
        title,
        author,
        genre,
        volume,
        description,
        copies,
        publicationDate,
        imageUrl
      };

      console.log('Final book data being sent:', JSON.stringify(bookData, null, 2));

          const bookCreateResponse = await fetch(window.CONTEXT_PATH + '/api/bookkeeper/add-book', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(bookData),
        credentials: "include"
      });

      console.log('Book creation response status:', bookCreateResponse.status);
      console.log('Book creation response headers:', [...bookCreateResponse.headers.entries()]);

      if (bookCreateResponse.ok) {
      const responseData = await bookCreateResponse.json().catch(() => null);
      const successMessage = responseData ? responseData.message : 'Book added successfully';
      msgBox.textContent = successMessage;
      msgBox.className = 'message-area success';
      msgBox.style.display = 'block';
      form.reset();
      
      // Clear image preview and file input
      const preview = document.getElementById('imagePreview');
      const previewImg = document.getElementById('imagePreviewImg');
      const previewName = document.getElementById('imagePreviewName');
      const fileInput = document.getElementById('bookImage');
      if (preview) {
        preview.style.display = 'none';
        previewImg.src = '';
        previewName.textContent = '';
      }
      if (fileInput) {
        fileInput.value = '';
      }
      
      // Always refresh book collection list
      loadBooksList();
      
      setTimeout(() => (msgBox.style.display = 'none'), 5000);
          } else {
        console.log('Book creation failed with status:', bookCreateResponse.status);
        const responseText = await bookCreateResponse.text();
        console.log('Error response text:', responseText);
        
        let responseData = null;
        try {
          responseData = JSON.parse(responseText);
        } catch (e) {
          console.log('Response is not valid JSON:', e);
        }
        
        const errorMessage = responseData ? responseData.message : 'Failed to add book';
        msgBox.textContent = errorMessage + ' (Status: ' + bookCreateResponse.status + ')';
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
}

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

    const response = await fetch(window.CONTEXT_PATH + '/api/bookkeeper/add-book-from-google', {
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
  fetch(window.CONTEXT_PATH + '/api/bookkeeper/books', {
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
                  img.src = window.CONTEXT_PATH + '/images/' + book.imageUrl;
                }
                
                img.alt = book.name || 'Book Cover';

                img.onerror = function() {
                  console.log('Failed to load image:', this.src);
                  this.style.display = 'none';
                  const placeholder = createBookPlaceholder(book);
                  bookImage.appendChild(placeholder);
                };
                
                img.onload = function() {
                  console.log('Successfully loaded image:', this.src);
                };
                
                bookImage.appendChild(img);
              } else {
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
              
              bookInfo.style.cursor = 'pointer';
              bookInfo.onclick = function() {
                window.open(window.CONTEXT_PATH + '/book-details.jsp?bookId=' + encodeURIComponent(book.publicId) + '&admin=true', '_blank');
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

  fetch(window.CONTEXT_PATH + '/api/bookkeeper/delete-book', {
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