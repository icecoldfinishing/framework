<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Home</title>
</head>
<body>
    <h1>Home Page</h1>
    <p>${message}</p>

    <hr>
    <h3>Navigation</h3>
    <ul>
        <li><a href="${pageContext.request.contextPath}/views/hello.jsp">Aller vers Hello.jsp</a></li>
        <li><a href="${pageContext.request.contextPath}/views/a.html">Aller vers a.html</a></li>
    </ul>
</body>
</html>
