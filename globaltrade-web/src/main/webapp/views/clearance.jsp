<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <title>Clearance Workspace</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css"/>
    </head>

    <body>
        <jsp:include page="/views/nav.jsp"/>
        <h2>Customs Clearance Workspace</h2>

        <c:if test="${not empty message}">
            <div class="msg-ok">${message}</div>
        </c:if>

        <c:if test="${not empty error}">
            <div class="msg-err">${error}</div>
        </c:if>

        <c:if test="${sessionActive}">
            <table>
                <tr>
                    <th>Active Inspection</th>
                    <th>Decision</th>
                </tr>
                <tr>
                    <td>Document under inspection</td>
                    <td>
                        <a href="clearance?action=approve"><b>Approve</b></a> &nbsp;|&nbsp;
                        <form method="get" action="clearance" style="display:inline">
                            <input type="hidden" name="action" value="reject"/>
                            <input type="submit" value="Reject"/>
                        </form>
                    </td>
                </tr>
            </table>
        </c:if>

        <h3>Documents Awaiting Clearance</h3>
        <table>
            <tr>
                <th>Doc #</th>
                <th>Type</th>
                <th>Shipment</th>
                <th></th>
            </tr>

            <c:forEach var="d" items="${awaiting}">
                <tr>
                    <td>${d.id}</td>
                    <td>${d.type}</td>
                    <td>${d.shipment.trackingNumber}</td>
                    <td><a href="clearance?action=start&id=${d.id}">Start inspection</a></td>
                </tr>
            </c:forEach>
        </table>
    </body>
</html>