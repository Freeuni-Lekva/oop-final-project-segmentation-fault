<%--
  Created by IntelliJ IDEA.
  User: elenejobava
  Date: 7/9/25
  Time: 21:30
  To change this template use File | Settings | File Templates.
--%>
<style>
    .search-results-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 0 20px;
    }

    .search-filters {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        padding: 20px;
        background: #f8fafa;
        border-radius: 8px;
    }

    .filter-group {
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .filter-group label {
        font-weight: 500;
        color: #4a5568;
    }

    .filter-group select {
        padding: 8px 12px;
        border: 1px solid #e2e6e5;
        border-radius: 6px;
        background: white;
        color: #4a5568;
        font-size: 14px;
        cursor: pointer;
    }

    .results-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
        gap: 20px;
        margin-top: 30px;
    }

    .book-card {
        background: white;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        overflow: hidden;
        transition: all 0.2s ease;
        cursor: pointer;
        position: relative;
    }

    .book-card:hover {
        transform: translateY(-3px);
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
    }

    .book-cover {
        width: 100%;
        height: 220px;
        position: relative;
        overflow: hidden;
        background: #f8f9fa;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .book-cover img {
        max-width: 90%;
        max-height: 90%;
        object-fit: contain;
        object-position: center;
        display: block;
    }

    .book-info {
        padding: 16px 14px;
    }

    .book-title {
        font-family: 'Poppins', sans-serif;
        font-size: 14px;
        font-weight: 600;
        color: #2d3748;
        margin: 0 0 6px 0;
        line-height: 1.3;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
    }

    .book-author {
        color: #718096;
        font-size: 12px;
        margin: 0 0 8px 0;
        font-weight: 400;
    }

    .book-rating {
        display: flex;
        align-items: center;
        gap: 4px;
        margin-bottom: 8px;
    }

    .stars {
        color: #ffd700;
        font-size: 12px;
        font-weight: bold;
    }

    .rating-text {
        color: #718096;
        font-size: 10px;
    }

    .book-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 10px;
        color: #718096;
        border-top: 1px solid #e2e8f0;
        padding-top: 8px;
    }

    .book-available.available {
        color: #38a169;
    }

    .book-available.unavailable {
        color: #e53e3e;
    }

    .loading-indicator {
        text-align: center;
        padding: 40px 20px;
        color: #718096;
    }

    .search-results-container h2 {
        font-size: 22px;
        color: #5a4d25;
        font-family: 'Poppins', sans-serif;
        font-weight: 600;
        margin-bottom: 20px;
    }

    .spinner {
        width: 40px;
        height: 40px;
        border: 4px solid #e2e8f0;
        border-top: 4px solid #a7cdcd;
        border-radius: 50%;
        animation: spin 1s linear infinite;
        margin: 0 auto 20px;
    }

    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
    }

    .no-results, .error-message {
        grid-column: 1 / -1;
        text-align: center;
        padding: 60px 20px;
        color: #718096;
        font-size: 18px;
        background: #f8fafa;
        border-radius: 8px;
    }

    .error-message {
        color: #e53e3e;
        background: #fed7d7;
    }

    @media (max-width: 768px) {
        .search-filters {
            flex-direction: column;
            gap: 15px;
            align-items: stretch;
        }

        .filter-group {
            justify-content: space-between;
        }

        .results-grid {
            grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
            gap: 16px;
        }
    }

    @media (max-width: 480px) {
        .results-grid {
            grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
            gap: 12px;
        }

        .book-cover {
            height: 180px;
        }
    }


</style>