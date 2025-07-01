<%--
  Created by IntelliJ IDEA.
  User: elenejobava
  Date: 6/16/25
  Time: 20:09
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Authorization Page</title>
  <link href="https://fonts.googleapis.com/css2?family=Poppins&display=swap" rel="stylesheet">
  <%@ include file="styles.jsp" %>

  <style>
    #signup-options button {
      margin: 6px;
      padding: 10px 15px;
      background-color: #66aa9b;
      color: white;
      border: none;
      border-radius: 5px;
      cursor: pointer;
    }

    #signup-options button:hover {
      background-color: #437e6e;
    }
  </style>
</head>
<body>

<h1 class="header-text">Explore the world's knowledge, cultures and ideas</h1>

<img src="${pageContext.request.contextPath}/images/image4.jpg" alt="Centered Banner" class="login-image"/>

<div class="login-box">
  <h2>Sign in to Freeuni Library</h2>

  <form id="loginForm">
    <input type="text" id="username" name="username" placeholder="Username" required />
    <input type="password" id="password" name="password" placeholder="Password" required />
    <button type="submit">Login</button>
  </form>

  <div class="signup-section">
    <p>Not a member? <a href="#" onclick="showSignupOptions()">Sign up</a></p>
    <small>
      By creating an account, you agree to the Freeuni Library's Terms of Service and Privacy Policy
    </small>

    <div id="signup-options" style="display: none; margin-top: 1rem;">
      <button onclick="window.location.href='${pageContext.request.contextPath}/signup-user.jsp'">Sign up as User</button>
      <button onclick="window.location.href='${pageContext.request.contextPath}/signup-bookkeeper.jsp'">Sign up as Bookkeeper</button>
    </div>
  </div>
</div>

<script>
  function showSignupOptions() {
    document.getElementById("signup-options").style.display = "block";
  }

  document.getElementById("loginForm").addEventListener("submit", async function (e) {
    e.preventDefault();
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value;

    try {
      const response = await fetch("<%= request.getContextPath() %>/api/authorization/login", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({ username, password }),
        credentials: "include"
      });


      const result = await response.json();
      if (response.ok && result.redirect) {
        window.location.href = result.redirect;
      } else {
        alert(result.message || "Login failed");
      }

    } catch (error) {
      console.error(error);
      alert("Check connection");
    }
  });
</script>

</body>
</html>