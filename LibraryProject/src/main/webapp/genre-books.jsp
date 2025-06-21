<%--
  Created by IntelliJ IDEA.
  User: elenejobava
  Date: 6/21/25
  Time: 12:00
  Genre books page - Using include approach
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<%@ include file="genre-data.jsp" %>
<%
  String genre = request.getParameter("genre");
  if (genre == null) genre = "fiction";
  Map<String, String> currentGenre = genreData.getOrDefault(genre, genreData.get("fiction"));
  String genreDisplay = currentGenre.get("name");
  String genreDescription = currentGenre.get("description");
%>
<!DOCTYPE html>
<html>
<head>
  <%@ include file="main-page-styles.jsp" %>
  <style>
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
      background: linear-gradient(45deg, #e7edec, #a7cdcd);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 48px;
      color: white;
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
  </style>
</head>
<body>
<div class="top-line1">
  <h2>Explore the world's knowledge, cultures and ideas</h2>
</div>

<div class="top-line2">
  <div class="library-title">
    <span class="pale-text">Freeuni</span>
    <span class="highlight-text">Library</span>
    <img src="images/cropped_circle_image.png" alt="owl" class="owl-icon">
  </div>

  <div class="nav-container">
    <a href="main-page.jsp" class="nav-box">
      <svg class="nav-icon" viewBox="0 0 24 24">
        <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
      </svg>
      Home
    </a>

    <div class="browse-dropdown">
      <a href="#" class="nav-box active" onclick="toggleDropdown(event)">
        <svg class="nav-icon" viewBox="0 0 24 24">
          <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
        </svg>
        Browse
        <svg class="dropdown-arrow" viewBox="0 0 24 24">
          <path d="M7 10l5 5 5-5z"/>
        </svg>
      </a>
      <div class="dropdown-content" id="browseDropdown">
        <a href="genre-books.jsp?genre=fiction" class="dropdown-item">Fiction</a>
        <a href="genre-books.jsp?genre=non-fiction" class="dropdown-item">Non-Fiction</a>
        <a href="genre-books.jsp?genre=mystery" class="dropdown-item">Mystery & Thriller</a>
        <a href="genre-books.jsp?genre=romance" class="dropdown-item">Romance</a>
        <a href="genre-books.jsp?genre=science-fiction" class="dropdown-item">Science Fiction</a>
        <a href="genre-books.jsp?genre=fantasy" class="dropdown-item">Fantasy</a>
        <a href="genre-books.jsp?genre=biography" class="dropdown-item">Biography</a>
        <a href="genre-books.jsp?genre=history" class="dropdown-item">History</a>
        <a href="genre-books.jsp?genre=self-help" class="dropdown-item">Self-Help</a>
        <a href="genre-books.jsp?genre=children" class="dropdown-item">Children's Books</a>
      </div>
    </div>

    <a href="#my-books" class="nav-box">
      <svg class="nav-icon" viewBox="0 0 24 24">
        <path d="M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 4h5v8l-2.5-1.5L6 12V4z"/>
      </svg>
      My Books
    </a>

    <a href="#search" class="nav-box">
      <svg class="nav-icon" viewBox="0 0 24 24">
        <path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
      </svg>
      Search
    </a>

    <a href="#profile" class="nav-box">
      <svg class="nav-icon" viewBox="0 0 24 24">
        <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
      </svg>
      Profile
    </a>
  </div>
</div>

<div class="genre-header">
  <h1 class="genre-title"><%= genreDisplay %></h1>
  <p class="genre-description"><%= genreDescription %></p>
</div>

<div class="main-content">
  <div class="books-container">
    <a href="main-page.jsp" class="back-link">
      <svg class="back-arrow" viewBox="0 0 24 24">
        <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
      </svg>
      Back to Home
    </a>

    <div class="filter-section">
      <div class="filter-left">
        <select class="filter-select">
          <option>Sort by Popularity</option>
          <option>Sort by Rating</option>
          <option>Sort by Date</option>
          <option>Sort by Title</option>
        </select>
      </div>
      <div class="results-count">
        Showing 12 books in <%= genreDisplay %>
      </div>
    </div>

    <div class="books-grid">
      //aq wignebs gamovitan mere
    </div>
  </div>
</div>

<script>
  function toggleDropdown(event) {
    event.preventDefault();
    const dropdown = document.getElementById('browseDropdown');
    dropdown.classList.toggle('show');
  }

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
  }
</script>

</body>
</html>