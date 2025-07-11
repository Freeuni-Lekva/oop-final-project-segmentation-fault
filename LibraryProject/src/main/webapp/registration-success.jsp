<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registration Successful - Freeuni Library</title>
    <link href="https://fonts.googleapis.com/css2?family=Poppins&display=swap" rel="stylesheet">
    <%@ include file="styles.jsp" %>
    
    <style>
        .success-box {
            background: white;
            padding: 2rem;
            width: 100%;
            max-width: 500px;
            border-radius: 10px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
            text-align: center;
        }

        .success-box h2 {
            font-family: 'Poppins', sans-serif;
            font-size: 25px;
            font-weight: bold;
            color: #333;
            margin-bottom: 1rem;
        }

        .success-icon {
            font-size: 48px;
            color: #4ade80;
            margin-bottom: 20px;
        }

        .success-message {
            font-size: 16px;
            line-height: 1.6;
            color: #555;
            margin-bottom: 20px;
        }

        .button {
            display: inline-block;
            background-color: #af8d8d;
            color: white;
            padding: 12px 25px;
            text-decoration: none;
            border-radius: 5px;
            font-weight: 500;
            margin: 10px 5px;
            transition: background-color 0.3s ease;
            border: none;
            cursor: pointer;
            font-size: 14px;
        }

        .button:hover {
            background-color: #d39797;
        }

        .button.secondary {
            background-color: #6c757d;
        }

        .button.secondary:hover {
            background-color: #5a6268;
        }

        .activation-info {
            background: #f8f9fa;
            border-radius: 8px;
            padding: 20px;
            margin: 20px 0;
            border: 1px solid #dee2e6;
            text-align: left;
        }

        .activation-info h3 {
            font-size: 18px;
            color: #333;
            margin-bottom: 10px;
        }

        .activation-info ul {
            color: #555;
            line-height: 1.6;
        }

        .resend-section {
            background: #e3f2fd;
            border-radius: 8px;
            padding: 20px;
            margin: 20px 0;
            border: 1px solid #bbdefb;
        }

        .resend-section h3 {
            font-size: 18px;
            color: #333;
            margin-bottom: 10px;
        }

        .message {
            margin-top: 15px;
            padding: 10px;
            border-radius: 4px;
            font-size: 14px;
        }

        .success-message-alert {
            background-color: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }

        .error-message {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }

        .email-display {
            font-weight: bold;
            color: #af8d8d;
        }
    </style>
</head>
<body>

<h1 class="header-text">Explore the world's knowledge, cultures and ideas</h1>

<img src="${pageContext.request.contextPath}/images/rococo.jpg" alt="Centered Banner" class="login-image"/>

<div class="success-box">
    <% String userEmail = request.getParameter("email"); %>
    
    <div class="success-icon">📧</div>
    <h2>Registration Successful!</h2>
    <p class="success-message">
        Thank you for registering with Freeuni Library! We've sent an activation email to 
        <% if (userEmail != null) { %>
            <span class="email-display"><%= userEmail %></span>
        <% } else { %>
            your email address
        <% } %>.
    </p>
    
    <div class="activation-info">
        <h3>Next Steps:</h3>
        <ul>
            <li>Check your email inbox for an activation message</li>
            <li>Click the activation link in the email</li>
            <li>Once activated, you can log in to your account</li>
            <li>If you don't see the email, check your spam folder</li>
        </ul>
    </div>
    
    <div class="resend-section">
        <h3>Didn't receive the email?</h3>
        <p>If you don't see the activation email in your inbox or spam folder, you can request a new one:</p>
        <% if (userEmail != null) { %>
            <button type="button" class="button secondary" onclick="resendActivationEmail()">
                Resend Activation Email
            </button>
        <% } else { %>
            <p style="color: #6c757d; font-size: 14px;">Please go to the login page and use the resend option there.</p>
        <% } %>
        <div id="resendMessage" class="message"></div>
    </div>
    
    <p class="success-message">
        <strong>Important:</strong> You must activate your account before you can log in.
    </p>
    
    <a href="<%= request.getContextPath() %>/login.jsp" class="button">Go to Login</a>
    <a href="<%= request.getContextPath() %>/main-page.jsp" class="button">Back to Home</a>
</div>

<script>
    function resendActivationEmail() {
        const button = document.querySelector('.button.secondary');
        const messageDiv = document.getElementById('resendMessage');
        const userEmail = '<%= userEmail != null ? userEmail : "" %>';
        
        if (!userEmail) {
            messageDiv.innerHTML = '<div class="error-message">Email address not available. Please use the resend option on the login page.</div>';
            return;
        }
        
        // Disable button and show loading state
        button.disabled = true;
        button.textContent = 'Sending...';
        messageDiv.innerHTML = '';
        
        fetch('<%= request.getContextPath() %>/api/activation/resend', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: userEmail,
                username: null
            })
        })
        .then(response => response.json())
        .then(data => {
            if (data.message) {
                messageDiv.innerHTML = '<div class="success-message-alert">' + data.message + '</div>';
            } else {
                messageDiv.innerHTML = '<div class="error-message">Failed to send activation email. Please try again later.</div>';
            }
        })
        .catch(error => {
            console.error('Error:', error);
            messageDiv.innerHTML = '<div class="error-message">Error: ' + error.message + '</div>';
        })
        .finally(() => {
            // Re-enable button
            button.disabled = false;
            button.textContent = 'Resend Activation Email';
        });
    }
</script>

</body>
</html> 