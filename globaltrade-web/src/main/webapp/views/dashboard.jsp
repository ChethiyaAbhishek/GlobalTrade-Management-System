<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Dashboard</title>
</head>
<body>
    <jsp:include page="/views/nav.jsp"/>

    <h2>Real-Time Supply Chain Monitoring</h2>
    <p><i>${identity}</i></p>


    <table border="1" cellpadding="6" style="border-collapse:collapse">

        <tr>
            <th>Total shipments</th>
            <th>Delayed</th>
            <th>Low stock items</th>
            <th>Customs deadlines (3d)</th>
            <th>Pending purchase requests</th>
        </tr>
        <tr>
            <td>${dashboard.totalShipments}</td>
            <td style="color:#a00">
                <b>${dashboard.delayedCount}</b>
            </td>
            <td>${dashboard.lowStockCount}</td>
            <td>${dashboard.pendingCustomsDocs}</td>
            <td>${pendingPurchases}</td>
        </tr>

    </table>

    <h3>Recent Alerts (JMS pipeline output)</h3>

    <table border="1" cellpadding="4" style="border-collapse:collapse">

        <tr>
            <th>Type</th>
            <th>Message</th>
            <th>Processed at</th>
        </tr>

        <c:forEach var="alert" items="${dashboard.recentAlerts}">
            <tr>
                <td>${alert.alertType}</td>
                <td>${alert.message}</td>
                <td>${alert.processedAt}</td>
            </tr>
        </c:forEach>

    </table>
</body></html>
