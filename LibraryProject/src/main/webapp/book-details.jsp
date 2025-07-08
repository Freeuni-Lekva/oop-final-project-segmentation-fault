<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Book Details - Library</title>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="book-details-styles.jsp">
</head>
<body>
<div class="book-details-header">
    <h1 class="book-title" id="bookTitle">Loading...</h1>
    <p class="book-author" id="bookAuthor"></p>
</div>

<div class="main-content">
    <div class="book-details-container">
        <a href="javascript:history.back()" class="back-link">
            <svg class="back-arrow" viewBox="0 0 24 24">
                <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"></path>
            </svg>
            Back to Books
        </a>

        <div class="book-details-content">
            <div class="book-cover-section">
                <div class="book-cover" id="bookCover">
                    <div class="book-cover-fallback">📚</div>
                </div>
                <div class="book-meta">
                    <span class="book-total-copies" id="bookTotalCopies"></span>
                    <span class="book-available" id="bookAvailable"></span>
                    <button class="reserve-button" id="reserveButton" disabled>Reserve Book</button>
                </div>
            </div>
            <div class="book-info-section">
                <div class="book-rating" id="bookRating"></div>
                <p class="book-description" id="bookDescription"></p>
                <div class="book-details-meta">
                    <p><strong>Genre:</strong> <span id="bookGenre"></span></p>
                    <p><strong>Volume:</strong> <span id="bookVolume"></span></p>
                    <p><strong>Published:</strong> <span id="bookDate"></span></p>
                </div>
            </div>
        </div>

        <div class="review-section">
            <h2>Write a Review</h2>
            <div class="review-form">
                <label for="reviewRating">Rating:</label>
                <select id="reviewRating" class="review-select">
                    <option value="1">1 Star</option>
                    <option value="2">2 Stars</option>
                    <option value="3">3 Stars</option>
                    <option value="4">4 Stars</option>
                    <option value="5">5 Stars</option>
                </select>
                <textarea id="reviewText" placeholder="Write your review here..." rows="5"></textarea>
                <button class="submit-review-button" id="submitReviewButton">Submit Review</button>
            </div>
        </div>

        <div class="loading-indicator" id="loadingIndicator">
            <div class="spinner"></div>
            <p>Loading book details...</p>
        </div>

        <div class="error-message" id="errorMessage" style="display: none;">
            <h3>Error Loading Book</h3>
            <p id="errorText">Unable to load book details. Please try again later.</p>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const bookId = new URLSearchParams(window.location.search).get('id');
        console.log('Book ID from URL:', bookId);

        const loadingIndicator = document.getElementById('loadingIndicator');
        const errorMessage = document.getElementById('errorMessage');
        const bookTitle = document.getElementById('bookTitle');
        const bookAuthor = document.getElementById('bookAuthor');
        const bookCover = document.getElementById('bookCover');
        const bookTotalCopies = document.getElementById('bookTotalCopies');
        const bookAvailable = document.getElementById('bookAvailable');
        const reserveButton = document.getElementById('reserveButton');
        const bookRating = document.getElementById('bookRating');
        const bookDescription = document.getElementById('bookDescription');
        const bookGenre = document.getElementById('bookGenre');
        const bookVolume = document.getElementById('bookVolume');
        const bookDate = document.getElementById('bookDate');
        const submitReviewButton = document.getElementById('submitReviewButton');

        function capitalizeWords(str) {
            if (!str) return str;
            return str.replace(/\b\w/g, char => char.toUpperCase());
        }

        function fetchBookDetails() {
            if (!bookId) {
                loadingIndicator.style.display = 'none';
                errorMessage.style.display = 'block';
                document.getElementById('errorText').textContent = 'No book ID provided in the URL.';
                return;
            }

            loadingIndicator.style.display = 'block';
            errorMessage.style.display = 'none';

            const contextPath = '<%= request.getContextPath() %>';
            const apiUrl = contextPath + '/api/books/details/' + encodeURIComponent(bookId);
            console.log('Fetching book details from:', apiUrl);

            fetch(apiUrl)
                .then(response => {
                    console.log('Response status:', response.status);
                    if (!response.ok) {
                        throw new Error(`HTTP error! Status: ${response.status}`);
                    }
                    return response.json();
                })
                .then(book => {
                    console.log('Book data received:', book);
                    if (!book) {
                        throw new Error('No book data returned');
                    }
                    displayBookDetails(book);
                    loadingIndicator.style.display = 'none';
                })
                .catch(error => {
                    console.error('Error fetching book details:', error);
                    loadingIndicator.style.display = 'none';
                    errorMessage.style.display = 'block';
                    document.getElementById('errorText').textContent =
                        'Unable to load book details: ' + error.message;
                });
        }

        function displayBookDetails(book) {
            bookTitle.textContent = capitalizeWords(book.name) || 'Unknown Title';
            bookAuthor.textContent = capitalizeWords(book.author) || 'Unknown Author';

            if (book.imageUrl) {
                bookCover.innerHTML = `<img src="/images/${book.imageUrl}" alt="${book.name || 'Book cover'}"
                    onerror="this.style.display='none'; this.nextElementSibling.style.display='flex'">
                    <div class="book-cover-fallback">📚</div>`;
            } else {
                bookCover.innerHTML = '<div class="book-cover-fallback">📚</div>';
            }

            bookTotalCopies.textContent = book.originalAmount != null ? `Total Copies: ${book.originalAmount}` : 'Total Copies: 0';
            bookAvailable.textContent = book.currentAmount > 0 ? 'Available' : 'Not Available';
            bookAvailable.className = 'book-available ' + (book.currentAmount > 0 ? 'available' : 'unavailable');
            reserveButton.disabled = !book.currentAmount || book.currentAmount <= 0;

            const rating = book.rating;
            if (rating != null && rating > 0) {
                const fullStars = Math.floor(rating);
                const hasHalfStar = rating % 1 >= 0.5;
                const stars = '★'.repeat(fullStars) + (hasHalfStar ? '☆' : '') + '☆'.repeat(5 - fullStars - (hasHalfStar ? 1 : 0));
                bookRating.innerHTML = `<span class="stars">${stars}</span><span class="rating-text">(${rating.toFixed(1)})</span>`;
            } else {
                bookRating.innerHTML = 'No reviews yet';
            }

            bookDescription.textContent = book.description || 'No description available';
            bookGenre.textContent = capitalizeWords(book.genre) || 'Unknown';
            bookVolume.textContent = book.volume != null ? book.volume : 'N/A';
            bookDate.textContent = capitalizeWords(book.date) || 'N/A';
        }

        reserveButton.addEventListener('click', function() {
            alert('Book reservation functionality to be implemented');
        });

        submitReviewButton.addEventListener('click', function() {
            const rating = document.getElementById('reviewRating').value;
            const reviewText = document.getElementById('reviewText').value;
            alert(`Review submission: ${rating} stars, ${reviewText}`);
        });

        fetchBookDetails();
    });
</script>
</body>
</html>