<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Login - GlobalTrade</title>
</head>
<body style="background:#f4f6f9">
<div style="max-width:360px; margin:80px auto; background:#fff; padding:24px; border:1px solid #ccc; border-radius:6px">
    <h2 style="color:#1f3864">GlobalTrade Logistics</h2>
    <p>Supply Chain Management Portal - sign in</p>
    <form action="j_security_check" method="post">
        <p>Username<br/>
            <input type="text" name="j_username" style="width:100%"/>
        </p>
        <p>Password<br/>
            <input type="password" name="j_password" style="width:100%"/>
        </p>
        <input type="submit" value="Sign in"/>
    </form>
</div>
</body>
</html>
