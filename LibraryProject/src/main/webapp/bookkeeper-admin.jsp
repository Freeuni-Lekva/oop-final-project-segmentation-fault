<%@ include file="components/admin-header.jsp" %>

<%@ include file="components/admin-auth.jsp" %>

<%@ include file="components/admin-navigation.jsp" %>

<div id="add-book" class="tab-content active">
  <%@ include file="admin_pages/add-book-page.jsp" %>
</div>

<div id="manage-orders" class="tab-content">
  <%@ include file="admin_pages/manage-orders-page.jsp" %>
</div>

<div id="manage-users" class="tab-content">
  <%@ include file="admin_pages/user-management-page.jsp" %>
</div>

<div id="book-collection" class="tab-content">
  <%@ include file="admin_pages/book-collection-page.jsp" %>
</div>

<%@ include file="components/admin-footer.jsp" %> 