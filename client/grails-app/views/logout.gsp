<form method="post" action="/client/sso/logout">
        <input type="hidden"
               name="${_csrf.parameterName}"
               value="${_csrf.token}"/>
    <input type="submit" value="logout">
</form>
