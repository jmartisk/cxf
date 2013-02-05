<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>New Subscription</title>
</head>
<body>

    <h2>New subscription</h2>
    <form action="CreateSubscriptionServlet">
        Target URL to notify: <input name="targeturl" type="text" width="400px"
                                     value="http://localhost:8080${pageContext.request.contextPath}/services/default"/>
        <br/>
        XPath filter: <input name="filter" type="text" value="//location[text()='Russia']"/><br/>
        <input type="submit" value="Submit"/>
    </form>


</body>
</html>