<%--
  Created by IntelliJ IDEA.
  User: elenejobava
  Date: 6/22/25
  Time: 00:46
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
        <a href="main-page.jsp" class="nav-box <%= request.getRequestURI().endsWith("main-page.jsp") ? "active" : "" %>">
            <svg class="nav-icon" viewBox="0 0 24 24">
                <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"></path>
            </svg>
            Home
        </a>

        <div class="browse-dropdown">
            <a href="#" class="nav-box <%= request.getRequestURI().endsWith("genre-books.jsp") ? "active" : "" %>" onclick="toggleDropdown(event)">
                <svg class="nav-icon" viewBox="0 0 24 24">
                    <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"></path>
                </svg>
                Browse
                <svg class="dropdown-arrow" viewBox="0 0 24 24">
                    <path d="M7 10l5 5 5-5z"></path>
                </svg>
            </a>
            <div class="dropdown-content" id="browseDropdown">
                <a href="genre-books.jsp?genre=classics" class="dropdown-item">Classics</a>
                <a href="genre-books.jsp?genre=fiction" class="dropdown-item">Fiction</a>
                <a href="genre-books.jsp?genre=crime" class="dropdown-item">Crime</a>
                <a href="genre-books.jsp?genre=mystery" class="dropdown-item">Mystery</a>
                <a href="genre-books.jsp?genre=romance" class="dropdown-item">Romance</a>
                <a href="genre-books.jsp?genre=memoir" class="dropdown-item">Memoir</a>
                <a href="genre-books.jsp?genre=fantasy" class="dropdown-item">Fantasy</a>
                <a href="genre-books.jsp?genre=horror" class="dropdown-item">Horror</a>
                <a href="genre-books.jsp?genre=history" class="dropdown-item">History</a>
                <a href="genre-books.jsp?genre=poetry" class="dropdown-item">Poetry</a>
                <a href="genre-books.jsp?genre=adventure" class="dropdown-item">Adventure</a>
                <a href="genre-books.jsp?genre=comedy" class="dropdown-item">Comedy</a>
                <a href="genre-books.jsp?genre=philosophy" class="dropdown-item">Philosophy</a>
                <a href="genre-books.jsp?genre=psychology" class="dropdown-item">Psychology</a>
            </div>
        </div>

        <a href="#my-books" class="nav-box">
            <svg class="nav-icon" viewBox="0 0 24 24">
                <path d="M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 4h5v8l-2.5-1.5L6 12V4z"></path>
            </svg>
            My Books
        </a>

        <a href="#search" class="nav-box">
            <svg class="nav-icon" viewBox="0 0 24 24">
                <path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"></path>
            </svg>
            Search
        </a>

        <a href="${pageContext.request.contextPath}/user/${sessionScope.username}" class="nav-box">
            <svg class="nav-icon" viewBox="0 0 24 24">
                <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"></path>
            </svg>
            Profile
        </a>
    </div>
</div>
