<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%@ page import="etu.sprint.model.MethodInfo" %>
<html>
<head>
    <title>Framework Info</title>
</head>
<body>
    <h1>Annotated Controllers and Methods</h1>
    <table border="1">
        <tr>
            <th>Controller Class</th>
            <th>Method Signatures</th>
        </tr>
        <%
            Map<String, List<MethodInfo>> controllerInfo = (Map<String, List<MethodInfo>>) request.getAttribute("controllerInfo");
            if (controllerInfo != null) {
                for (Map.Entry<String, List<MethodInfo>> entry : controllerInfo.entrySet()) {
        %>
        <tr>
            <td><%= entry.getKey() %></td>
            <td>
                <ul>
                    <% for (MethodInfo methodInfo : entry.getValue()) { %>
                    <li><%= methodInfo.getSignature() %></li>
                    <% } %>
                </ul>
            </td>
        </tr>
        <%
                }
            }
        %>
    </table>
</body>
</html>
