<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="main-page-styles.jsp" %>
    <title>My Books - Freeuni Library</title>
    <script src="${pageContext.request.contextPath}/dropdown-script.js"></script>
    <%@ include file="my-books-styles.jsp" %>
</head>
<body>
<%@ include file="main-page-header.jsp" %>

<script>
    // Fix search form
    document.addEventListener('DOMContentLoaded', function() {
        const searchForm = document.getElementById('searchForm');
        if (searchForm) {
            searchForm.addEventListener('submit', function(e) {
                e.preventDefault();
                const query = document.getElementById('searchInput').value;
                window.location.href = '${pageContext.request.contextPath}/search-results.jsp?query=' + encodeURIComponent(query);
            });
        }
    });
</script>

<div class="user-books-header">
    <div class="container">
        <h1 class="user-books-title">My Library</h1>
        <p class="user-books-subtitle">Manage your reading journey</p>
    </div>
</div>

<div class="main-content">
    <div class="container">
        <div class="user-books-tabs">
            <div class="user-tab active" data-tab="reading">Currently Reading</div>
            <div class="user-tab" data-tab="reserved">Reserved Books</div>
            <div class="user-tab" data-tab="completed">Read Books</div>
        </div>

        <!-- Currently Reading Tab -->
        <div id="reading" class="tab-content active">
            <div class="scroll-container">
                <button class="scroll-nav scroll-nav-left" onclick="scrollSection('reading-scroll', -200)">
                    <svg viewBox="0 0 24 24"><path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/></svg>
                </button>
                <div class="books-scroll" id="reading-scroll">
                    <div class="loading-indicator">
                        <div class="spinner"></div>
                        <p>Loading currently reading books...</p>
                    </div>
                </div>
                <button class="scroll-nav scroll-nav-right" onclick="scrollSection('reading-scroll', 200)">
                    <svg viewBox="0 0 24 24"><path d="M8.59 16.59L10 18l6-6-6-6-1.41 1.41L13.17 12z"/></svg>
                </button>
            </div>
            <div class="empty-state" id="readingEmpty" style="display: none;">
                <div class="empty-state-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
                    </svg>
                </div>
                <h3>No Currently Reading Books</h3>
                <p>You don't have any books in progress at the moment.</p>
            </div>
        </div>

        <!-- Reserved Books Tab -->
        <div id="reserved" class="tab-content">
            <div class="scroll-container">
                <button class="scroll-nav scroll-nav-left" onclick="scrollSection('reserved-scroll', -200)">
                    <svg viewBox="0 0 24 24"><path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/></svg>
                </button>
                <div class="books-scroll" id="reserved-scroll">
                    <div class="loading-indicator">
                        <div class="spinner"></div>
                        <p>Loading reserved books...</p>
                    </div>
                </div>
                <button class="scroll-nav scroll-nav-right" onclick="scrollSection('reserved-scroll', 200)">
                    <svg viewBox="0 0 24 24"><path d="M8.59 16.59L10 18l6-6-6-6-1.41 1.41L13.17 12z"/></svg>
                </button>
            </div>
            <div class="empty-state" id="reservedEmpty" style="display: none;">
                <div class="empty-state-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M4 4v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8.342c0-.53-.21-1.04-.586-1.414l-3.342-3.342A2 2 0 0014.658 3H6c-1.1 0-2 .9-2 2z"/>
                        <path d="M14 3v5h5"/>
                        <path d="M8 12h8"/>
                        <path d="M8 16h8"/>
                    </svg>
                </div>
                <h3>No Reserved Books</h3>
                <p>You don't have any books reserved at the moment.</p>
            </div>
        </div>

        <!-- Read Books Tab -->
        <div id="completed" class="tab-content">
            <div class="scroll-container">
                <button class="scroll-nav scroll-nav-left" onclick="scrollSection('completed-scroll', -200)">
                    <svg viewBox="0 0 24 24"><path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/></svg>
                </button>
                <div class="books-scroll" id="completed-scroll">
                    <div class="loading-indicator">
                        <div class="spinner"></div>
                        <p>Loading read books...</p>
                    </div>
                </div>
                <button class="scroll-nav scroll-nav-right" onclick="scrollSection('completed-scroll', 200)">
                    <svg viewBox="0 0 24 24"><path d="M8.59 16.59L10 18l6-6-6-6-1.41 1.41L13.17 12z"/></svg>
                </button>
            </div>
            <div class="empty-state" id="completedEmpty" style="display: none;">
                <div class="empty-state-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                        <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
                        <path d="M8 7h8"/>
                        <path d="M8 11h8"/>
                        <path d="M8 15h5"/>
                    </svg>
                </div>
                <h3>No Read Books</h3>
                <p>You haven't marked any books as read yet.</p>
            </div>
        </div>
    </div>
</div>

<script>
    function scrollSection(elementId, amount) {
        const element = document.getElementById(elementId);
        element.scrollBy({ left: amount, behavior: 'smooth' });
    }

    function createBookCard(book, status, date) {
        const defaultCovers = [
            '${pageContext.request.contextPath}/images/noCover1.jpg',
            '${pageContext.request.contextPath}/images/noCover2.jpg',
            '${pageContext.request.contextPath}/images/noCover3.jpg',
            '${pageContext.request.contextPath}/images/noCover4.jpg',
            '${pageContext.request.contextPath}/images/noCover5.jpg',
            '${pageContext.request.contextPath}/images/noCover6.jpg',
            '${pageContext.request.contextPath}/images/noCover7.jpg',
            '${pageContext.request.contextPath}/images/noCover8.jpg',
            '${pageContext.request.contextPath}/images/noCover9.jpg',
            '${pageContext.request.contextPath}/images/noCover10.jpg',
            '${pageContext.request.contextPath}/images/noCover11.jpg',
            '${pageContext.request.contextPath}/images/noCover12.jpg'
        ];

        const getRandomDefaultCover = () => {
            return defaultCovers[Math.floor(Math.random() * defaultCovers.length)];
        };

        const imageUrl = book.imageUrl
            ? '/images/' + book.imageUrl
            : getRandomDefaultCover();

        const rating = book.rating || 0;
        const fullStars = Math.floor(rating);
        const hasHalfStar = rating % 1 >= 0.5;
        const stars = '★'.repeat(fullStars) + (hasHalfStar ? '☆' : '') + '☆'.repeat(5 - fullStars - (hasHalfStar ? 1 : 0));

        const statusClass = {
            'reading': 'status-reading',
            'reserved': 'status-reserved',
            'completed': 'status-completed'
        }[status];

        const statusText = {
            'reading': 'Currently Reading',
            'reserved': 'Reserved',
            'completed': 'Read'
        }[status];

        const dateText = date ? new Date(date).toLocaleDateString() : '';
        const dateLabel = {
            'completed': 'Finished',
            'reserved': 'Reserved on'
        }[status];

        const ratingHtml = rating > 0
            ? '<div class="book-rating">' +
            '<span class="stars">' + stars + '</span>' +
            '<span class="rating-text">(' + rating.toFixed(1) + ')</span>' +
            '</div>'
            : '';

        const dateHtml = dateText
            ? '<span class="book-date">' + dateLabel + ': ' + dateText + '</span>'
            : '';

        return '<div class="book-card" onclick="window.location.href=\'${pageContext.request.contextPath}/book-details.jsp?id=' + book.publicId + '\'">' +
            '<div class="book-cover">' +
            '<img src="' + imageUrl + '" alt="' + book.name + '" onerror="this.src=\'' + getRandomDefaultCover() + '\'">' +
            '</div>' +
            '<div class="book-info">' +
            '<h3 class="book-title">' + book.name + '</h3>' +
            '<p class="book-author">by ' + book.author + '</p>' +
            ratingHtml +
            '<div class="book-meta">' +
            '<span class="book-status ' + statusClass + '">' + statusText + '</span>' +
            dateHtml +
            '</div>' +
            '</div>' +
            '</div>';
    }

    function displayBooks(books, status, scrollId, emptyId) {
        const scroll = document.getElementById(scrollId);
        const emptyState = document.getElementById(emptyId);

        if (!books || books.length === 0) {
            scroll.style.display = 'none';
            emptyState.style.display = 'block';
            return;
        }

        scroll.style.display = 'flex';
        emptyState.style.display = 'none';

        const booksHTML = books.map(item => {
            let book, date;

            if (status === 'reserved') {
                book = item.book;
                date = item.reservedDate;
            } else {
                book = item;
                date = status === 'completed' ? item.dateRead : null;
            }

            if (!book) {
                console.error('Invalid book data:', item);
                return '';
            }

            return createBookCard(book, status, date);
        }).filter(html => html !== '').join('');

        scroll.innerHTML = booksHTML;
    }

    document.addEventListener('DOMContentLoaded', function() {
        // Tab switching functionality
        const tabs = document.querySelectorAll('.user-tab');
        tabs.forEach(tab => {
            tab.addEventListener('click', function() {
                // Remove active class from all tabs and content
                tabs.forEach(t => t.classList.remove('active'));
                document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));

                // Add active class to clicked tab and corresponding content
                this.classList.add('active');
                const tabId = this.getAttribute('data-tab');
                document.getElementById(tabId).classList.add('active');
            });
        });

        const segments = window.location.pathname.split("/");
        const userIndex = segments.indexOf("user");
        const username = segments[userIndex + 1];

        if (!username) {
            console.error("Username not found in URL");
            displayError('Could not identify user. Please refresh the page.');
        } else {
            fetch('${pageContext.request.contextPath}/api/user/' + username, {
                method: 'GET',
                credentials: 'include'
            })
                .then(res => {
                    if (!res.ok) throw new Error("User not found");
                    return res.json();
                })
                .then(data => {
                    const user = data.user;
                    displayBooks(user.currentlyReading, 'reading', 'reading-scroll', 'readingEmpty');
                    displayBooks(user.orders, 'reserved', 'reserved-scroll', 'reservedEmpty');
                    displayBooks(user.readBooks, 'completed', 'completed-scroll', 'completedEmpty');
                })
                .catch(err => {
                    console.error(err);
                    displayError('Could not load books. Please try again later.');
                });
        }
    });

    function displayError(message) {
        document.querySelector('.main-content').innerHTML = `
                <div class="container">
                    <div class="error-message">
                        <h3>Error</h3>
                        <p>${message}</p>
                        <button onclick="location.reload()" style="margin-top: 10px; padding: 8px 16px; background: #8b7355; color: white; border: none; border-radius: 4px; cursor: pointer;">
                            Refresh Page
                        </button>
                    </div>
                </div>
            `;
    }
</script>
</body>
</html>