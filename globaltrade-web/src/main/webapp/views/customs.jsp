<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <title>Customs</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css"/>
    </head>
    <body>

        <jsp:include page="/views/nav.jsp"/>

        <h2>Customs Documentation & Deadlines</h2>

        <c:if test="${not empty message}">
            <p class="msg-ok">${message}</p>
        </c:if>

        <c:if test="${not empty error}">
            <p class="msg-err">${error}</p>
        </c:if>

        <h3>Approaching Deadlines (3-day window)</h3>

        <table border="1" cellpadding="4" style="border-collapse:collapse">
            <tr>
                <th>Doc #</th>
                <th>Type</th>
                <th>Status</th>
                <th>Filed</th>
                <th>Deadline</th>
            </tr>

            <c:forEach var="d" items="${deadlines}">
                <tr>
                    <td>${d.id}</td>
                    <td>${d.type}</td>
                    <td>${d.status}</td>
                    <td>${d.filedAt}</td>
                    <td>${d.deadline}</td>
                </tr>
            </c:forEach>
        </table>

        <h3>Draft Documents (not yet filed)</h3>
        <table border="1" cellpadding="4">
            <tr><th>Doc #</th><th>Type</th><th>Shipment</th><th>Action</th></tr>
            <c:forEach var="d" items="${drafts}">
                <tr>
                    <td>${d.id}</td><td>${d.type}</td><td>${d.shipment.trackingNumber}</td>
                    <td><a href="customs?action=file&id=${d.id}">File Document</a></td>
                </tr>
            </c:forEach>
        </table>

        <h3>Awaiting Clearance</h3>

        <table border="1" cellpadding="4" style="border-collapse:collapse">
            <tr>
                <th>Doc #</th>
                <th>Type</th>
                <th>Shipment</th>
                <th>Filed</th>
            </tr>

            <c:forEach var="d" items="${awaiting}">
                <tr>
                    <td>${d.id}</td>
                    <td>${d.type}</td>
                    <td>${d.shipment.trackingNumber}</td>
                    <td>${d.filedAt}</td>
                </tr>
            </c:forEach>
        </table>

        <h3>Submit Documentation Package</h3>

        <form method="get" action="customs">
            <input type="hidden" name="action" value="submit"/>
            Shipment:
            <select name="shipmentId">
                <c:forEach var="s" items="${shipments}">
                    <option value="${s.id}">${s.trackingNumber} [${s.shipmentType}]</option>
                </c:forEach>
            </select>

            <input type="submit" value="Submit package"/>
        </form>
    </body>
</html>