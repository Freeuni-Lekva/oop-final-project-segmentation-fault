<%--
  Created by IntelliJ IDEA.
  User: elenejobava
  Date: 6/17/25
  Time: 01:36
  To change this template use File | Settings | File Templates.
--%>
<script>
  function validatePasswords() {
    const password = document.querySelector('input[name="password"]').value;
    const confirmPassword = document.querySelector('input[name="confirmPassword"]').value;

    if (password !== confirmPassword) {
      alert('Passwords do not match, please try again');
      return false;
    }
    return true;
  }

  document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('form');
    form.addEventListener('submit', function(e) {
      if (!validatePasswords()) {
        e.preventDefault();
      }
    });
  });
</script>
