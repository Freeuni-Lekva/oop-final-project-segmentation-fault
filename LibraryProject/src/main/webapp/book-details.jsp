<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="main-page-styles.jsp" %>
    <%@ include file="book-details-styles.jsp" %>
    <title>Book Details - Freeuni Library</title>
</head>
<body>
<%@ include file="main-page-header.jsp" %>

<div class="book-details-container">
    <div id="loading" class="loading">Loading book details...</div>
    <div id="error" class="error" style="display: none;">Failed to load book details. Please try again.</div>

    <div class="book-details" id="bookDetails" style="display: none;">
        <div class="book-cover-large">
            <img id="bookImage" src="" alt="Book cover" onerror="handleImageError(this)">
        </div>

        <div class="book-info-details">
            <!-- Header Section -->
            <div class="book-header-section">
                <h1 class="book-title-large" id="bookTitle">Loading...</h1>
                <p class="book-author-large" id="bookAuthor">by Loading...</p>

                <div class="book-rating-large">
                    <span class="stars-large" id="bookStars">☆☆☆☆☆</span>
                    <span class="rating-text-large" id="bookRating">(No rating)</span>
                </div>
            </div>

            <!-- Book Information Grid -->
            <div class="book-metadata-grid">
                <div class="metadata-section">
                    <h3 class="section-title">Book Details</h3>
                    <div class="book-meta-details">
                        <div class="book-meta-item">
                            <svg viewBox="0 0 24 24"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"></path></svg>
                            <span><strong>Genre:</strong> <span id="bookGenre">Loading...</span></span>
                        </div>
                        <div class="book-meta-item">
                            <svg viewBox="0 0 24 24"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"></path></svg>
                            <span><strong>Volume:</strong> <span id="bookVolume">Loading...</span></span>
                        </div>
                        <div class="book-meta-item">
                            <svg viewBox="0 0 24 24"><path d="M17 3H7c-1.1 0-1.99.9-1.99 2L5 21l7-3 7 3V5c0-1.1-.9-2-2-2z"></path></svg>
                            <span><strong>Date:</strong> <span id="bookDate">Loading...</span></span>
                        </div>
                    </div>
                </div>

                <div class="availability-section">
                    <h3 class="section-title">Availability</h3>
                    <div class="availability-status" id="availabilityStatus">
                        Loading availability...
                    </div>
                    <div class="availability-details">
                        <p><strong>Available Copies:</strong> <span id="availableCopies">-</span></p>
                        <p><strong>Status:</strong> <span id="bookStatus">-</span></p>
                    </div>
                </div>
            </div>

            <!-- Actions Section -->
            <div class="actions-section">
                <h3 class="section-title">Actions</h3>
                <% if ("BOOKKEEPER".equals(session.getAttribute("role"))) { %>
                    <!-- Admin Actions -->
                    <div class="action-buttons">
                        <button class="delete-button" id="deleteButton" onclick="deleteBook()">
                            <svg viewBox="0 0 24 24" class="nav-icon">
                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                            </svg>
                            Delete Book
                        </button>
                        <button class="back-to-admin-button" onclick="goBackToAdmin()">
                            <svg viewBox="0 0 24 24" class="nav-icon">
                                <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"></path>
                            </svg>
                            Back to Admin Panel
                        </button>
                    </div>
                <% } else { %>
                    <!-- User Actions -->
                    <div class="action-buttons">
                        <button class="reserve-button" id="reserveButton" disabled>
                            <svg viewBox="0 0 24 24" class="nav-icon">
                                <path d="M17 3H7c-1.1 0-1.99.9-1.99 2L5 21l7-3 7 3V5c0-1.1-.9-2-2-2z"></path>
                            </svg>
                            <span id="reserveButtonText">Reserve Book</span>
                        </button>
                        <button class="review-button" onclick="toggleReviewForm()">
                            <svg viewBox="0 0 24 24" class="nav-icon">
                                <path d="M21 5h-2.64l1.14-3.14L17.15 1l-1.46 4H3v14h18V5zm-5 12.86L12 15.44 8 17.86l1.29-5.52L5 8.62h5.48L12 3.43l1.52 5.19H19l-4.29 3.72 1.29 5.51z"></path>
                            </svg>
                            Write a Review
                        </button>
                    </div>

                    <div class="review-form" id="reviewForm">
                        <form id="reviewFormSubmit" action="<%= request.getContextPath() %>/api/reviews/submit" method="POST">
                            <input type="hidden" name="bookId" id="reviewBookId" value="">
                            <input type="hidden" name="rating" id="selectedRating" value="">
                            
                            <textarea name="reviewText" placeholder="Write your review here..." required></textarea>
                            
                            <div class="star-rating-container">
                                <label class="rating-label">Rating:</label>
                                <div class="star-rating" id="starRating">
                                    <span class="star" data-rating="1">☆</span>
                                    <span class="star" data-rating="2">☆</span>
                                    <span class="star" data-rating="3">☆</span>
                                    <span class="star" data-rating="4">☆</span>
                                    <span class="star" data-rating="5">☆</span>
                                </div>
                                <span class="rating-text" id="ratingText">Click to rate</span>
                            </div>
                            
                            <button type="submit" class="submit-review">Submit Review</button>
                        </form>
                    </div>
                <% } %>
            </div>

            <!-- Description Section -->
            <div class="description-section">
                <h3 class="section-title">Description</h3>
                <div class="book-description" id="bookDescription">
                    Loading description...
                </div>
            </div>
        </div>
    </div>

    <!-- Reviews Section -->
    <div class="reviews-section" id="reviewsSection" style="display: none;">
        <h3 class="section-title">Reviews</h3>
        <div id="reviewsLoading" class="reviews-loading">Loading reviews...</div>
        <div id="reviewsContainer" class="reviews-container"></div>
        <div id="noReviews" class="no-reviews" style="display: none;">
            <% if ("BOOKKEEPER".equals(session.getAttribute("role"))) { %>
                <p>No reviews available for this book.</p>
            <% } else { %>
                <p>No reviews yet. Be the first to write a review!</p>
            <% } %>
        </div>
    </div>
</div>

<script>
    let currentBook = null;

    const defaultCovers = [
        '<%= request.getContextPath() %>/images/noCover1.jpg',
        '<%= request.getContextPath() %>/images/noCover2.jpg',
        '<%= request.getContextPath() %>/images/noCover3.jpg',
        '<%= request.getContextPath() %>/images/noCover4.jpg',
        '<%= request.getContextPath() %>/images/noCover5.jpg',
        '<%= request.getContextPath() %>/images/noCover6.jpg',
        '<%= request.getContextPath() %>/images/noCover7.jpg',
        '<%= request.getContextPath() %>/images/noCover8.jpg',
        '<%= request.getContextPath() %>/images/noCover9.jpg',
        '<%= request.getContextPath() %>/images/noCover10.jpg',
        '<%= request.getContextPath() %>/images/noCover11.jpg',
        '<%= request.getContextPath() %>/images/noCover12.jpg'
    ];

    function getRandomDefaultCover() {
        return defaultCovers[Math.floor(Math.random() * defaultCovers.length)];
    }

    function handleImageError(img) {
        img.src = getRandomDefaultCover();
    }

    function capitalizeFirstLetter(string) {
        if (!string) return string;
        return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase();
    }

    function formatVolume(volume) {
        if (!volume || volume === 'N/A') return 'N/A';
        return volume + ' Pages';
    }

    function getBookIdFromUrl() {
        const urlParams = new URLSearchParams(window.location.search);
        // Check for both 'id' and 'bookId' parameters (admin panel uses 'bookId')
        const bookId = urlParams.get('id') || urlParams.get('bookId');
        if (!bookId) {
            const pathParts = window.location.pathname.split('/');
            return pathParts[pathParts.length - 1];
        }
        return bookId;
    }

    function renderStars(rating) {
        const fullStars = Math.floor(rating);
        const hasHalfStar = rating % 1 >= 0.5;
        const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);

        return '★'.repeat(fullStars) + (hasHalfStar ? '☆' : '') + '☆'.repeat(emptyStars);
    }

    function loadBookDetails() {
        const bookId = getBookIdFromUrl();

        if (!bookId) {
            showError('No book ID provided');
            return;
        }

        fetch('<%= request.getContextPath() %>/api/books/details/' + encodeURIComponent(bookId))
            .then(response => {
                console.log('Book details response status:', response.status);
                console.log('Book details response ok:', response.ok);
                if (!response.ok) {
                    return response.text().then(text => {
                        console.error('Server error response:', text);
                        throw new Error('Failed to fetch book details: ' + response.status + ' - ' + text);
                    });
                }
                return response.json();
            })
            .then(book => {
                console.log('Successfully loaded book details:', book);
                currentBook = book;
                displayBookDetails(book);
                loadBookReviews(book.publicId);
                // Check reservation status only if reserve button exists (for regular users)
                const reserveButton = document.getElementById('reserveButton');
                if (reserveButton) {
                    return checkReservationStatus(book.publicId);
                }
                return Promise.resolve();
            })
            .catch(error => {
                console.error('Error loading book details:', error);
                showError('Failed to load book details. Please try again.');
            });
    }

    function checkReservationStatus(bookId) {
        return fetch('<%= request.getContextPath() %>/api/books/check-reservation/' + encodeURIComponent(bookId))
            .then(response => response.json())
            .then(data => {
                updateReserveButtonState(data.reserved);
                return data.reserved;
            })
            .catch(error => {
                console.error('Error checking reservation status:', error);
                // Don't change button state if check fails
                return false;
            });
    }

    function updateReserveButtonState(isReserved) {
        const reserveButton = document.getElementById('reserveButton');
        const reserveButtonText = document.getElementById('reserveButtonText');

        // Safety check: if elements don't exist (admin users), return early
        if (!reserveButton || !reserveButtonText) {
            return;
        }

        // Clear all existing classes and states first
        reserveButton.classList.remove('cancel-button', 'reserved');
        
        if (isReserved) {
            reserveButtonText.textContent = 'Cancel Reservation';
            reserveButton.disabled = false;
            reserveButton.classList.add('cancel-button');
            reserveButton.onclick = cancelReservation;
        } else {
            // Only set to reserve state if book is available
            if (currentBook && currentBook.currentAmount > 0) {
                reserveButtonText.textContent = 'Reserve Book';
                reserveButton.disabled = false;
                reserveButton.onclick = function(e) {
                    e.preventDefault();
                    showReservationModal();
                };
            } else {
                reserveButtonText.textContent = 'Reserve in Advance';
                reserveButton.disabled = false;
                reserveButton.onclick = function(e) {
                    e.preventDefault();
                    showReservationModal();
                };
            }
        }
    }

    function refreshBookRatingAndReviews() {
        if (!currentBook || !currentBook.publicId) return;
        
        // Silently fetch updated book data to get new rating
        fetch('<%= request.getContextPath() %>/api/books/details/' + encodeURIComponent(currentBook.publicId))
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to fetch updated book data');
                }
                return response.json();
            })
            .then(book => {
                // Update the book rating display without showing loading states
                currentBook = book;
                const rating = book.rating || 0;
                document.getElementById('bookStars').textContent = renderStars(rating);
                document.getElementById('bookRating').textContent = rating > 0 ?
                    '(' + rating.toFixed(1) + ')' : '(No rating)';
                
                // Also refresh the reviews to show the new review
                loadBookReviews(book.publicId);
            })
            .catch(error => {
                console.error('Error refreshing book rating:', error);
                loadBookDetails();
            });
    }

    function loadBookReviews(bookId) {
        if (!bookId) return;

        fetch('<%= request.getContextPath() %>/api/books/book/' + encodeURIComponent(bookId))
            .then(response => {
                if (!response.ok) {
                    document.getElementById('reviewsLoading').style.display = 'none';
                    document.getElementById('noReviews').style.display = 'block';
                    document.getElementById('reviewsSection').style.display = 'block';
                    return;
                }
                return response.json();
            })
            .then(reviews => {
                if (reviews) {
                    displayReviews(reviews);
                }
            })
            .catch(error => {
                console.error('Error loading reviews:', error);
                document.getElementById('reviewsLoading').style.display = 'none';
                document.getElementById('noReviews').style.display = 'block';
                document.getElementById('reviewsSection').style.display = 'block';
            });
    }

    function displayReviews(reviews) {
        const reviewsSection = document.getElementById('reviewsSection');
        const reviewsLoading = document.getElementById('reviewsLoading');
        const reviewsContainer = document.getElementById('reviewsContainer');
        const noReviews = document.getElementById('noReviews');

        reviewsSection.style.display = 'block';
        reviewsLoading.style.display = 'none';

        if (!reviews || reviews.length === 0) {
            noReviews.style.display = 'block';
            reviewsContainer.innerHTML = '';
            return;
        }

        noReviews.style.display = 'none';

        let reviewsHtml = '';
        reviews.forEach(function(review) {
            const author = escapeHtml(review.username || 'Anonymous');
            const rating = renderStars(review.rating || 0);
            const date = formatDate(review.date || review.createdAt || review.reviewDate || Date.now());
            const text = escapeHtml(review.comment || '');

            reviewsHtml += '<div class="review-item">' +
                '<div class="review-header">' +
                '<div class="review-author">' + author + '</div>' +
                '<div class="review-rating">' + rating + '</div>' +
                '<div class="review-date">' + date + '</div>' +
                '</div>' +
                '<div class="review-text">' + text + '</div>' +
                '</div>';
        });

        reviewsContainer.innerHTML = reviewsHtml;
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function formatDate(dateString) {
        try {
            const date = new Date(dateString);
            return date.toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'short',
                day: 'numeric'
            });
        } catch (e) {
            return 'Unknown date';
        }
    }

    function displayBookDetails(book) {
        document.getElementById('loading').style.display = 'none';
        document.getElementById('error').style.display = 'none';
        document.getElementById('bookDetails').style.display = 'block';

        const imageSrc = book.imageUrl ?
            '<%= request.getContextPath() %>/images/' + book.imageUrl :
            getRandomDefaultCover();
        document.getElementById('bookImage').src = imageSrc;
        document.getElementById('bookImage').alt = book.name || 'Book cover';

        document.getElementById('bookTitle').textContent = book.name || 'Unknown Title';
        document.getElementById('bookAuthor').textContent = 'by ' + (book.author || 'Unknown Author');
        document.getElementById('bookGenre').textContent = capitalizeFirstLetter(book.genre) || 'Unknown Genre';
        document.getElementById('bookVolume').textContent = formatVolume(book.volume);
        document.getElementById('bookDate').textContent = book.date || 'Unknown Date';

        const rating = book.rating || 0;
        document.getElementById('bookStars').textContent = renderStars(rating);
        document.getElementById('bookRating').textContent = rating > 0 ?
            '(' + rating.toFixed(1) + ')' : '(No rating)';

        const availabilityElement = document.getElementById('availabilityStatus');

        if (book.currentAmount && book.currentAmount > 0) {
            availabilityElement.textContent = 'Available to reserve';
            availabilityElement.className = 'availability-status available';
            document.getElementById('bookStatus').textContent = 'Available';
        } else {
            availabilityElement.textContent = 'Currently unavailable';
            availabilityElement.className = 'availability-status unavailable';
            document.getElementById('bookStatus').textContent = 'Unavailable';
        }

        document.getElementById('availableCopies').textContent = book.currentAmount || '0';
        document.getElementById('bookDescription').textContent =
            book.description || 'No description available.';
        
        // Set the review book ID if the field exists (only for non-admin users)
        const reviewBookIdField = document.getElementById('reviewBookId');
        if (reviewBookIdField) {
            const bookPublicId = book.publicId || '';
            reviewBookIdField.value = bookPublicId;
        }
        
        // Don't set button state here - let checkReservationStatus handle it
    }

    function showError(message) {
        document.getElementById('loading').style.display = 'none';
        document.getElementById('bookDetails').style.display = 'none';
        document.getElementById('error').style.display = 'block';
        document.getElementById('error').textContent = message;
    }

    function reserveBook(days) {
    if (!currentBook || !currentBook.publicId) {
        alert('Unable to reserve book. Please refresh the page and try again.');
        return;
    }

    fetch('<%= request.getContextPath() %>/api/user/reserve', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ bookId: currentBook.publicId, duration: days })
    })
        .then(response => {
            return response.json().then(data => {
                if (!response.ok) {
                    throw new Error(data.message || 'Failed to reserve book');
                }
                return data;
            });
        })
        .then(data => {
            console.log(data)
            alert(data.message);
            updateReserveButtonState(true);
        })
        .catch(error => {
            console.error('Error reserving book:', error);
            alert(error.message || 'Failed to reserve book. Please try again.');
        });
}

    function cancelReservation() {
        if (!currentBook || !currentBook.publicId) {
            alert('Unable to cancel reservation. Please refresh the page and try again.');
            return;
        }

        fetch('<%= request.getContextPath() %>/api/user/cancel-reservation', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ bookId: currentBook.publicId })
        })
            .then(response => {
                return response.json().then(data => {
                    if (!response.ok) {
                        throw new Error(data.message || 'Failed to cancel reservation');
                    }
                    return data;
                });
            })
            .then(data => {
                alert('Reservation cancelled successfully!');
                updateReserveButtonState(false);
            })
            .catch(error => {
                console.error('Error cancelling reservation:', error);
                alert(error.message || 'Failed to cancel reservation. Please try again.');
            });
    }

    function toggleReviewForm() {
        document.getElementById('reviewForm').classList.toggle('show');
    }

    function initializeStarRating() {
        const stars = document.querySelectorAll('.star');
        const ratingText = document.getElementById('ratingText');
        const selectedRatingInput = document.getElementById('selectedRating');
        let currentRating = 0;

        stars.forEach((star, index) => {
            // Mouse hover effect
            star.addEventListener('mouseenter', function() {
                resetStars();
                highlightStars(index + 1);
                const rating = index + 1;
                ratingText.textContent = getRatingText(rating);
            });

            // Click to select rating
            star.addEventListener('click', function() {
                currentRating = index + 1;
                selectedRatingInput.value = currentRating;
                resetStars();
                fillStars(currentRating);
                ratingText.textContent = getRatingText(currentRating);
                ratingText.classList.add('selected');
            });
        });

        // Reset to current rating on mouse leave
        document.getElementById('starRating').addEventListener('mouseleave', function() {
            resetStars();
            if (currentRating > 0) {
                fillStars(currentRating);
                ratingText.textContent = getRatingText(currentRating);
                ratingText.classList.add('selected');
            } else {
                ratingText.textContent = 'Click to rate';
                ratingText.classList.remove('selected');
            }
        });

        function resetStars() {
            stars.forEach(star => {
                star.classList.remove('filled', 'hovered');
            });
        }

        function highlightStars(rating) {
            for (let i = 0; i < rating; i++) {
                stars[i].classList.add('hovered');
            }
        }

        function fillStars(rating) {
            for (let i = 0; i < rating; i++) {
                stars[i].classList.add('filled');
            }
        }

        function getRatingText(rating) {
            const ratingTexts = {
                1: '1 Star - Poor',
                2: '2 Stars - Fair', 
                3: '3 Stars - Good',
                4: '4 Stars - Very Good',
                5: '5 Stars - Excellent'
            };
            return ratingTexts[rating] || 'Click to rate';
        }
    }

    // Admin functions
    function deleteBook() {
        if (!currentBook || !currentBook.publicId) {
            alert('Unable to delete book. Please refresh the page and try again.');
            return;
        }

        if (!confirm('Are you sure you want to delete this book? This action cannot be undone.')) {
            return;
        }

        const deleteButton = document.getElementById('deleteButton');
        deleteButton.disabled = true;
        deleteButton.textContent = 'Deleting...';

        fetch('<%= request.getContextPath() %>/api/bookkeeper/delete-book', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ bookPublicId: currentBook.publicId }),
            credentials: "include"
        })
        .then(response => {
            return response.json().then(data => {
                if (response.ok && data.status === 'success') {
                    alert(data.message || 'Book deleted successfully');
                    // Redirect back to admin panel
                    window.location.href = '<%= request.getContextPath() %>/bookkeeper-admin.jsp';
                } else {
                    throw new Error(data.message || 'Failed to delete book');
                }
            });
        })
        .catch(error => {
            console.error('Error deleting book:', error);
            alert('Failed to delete book: ' + error.message);
            deleteButton.disabled = false;
            deleteButton.textContent = 'Delete Book';
        });
    }

    function goBackToAdmin() {
        window.location.href = '<%= request.getContextPath() %>/bookkeeper-admin.jsp';
    }

    function showReservationModal() {
        var modal = document.getElementById('reservationModal');
        modal.classList.add('show');
        document.getElementById('reservationDays').value = 7;
    }
    function hideReservationModal() {
        var modal = document.getElementById('reservationModal');
        modal.classList.remove('show');
    }

    document.addEventListener('DOMContentLoaded', function() {
        loadBookDetails();
        
        // Check if review form exists (only for non-admin users)
        const reviewForm = document.getElementById('reviewFormSubmit');
        if (reviewForm) {
            initializeStarRating();
            
            reviewForm.addEventListener('submit', function(event) {
                event.preventDefault();
                
                const formData = new FormData(this);
                
                // Check if bookId is present
                const bookId = formData.get('bookId');
                if (!bookId || bookId.trim() === '') {
                    alert('Error: Book ID is missing. Please refresh the page and try again.');
                    return;
                }
                
                // Check if reviewText is present
                const reviewText = formData.get('reviewText');
                if (!reviewText || reviewText.trim() === '') {
                    alert('Error: Please write a review.');
                    return;
                }
                
                // Check if rating is present
                const rating = formData.get('rating');
                if (!rating || rating === '') {
                    alert('Error: Please select a star rating.');
                    return;
                }
                
                // Convert FormData to URLSearchParams for proper encoding
                const urlParams = new URLSearchParams();
                for (let [key, value] of formData.entries()) {
                    urlParams.append(key, value);
                }
                
                fetch(this.action, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: urlParams
                })
                    .then(response => {
                        return response.json().then(data => {
                        if (!response.ok) {
                                throw new Error(data.message || 'Failed to submit review');
                        }
                            return data;
                        });
                    })
                    .then(data => {
                        if (data.success) {
                        alert('Review submitted successfully!');
                        document.getElementById('reviewForm').classList.remove('show');
                        this.reset();
                        // Reset star rating
                        document.querySelectorAll('.star').forEach(star => {
                            star.classList.remove('filled');
                        });
                        document.getElementById('selectedRating').value = '';
                        document.getElementById('ratingText').textContent = 'Click to rate';
                        document.getElementById('ratingText').classList.remove('selected');
                        
                        // Instantly refresh rating and reviews without loading states
                        refreshBookRatingAndReviews();
                        } else {
                            throw new Error(data.message || 'Failed to submit review');
                        }
                    })
                    .catch(error => {
                        console.error('Error submitting review:', error);
                        alert(error.message);
                    });
            });
        }

        // Modal event listeners
        document.getElementById('closeReservationModal').onclick = hideReservationModal;
        document.getElementById('cancelReservationBtn').onclick = hideReservationModal;
        document.getElementById('confirmReservationBtn').onclick = function() {
            const days = parseInt(document.getElementById('reservationDays').value, 10);
            if (isNaN(days) || days < 1 || days > 30) {
                alert('Please enter a valid number of days (1-30).');
                return;
            }
            hideReservationModal();
            console.log("Days: " + days)
            reserveBook(days);
        };
    });

    // Navigation functionality
    function toggleDropdown(event) {
        event.preventDefault();
        const dropdown = document.getElementById('browseDropdown');
        dropdown.classList.toggle('show');
    }

    // Close dropdown when clicking outside
    document.addEventListener('click', function(event) {
        var dropdown = document.getElementById('browseDropdown');
        var browseButton = event.target.closest('.browse-dropdown');

        if (!browseButton) {
            dropdown.classList.remove('show');
        }
    });

    // Handle dropdown item clicks
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
</script>
<!-- Reservation Duration Modal -->
<div id="reservationModal" class="modal" style="display:none;">
    <div class="modal-content reservation-modal-content">
        <button class="close-modal-btn" id="closeReservationModal" aria-label="Close">&times;</button>
        <h2 class="modal-title">Reserve Book</h2>
        <p class="modal-desc">How many days do you want to reserve this book for?</p>
        <input type="number" id="reservationDays" min="1" max="30" value="7" required class="modal-input" />
        <div class="modal-actions">
            <button id="confirmReservationBtn" type="button" class="modal-btn confirm-btn">Confirm</button>
            <button id="cancelReservationBtn" type="button" class="modal-btn cancel-btn">Cancel</button>
        </div>
    </div>
</div>
<style>
/* Reservation Modal Styles */
.modal {
    display: none;
    position: fixed;
    z-index: 2000;
    left: 0; top: 0; width: 100vw; height: 100vh;
    background: rgba(30, 30, 40, 0.65);
    backdrop-filter: blur(4px);
    justify-content: center;
    align-items: center;
    transition: all 0.3s ease;
}

.modal.show { 
    display: flex !important;
    animation: modalBackdropShow 0.3s ease;
}

.reservation-modal-content {
    background: linear-gradient(to bottom, #ffffff, #fefdf8);
    color: #2d3748;
    font-family: 'Poppins', sans-serif;
    padding: 2.5rem;
    border-radius: 20px;
    box-shadow: 
        0 10px 25px rgba(30,30,60,0.1),
        0 6px 12px rgba(0,0,0,0.08),
        0 0 0 1px rgba(255,255,255,0.1);
    max-width: 400px;
    width: 90vw;
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    transform: translateY(20px);
    opacity: 0;
    animation: modalContentShow 0.4s ease forwards;
}

@keyframes modalBackdropShow {
    from { background: rgba(30, 30, 40, 0); backdrop-filter: blur(0px); }
    to { background: rgba(30, 30, 40, 0.65); backdrop-filter: blur(4px); }
}

@keyframes modalContentShow {
    to { transform: translateY(0); opacity: 1; }
}

.close-modal-btn {
    position: absolute;
    right: 20px;
    top: 20px;
    background: #f7f5f2;
    border: none;
    width: 32px;
    height: 32px;
    border-radius: 16px;
    font-size: 20px;
    color: #8b7355;
    cursor: pointer;
    transition: all 0.2s ease;
    display: flex;
    align-items: center;
    justify-content: center;
}

.close-modal-btn:hover {
    background: #f2ede6;
    color: #d32f2f;
    transform: rotate(90deg);
}

.modal-title {
    font-size: 1.5rem;
    font-weight: 600;
    margin-bottom: 1rem;
    color: #5a4d25;
    letter-spacing: -0.01em;
}

.modal-desc {
    font-size: 1rem;
    margin-bottom: 1.5rem;
    color: #718096;
    text-align: center;
    line-height: 1.5;
}

.modal-input {
    width: 100px;
    padding: 0.75rem 1rem;
    font-size: 1.1rem;
    border: 2px solid #e6cb58;
    border-radius: 12px;
    outline: none;
    margin-bottom: 1.5rem;
    text-align: center;
    transition: all 0.2s ease;
    background: #fefdf8;
    color: #5a4d25;
    font-family: 'Poppins', sans-serif;
}

.modal-input:focus {
    border-color: #f5c45e;
    box-shadow: 0 0 0 3px rgba(245, 196, 94, 0.15);
    background: #ffffff;
}

.modal-input::-webkit-inner-spin-button,
.modal-input::-webkit-outer-spin-button {
    opacity: 1;
    height: 24px;
}

.modal-actions {
    display: flex;
    gap: 1rem;
    width: 100%;
    justify-content: center;
}

.modal-btn {
    padding: 0.75rem 1.5rem;
    border-radius: 12px;
    border: none;
    font-size: 1rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
    font-family: 'Poppins', sans-serif;
}

.confirm-btn {
    background: linear-gradient(135deg, #F5C45E 0%, #E8B850 100%);
    color: #ffffff;
    box-shadow: 0 4px 12px rgba(245, 196, 94, 0.3);
}

.confirm-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(245, 196, 94, 0.4);
    background: linear-gradient(135deg, #E8B850 0%, #DBAC48 100%);
}

.confirm-btn:active {
    transform: translateY(0);
}

.cancel-btn {
    background: #f7f5f2;
    color: #8b7355;
    border: 1px solid #e6cb58;
}

.cancel-btn:hover {
    background: #f2ede6;
    color: #d32f2f;
    border-color: #d32f2f;
}

@media (max-width: 500px) {
    .reservation-modal-content {
        padding: 2rem 1.5rem;
        max-width: 95vw;
    }
    .modal-title { 
        font-size: 1.25rem; 
    }
    .modal-desc {
        font-size: 0.95rem;
    }
    .modal-btn {
        padding: 0.75rem 1.25rem;
        font-size: 0.95rem;
    }
}
</style>
</body>
</html>