<%--
  Created by IntelliJ IDEA.
  User: elenejobava
  Date: 6/22/25
  Time: 00:35
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

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
