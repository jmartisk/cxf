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
        Requested expiration date: <input id="expires" name="expires" type="text" value="2016-06-26T12:23:12.000-01:00"/>
        unset <input name="expires-set" type="checkbox" value="false"/>    <!-- onselect="Document.getElementById('expires').disabled = true; -->
        <br/>
        <input type="submit" value="Submit"/>
    </form>

    <a href="index.jsp">Back to main page</a>

</body>
</html>