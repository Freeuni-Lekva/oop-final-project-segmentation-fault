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
  <%@ include file="genre-styles.jsp" %>
  <title></title></head>
<body>
  <%@ include file="main-page-header.jsp" %>
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
          <select class="filter-select">
            <option>Sort by Rating</option>
            <option>Sort by Date</option>
            <option>Sort by Title</option>
          </select>
        </label>
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

<script src="dropdown-script.js"></script>

</body>
</html>