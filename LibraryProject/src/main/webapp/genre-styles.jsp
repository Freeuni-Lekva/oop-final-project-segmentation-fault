<style>
    * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
    }

    body {
        font-family: 'Poppins', sans-serif;
        background-color: #f8fafa;
        color: #2d3748;
        line-height: 1.6;
    }

    .genre-header {
        position: relative;
        width: 100%;
        height: 190px;
        background-image: url('images/image1.jpg');
        background-size: cover;
        background-position: center 25%;
        background-repeat: no-repeat;
        overflow: hidden;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        align-items: center;
        padding: 20px;
    }

    .genre-title {
        font-family: 'Playfair Display', serif;
        font-size: 2.8rem;
        font-weight: 400;
        margin: 0;
        color: #ffffff;
        letter-spacing: -0.5px;
        text-align: center;
        text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
    }

    .genre-description {
        font-size: 15px;
        font-weight: 400;
        font-style: italic;
        max-width: 800px;
        margin: 0;
        line-height: 1.6;
        opacity: 1;
        text-align: center;
        color: #ffffff;
        text-shadow:
                0 0 5px #ffffff,
                0 0 15px #ffaa00,
                0 0 20px #ff880033;
    }

    .books-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 0 20px;
    }

    .books-grid {
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

    .book-cover-fallback {
        width: 100%;
        height: 100%;
        background: linear-gradient(135deg, #e7edec 0%, #a7cdcd 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 40px;
        color: white;
        position: absolute;
        top: 0;
        left: 0;
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

    .book-summary {
        color: #4a5568;
        font-size: 11px;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        margin-bottom: 10px;
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

    .book-volume,
    .book-available {
        font-weight: 500;
    }

    .book-available.available {
        color: #38a169;
    }

    .book-available.unavailable {
        color: #e53e3e;
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
        cursor: pointer;
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
        margin-top: 20px;
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

    .loading-indicator {
        text-align: center;
        padding: 40px 20px;
        color: #718096;
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

    .error-message {
        text-align: center;
        padding: 40px 20px;
        color: #e53e3e;
        background: #fed7d7;
        border-radius: 8px;
        margin-top: 20px;
    }

    .no-books {
        grid-column: 1 / -1;
        text-align: center;
        padding: 60px 20px;
        color: #718096;
        font-size: 18px;
        background: #f8fafa;
        border-radius: 8px;
    }

    @media (max-width: 768px) {
        .filter-section {
            flex-direction: column;
            gap: 15px;
            align-items: stretch;
        }

        .filter-left {
            justify-content: center;
        }

        .results-count {
            text-align: center;
        }

        .books-grid {
            grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
            gap: 16px;
        }

        .genre-title {
            font-size: 28px;
        }

        .genre-description {
            font-size: 16px;
        }

        .book-info {
            padding: 12px 10px;
        }
    }

    @media (max-width: 480px) {
        .books-grid {
            grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
            gap: 12px;
        }

        .book-cover {
            height: 180px;
        }
    }
</style>