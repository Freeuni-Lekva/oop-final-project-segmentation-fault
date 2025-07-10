<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<%@ include file="genre-data.jsp" %>
<%
    String genre = request.getParameter("genre");
    if (genre == null) genre = "classics";

    Map<String, String> currentGenre = ((Map<String, Map<String, String>>)request.getAttribute("genreData"))
            .getOrDefault(genre,
                    ((Map<String, Map<String, String>>)request.getAttribute("genreData")).get("classics"));

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
    <%@ include file="genre-styles.jsp" %>
</head>
<body>

<div class="genre-header" style="background-image: url('images/<%= genre.replaceAll("[^a-zA-Z0-9_-]", "") %>.jpg');">
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
            </div>
            <div class="results-count" id="resultsCount">
                Loading books...
            </div>
        </div>

        <div class="loading-indicator" id="loadingIndicator">
            <div class="spinner"></div>
            <p>Loading books...</p>
        </div>

        <div class="error-message" id="errorMessage" style="display: none;">
            <h3>Error Loading Books</h3>
            <p id="errorText">Unable to load books. Please try again later.</p>
        </div>

        <div class="books-grid" id="booksGrid">

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

        function fetchBooks(sortCriteria) {
            loadingIndicator.style.display = 'block';
            errorMessage.style.display = 'none';
            booksGrid.innerHTML = '';

            var contextPath = '<%= request.getContextPath() %>';
            var apiUrl = contextPath + '/api/books/get-books-by-genre/' + genre;

            // Add sorting parameter if provided
            if (sortCriteria) {
                apiUrl += '?sort=' + encodeURIComponent(sortCriteria);
            }

            fetch(apiUrl)
                .then(function(response) {
                    if (!response.ok) {
                        throw new Error('HTTP error! status: ' + response.status);
                    }
                    return response.json();
                })
                .then(function(books) {
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

            var defaultCovers = [
                contextPath + '/images/noCover1.jpg',
                contextPath + '/images/noCover2.jpg',
                contextPath + '/images/noCover3.jpg',
                contextPath + '/images/noCover4.jpg',
                contextPath + '/images/noCover5.jpg',
                contextPath + '/images/noCover6.jpg',
                contextPath + '/images/noCover7.jpg',
                contextPath + '/images/noCover8.jpg',
                contextPath + '/images/noCover9.jpg',
                contextPath + '/images/noCover10.jpg',
                contextPath + '/images/noCover11.jpg',
                contextPath + '/images/noCover12.jpg'
            ];

            function getRandomDefaultCover() {
                var randomIndex = Math.floor(Math.random() * defaultCovers.length);
                return defaultCovers[randomIndex];
            }

            if (book.imageUrl) {
                var imagePath = '/images/' + book.imageUrl;
                imageHtml = '<img src="' + imagePath + '" alt="' + (book.name || 'Book cover') + '"' +
                    ' onerror="this.src=\'' + getRandomDefaultCover() + '\'">';
            } else {
                imageHtml = '<img src="' + getRandomDefaultCover() + '" alt="No book cover">';
            }

            var rating = book.rating || 0;
            var fullStars = Math.floor(rating);
            var hasHalfStar = rating % 1 >= 0.5;
            var stars = '★'.repeat(fullStars) + (hasHalfStar ? '☆' : '') + '☆'.repeat(5 - fullStars - (hasHalfStar ? 1 : 0));

            var bookId = book.publicId;
            var bookTitle = book.name || 'Unknown Title';
            var bookAuthor = book.author || 'Unknown Author';
            var bookDescription = book.description || '';
            var currentAmount = book.currentAmount || 0;
            var totalAmount = book.totalAmount || 0;

            var ratingHtml = '';
            if (rating > 0) {
                ratingHtml = '<div class="book-rating">' +
                    '<span class="stars">' + stars + '</span>' +
                    '<span class="rating-text">(' + rating.toFixed(1) + ')</span>' +
                    '</div>';
            }

            var descriptionHtml = '';
            if (bookDescription) {
                descriptionHtml = '<p class="book-summary">' + bookDescription + '</p>';
            }

            var availabilityClass = currentAmount > 0 ? 'available' : 'unavailable';

                            var totalCopiesText = totalAmount > 0 ? totalAmount + ' total copies' : 'No copies';
            var currentCopiesText = currentAmount > 0 ? currentAmount + ' available' : 'None available';

            return '<div class="book-card" onclick="viewBookDetails(\'' + bookId + '\')">' +
                '<div class="book-cover">' + imageHtml + '</div>' +
                '<div class="book-info">' +
                '<h3 class="book-title">' + bookTitle + '</h3>' +
                '<p class="book-author">' + bookAuthor + '</p>' +
                ratingHtml +
                descriptionHtml +
                '<div class="book-meta">' +
                '<span class="book-total-copies">' + totalCopiesText + '</span>' +
                '<span class="book-available ' + availabilityClass + '">' + currentCopiesText + '</span>' +
                '</div>' +
                '</div>' +
                '</div>';
        }

        function updateResultsCount(count) {
            resultsCount.textContent = 'Showing ' + count + ' books in <%= genreDisplay %>';
        }

        function sortBooks(criteria) {
            switch(criteria) {
                case 'title':
                    filteredBooks.sort(function(a, b) {
                        return (a.name || '').localeCompare(b.name || '');
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
                        return (b.currentAmount || 0) - (a.currentAmount || 0);
                    });
                    break;
            }

            fetchBooks(criteria);
            displayBooks(filteredBooks);
        }

        function viewBookDetails(bookId) {
            window.location.href = 'book-details.jsp?id=' + encodeURIComponent(bookId);
        }

        window.viewBookDetails = viewBookDetails;

        sortSelect.addEventListener('change', function() {
            sortBooks(this.value);
        });

        fetchBooks('rating'); // Default sort by rating
    });
</script>

</body>
</html>