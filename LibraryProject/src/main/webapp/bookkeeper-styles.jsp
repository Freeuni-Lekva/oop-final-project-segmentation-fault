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



    .nav-box:hover {
        background-color: #f5f0e6;
        border-color: #face28;
        color: #5d4e37;
        transform: translateY(-1px);
        box-shadow: 0 2px 8px rgba(245, 196, 94, 0.2);
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


    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(10px); }
        to { opacity: 1; transform: translateY(0); }
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



    .users-list-section h3 {
        margin-bottom: 15px;
        color: #5d4e37;
        font-size: 18px;
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



    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
    }


    .book-image img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 4px;
    }


    .btn-small svg {
        width: 12px;
        height: 12px;
    }

    @media (max-width: 768px) {


        .admin-header h1 {
            font-size: 24px;
        }


    }
</style>