<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%

    Map<String, Map<String, String>> genreData = new HashMap<>();

    Map<String, String> fiction = new HashMap<>();
    fiction.put("name", "Fiction");
    fiction.put("description", "Explore imaginative stories and literary works");
    genreData.put("fiction", fiction);

    Map<String, String> romance = new HashMap<>();
    romance.put("name", "Romance");
    romance.put("description", "Love stories and romantic adventures");
    genreData.put("romance", romance);

    Map<String, String> mystery = new HashMap<>();
    mystery.put("name", "Mystery");
    mystery.put("description", "Thrilling mysteries and detective stories");
    genreData.put("mystery", mystery);

    Map<String, String> scifi = new HashMap<>();
    scifi.put("name", "Science Fiction");
    scifi.put("description", "Futuristic stories and scientific adventures");
    genreData.put("science-fiction", scifi);

    // Get genre parameter
    String genre = request.getParameter("genre");
    if (genre == null) genre = "fiction";
    Map<String, String> currentGenre = genreData.getOrDefault(genre, genreData.get("fiction"));
    String genreDisplay = currentGenre.get("name");
    String genreDescription = currentGenre.get("description");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= genreDisplay %> Books - Library</title>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">

    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Poppins', sans-serif;
            background-color: #f8fafa;
            color: #2d3748;
            line-height: 1.6;
        }

        .genre-header {
            background: linear-gradient(135deg, #a7cdcd 0%, #7ba8a8 100%);
            color: white;
            padding: 40px 20px;
            text-align: center;
            margin-bottom: 30px;
        }

        .genre-title {
            font-family: 'Poppins', sans-serif;
            font-size: 36px;
            font-weight: 600;
            margin: 0 0 15px 0;
            text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
        }

        .genre-description {
            font-size: 18px;
            font-weight: 300;
            max-width: 800px;
            margin: 0 auto;
            line-height: 1.6;
            opacity: 0.95;
        }

        .books-container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 20px;
        }

        .books-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
            gap: 30px;
            margin-top: 30px;
        }

        .book-card {
            background: white;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            overflow: hidden;
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            cursor: pointer;
        }

        .book-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
        }

        .book-cover {
            width: 100%;
            height: 300px;
            position: relative;
            overflow: hidden;
        }

        .book-cover img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
        }

        .book-cover-fallback {
            width: 100%;
            height: 100%;
            background: linear-gradient(45deg, #e7edec, #a7cdcd);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 48px;
            color: white;
            position: absolute;
            top: 0;
            left: 0;
        }

        .book-info {
            padding: 20px;
        }

        .book-title {
            font-family: 'Poppins', sans-serif;
            font-size: 18px;
            font-weight: 600;
            color: #2d3748;
            margin: 0 0 8px 0;
            line-height: 1.3;
        }

        .book-author {
            color: #718096;
            font-size: 14px;
            margin: 0 0 12px 0;
        }

        .book-rating {
            display: flex;
            align-items: center;
            gap: 5px;
            margin-bottom: 10px;
        }

        .stars {
            color: #ffd700;
            font-size: 14px;
            font-weight: bold;
        }

        .rating-text {
            color: #718096;
            font-size: 12px;
        }

        .book-summary {
            color: #4a5568;
            font-size: 13px;
            line-height: 1.4;
            display: -webkit-box;
            -webkit-line-clamp: 3;
            -webkit-box-orient: vertical;
            overflow: hidden;
            margin-bottom: 12px;
        }

        .book-meta {
            display: flex;
            justify-content: space-between;
            font-size: 12px;
            color: #718096;
            border-top: 1px solid #e2e8f0;
            padding-top: 8px;
        }

        .book-volume,
        .book-available {
            font-weight: 500;
        }

        .filter-section {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            padding: 20px;
            background: #f8fafa;
            border-radius: 8px;
        }

        .filter-left {
            display: flex;
            gap: 15px;
            align-items: center;
        }

        .filter-select {
            padding: 8px 12px;
            border: 1px solid #e2e6e5;
            border-radius: 6px;
            background: white;
            color: #4a5568;
            font-size: 14px;
            cursor: pointer;
        }

        .results-count {
            color: #718096;
            font-size: 14px;
        }

        .back-link {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            color: #a7cdcd;
            text-decoration: none;
            font-weight: 500;
            margin-bottom: 20px;
            transition: color 0.3s ease;
        }

        .back-link:hover {
            color: #7ba8a8;
        }

        .back-arrow {
            width: 16px;
            height: 16px;
            fill: currentColor;
        }

        .loading-indicator {
            text-align: center;
            padding: 40px 20px;
            color: #718096;
        }

        .spinner {
            width: 40px;
            height: 40px;
            border: 4px solid #e2e8f0;
            border-top: 4px solid #a7cdcd;
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin: 0 auto 20px;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        .error-message {
            text-align: center;
            padding: 40px 20px;
            color: #e53e3e;
            background: #fed7d7;
            border-radius: 8px;
            margin-top: 20px;
        }

        .no-books {
            grid-column: 1 / -1;
            text-align: center;
            padding: 60px 20px;
            color: #718096;
            font-size: 18px;
            background: #f8fafa;
            border-radius: 8px;
        }

        .book-available.available {
            color: #38a169;
            font-weight: 600;
        }

        .book-available.unavailable {
            color: #e53e3e;
            font-weight: 600;
        }

        /* Debug info styling */
        .debug-info {
            position: fixed;
            top: 10px;
            right: 10px;
            background: rgba(0,0,0,0.8);
            color: white;
            padding: 10px;
            border-radius: 5px;
            font-family: monospace;
            font-size: 12px;
            z-index: 1000;
            max-width: 300px;
            display: none;
        }

        @media (max-width: 768px) {
            .filter-section {
                flex-direction: column;
                gap: 15px;
                align-items: stretch;
            }

            .filter-left {
                justify-content: center;
            }

            .results-count {
                text-align: center;
            }

            .books-grid {
                grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
                gap: 20px;
            }

            .genre-title {
                font-size: 28px;
            }

            .genre-description {
                font-size: 16px;
            }
        }
    </style>
</head>
<body>

<!-- Debug info panel -->
<div class="debug-info" id="debugInfo">
    <div>Context Path: <span id="debugContextPath"></span></div>
    <div>Image attempts: <span id="debugImageAttempts">0</span></div>
    <div>Image errors: <span id="debugImageErrors">0</span></div>
</div>

<div class="genre-header">
    <h1 class="genre-title"><%= genreDisplay %></h1>
    <p class="genre-description"><%= genreDescription %></p>
</div>

<div class="main-content">
    <div class="books-container">
        <a href="main-page.jsp" class="back-link">
            <svg class="back-arrow" viewBox="0 0 24 24">
                <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"></path>
            </svg>
            Back to Home
        </a>

        <div class="filter-section">
            <div class="filter-left">
                <label>
                    <select class="filter-select" id="sortSelect">
                        <option value="rating">Sort by Rating</option>
                        <option value="title">Sort by Title</option>
                        <option value="author">Sort by Author</option>
                        <option value="available">Show Available First</option>
                    </select>
                </label>
                <button onclick="toggleDebug()" style="padding: 8px 12px; background: #a7cdcd; color: white; border: none; border-radius: 6px; cursor: pointer;">
                    Debug Images
                </button>
            </div>
            <div class="results-count" id="resultsCount">
                Loading books...
            </div>
        </div>

        <!-- Loading indicator -->
        <div class="loading-indicator" id="loadingIndicator">
            <div class="spinner"></div>
            <p>Loading books...</p>
        </div>

        <!-- Error message -->
        <div class="error-message" id="errorMessage" style="display: none;">
            <h3>Error Loading Books</h3>
            <p id="errorText">Unable to load books. Please try again later.</p>
        </div>

        <div class="books-grid" id="booksGrid">
            <!-- Books will be populated here by JavaScript -->
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        var genre = '<%= genre %>';
        var booksGrid = document.getElementById('booksGrid');
        var loadingIndicator = document.getElementById('loadingIndicator');
        var errorMessage = document.getElementById('errorMessage');
        var resultsCount = document.getElementById('resultsCount');
        var sortSelect = document.getElementById('sortSelect');

        var allBooks = [];
        var filteredBooks = [];
        var imageAttempts = 0;
        var imageErrors = 0;

        // Debug functionality
        window.toggleDebug = function() {
            var debugInfo = document.getElementById('debugInfo');
            debugInfo.style.display = debugInfo.style.display === 'none' ? 'block' : 'none';
            updateDebugInfo();
        };

        function updateDebugInfo() {
            var contextPath = '<%= request.getContextPath() %>';
            document.getElementById('debugContextPath').textContent = contextPath;
            document.getElementById('debugImageAttempts').textContent = imageAttempts;
            document.getElementById('debugImageErrors').textContent = imageErrors;
        }

        function fetchBooks() {
            loadingIndicator.style.display = 'block';
            errorMessage.style.display = 'none';
            booksGrid.innerHTML = '';

            // Get the context path dynamically
            var contextPath = '<%= request.getContextPath() %>';
            var apiUrl = contextPath + '/api/books/get-books-by-genre/' + genre;

            console.log('Fetching from:', apiUrl); // Debug log

            fetch(apiUrl)
                .then(function(response) {
                    console.log('Response status:', response.status); // Debug log
                    if (!response.ok) {
                        throw new Error('HTTP error! status: ' + response.status);
                    }
                    return response.json();
                })
                .then(function(books) {
                    console.log('Books received:', books); // Debug log
                    allBooks = books;
                    filteredBooks = books.slice(); // Copy array
                    displayBooks(filteredBooks);
                    updateResultsCount(filteredBooks.length);
                    loadingIndicator.style.display = 'none';
                })
                .catch(function(error) {
                    console.error('Error fetching books:', error);
                    loadingIndicator.style.display = 'none';
                    errorMessage.style.display = 'block';
                    document.getElementById('errorText').textContent =
                        'Unable to load books: ' + error.message;
                });
        }

        function displayBooks(books) {
            if (books.length === 0) {
                booksGrid.innerHTML = '<div class="no-books"><h3>No books found</h3><p>No books available in this genre at the moment.</p></div>';
                return;
            }

            var booksHTML = books.map(function(book) {
                return createBookCard(book);
            }).join('');
            booksGrid.innerHTML = booksHTML;
        }

        function createBookCard(book) {

            var contextPath = '<%= request.getContextPath() %>';
            var imageHtml = '';

            // Check if the book has an imageName field
            if (book.imageUrl) {
                // Construct the final, correct URL
                // Example: /MyLibraryApp/book-images/xyz123.jpg
                var imagePath = '/book-images/' + book.imageUrl;

                console.log("Constructed Image Path: " + imagePath); // Crucial for debugging
                console.log("Book Title: " + book.title); // Crucial for debugging

                imageHtml = '<img src="' + imagePath + '" alt="' + (book.title || 'Book cover') + '"' +
                    ' onerror="this.style.display=\'none\'; this.nextElementSibling.style.display=\'flex\'">' +
                    '<div class="book-cover-fallback" style="display:none">📚</div>';

                console.log(imageHtml)
            } else {
                // This handles cases where a book has no image at all
                imageHtml = '<div class="book-cover-fallback">📚</div>';
            }


            // Build rating stars
            var rating = book.rating || 0;
            var fullStars = Math.floor(rating);
            var hasHalfStar = rating % 1 >= 0.5;
            var stars = '★'.repeat(fullStars) + (hasHalfStar ? '☆' : '') + '☆'.repeat(5 - fullStars - (hasHalfStar ? 1 : 0));

            // Book details with fallbacks
            var bookId = book.id || book.title;
            var bookTitle = book.title || 'Unknown Title';
            var bookAuthor = book.author || 'Unknown Author';
            var bookDescription = book.description || '';
            var totalCopies = book.totalCopies || 0;
            var availableCopies = book.availableCopies || 0;

            // Build rating HTML
            var ratingHtml = '';
            if (rating > 0) {
                ratingHtml = '<div class="book-rating">' +
                    '<span class="stars">' + stars + '</span>' +
                    '<span class="rating-text">(' + rating.toFixed(1) + ')</span>' +
                    '</div>';
            }

            // Build description HTML
            var descriptionHtml = '';
            if (bookDescription) {
                descriptionHtml = '<p class="book-summary">' + bookDescription + '</p>';
            }

            // Build availability info
            var availabilityClass = availableCopies > 0 ? 'available' : 'unavailable';
            var availabilityText = availableCopies !== undefined ?
                (availableCopies > 0 ? availableCopies + ' available' : 'Not available') :
                'Availability unknown';

            var volumeText = totalCopies ? totalCopies + ' copies' : 'Volume info N/A';

            // Return complete book card HTML
            return '<div class="book-card" onclick="viewBookDetails(\'' + bookId + '\')">' +
                '<div class="book-cover">' + imageHtml + '</div>' +
                '<div class="book-info">' +
                '<h3 class="book-title">' + bookTitle + '</h3>' +
                '<p class="book-author">' + bookAuthor + '</p>' +
                ratingHtml +
                descriptionHtml +
                '<div class="book-meta">' +
                '<span class="book-volume">' + volumeText + '</span>' +
                '<span class="book-available ' + availabilityClass + '">' + availabilityText + '</span>' +
                '</div>' +
                '</div>' +
                '</div>';
        }

// Enhanced image error handling - Updated for new path structure
        window.handleImageError = function(img, bookTitle, originalPath) {
            imageErrors++;
            updateDebugInfo();

            console.log('Image failed to load for ' + bookTitle + ', original path: ' + originalPath);

            var contextPath = '<%= request.getContextPath() %>';
            var imageFileName = originalPath ? originalPath.split('/').pop() : '';

            // Updated alternative paths to use the new /book-images/ mapping
            var alternativePaths = [
                contextPath + '/book-images/' + imageFileName,
                '/book-images/' + imageFileName,
                contextPath + '/images/' + imageFileName,  // Fallback to old path
                './images/' + imageFileName
            ];

            var currentIndex = img.getAttribute('data-retry-index') || 0;
            currentIndex = parseInt(currentIndex);

            if (currentIndex < alternativePaths.length) {
                console.log('Trying alternative path ' + currentIndex + ': ' + alternativePaths[currentIndex]);
                img.setAttribute('data-retry-index', currentIndex + 1);
                img.src = alternativePaths[currentIndex];
            } else {
                // All paths failed, show fallback
                console.log('All image paths failed for ' + bookTitle + ', showing fallback');
                img.style.display = 'none';
                img.nextElementSibling.style.display = 'flex';
            }
        };

        function updateResultsCount(count) {
            resultsCount.textContent = 'Showing ' + count + ' books in <%= genreDisplay %>';
        }

        function sortBooks(criteria) {
            switch(criteria) {
                case 'title':
                    filteredBooks.sort(function(a, b) {
                        return (a.title || '').localeCompare(b.title || '');
                    });
                    break;
                case 'author':
                    filteredBooks.sort(function(a, b) {
                        return (a.author || '').localeCompare(b.author || '');
                    });
                    break;
                case 'rating':
                    filteredBooks.sort(function(a, b) {
                        return (b.rating || 0) - (a.rating || 0);
                    });
                    break;
                case 'available':
                    filteredBooks.sort(function(a, b) {
                        return (b.availableCopies || 0) - (a.availableCopies || 0);
                    });
                    break;
            }
            displayBooks(filteredBooks);
        }

        function viewBookDetails(bookId) {
            window.location.href = 'book-details.jsp?id=' + encodeURIComponent(bookId);
        }

        window.viewBookDetails = viewBookDetails;

        sortSelect.addEventListener('change', function() {
            sortBooks(this.value);
        });

        fetchBooks();
    });
</script>

</body>
</html>