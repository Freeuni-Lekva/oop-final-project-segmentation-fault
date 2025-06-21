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
        background-color: #ffffff;
        color: #333;
    }

    .top-line1 {
        position: relative;
        width: 100%;
        height: 190px;
        background-image: url('images/image.jpg');
        background-size: cover;
        background-position: center 25%;
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
        background-color: #e7edec;
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
        color: #a7b8b6;
        margin-right: 2px;
        font-size: 18px;
        font-weight: bold;
    }

    .highlight-text {
        color: rgba(92, 108, 104, 0.69);
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
        background-color: rgb(229, 236, 234);
        border: 1px solid rgb(182, 193, 191);
        border-radius: 6px;
        color: #798e8b;
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
        background-color: rgb(208, 221, 219);
        border-color: #e2e6e5;
        color: #4a5568;
        transform: translateY(-1px);
        box-shadow: 0 2px 8px rgba(202, 175, 96, 0.2);
    }

    .nav-box.active {
        background-color: #a7cdcd;
        color: #ffffff;
        border-color: #accac5;
    }

    .nav-icon {
        width: 16px;
        height: 16px;
        fill: currentColor;
    }

    .main-content {
        padding: 20px;
        min-height: 600px;
        background-color: #ffffff;
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
        color: #4a5568;
        font-size: 28px;
        font-weight: 600;
        margin-bottom: 10px;
    }

    .admin-header p {
        color: #798e8b;
        font-size: 16px;
    }

    .admin-tabs {
        display: flex;
        justify-content: center;
        margin-bottom: 30px;
        background: #f8fafa;
        border-radius: 12px;
        padding: 8px;
        box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05);
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
        color: #798e8b;
        font-size: 14px;
    }

    .admin-tab.active {
        background-color: #a7cdcd;
        color: white;
        transform: translateY(-1px);
        box-shadow: 0 3px 8px rgba(167, 205, 205, 0.3);
    }

    .admin-tab:hover:not(.active) {
        background-color: #e8f2f2;
        color: #4a5568;
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
        background: #ffffff;
        border-radius: 12px;
        padding: 30px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
        border: 1px solid #e7edec;
        margin-bottom: 30px;
    }

    .form-section-title {
        font-family: 'Poppins', sans-serif;
        color: #4a5568;
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
        color: #4a5568;
        font-size: 14px;
    }

    .form-group input,
    .form-group textarea,
    .form-group select {
        width: 100%;
        padding: 12px 16px;
        border: 1px solid #d1d9d8;
        border-radius: 8px;
        font-size: 14px;
        transition: all 0.3s ease;
        background: #f8fafa;
        color: #4a5568;
    }

    .form-group input:focus,
    .form-group textarea:focus,
    .form-group select:focus {
        outline: none;
        border-color: #a7cdcd;
        background: white;
        box-shadow: 0 0 0 3px rgba(167, 205, 205, 0.1);
    }

    .form-group textarea {
        resize: vertical;
        min-height: 80px;
    }

    .file-upload-area {
        border: 2px dashed #d1d9d8;
        border-radius: 8px;
        padding: 20px;
        text-align: center;
        background: #f8fafa;
        transition: all 0.3s ease;
        cursor: pointer;
        position: relative;
    }

    .file-upload-area:hover {
        border-color: #a7cdcd;
        background: #f0f5f5;
    }

    .file-upload-area.drag-over {
        border-color: #a7cdcd;
        background: #e8f2f2;
    }

    .file-upload-icon {
        width: 48px;
        height: 48px;
        margin: 0 auto 10px;
        fill: #a7cdcd;
    }

    .file-upload-text {
        color: #798e8b;
        font-size: 14px;
        margin-bottom: 5px;
    }

    .file-upload-subtext {
        color: #a7b8b6;
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
        background: #e8f2f2;
        border-radius: 6px;
        display: none;
    }

    .file-preview.show {
        display: block;
    }

    .file-preview-name {
        font-size: 13px;
        color: #4a5568;
        font-weight: 500;
    }

    .btn {
        background-color: #a7cdcd;
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
    }

    .btn:hover {
        background-color: #96b8b8;
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(167, 205, 205, 0.3);
    }

    .btn:active {
        transform: translateY(0);
    }

    .btn:disabled {
        background-color: #d1d9d8;
        cursor: not-allowed;
        transform: none;
        box-shadow: none;
    }

    .btn-danger {
        background-color: #e74c3c;
    }

    .btn-danger:hover {
        background-color: #c0392b;
        box-shadow: 0 4px 12px rgba(231, 76, 60, 0.3);
    }

    .btn-success {
        background-color: #27ae60;
    }

    .btn-success:hover {
        background-color: #219a52;
        box-shadow: 0 4px 12px rgba(39, 174, 96, 0.3);
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
        background: white;
        border-radius: 12px;
        padding: 20px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
        border: 1px solid #e7edec;
        transition: all 0.3s ease;
        position: relative;
    }

    .book-card:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
    }

    .book-card::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 3px;
        background: linear-gradient(90deg, #a7cdcd, #96b8b8);
        border-radius: 12px 12px 0 0;
    }

    .book-title {
        font-family: 'Poppins', sans-serif;
        font-size: 16px;
        font-weight: 600;
        color: #4a5568;
        margin-bottom: 8px;
    }

    .book-author {
        color: #a7cdcd;
        font-weight: 500;
        margin-bottom: 12px;
        font-size: 14px;
    }

    .book-description {
        color: #798e8b;
        margin-bottom: 12px;
        line-height: 1.5;
        font-size: 13px;
    }

    .book-genre {
        display: inline-block;
        background: #f0f5f5;
        color: #4a5568;
        padding: 4px 12px;
        border-radius: 20px;
        font-size: 12px;
        font-weight: 500;
        border: 1px solid #e7edec;
    }

    .status-indicator {
        position: absolute;
        top: 15px;
        right: 15px;
        width: 10px;
        height: 10px;
        border-radius: 50%;
        background: #27ae60;
    }

    .status-indicator.borrowed {
        background: #e74c3c;
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
        background: #f8fafa;
        padding: 20px;
        border-radius: 8px;
        border: 1px solid #e7edec;
    }

    .users-list-section {
        background: #f8fafa;
        padding: 20px;
        border-radius: 8px;
        border: 1px solid #e7edec;
    }

    .users-list-section h3 {
        margin-bottom: 15px;
        color: #4a5568;
        font-size: 18px;
    }

    .search-container {
        margin-bottom: 15px;
    }

    .search-input {
        width: 100%;
        padding: 10px 12px;
        border: 1px solid #d1d9d8;
        border-radius: 6px;
        font-size: 14px;
        background: white;
    }

    .search-input:focus {
        outline: none;
        border-color: #a7cdcd;
        box-shadow: 0 0 0 2px rgba(167, 205, 205, 0.1);
    }

    .users-list {
        max-height: 300px;
        overflow-y: auto;
        border: 1px solid #e7edec;
        border-radius: 6px;
        background: white;
    }

    .user-item {
        padding: 12px 15px;
        padding-right: 80px;
        border-bottom: 1px solid #f0f0f0;
        cursor: pointer;
        transition: background-color 0.2s;
        position: relative;
        min-height: 44px;
        display: flex;
        align-items: center;
    }

    .user-item:hover {
        background-color: #f8fafa;
    }

    .user-item:last-child {
        border-bottom: none;
    }

    .user-item.banned {
        background-color: #fff5f5;
        color: #c53030;
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
        background: rgba(0,0,0,0.1);
        width: 60px;
        text-align: center;
    }

    .user-item.banned .status {
        background: #fed7d7;
        color: #c53030;
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
        border: 1px solid #d1d9d8;
        border-radius: 6px;
        font-size: 14px;
        background: white;
        min-width: 0;
    }
    .username-search-input:focus {
        outline: none;
        border-color: #a7cdcd;
        box-shadow: 0 0 0 2px rgba(167, 205, 205, 0.1);
    }

    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
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
    }
</style>
