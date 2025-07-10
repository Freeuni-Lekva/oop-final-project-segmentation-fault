document.addEventListener('DOMContentLoaded', function() {
  loadUsersList();
  loadBooksList();
});

// Auto-refresh book list when returning to admin panel (e.g., after deleting a book in another tab)
document.addEventListener('visibilitychange', function() {
  if (!document.hidden) {
    // Page became visible again - refresh the book list in case books were deleted
    if (document.getElementById('book-collection').classList.contains('active')) {
      console.log('Admin panel became visible - refreshing book list');
      loadBooksList();
    }
  }
});

// Also refresh when window gains focus
window.addEventListener('focus', function() {
  if (document.getElementById('book-collection').classList.contains('active')) {
    console.log('Admin panel gained focus - refreshing book list');
    loadBooksList();
  }
}); 