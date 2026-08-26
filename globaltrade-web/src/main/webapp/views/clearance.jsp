<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <title>Clearance Workspace</title>
    </head>

    <body>

        <jsp:include page="/views/nav.jsp"/>

        <h2>Customs Clearance Workspace</h2>

        <c:if test="${not empty message}">
            <p style="color:#060">${message}</p>
        </c:if>

        <c:if test="${not empty error}">
            <p style="color:#a00">${error}</p>
        </c:if>

        <c:if test="${sessionActive}">
            <div style="border:2px solid #1f3864;padding:10px;margin:10px 0">
                <h3>Active inspection session</h3>
                <ul>
                    <c:forEach var="note" items="${notes}">
                        <li>${note}</li>
                    </c:forEach>
                </ul>
                <form method="get" action="clearance">
                    <input type="hidden" name="action" value="note"/>
                    <input name="text" style="width:300px" placeholder="Inspection note..."/>
                    <input type="submit" value="Add note"/>
                </form>
                <p>
                    <a href="clearance?action=approve">
                        <b>Approve clearance</b>
                    </a> |
                </p>
                <form method="get" action="clearance">
                    <input type="hidden" name="action" value="reject"/>
                    <input name="reason" placeholder="Rejection reason (mandatory)"/>
                    <input type="submit" value="Reject"/>
                </form>
            </div>
        </c:if>

        <h3>Documents Awaiting Clearance</h3>

        <table border="1" cellpadding="4" style="border-collapse:collapse">
            <tr>
                <th>Doc #</th>
                <th>Type</th>
                <th>Shipment</th>
                <th>Notes so far</th>
                <th></th>
            </tr>

            <c:forEach var="d" items="${awaiting}">
                <tr>
                    <td>${d.id}</td>
                    <td>${d.type}</td>
                    <td>${d.shipment.trackingNumber}</td>
                    <td>${d.inspectionNotes}</td>
                    <td><a href="clearance?action=start&id=${d.id}">Start inspection</a></td>
                </tr>
            </c:forEach>
        </table>
    </body>
</html>