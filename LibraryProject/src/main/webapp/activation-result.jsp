<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Account Activation - Freeuni Library</title>
    <%@ include file="styles.jsp" %>
    <%@ include file="activation-styles.jsp" %>
</head>
<body>

<h1 class="header-text">Explore the world's knowledge, cultures and ideas</h1>

<img src="${pageContext.request.contextPath}/images/rococo.jpg" alt="Centered Banner" class="login-image"/>

<div class="activation-box">
    <% 
        String success = (String) request.getAttribute("success");
        String error = (String) request.getAttribute("error");
        String username = (String) request.getAttribute("username");
    %>
    
    <% if (success != null) { %>
        <div class="success-icon">✓</div>
        <h2>Account Activated Successfully!</h2>
        <p class="activation-message"><%= success %></p>
        <% if (username != null) { %>
            <p class="activation-message">Welcome, <strong><%= username %></strong>! You can now log in to your account.</p>
        <% } %>
        <a href="<%= request.getContextPath() %>/login.jsp" class="button">Go to Login</a>
    <% } else { %>
        <div class="error-icon">✕</div>
        <h2>Activation Failed</h2>
        <p class="activation-message">
            <%= error != null ? error : "There was an error activating your account. The activation link may be invalid or expired." %>
        </p>
        
        <div class="resend-section">
            <h3>Resend Activation Email</h3>
            <p>If your activation link has expired, you can request a new one:</p>
            <form id="resendForm">
                <input type="email" id="email" name="email" placeholder="Email Address" required>
                <button type="submit">Resend Activation Email</button>
            </form>
            <div id="resendMessage" class="message"></div>
        </div>
        
        <a href="<%= request.getContextPath() %>/main-page.jsp" class="button">Back to Home</a>
    <% } %>
</div>

<script>
    document.getElementById('resendForm').addEventListener('submit', function(e) {
        e.preventDefault();
        
        const email = document.getElementById('email').value;
        const messageDiv = document.getElementById('resendMessage');
        
        fetch('<%= request.getContextPath() %>/api/activation/resend', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: email,
                username: null
            })
        })
        .then(response => response.json())
        .then(data => {
            if (data.message) {
                messageDiv.innerHTML = '<div class="success-message">' + data.message + '</div>';
            } else {
                messageDiv.innerHTML = '<div class="error-message">Failed to send activation email.</div>';
            }
        })
        .catch(error => {
            messageDiv.innerHTML = '<div class="error-message">Error: ' + error.message + '</div>';
        });
    });
</script>

</body>
</html> 