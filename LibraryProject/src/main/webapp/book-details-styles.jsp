<%@ page contentType="text/css;charset=UTF-8" language="java" %>
<%-- This file will be served as CSS --%>
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: 'Poppins', sans-serif;
    background-color: #f0f4f8;
    color: #2d3748;
    line-height: 1.6;
}

/* Header */
.book-details-header {
    width: 100%;
    height: 200px;
    background: linear-gradient(135deg, #4a90e2 0%, #50c8b4 100%);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 20px;
}

.book-title {
    font-family: 'Playfair Display', serif;
    font-size: 2.5rem;
    font-weight: 400;
    color: #fff;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.book-author {
    font-size: 1.2rem;
    color: #fff;
    text-shadow: 0 0 5px #ffffff;
}

/* Layout */
.book-details-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

.book-details-content {
    display: grid;
    grid-template-columns: 1fr 2fr;
    gap: 30px;
    margin: 30px 0;
    background: white;
    border-radius: 10px;
    padding: 20px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.book-cover {
    width: 250px;
    height: 350px;
    background: #f8f9fa;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
}

.book-cover img {
    max-width: 90%;
    max-height: 90%;
}

.book-cover-fallback {
    position: absolute;
    font-size: 50px;
    color: white;
    background: linear-gradient(135deg, #50c8b4 0%, #4a90e2 100%);
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
}

.book-meta {
    margin-top: 15px;
    font-size: 14px;
    text-align: center;
}

.book-available.available {
    color: #28a745;
    font-weight: 600;
}

.book-available.unavailable {
    color: #dc3545;
    font-weight: 600;
}

.reserve-button {
    margin-top: 10px;
    padding: 10px 20px;
    background: #2db2a4;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
}

.reserve-button:hover:not(:disabled) {
    background: #258a7e;
}

.reserve-button:disabled {
    background: #b0bec5;
    cursor: not-allowed;
}

.book-info-section {
    padding: 10px;
}

.book-rating {
    margin-bottom: 15px;
}

.stars {
    color: #f1c40f;
    font-size: 18px;
}

.rating-text {
    color: #2d3748;
    font-size: 14px;
}

.book-description {
    margin-bottom: 20px;
    font-size: 14px;
    color: #34495e;
}

.book-details-meta p {
    margin: 6px 0;
    font-size: 14px;
}

/* Review section */
.review-section {
    margin-top: 30px;
    padding: 20px;
    background: linear-gradient(135deg, #f8f9fa, #e6f0fa);
    border-radius: 10px;
}

.review-section h2 {
    font-size: 1.5rem;
    margin-bottom: 15px;
}

.review-form {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.review-select,
.review-form textarea {
    padding: 10px;
    border: 1px solid #ccc;
    border-radius: 6px;
    font-size: 14px;
}

.submit-review-button {
    padding: 10px 20px;
    background: #2db2a4;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
}

.submit-review-button:hover {
    background: #258a7e;
}

/* Back link */
.back-link {
    color: #4a90e2;
    text-decoration: none;
    font-weight: 500;
    margin-top: 20px;
    display: inline-flex;
    align-items: center;
    gap: 6px;
}

.back-arrow {
    width: 16px;
    height: 16px;
    fill: currentColor;
}

/* Loading and error */
.loading-indicator,
.error-message {
    text-align: center;
    padding: 40px 20px;
    font-size: 16px;
}

.spinner {
    width: 40px;
    height: 40px;
    border: 4px solid #ccc;
    border-top: 4px solid #2db2a4;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin: 0 auto 15px;
}

@keyframes spin {
    to { transform: rotate(360deg); }
}

.error-message {
    background: #ffe6e6;
    color: #dc3545;
    border-radius: 10px;
}
