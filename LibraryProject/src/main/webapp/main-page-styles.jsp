<%--
  Created by IntelliJ IDEA.
  User: elenejobava
  Date: 6/17/25
  Time: 22:51
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap');
    @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600&display=swap');

    body {
        margin: 0;
        padding: 0;
        font-family: Arial, sans-serif;
    }

    .top-line1 {
        position: relative;
        width: 100%;
        height: 230px;
        background-image: url('${pageContext.request.contextPath}/images/image0.jpg');
        background-size: cover;
        background-position: center 75%;
        background-repeat: no-repeat;
        display: flex;
        justify-content: center;
        align-items: center;
        overflow: hidden;
    }

    .top-line1::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.18);
        z-index: 1;
    }

    .top-line1 h2 {
        position: relative;
        z-index: 2;
        margin: 0;
        font-family: 'Poppins', sans-serif;
        color: #faf6e6;
        font-size: 24px;
        font-weight: 500;
        text-align: center;
        text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
    }

    .top-line2 {
        position: relative;
        width: 100%;
        height: 50px;
        background-color: #ffffff;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 20px;
        box-sizing: border-box;
    }

    .library-title {
        display: flex;
        align-items: center;
    }

    .pale-text {
        color: #b8a082;
        margin-right: 2px;
        font-size: 18px;
        font-weight: bold;
    }

    .highlight-text {
        color: #a39077;
        font-weight: bold;
        font-size: 18px;
    }

    .nav-container {
        display: flex;
        gap: 15px;
        align-items: center;
    }

    .nav-box {
        padding: 8px 16px;
        background-color: #f7f5f2;
        border: 1px solid #e0d6c7;
        border-radius: 6px;
        color: #8b7355;
        font-size: 14px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.3s ease;
        text-decoration: none;
        display: flex;
        align-items: center;
        gap: 6px;
        position: relative;
    }

    .nav-box:hover {
        background-color: #f2ede6;
        border-color: #d4c4b0;
        color: #6b5a42;
        transform: translateY(-1px);
        box-shadow: 0 2px 8px rgba(139, 115, 85, 0.15);
    }

    .nav-box.active {
        background-color: #d4c4b0;
        color: #ffffff;
        border-color: #c4ad8e;
    }

    .nav-icon {
        width: 16px;
        height: 16px;
        fill: currentColor;
    }

    .dropdown-arrow {
        width: 12px;
        height: 12px;
        fill: currentColor;
        margin-left: 4px;
        transition: transform 0.3s ease;
    }

    .browse-dropdown {
        position: relative;
        display: inline-block;
    }

    .dropdown-content {
        display: none;
        position: absolute;
        top: 100%;
        left: 0;
        background-color: #ffffff;
        min-width: 200px;
        box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
        border-radius: 8px;
        border: 1px solid #e2e6e5;
        z-index: 1000;
        margin-top: 4px;
    }

    .dropdown-content.show {
        display: block;
        animation: fadeIn 0.3s ease;
    }

    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(-10px); }
        to { opacity: 1; transform: translateY(0); }
    }

    .dropdown-item {
        display: block;
        padding: 12px 16px;
        color: #4a5568;
        text-decoration: none;
        font-size: 14px;
        font-weight: 400;
        transition: background-color 0.2s ease;
        border-bottom: 1px solid #f0f4f3;
    }

    .dropdown-item:last-child {
        border-bottom: none;
        border-radius: 0 0 8px 8px;
    }

    .dropdown-item:first-child {
        border-radius: 8px 8px 0 0;
    }

    .dropdown-item:hover {
        background-color: #f8fafa;
        color: #2d3748;
    }

    .browse-dropdown:hover .dropdown-arrow {
        transform: rotate(180deg);
    }

    .owl-icon {
        width: 30px;
        height: 30px;
        vertical-align: middle;
        margin-left: 10px;
    }

    /* Authentication button styles */
    .auth-btn {
        border: 2px solid #8b7355 !important;
        font-weight: 600 !important;
    }

    .auth-btn:hover {
        background-color: #8b7355 !important;
        color: #ffffff !important;
        border-color: #6b5a42 !important;
    }

    .main-content {
        padding: 20px;
        min-height: 400px;
        background-color: #ffffff;
    }

    .sample-content {
        max-width: 1200px;
        margin: 0 auto;
        padding: 40px 20px;
    }

    .sample-content h3 {
        color: #333;
        font-family: 'Poppins', sans-serif;
        margin-bottom: 20px;
    }

    .sample-content p {
        color: #666;
        line-height: 1.6;
        margin-bottom: 15px;
    }

    /* Main content styles */
    .books-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 0 20px;
    }

    .books-section {
        margin-bottom: 40px;
    }

    .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
    }

    .section-title {
        font-size: 1.8rem;
        font-weight: 600;
        color: #5a4d25;
        margin: 0;
    }

    .scroll-container {
        position: relative;
        overflow: hidden;
    }

    .books-scroll {
        display: flex;
        gap: 20px;
        overflow-x: auto;
        scroll-behavior: smooth;
        padding-bottom: 10px;
        scrollbar-width: thin;
        scrollbar-color: #D2B48C #f1f1f1;
    }

    .books-scroll::-webkit-scrollbar {
        height: 8px;
    }

    .books-scroll::-webkit-scrollbar-track {
        background: #f1f1f1;
        border-radius: 10px;
    }

    .books-scroll::-webkit-scrollbar-thumb {
        background: #D2B48C;
        border-radius: 10px;
    }

    .books-scroll::-webkit-scrollbar-thumb:hover {
        background: #C8A882;
    }

    .scroll-nav {
        position: absolute;
        top: 50%;
        transform: translateY(-50%);
        background: rgb(213, 191, 127) !important;
        border: none;
        border-radius: 50%;
        width: 40px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.2s ease;
        z-index: 10;
        opacity: 0;
        visibility: hidden;
    }

    .scroll-container:hover .scroll-nav {
        opacity: 1;
        visibility: visible;
    }

    .scroll-nav:hover {
        background: rgb(211, 194, 148) !important;
        transform: translateY(-50%) scale(1.1);
    }

    .scroll-nav svg {
        width: 20px;
        height: 20px;
        fill: white;
    }

    .book-card {
        background: white;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        overflow: hidden;
        transition: all 0.2s ease;
        cursor: pointer;
        position: relative;
        min-width: 160px;
        max-width: 160px;
        flex-shrink: 0;
    }

    .book-card:hover {
        transform: translateY(-3px);
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
    }

    .book-cover {
        width: 100%;
        height: 200px;
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
        padding: 12px 10px;
    }

    .book-title {
        font-family: 'Poppins', sans-serif;
        font-size: 13px;
        font-weight: 600;
        color: #2d3748;
        margin: 0 0 6px 0;
        line-height: 1.3;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        min-height: 34px;
    }

    .book-author {
        color: #718096;
        font-size: 11px;
        margin: 0 0 8px 0;
        font-weight: 400;
        display: -webkit-box;
        -webkit-line-clamp: 1;
        -webkit-box-orient: vertical;
        overflow: hidden;
    }

    .book-rating {
        display: flex;
        align-items: center;
        gap: 4px;
        margin-bottom: 8px;
    }

    .stars {
        color: #ffd700;
        font-size: 11px;
        font-weight: bold;
    }

    .rating-text {
        color: #718096;
        font-size: 9px;
    }

    .book-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 9px;
        color: #718096;
        border-top: 1px solid #e2e8f0;
        padding-top: 6px;
    }

    .book-available {
        font-weight: 500;
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
        text-align: center;
        padding: 60px 20px;
        color: #718096;
        font-size: 18px;
        background: #f8fafa;
        border-radius: 8px;
        min-width: 100%;
    }

    .hero-section {
        display: none;
    }

    .hero-section h1 {
        display: none;
    }

    .stats-section {
        display: flex;
        justify-content: center;
        gap: 20px;
        margin-bottom: 30px;
        flex-wrap: wrap;
    }

    .stat-item {
        text-align: center;
        padding: 15px;
        background: white;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        min-width: 80px;
    }

    .stat-number {
        font-size: 1.2rem;
        font-weight: 700;
        color: #937c3c;
        margin-bottom: 5px;
    }

    .stat-label {
        font-size: 0.8rem;
        color: #718096;
        font-weight: 500;
    }

    .search-container {
        position: relative;
        width: 300px;
        margin: 0 30px 0 15px;
    }

    .search-input {
        width: 100%;
        padding: 8px 15px;
        border: 1px solid #e0d6c7;
        border-radius: 20px;
        background-color: #f7f5f2;
        font-size: 14px;
        color: #8b7355;
        outline: none;
        transition: all 0.3s ease;
    }

    .search-input:focus {
        border-color: #d4c4b0;
        box-shadow: 0 0 0 2px rgba(139, 115, 85, 0.2);
    }

    .search-button {
        position: absolute;
        right: 5px;
        top: 50%;
        transform: translateY(-50%);
        background: none;
        border: none;
        cursor: pointer;
        padding: 5px;
    }

    .search-icon {
        width: 20px;
        height: 20px;
        fill: #8b7355;
    }

    @media (max-width: 768px) {
        .section-title {
            font-size: 1.5rem;
        }

        .book-card {
            min-width: 140px;
            max-width: 140px;
        }

        .book-cover {
            height: 180px;
        }

        .hero-section h1 {
            font-size: 1.3rem;
        }

        .stats-section {
            gap: 15px;
        }

        .stat-item {
            min-width: 70px;
            padding: 12px;
        }

        .stat-number {
            font-size: 1.1rem;
        }
    }

    @media (max-width: 480px) {
        .book-card {
            min-width: 120px;
            max-width: 120px;
        }

        .book-cover {
            height: 160px;
        }

        .hero-section {
            padding: 20px 15px;
        }

        .hero-section h1 {
            font-size: 1.2rem;
        }

        .stats-section {
            flex-direction: row;
            justify-content: space-around;
        }

        .stat-item {
            min-width: 60px;
            padding: 10px;
        }
    }
</style>