<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
  <%@ include file="main-page-styles.jsp" %>
  <title>Freeuni Library</title>
</head>
<body>
<%@ include file="main-page-header.jsp" %>

<div class="main-content">

  <div class="hero-section">
    <h1>Welcome to Freeuni Library</h1>
  </div>

  <div class="stats-section">
    <div class="stat-item">
      <div class="stat-number" id="totalBooks">-</div>
      <div class="stat-label">Books</div>
    </div>
    <div class="stat-item">
      <div class="stat-number" id="availableBooks">-</div>
      <div class="stat-label">Available</div>
    </div>
    <div class="stat-item">
      <div class="stat-number" id="totalGenres">-</div>
      <div class="stat-label">Genres</div>
    </div>
  </div>

  <div class="books-container">

    <div class="books-section">
      <div class="section-header">
        <h2 class="section-title">Our Collection</h2>
      </div>

      <div class="scroll-container">
        <button class="scroll-nav scroll-nav-left" onclick="scrollSection('featuredScroll', -200)">
          <svg viewBox="0 0 24 24"><path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/></svg>
        </button>

        <div class="books-scroll" id="featuredScroll">
          <div class="loading-indicator">
            <div class="spinner"></div>
            <p>Loading our collection...</p>
          </div>
        </div>

        <button class="scroll-nav scroll-nav-right" onclick="scrollSection('featuredScroll', 200)">
          <svg viewBox="0 0 24 24"><path d="M8.59 16.59L10 18l6-6-6-6-1.41 1.41L13.17 12z"/></svg>
        </button>
      </div>
    </div>


    <div class="books-section">
      <div class="section-header">
        <h2 class="section-title">Recently Added</h2>
      </div>

      <div class="scroll-container">
        <button class="scroll-nav scroll-nav-left" onclick="scrollSection('recentScroll', -200)">
          <svg viewBox="0 0 24 24"><path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/></svg>
        </button>

        <div class="books-scroll" id="recentScroll">
          <div class="loading-indicator">
            <div class="spinner"></div>
            <p>Loading recent books...</p>
          </div>
        </div>

        <button class="scroll-nav scroll-nav-right" onclick="scrollSection('recentScroll', 200)">
          <svg viewBox="0 0 24 24"><path d="M8.59 16.59L10 18l6-6-6-6-1.41 1.41L13.17 12z"/></svg>
        </button>
      </div>
    </div>


    <div class="books-section" id="recommendationsSection" style="display: none;">
      <div class="section-header">
        <h2 class="section-title">Recommended for You</h2>
      </div>

      <div class="scroll-container">
        <button class="scroll-nav scroll-nav-left" onclick="scrollSection('recommendationsScroll', -200)">
          <svg viewBox="0 0 24 24"><path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/></svg>
        </button>

        <div class="books-scroll" id="recommendationsScroll">
          <div class="loading-indicator">
            <div class="spinner"></div>
            <p>Loading recommendations...</p>
          </div>
        </div>

        <button class="scroll-nav scroll-nav-right" onclick="scrollSection('recommendationsScroll', 200)">
          <svg viewBox="0 0 24 24"><path d="M8.59 16.59L10 18l6-6-6-6-1.41 1.41L13.17 12z"/></svg>
        </button>
      </div>
    </div>


    <div class="books-section">
      <div class="section-header">
        <h2 class="section-title">Popular This Month</h2>
      </div>

      <div class="scroll-container">
        <button class="scroll-nav scroll-nav-left" onclick="scrollSection('popularScroll', -200)">
          <svg viewBox="0 0 24 24"><path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/></svg>
        </button>

        <div class="books-scroll" id="popularScroll">
          <div class="loading-indicator">
            <div class="spinner"></div>
            <p>Loading popular books...</p>
          </div>
        </div>

        <button class="scroll-nav scroll-nav-right" onclick="scrollSection('popularScroll', 200)">
          <svg viewBox="0 0 24 24"><path d="M8.59 16.59L10 18l6-6-6-6-1.41 1.41L13.17 12z"/></svg>
        </button>
      </div>
    </div>
  </div>
</div>

<script>
  document.addEventListener('click', function(event) {
    var dropdown = document.getElementById('browseDropdown');
    var browseButton = event.target.closest('.browse-dropdown');

    if (!browseButton) {
      dropdown.classList.remove('show');
    }
  });

  document.addEventListener('DOMContentLoaded', function() {
    var contextPath = '<%= request.getContextPath() %>';

    var isLoggedIn = checkUserLogin();

    console.log('User logged in:', isLoggedIn);

    if (isLoggedIn) {
      document.getElementById('recommendationsSection').style.display = 'block';
      fetchRecommendations();
    } else {
      console.log('User not logged in, hiding recommendations section');
      document.getElementById('recommendationsSection').style.display = 'none';
    }


    fetchOurCollection();
    fetchRecentBooks();
    fetchPopularBooks();
    updateStats();

    function checkUserLogin() {
      var username = '<%= session.getAttribute("username") != null ? session.getAttribute("username").toString() : "" %>';

      console.log('Session username:', username);

      if (username && username.trim() !== '' && username !== 'null') {
        console.log('User authenticated via session:', username);
        return true;
      }

      var userIndicators = [
        '.user-info',
        '.username',
        '[data-username]',
        '.user-profile',
        '.logout-btn',
        '.user-menu'
      ];

      for (var i = 0; i < userIndicators.length; i++) {
        var element = document.querySelector(userIndicators[i]);
        if (element) {
          console.log('Found user indicator element:', userIndicators[i]);
          return true;
        }
      }

      console.log('No clear authentication indicators found, testing endpoint...');
      return false;
    }

    function updateStats() {
      fetch(contextPath + '/api/books/all')
              .then(function(response) {
                if (!response.ok) {
                  throw new Error('HTTP error! status: ' + response.status);
                }
                return response.json();
              })
              .then(function(books) {
                console.log('Raw books data:', books);
                console.log('First book structure:', books[0]);

                document.getElementById('totalBooks').textContent = books.length;

                var availableCount = books.filter(function(book) {
                  console.log('Book availability check:', book.title, 'currentAmount:', book.currentAmount, 'type:', typeof book.currentAmount);
                  var current = book.currentAmount ? Number(book.currentAmount) : 0;
                  return current > 0;
                }).length;

                console.log('Available books count:', availableCount);
                document.getElementById('availableBooks').textContent = availableCount;

                var genres = new Set();
                books.forEach(function(book) {
                  console.log('Book genre:', book.genre, 'type:', typeof book.genre);
                  if (book.genre && typeof book.genre === 'string' && book.genre.trim() !== '') {
                    var normalizedGenre = book.genre.trim().toLowerCase();
                    genres.add(normalizedGenre);
                  }
                });

                console.log('Unique genres:', Array.from(genres));
                document.getElementById('totalGenres').textContent = genres.size;

                console.log('Stats Debug Info:');
                console.log('Total books:', books.length);
                console.log('Available books:', availableCount);
                console.log('Unique genres:', genres.size);


                var booksWithIssues = books.filter(function(book) {
                  var current = book.currentAmount ? Number(book.currentAmount) : 0;
                  return isNaN(current) || current < 0;
                });
                if (booksWithIssues.length > 0) {
                  console.log('Books with availability data issues:', booksWithIssues);
                }
              })
              .catch(function(error) {
                console.error('Error fetching stats:', error);

                document.getElementById('totalBooks').textContent = '0';
                document.getElementById('availableBooks').textContent = '0';
                document.getElementById('totalGenres').textContent = '0';
              });
    }

    function fetchRecommendations() {
      var recommendationsScroll = document.getElementById('recommendationsScroll');
      var recommendationsSection = document.getElementById('recommendationsSection');

      console.log('Fetching recommendations from:', contextPath + '/api/user/recommend');

      fetch(contextPath + '/api/user/recommend')
              .then(function(response) {
                console.log('Recommendations response status:', response.status);

                if (response.status === 401) {
                  console.log('User not authenticated - hiding recommendations');
                  recommendationsSection.style.display = 'none';
                  return null;
                }

                if (response.status === 404) {
                  console.log('User not found');
                  recommendationsSection.style.display = 'none';
                  return null;
                }

                if (!response.ok) {
                  throw new Error('HTTP error! status: ' + response.status);
                }

                recommendationsSection.style.display = 'block';
                return response.json();
              })
              .then(function(books) {
                if (!books) return;

                console.log('Parsed recommendations:', books);

                if (books && books.length > 0) {
                  displayBooks(books, recommendationsScroll);
                } else {
                  recommendationsScroll.innerHTML =
                          '<div class="no-books">' +
                          '<h3>No Recommendations Yet</h3>' +
                          '<p>Read and rate some books to get personalized recommendations!</p>' +
                          '</div>';
                }
              })
              .catch(function(error) {
                console.error('Error fetching recommendations:', error);
                recommendationsSection.style.display = 'none';
              });
    }

    function fetchOurCollection() {
      var featuredScroll = document.getElementById('featuredScroll');

      fetch(contextPath + '/api/books/all')
              .then(function(response) {
                if (!response.ok) {
                  throw new Error('HTTP error! status: ' + response.status);
                }
                return response.json();
              })
              .then(function(books) {

                var collectionBooks = books.slice(0, 25);
                displayBooks(collectionBooks, featuredScroll);
              })
              .catch(function(error) {
                console.error('Error fetching our collection:', error);
                displayError(featuredScroll, 'Unable to load our collection');
              });
    }

    function fetchRecentBooks() {
      var recentScroll = document.getElementById('recentScroll');

      fetch(contextPath + '/api/books/all')
              .then(function(response) {
                if (!response.ok) {
                  throw new Error('HTTP error! status: ' + response.status);
                }
                return response.json();
              })
              .then(function(books) {
                var recentBooks = books.slice(25, 50);
                displayBooks(recentBooks, recentScroll);
              })
              .catch(function(error) {
                console.error('Error fetching recent books:', error);
                displayError(recentScroll, 'Unable to load recent books');
              });
    }

    function fetchPopularBooks() {
      var popularScroll = document.getElementById('popularScroll');

      fetch(contextPath + '/api/books/all')
              .then(function(response) {
                if (!response.ok) {
                  throw new Error('HTTP error! status: ' + response.status);
                }
                return response.json();
              })
              .then(function(books) {

                var popularBooks = books
                        .sort(function(a, b) { return (b.rating || 0) - (a.rating || 0); })
                        .slice(0, 25);
                displayBooks(popularBooks, popularScroll);
              })
              .catch(function(error) {
                console.error('Error fetching popular books:', error);
                displayError(popularScroll, 'Unable to load popular books');
              });
    }

    function displayBooks(books, container) {
      if (books.length === 0) {
        container.innerHTML = '<div class="no-books"><h3>No books found</h3><p>No books available in this section.</p></div>';
        return;
      }

      var booksHTML = books.map(function(book) {
        return createBookCard(book);
      }).join('');

      container.innerHTML = booksHTML;
    }

    function displayError(container, message) {
      container.innerHTML = '<div class="error-message"><h3>Error</h3><p>' + message + '</p></div>';
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
        imageHtml = '<img src="' + imagePath + '" alt="' + (book.title || 'Book cover') + '"' +
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
      var currentAmount = book.currentAmount || 0;
      var originalAmount = book.originalAmount || 0;

      var ratingHtml = '';
      if (rating > 0) {
        ratingHtml = '<div class="book-rating">' +
                '<span class="stars">' + stars + '</span>' +
                '<span class="rating-text">(' + rating.toFixed(1) + ')</span>' +
                '</div>';
      }

      var availabilityClass = currentAmount > 0 ? 'available' : 'unavailable';
      var availabilityText = currentAmount > 0 ? 'Available' : 'Unavailable';

      return '<div class="book-card" onclick="viewBookDetails(\'' + bookId + '\')">' +
              '<div class="book-cover">' + imageHtml + '</div>' +
              '<div class="book-info">' +
              '<h3 class="book-title">' + bookTitle + '</h3>' +
              '<p class="book-author">' + bookAuthor + '</p>' +
              ratingHtml +
              '<div class="book-meta">' +
              '<span class="book-available ' + availabilityClass + '">' + availabilityText + '</span>' +
              '</div>' +
              '</div>' +
              '</div>';
    }

    function viewBookDetails(bookId) {
      window.location.href = 'book-details.jsp?id=' + encodeURIComponent(bookId);
    }


    window.viewBookDetails = viewBookDetails;
  });

  function scrollSection(sectionId, scrollAmount) {
    var section = document.getElementById(sectionId);
    section.scrollBy({
      left: scrollAmount,
      behavior: 'smooth'
    });
  }

  function toggleDropdown(event) {
    event.preventDefault();

    var dropdown = document.getElementById('browseDropdown');


    var allDropdowns = document.querySelectorAll('.dropdown-content');
    allDropdowns.forEach(function(dd) {
      dd.classList.remove('show');
    });


    dropdown.classList.toggle('show');
  }

  window.scrollSection = scrollSection;
</script>

</body>
</html>