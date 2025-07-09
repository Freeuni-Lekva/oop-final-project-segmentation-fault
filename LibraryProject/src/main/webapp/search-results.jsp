<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
  <%@ include file="main-page-styles.jsp" %>
  <title>Search Results - Freeuni Library</title>
  <%@ include file="search-results-styles.jsp" %>
</head>
<body>
<%@ include file="main-page-header.jsp" %>

<div class="main-content">
  <div class="search-results-container">
    <h2>Search Results for: <span id="searchQuery"><%= request.getParameter("query") %></span></h2>

    <div class="search-filters">
      <div class="filter-group">
        <label for="sortBy">Sort by:</label>
        <select id="sortBy" onchange="sortResults()">
          <option value="rating">Rating</option>
          <option value="title">Title</option>
          <option value="author">Author</option>
        </select>
      </div>

      <div class="filter-group">
        <label for="availability">Availability:</label>
        <select id="availability" onchange="filterResults()">
          <option value="all">All</option>
          <option value="available">Available Only</option>
        </select>
      </div>
    </div>

    <div class="search-results" id="searchResults">
      <div class="loading-indicator">
        <div class="spinner"></div>
        <p>Searching for books...</p>
      </div>
    </div>
  </div>
</div>

<script>
  const contextPath = '<%= request.getContextPath() %>';

  function performSearch(query) {
    const sortBy = document.getElementById('sortBy').value;
    const availability = document.getElementById('availability').value;
    const apiUrl = contextPath + '/api/books/search?term=' + encodeURIComponent(query) +
            '&sort=' + encodeURIComponent(sortBy) +
            '&availability=' + encodeURIComponent(availability);

    fetch(apiUrl)
            .then(function(response) {
              if (!response.ok) throw new Error('HTTP error! status: ' + response.status);
              return response.json();
            })
            .then(function(books) {
              displaySearchResults(books);
            })
            .catch(function(error) {
              console.error('Error fetching search results:', error);
              displayError('Unable to load search results: ' + error.message);
            });
  }

  function sortResults() {
    const query = '<%= request.getParameter("query") %>';
    if (query && query.trim() !== '') {
      performSearch(query);
    }
  }

  function filterResults() {
    sortResults(); // Same function handles both sorting and availability
  }

  function displaySearchResults(books) {
    const resultsContainer = document.getElementById('searchResults');
    if (!books || books.length === 0) {
      resultsContainer.innerHTML =
              '<div class="no-results"><p>No books found matching your search.</p></div>';
      return;
    }

    let resultsHTML = '<div class="results-grid">';
    books.forEach(function(book) {
      resultsHTML += createBookCard(book);
    });
    resultsHTML += '</div>';
    resultsContainer.innerHTML = resultsHTML;
  }

  function createBookCard(book) {
    const defaultCovers = [...Array(12).keys()].map(i => contextPath + '/images/noCover' + (i + 1) + '.jpg');
    const getRandomCover = () => defaultCovers[Math.floor(Math.random() * defaultCovers.length)];
    const imageHtml = book.imageUrl
            ? '<img src="' + contextPath + '/images/' + book.imageUrl + '" alt="' + (book.name || 'Book') + '"' +
            ' onerror="this.src=\'' + getRandomCover() + '\'">'
            : '<img src="' + getRandomCover() + '" alt="No cover">';

    const rating = book.rating || 0;
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;
    const stars = '★'.repeat(fullStars) + (hasHalfStar ? '☆' : '') + '☆'.repeat(5 - fullStars - (hasHalfStar ? 1 : 0));
    const ratingHtml = rating > 0
            ? '<div class="book-rating"><span class="stars">' + stars + '</span><span class="rating-text">(' + rating.toFixed(1) + ')</span></div>'
            : '';

    const bookId = book.publicId;
    const availabilityClass = book.currentAmount > 0 ? 'available' : 'unavailable';
    const availabilityText = book.currentAmount > 0 ? 'Available' : 'Unavailable';

    return '<div class="book-card" onclick="viewBookDetails(\'' + bookId + '\')">' +
            '<div class="book-cover">' + imageHtml + '</div>' +
            '<div class="book-info">' +
            '<h3 class="book-title">' + (book.name || 'Unknown Title') + '</h3>' +
            '<p class="book-author">' + (book.author || 'Unknown Author') + '</p>' +
            ratingHtml +
            '<div class="book-meta"><span class="book-available ' + availabilityClass + '">' + availabilityText + '</span></div>' +
            '</div>' +
            '</div>';
  }

  function displayError(message) {
    document.getElementById('searchResults').innerHTML =
            '<div class="error-message"><h3>Error</h3><p>' + message + '</p></div>';
  }

  function viewBookDetails(bookId) {
    window.location.href = 'book-details.jsp?id=' + encodeURIComponent(bookId);
  }

  function toggleDropdown(event) {
    event.preventDefault();
    const dropdown = document.getElementById('browseDropdown');
    dropdown.classList.toggle('show');
  }

  document.addEventListener('click', function(event) {
    const dropdown = document.getElementById('browseDropdown');
    const browseButton = event.target.closest('.browse-dropdown');
    if (!browseButton) {
      dropdown.classList.remove('show');
    }
  });

  window.onclick = function(event) {
    if (!event.target.matches('.nav-box') && !event.target.closest('.browse-dropdown')) {
      const dropdown = document.getElementById('browseDropdown');
      if (dropdown.classList.contains('show')) {
        dropdown.classList.remove('show');
      }
    }

    if (event.target.classList.contains('dropdown-item')) {
      window.location.href = event.target.getAttribute('href');
    }
  };

  document.addEventListener('DOMContentLoaded', function() {
    const query = '<%= request.getParameter("query") %>';

    if (query && query.trim() !== '') {
      performSearch(query);
    } else {
      document.getElementById('searchResults').innerHTML =
              '<div class="no-results"><p>Please enter a search term.</p></div>';
    }
  });
</script>

</body>
</html>