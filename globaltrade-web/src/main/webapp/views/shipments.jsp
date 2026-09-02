<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <title>Shipments</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css"/>
    </head>
    <body>

        <jsp:include page="/views/nav.jsp"/>

        <h2>Shipment Tracking</h2>

        <form method="get" action="track">
            <input type="text" name="trackingNumber" placeholder="Tracking number (GT-...)"/>
            <input type="submit" value="Track"/>
        </form>

        <p>
            <a href="track">All</a> |
            <a href="track?type=INBOUND">Inbound</a> |
            <a href="track?type=OUTBOUND">Outbound</a>
        </p>

        <c:if test="${not empty error}">
            <p class="msg-err">${error}</p>
        </c:if>

        <c:if test="${not empty shipment}">
            <h3>${shipment.trackingNumber} [${shipment.shipmentType}] - ${shipment.status}</h3>

            <p>Route: ${shipment.routeDescription}
                <br/>
                    ${shipment.origin} -> ${shipment.destination} (currently: ${shipment.currentLocation})
                <br/>
                    ETA: ${shipment.estimatedDelivery} | Delivered: ${shipment.actualDelivery}
            </p>

            <table border="1" cellpadding="4" style="border-collapse:collapse">
                <tr>
                    <th>Event</th>
                    <th>Location</th>
                    <th>Description</th>
                    <th>Time</th>
                </tr>

                <c:forEach var="event" items="${history}">
                    <tr>
                        <td>${event.eventType}</td>
                        <td>${event.location}</td>
                        <td>${event.description}</td>
                        <td>${event.timestamp}</td>
                    </tr>
                </c:forEach>
            </table>
        </c:if>

        <h3>Shipments ${selectedType}</h3>

        <table border="1" cellpadding="4" style="border-collapse:collapse">
            <tr>
                <th>Tracking #</th>
                <th>Type</th>
                <th>Status</th>
                <th>Origin</th>
                <th>Destination</th>
                <th>Route</th>
                <th>ETA</th>
                <th>Actions</th>
            </tr>
            <c:forEach var="s" items="${shipments}">
                <tr>
                    <td>${s.trackingNumber}</td>
                    <td>${s.shipmentType}</td>
                    <td><b>${s.status}</b></td>
                    <td>${s.origin}</td>
                    <td>${s.destination}</td>
                    <td>${s.routeDescription}</td>
                    <td>${s.estimatedDelivery}</td>
                    <td>
                        <c:if test="${canUpdate}">
                            <c:if test="${s.status == 'PENDING'}">
                                <form method="get" action="track" style="display:inline">
                                    <input type="hidden" name="action" value="dispatch"/>
                                    <input type="hidden" name="id" value="${s.id}"/>
                                    <input type="submit" value="Mark In Transit"/>
                                </form>
                            </c:if>

                            <c:if test="${s.status == 'IN_TRANSIT'}">
                                <form method="get" action="track" style="display:inline">
                                    <input type="hidden" name="action" value="deliver"/>
                                    <input type="hidden" name="id" value="${s.id}"/>
                                    <input type="submit" value="Mark Delivered"/>
                                </form>
                                <form method="get" action="track" style="display:inline">
                                    <input type="hidden" name="action" value="delay"/>
                                    <input type="hidden" name="id" value="${s.id}"/>
                                    <input type="submit" value="Mark Delayed"/>
                                </form>
                            </c:if>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </body>
</html>