<%@ page import="demo.wseventing.ApplicationSingleton" %>
<%@ page import="demo.wseventing.eventapi.CatastrophicEventSinkImpl" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title></title>
</head>
<body>
    <h2>Event sinks</h2>
    <h3>List of registered event sinks</h3>
    <table border="1">
        <tr><td>URL</td><td>Is started?</td><td>Event log</td></tr>
        <%
            for (CatastrophicEventSinkImpl sink : ApplicationSingleton.getInstance().getEventSinks()) {
                %>
                    <tr>
                        <td><%=sink.getFullURL()%></td>
                        <td><%=sink.isRunning()%></td>
                        <td><a href="eventlog.jsp?sink=<%=sink.getShortURL()%>">List events</a></td>
                    </tr>
                <%
            }
        %>
    </table>
</body>
</html>