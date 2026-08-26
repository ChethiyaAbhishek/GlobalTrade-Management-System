<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <title>Administration</title>
    </head>
    <body>

        <jsp:include page="/views/nav.jsp"/>

        <h2>Administration Console</h2>

        <c:if test="${not empty message}">
            <p style="color:#060">${message}</p>
        </c:if>

        <p>
            <a href="admin?action=assessVendors"><b>Run vendor assessment now</b></a>
        </p>

        <h3>Active EJB Timers</h3>

        <ul>
            <c:forEach var="t" items="${activeTimers}">
                <li>${t}</li>
            </c:forEach>
        </ul>

        <h3>Fleet Status Report</h3>

        <pre style="background:#f0f0f0;padding:10px">${fleetReport}</pre>

        <h3>Recent Audit Log Entries</h3>

        <table border="1" cellpadding="4" style="border-collapse:collapse">
            <tr>
                <th>User</th>
                <th>Bean</th>
                <th>Method</th>
                <th>Result</th>
                <th>Time (ms)</th>
                <th>When</th>
            </tr>

            <c:forEach var="log" items="${auditLogs}">
                <tr>
                    <td>${log.username}</td>
                    <td>${log.beanName}</td>
                    <td>${log.methodName}</td>
                    <td>${log.result}</td>
                    <td>${log.executionTimeMs}</td>
                    <td>${log.timestamp}</td>
                </tr>
            </c:forEach>
        </table>

        <h3>Recent Alerts</h3>

        <table border="1" cellpadding="4" style="border-collapse:collapse">
            <tr>
                <th>Type</th>
                <th>Message</th>
                <th>Source</th>
                <th>When</th>
            </tr>

            <c:forEach var="a" items="${alerts}">
                <tr>
                    <td>${a.alertType}</td>
                    <td>${a.message}</td>
                    <td>${a.source}</td>
                    <td>${a.processedAt}</td>
                </tr>
            </c:forEach>
        </table>
    </body>
</html>