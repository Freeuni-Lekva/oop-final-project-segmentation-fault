<%--
  Created by IntelliJ IDEA.
  User: elenejobava
  Date: 6/19/25
  Time: 20:54
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap');
    @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600&display=swap');

    * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
    }

    body {
        font-family: 'Inter', Arial, sans-serif;
        background-color: #faf8f0;
        color: #5d4e37;
    }

    .top-line1 {
        position: relative;
        width: 100%;
        height: 300px;
        background-image: url('images/image0.jpg');
        background-size: cover;
        background-position: center 60%;
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
        background: rgba(93, 78, 55, 0.18);
        z-index: 1;
    }

    .top-line1 h2 {
        position: relative;
        z-index: 2;
        margin: 0;
        font-family: 'Poppins', sans-serif;
        color: #faf8f0;
        font-size: 24px;
        font-weight: 500;
        text-align: center;
        text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
    }

    .top-line2 {
        position: relative;
        width: 100%;
        height: 50px;
        background-color: #f7f3f0;
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
        color: #b8a76b;
        margin-right: 2px;
        font-size: 18px;
        font-weight: bold;
    }

    .highlight-text {
        color: #8b7355;
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
        background-color: #fefdf8;
        border: 1px solid #e6cb58;
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
    }

    .nav-box:hover {
        background-color: #f5f0e6;
        border-color: #face28;
        color: #5d4e37;
        transform: translateY(-1px);
        box-shadow: 0 2px 8px rgba(245, 196, 94, 0.2);
    }

    .nav-box.active {
        background-color: #f5c45e;
        color: #ffffff;
        border-color: #e8b850;
    }

    .nav-icon {
        width: 16px;
        height: 16px;
        fill: currentColor;
    }

    .main-content {
        padding: 20px;
        min-height: 600px;
        background-color: #faf8f0;
    }

    .admin-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 20px;
    }

    .admin-header {
        text-align: center;
        margin-bottom: 40px;
    }

    .admin-header h1 {
        font-family: 'Poppins', sans-serif;
        color: #5d4e37;
        font-size: 28px;
        font-weight: 600;
        margin-bottom: 10px;
    }

    .admin-header p {
        color: #8b7355;
        font-size: 16px;
    }

    .admin-tabs {
        display: flex;
        justify-content: center;
        margin-bottom: 30px;
        background: #f9f7f4;
        border-radius: 12px;
        padding: 8px;
        box-shadow: inset 0 2px 4px rgba(160, 130, 98, 0.05);
    }

    .admin-tab {
        background: transparent;
        border: none;
        padding: 12px 20px;
        margin: 0 4px;
        border-radius: 8px;
        cursor: pointer;
        font-weight: 500;
        transition: all 0.3s ease;
        color: #8b7355;
        font-size: 14px;
    }

    .admin-tab.active {
        background-color: #f5c45e;
        color: white;
        transform: translateY(-1px);
        box-shadow: 0 3px 8px rgba(245, 196, 94, 0.3);
    }

    .admin-tab:hover:not(.active) {
        background-color: #f5f0e6;
        color: #5d4e37;
    }

    .tab-content {
        display: none;
        animation: fadeIn 0.4s ease-in-out;
    }

    .tab-content.active {
        display: block;
    }

    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(10px); }
        to { opacity: 1; transform: translateY(0); }
    }

    .form-container {
        background: #fefdf8;
        border-radius: 12px;
        padding: 30px;
        box-shadow: 0 4px 20px rgba(160, 130, 98, 0.08);
        border: 1px solid #f7f3f0;
        margin-bottom: 30px;
    }

    .form-section-title {
        font-family: 'Poppins', sans-serif;
        color: #5d4e37;
        font-size: 20px;
        font-weight: 600;
        margin-bottom: 20px;
        text-align: center;
    }

    .form-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
        gap: 20px;
        margin-bottom: 25px;
    }

    .form-group {
        position: relative;
    }

    .form-group label {
        display: block;
        margin-bottom: 8px;
        font-weight: 500;
        color: #5d4e37;
        font-size: 14px;
    }

    .form-group input,
    .form-group textarea,
    .form-group select {
        width: 100%;
        padding: 12px 16px;
        border: 1px solid #e6cb58;
        border-radius: 8px;
        font-size: 14px;
        transition: all 0.3s ease;
        background: #f9f7f4;
        color: #5d4e37;
    }

    .form-group input:focus,
    .form-group textarea:focus,
    .form-group select:focus {
        outline: none;
        border-color: #f5c45e;
        background: white;
        box-shadow: 0 0 0 3px rgba(245, 196, 94, 0.1);
    }

    .form-group textarea {
        resize: vertical;
        min-height: 80px;
    }

    .file-upload-area {
        border: 2px dashed #e6cb58;
        border-radius: 8px;
        padding: 20px;
        text-align: center;
        background: #f9f7f4;
        transition: all 0.3s ease;
        cursor: pointer;
        position: relative;
    }

    .file-upload-area:hover {
        border-color: #f5c45e;
        background: #f5f0e6;
    }

    .file-upload-area.drag-over {
        border-color: #f5c45e;
        background: #f5f0e6;
    }

    .file-upload-icon {
        width: 48px;
        height: 48px;
        margin: 0 auto 10px;
        fill: #f5c45e;
    }

    .file-upload-text {
        color: #8b7355;
        font-size: 14px;
        margin-bottom: 5px;
    }

    .file-upload-subtext {
        color: #b8a76b;
        font-size: 12px;
    }

    .file-input {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        opacity: 0;
        cursor: pointer;
    }

    .file-preview {
        margin-top: 10px;
        padding: 10px;
        background: #f5f0e6;
        border-radius: 6px;
        display: none;
    }

    .file-preview.show {
        display: block;
    }

    .file-preview-name {
        font-size: 13px;
        color: #5d4e37;
        font-weight: 500;
    }

    .btn {
        background: linear-gradient(135deg, #F5C45E 0%, #E8B850 100%);
        color: white;
        border: none;
        padding: 12px 24px;
        border-radius: 8px;
        font-size: 14px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 8px;
        box-shadow: 0 4px 12px rgba(245, 196, 94, 0.3);
    }

    .btn:hover {
        background: linear-gradient(135deg, #E8B850 0%, #DBAC48 100%);
        transform: translateY(-1px);
        box-shadow: 0 6px 16px rgba(219, 172, 72, 0.4);
    }

    .btn:active {
        transform: translateY(0);
    }

    .btn:disabled {
        background: #e6cb58;
        cursor: not-allowed;
        transform: none;
        box-shadow: none;
        opacity: 0.5;
    }

    .btn-danger {
        background: linear-gradient(135deg, #d73a49 0%, #cb2431 100%);
    }

    .btn-danger:hover {
        background: linear-gradient(135deg, #cb2431 0%, #b52d3a 100%);
        box-shadow: 0 4px 12px rgba(203, 36, 49, 0.3);
    }

    .btn-success {
        background: linear-gradient(135deg, #28a745 0%, #218838 100%);
    }

    .btn-success:hover {
        background: linear-gradient(135deg, #218838 0%, #1e7e34 100%);
        box-shadow: 0 4px 12px rgba(33, 136, 56, 0.3);
    }

    .action-buttons {
        display: flex;
        gap: 12px;
        justify-content: center;
        flex-wrap: wrap;
    }

    .books-section {
        margin-top: 30px;
    }

    .books-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        gap: 20px;
        margin-top: 20px;
    }

    .book-card {
        background: #fefdf8;
        border-radius: 12px;
        padding: 20px;
        box-shadow: 0 2px 12px rgba(160, 130, 98, 0.08);
        border: 1px solid #f7f3f0;
        transition: all 0.3s ease;
        position: relative;
    }

    .book-card:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 25px rgba(160, 130, 98, 0.12);
    }

    .book-card::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 3px;
        background: linear-gradient(90deg, #f5c45e, #e8b850);
        border-radius: 12px 12px 0 0;
    }

    .book-title {
        font-family: 'Poppins', sans-serif;
        font-size: 16px;
        font-weight: 600;
        color: #5d4e37;
        margin-bottom: 8px;
    }

    .book-author {
        color: #f5c45e;
        font-weight: 500;
        margin-bottom: 12px;
        font-size: 14px;
    }

    .book-description {
        color: #8b7355;
        margin-bottom: 12px;
        line-height: 1.5;
        font-size: 13px;
    }

    .book-genre {
        display: inline-block;
        background: #f5f0e6;
        color: #5d4e37;
        padding: 4px 12px;
        border-radius: 20px;
        font-size: 12px;
        font-weight: 500;
        border: 1px solid #e6cb58;
    }

    .status-indicator {
        position: absolute;
        top: 15px;
        right: 15px;
        width: 10px;
        height: 10px;
        border-radius: 50%;
        background: #28a745;
    }

    .status-indicator.borrowed {
        background: #d73a49;
    }

    .notification {
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 12px 20px;
        border-radius: 8px;
        color: white;
        font-weight: 500;
        z-index: 1000;
        transform: translateX(400px);
        transition: transform 0.3s ease;
        font-size: 14px;
    }

    .user-management-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
        gap: 20px;
    }

    .loading-spinner {
        width: 16px;
        height: 16px;
        border: 2px solid transparent;
        border-top: 2px solid currentColor;
        border-radius: 50%;
        animation: spin 1s linear infinite;
    }

    .message-area {
        margin-top: 10px;
        padding: 12px 16px;
        border-radius: 8px;
        font-size: 12px;
        display: none;
    }

    .message-area.success {
        background-color: #d4edda;
        color: #155724;
        border: 1px solid #c3e6cb;
    }

    .message-area.error {
        background-color: #f8d7da;
        color: #721c24;
        border: 1px solid #f5c6cb;
    }

    .user-management-container {
        display: flex;
        flex-direction: column;
        gap: 30px;
    }

    .username-input-section {
        background: #f9f7f4;
        padding: 20px;
        border-radius: 8px;
        border: 1px solid #e6cb58;
    }

    .users-list-section {
        background: #f9f7f4;
        padding: 20px;
        border-radius: 8px;
        border: 1px solid #e6cb58;
    }

    .users-list-section h3 {
        margin-bottom: 15px;
        color: #5d4e37;
        font-size: 18px;
    }

    .search-container {
        margin-bottom: 15px;
    }

    .search-input {
        width: 100%;
        padding: 10px 12px;
        border: 1px solid #e6cb58;
        border-radius: 6px;
        font-size: 14px;
        background: #fefdf8;
        color: #5d4e37;
    }

    .search-input:focus {
        outline: none;
        border-color: #f5c45e;
        box-shadow: 0 0 0 2px rgba(245, 196, 94, 0.1);
    }

    .users-list {
        max-height: 300px;
        overflow-y: auto;
        border: 1px solid #e6cb58;
        border-radius: 6px;
        background: #fefdf8;
    }

    .user-item {
        padding: 12px 15px;
        padding-right: 80px;
        border-bottom: 1px solid #f5f0e6;
        cursor: pointer;
        transition: background-color 0.2s;
        position: relative;
        min-height: 44px;
        display: flex;
        align-items: center;
        color: #5d4e37;
    }

    .user-item:hover {
        background-color: #f5f0e6;
    }

    .user-item:last-child {
        border-bottom: none;
    }

    .user-item.banned {
        background-color: #fff5f5;
        color: #d73a49;
    }

    .user-item.active {
        background-color: #f0fff4;
        color: #2f855a;
    }

    .user-item .username {
        font-weight: 500;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex: 1;
    }

    .user-item .status {
        position: absolute;
        right: 15px;
        top: 50%;
        transform: translateY(-50%);
        font-size: 12px;
        font-weight: 400;
        padding: 2px 8px;
        border-radius: 12px;
        background: rgba(184, 167, 107, 0.1);
        width: 60px;
        text-align: center;
    }

    .user-item.banned .status {
        background: #fed7d7;
        color: #d73a49;
    }

    .user-item.active .status {
        background: #c6f6d5;
        color: #2f855a;
    }

    .username-action-row {
        display: flex;
        align-items: center;
        margin-bottom: 10px;
    }
    .username-search-input {
        flex: 1;
        padding: 10px 12px;
        border: 1px solid #e6cb58;
        border-radius: 6px;
        font-size: 14px;
        background: #fefdf8;
        color: #5d4e37;
        min-width: 0;
    }
    .username-search-input:focus {
        outline: none;
        border-color: #f5c45e;
        box-shadow: 0 0 0 2px rgba(245, 196, 94, 0.1);
    }

    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
    }

    .book-management-container {
        display: flex;
        flex-direction: column;
        gap: 20px;
    }

    .search-filters-row {
        display: flex;
        align-items: center;
        gap: 15px;
        margin-bottom: 10px;
        flex-wrap: wrap;
    }

    .book-search-input {
        flex: 1;
        padding: 10px 12px;
        border: 1px solid #e6cb58;
        border-radius: 6px;
        font-size: 14px;
        background: #fefdf8;
        color: #5d4e37;
        min-width: 250px;
    }

    .book-search-input:focus {
        outline: none;
        border-color: #f5c45e;
        box-shadow: 0 0 0 2px rgba(245, 196, 94, 0.1);
    }

    .filter-buttons {
        display: flex;
        gap: 10px;
        align-items: center;
    }

    .filter-select {
        padding: 8px 12px;
        border: 1px solid #e6cb58;
        border-radius: 6px;
        font-size: 14px;
        background: #fefdf8;
        color: #5d4e37;
        cursor: pointer;
    }

    .filter-select:focus {
        outline: none;
        border-color: #f5c45e;
        box-shadow: 0 0 0 2px rgba(245, 196, 94, 0.1);
    }

    .books-list-section {
        background: #f9f7f4;
        padding: 20px;
        border-radius: 8px;
        border: 1px solid #e6cb58;
    }

    .books-list {
        max-height: 500px;
        overflow-y: auto;
        border: 1px solid #e6cb58;
        border-radius: 6px;
        background: #fefdf8;
    }

    .book-item {
        padding: 15px;
        border-bottom: 1px solid #f5f0e6;
        transition: background-color 0.2s;
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 15px;
    }

    .book-item:hover {
        background-color: #f5f0e6;
    }

    .book-item:last-child {
        border-bottom: none;
    }

    .book-info {
        display: flex;
        align-items: center;
        gap: 15px;
        flex: 1;
        min-width: 0;
    }

    .book-image {
        flex-shrink: 0;
        width: 50px;
        height: 70px;
        border-radius: 4px;
        overflow: hidden;
        background: #f5f0e6;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .book-image img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 4px;
    }

    .book-details {
        flex: 1;
        min-width: 0;
    }

    .book-name {
        font-weight: 600;
        color: #5d4e37;
        font-size: 16px;
        margin-bottom: 4px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .book-author {
        color: #8b7355;
        font-size: 14px;
        margin-bottom: 6px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .book-meta {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 12px;
        color: #b8a76b;
        flex-wrap: wrap;
    }

    .book-genre,
    .book-volume,
    .book-year,
    .book-amount {
        white-space: nowrap;
    }

    .book-actions {
        flex-shrink: 0;
    }

    .btn-small {
        padding: 6px 12px;
        font-size: 12px;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-small svg {
        width: 12px;
        height: 12px;
    }

    @media (max-width: 768px) {
        .admin-tabs {
            flex-direction: column;
            gap: 4px;
        }

        .admin-tab {
            margin: 2px 0;
        }

        .admin-header h1 {
            font-size: 24px;
        }

        .user-management-grid {
            grid-template-columns: 1fr;
        }

        .search-filters-row {
            flex-direction: column;
            align-items: stretch;
        }

        .book-search-input {
            min-width: auto;
        }

        .book-item {
            flex-direction: column;
            align-items: flex-start;
            gap: 10px;
        }

        .book-info {
            width: 100%;
        }

        .book-actions {
            width: 100%;
            text-align: right;
        }
    }
</style>