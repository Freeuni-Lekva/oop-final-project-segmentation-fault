<%@ page contentType="text/html;charset=UTF-8" %>
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

        .error-message {
            color: red;
            margin-top: 1rem;
            display: none;
        }
    </style>
</head>
<body>

<img src="${pageContext.request.contextPath}/images/rococo.jpg" alt="Centered Banner" class="login-image"/>

<div class="login-box">
    <h2>
        Sign Up
        <img src="images/cropped_circle_image.jpg" alt="owl" class="owl-icon">
    </h2>

    <!-- Removed form action and method, using JS -->
    <form id="registerForm">
        <input type="text" id="username" name="username" placeholder="Username" required />
        <input type="password" id="password" name="password" placeholder="Password" required />
        <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Confirm Password" required />
        <input type="hidden" id="role" name="role" value="USER" />

        <button type="submit">Sign Up</button>
    </form>

    <button class="back-button" onclick="window.location.href='${pageContext.request.contextPath}/login.jsp'">Back to Login</button>

    <div class="signin-section">
        <p>Already have an account? <a href="${pageContext.request.contextPath}/login.jsp">Sign in</a></p>
        <small>
            By creating an account, you agree to the Freeuni Library's Terms of Service and Privacy Policy
        </small>
    </div>

    <div id="errorMessage" class="error-message"></div>
</div>

<%@ include file="password-validation.jsp" %>

<script>
    document.getElementById("registerForm").addEventListener("submit", async function (e) {
        e.preventDefault();

        const username = document.getElementById("username").value.trim();
        const password = document.getElementById("password").value;
        const confirmPassword = document.getElementById("confirmPassword").value;
        const role = document.getElementById("role").value;

        if (password !== confirmPassword) {
            alert("Passwords do not match.");
            return;
        }

        try {
            const response = await fetch("<%= request.getContextPath() %>/api/authorization/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ username, password, role }),
                credentials: "include"
            });

            const result = await response.json();
            console.log(result.redirect)
            if (response.ok && result.redirect) {
                window.location.href = result.redirect;
            } else {
                alert(result.message || "Registration failed.");
            }
        } catch (err) {
            console.error(err);
            alert("An error occurred while registering.");
        }
    });
</script>>

</body>
</html>
