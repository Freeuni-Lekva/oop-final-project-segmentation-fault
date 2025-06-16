<%--
  Created by IntelliJ IDEA.
  User: elenejobava
  Date: 6/16/25
  Time: 23:18
  To change this template use File | Settings | File Templates.
--%>
<link href="https://fonts.googleapis.com/css2?family=Poppins&display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Quicksand:wght@500&display=swap" rel="stylesheet">
<style>
    body {
        margin: 0;
        padding: 0;
        font-family: Arial, sans-serif;
        background-color: #e9eded;
        display: flex;
        flex-direction: column;
        align-items: center;
    }

    .header-text {
        font-family: 'Quicksand', sans-serif;
        font-size: 30px;
        font-weight: 500;
        text-align: center;
        color: #3b3b3b;
        margin: 30px 0 25px;
        letter-spacing: 0.6px;
    }

    .login-image {
        width: 100%;
        max-width: 800px;
        height: 200px;
        object-fit: cover;
        margin: 0px 0 30px;
        border-radius: 12px;
        box-shadow: 0 4px 8px rgba(0,0,0,0.2);
    }

    .login-box {
        background: white;
        padding: 2rem;
        width: 100%;
        max-width: 400px;
        border-radius: 10px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }

    .login-box h2 {
        text-align: center;
        margin-bottom: 1rem;
        font-family: 'Poppins', sans-serif;
        font-size: 25px;
        font-weight: bold;
        color: #333;
    }

    .login-box input {
        width: 100%;
        padding: 10px;
        margin: 10px 0;
        box-sizing: border-box;
        border: 1px solid #ddd;
        border-radius: 4px;
    }

    .login-box button {
        width: 100%;
        padding: 10px;
        background-color: #66aa9b;
        color: white;
        border: none;
        border-radius: 5px;
        font-size: 1rem;
        cursor: pointer;
    }

    .login-box button:hover {
        background-color: #437e6e;
    }

    .signup-section, .signin-section {
        margin-top: 1.5rem;
        text-align: center;
        font-size: 14px;
        color: #555;
    }

    .signup-section a, .signin-section a {
        color: #0077cc;
        text-decoration: none;
        font-weight: bold;
    }

    .signup-section a:hover, .signin-section a:hover {
        text-decoration: underline;
    }

    .owl-icon {
        width: 70px;
        height: 70px;
        vertical-align: middle;
    }
</style>
