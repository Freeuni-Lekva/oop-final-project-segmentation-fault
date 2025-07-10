<%--
  Created by IntelliJ IDEA.
  User: elenejobava
  Date: 7/10/25
  Time: 18:34
  To change this template use File | Settings | File Templates.
--%>
<style>
  .user-books-header {
    background-color: #ffffff;
    padding: 30px 0;
    border-bottom: 1px solid #e0d6c7;
    margin-bottom: 30px;
  }

  .user-books-title {
    text-align: center;
    color: #5a4d25;
    font-family: 'Poppins', sans-serif;
    font-size: 2.2rem;
    margin-bottom: 10px;
  }

  .user-books-subtitle {
    text-align: center;
    color: #8b7355;
    font-size: 1rem;
    margin-bottom: 0;
  }

  .user-books-tabs {
    display: flex;
    justify-content: center;
    margin-bottom: 30px;
    border-bottom: 1px solid #e0d6c7;
  }

  .user-tab {
    padding: 12px 25px;
    margin: 0 5px;
    cursor: pointer;
    color: #8b7355;
    font-weight: 500;
    border-bottom: 3px solid transparent;
    transition: all 0.3s ease;
  }

  .user-tab:hover {
    color: #6b5a42;
    border-bottom-color: #d4c4b0;
  }

  .user-tab.active {
    color: #5a4d25;
    border-bottom-color: #8b7355;
    font-weight: 600;
  }

  .tab-content {
    display: none;
  }

  .tab-content.active {
    display: block;
  }

  .empty-state {
    text-align: center;
    padding: 60px 20px;
    color: #718096;
    font-size: 1.1rem;
    background: #f8fafa;
    border-radius: 8px;
    margin: 20px 0;
  }

  .empty-state-icon {
    font-size: 3rem;
    color: #d4c4b0;
    margin-bottom: 20px;
  }

  .scroll-container {
    position: relative;
    margin: 20px 0;
    padding-left: 20px;
    background: white;  /* Added white background */
    border-radius: 8px;  /* Added border radius */
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);  /* Added subtle shadow */
  }

  .books-scroll {
    display: flex;
    gap: 20px;
    overflow-x: auto;
    padding: 20px 10px;
    scroll-behavior: smooth;
    background: white;  /* Added white background */
  }

  .books-scroll::-webkit-scrollbar {
    height: 8px;
  }

  .books-scroll::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
  }

  .books-scroll::-webkit-scrollbar-thumb {
    background: #8b7355;
    border-radius: 4px;
  }

  .scroll-nav {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    background: white;
    border: 1px solid #e0d6c7;
    border-radius: 50%;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 10;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
  }

  .scroll-nav:hover {
    background: #f8f5ee;
    border-color: #8b7355;
  }

  .scroll-nav-left {
    left: -20px;
  }

  .scroll-nav-right {
    right: -20px;
  }

  .scroll-nav svg {
    width: 20px;
    height: 20px;
    fill: #8b7355;
  }

  .book-card {
    flex: 0 0 160px;  /* Changed from 180px to 160px */
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    cursor: pointer;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
  }

  .book-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  }

  .book-cover {
    width: 100%;
    height: 200px;  /* Changed from 220px to 200px */
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
    padding: 12px 12px;  /* Reduced padding from 16px 14px to 12px 12px */
    background: white;  /* Added explicit white background */
  }

  .book-title {
    font-family: 'Poppins', sans-serif;
    font-size: 13px;  /* Reduced from 14px to 13px */
    font-weight: 600;
    color: #2d3748;
    margin: 0 0 4px 0;  /* Reduced margin from 6px to 4px */
    line-height: 1.3;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .book-author {
    color: #718096;
    font-size: 11px;  /* Reduced from 12px to 11px */
    margin: 0 0 6px 0;  /* Reduced margin from 8px to 6px */
    font-weight: 400;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .book-rating {
    display: flex;
    align-items: center;
    gap: 4px;  /* Added gap */
    margin-bottom: 8px;
  }

  .stars {
    color: #ffd700;
    font-size: 12px;  /* Changed from 14px to 12px */
    font-weight: bold;
  }

  .rating-text {
    color: #718096;
    font-size: 10px;  /* Changed from 0.8rem to 10px */
  }

  .book-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 10px;  /* Changed from auto to 10px */
    color: #718096;
    border-top: 1px solid #e2e8f0;  /* Added border top */
    padding-top: 8px;  /* Added padding top */
  }

  .book-status {
    font-size: 10px;  /* Changed from 0.8rem to 10px */
    padding: 2px 8px;
    border-radius: 4px;
    font-weight: 500;
  }

  .status-reading {
    background: #bee3f8;
    color: #2b6cb0;
  }

  .status-reserved {
    background: #feebc8;
    color: #b7791f;
  }

  .status-completed {
    background: #c6f6d5;
    color: #22543d;
  }

  .book-date {
    color: #718096;
    font-size: 0.8rem;
  }

  .loading-indicator {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 20px;
    color: #718096;
  }

  .spinner {
    width: 40px;
    height: 40px;
    border: 4px solid #e0d6c7;
    border-top: 4px solid #8b7355;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 20px;
  }

  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }

  .error-message {
    text-align: center;
    padding: 60px 20px;
    color: #e53e3e;
    background: #fed7d7;
    border-radius: 8px;
    margin: 20px 0;
  }

  .no-books-message {
    text-align: center;
    padding: 60px 20px;
    color: #718096;
    font-size: 1.1rem;
    background: #f8fafa;
    border-radius: 8px;
    margin: 20px 0;
  }
</style>