<%--
  Created by IntelliJ IDEA.
  User: elenejobava
  Date: 6/16/25
  Time: 20:09
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>User Sign Up</title>
    <link href="https://fonts.googleapis.com/css2?family=Poppins&display=swap" rel="stylesheet">
    <%@ include file="styles.jsp" %>

    <style>
        .login-image {
            margin: 60px 0 20px;
        }

        .back-button {
            margin-top: 1rem;
            width: 100%;
            padding: 10px;
            background-color: #6c757d;
            color: white;
            border: none;
            border-radius: 5px;
            font-size: 1rem;
            cursor: pointer;
        }

        .back-button:hover {
            background-color: #545b62;
        }
    </style>
</head>
<body>

<img src="${pageContext.request.contextPath}/image4.jpg" alt="Centered Banner" class="login-image"/>

<div class="login-box">
    <h2>
        Sign Up as User
        <img src="owl.jpg" alt="owl" class="owl-icon">
    </h2>

    <form action="${pageContext.request.contextPath}/api/authorization/register" method="post">
        <input type="text" name="username" placeholder="Username" required />
        <input type="password" name="password" placeholder="Password" required />
        <input type="password" name="confirmPassword" placeholder="Confirm Password" required />
        <input type="hidden" name="role" value="USER" />

        <button type="submit">Sign Up</button>
    </form>

    <button class="back-button" onclick="window.location.href='${pageContext.request.contextPath}/login.jsp'">Back to Login</button>

    <div class="signin-section">
        <p>Already have an account? <a href="${pageContext.request.contextPath}/login.jsp">Sign in</a></p>
        <small>
            By creating an account, you agree to the Freeuni Library's Terms of Service and Privacy Policy
        </small>
    </div>
</div>

</body>
</html>