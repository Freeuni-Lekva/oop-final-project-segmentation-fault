<script>
  // Check if user is authorized to access admin page
  async function checkAdminAccess() {
    try {
      // Try to access a bookkeeper-only endpoint to verify permissions
      const response = await fetch("${pageContext.request.contextPath}/api/bookkeeper/users", {
        method: "GET",
        credentials: "include"
      });
      
      if (!response.ok) {
        // User is not authorized, silently redirect
        window.location.href = "${pageContext.request.contextPath}/main-page.jsp";
        return;
      }
    } catch (error) {
      // If there's any error, redirect to main page
      window.location.href = "${pageContext.request.contextPath}/main-page.jsp";
    }
  }
  
  // Run the check when page loads
  checkAdminAccess();
</script> 