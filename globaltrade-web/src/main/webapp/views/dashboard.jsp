<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <title>Dashboard</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css"/>
    </head>
    <body>

        <jsp:include page="/views/nav.jsp"/>

        <c:if test="${pageContext.request.isUserInRole('VENDOR_REPRESENTATIVE')}">
            <h2>My Vendor Portal</h2>
            <c:choose>
                <c:when test="${not empty vendors}">
                    <table>
                        <tr>
                            <th>Company</th><th>Country</th><th>Rating</th><th>Status</th>
                            <th>On-Time %</th><th>Defect %</th><th>Overall Score</th>
                        </tr>
                        <c:forEach var="v" items="${vendors}">
                            <tr>
                                <td>${v.name}</td>
                                <td>${v.country}</td>
                                <td>${v.rating}</td>
                                <td>${v.status}</td>
                                <c:set var="p" value="${myScores[v.id]}"/>
                                <c:choose>
                                    <c:when test="${p != null}">
                                        <td>${p.onTimeDeliveryRate}</td>
                                        <td>${p.defectRate}</td>
                                        <td><b>${p.overallScore}</b></td>
                                    </c:when>
                                    <c:otherwise><td colspan="3">No assessments yet</td></c:otherwise>
                                </c:choose>
                            </tr>
                        </c:forEach>
                    </table>
                </c:when>
                <c:otherwise><div class="msg-err">No vendor account linked to your user.</div></c:otherwise>
            </c:choose>
        </c:if>

        <c:if test="${not pageContext.request.isUserInRole('VENDOR_REPRESENTATIVE')}">
            <h2>Supply Chain Dashboard</h2>
            <p><i>${identity}</i></p>

            <table>
                <tr>
                    <th>Total Shipments</th>
                    <th>Delayed</th>
                </tr>
                <tr>
                    <td>${dashboard.totalShipments}</td>
                    <td style="color:#b91c1c">
                        <b>${dashboard.delayedCount}</b>
                    </td>
                </tr>
            </table>

            <c:if test="${pageContext.request.isUserInRole('COORDINATOR') || pageContext.request.isUserInRole('ADMIN')}">
                <h3>Orders</h3>
                <table>
                    <tr>
                        <th>Total Orders</th>
                    </tr>
                    <tr>
                        <td>${orderCount}</td>
                    </tr>
                </table>
            </c:if>

            <c:if test="${pageContext.request.isUserInRole('WAREHOUSE_MANAGER') || pageContext.request.isUserInRole('ADMIN')}">
                <h3>Warehouse</h3>
                <table>
                    <tr>
                        <th>Low-Stock Items</th>
                        <th>Pending Purchase Requests</th>
                    </tr>
                    <tr>
                        <td>${dashboard.lowStockCount}</td>
                        <td>${pendingPurchases}</td>
                    </tr>
                </table>
            </c:if>

            <c:if test="${pageContext.request.isUserInRole('CUSTOMS_AGENT') || pageContext.request.isUserInRole('ADMIN')}">
                <h3>Customs Compliance</h3>
                <table>
                    <tr>
                        <th>Deadlines (3-day window)</th>
                        <th>Awaiting Clearance</th>
                    </tr>
                    <tr>
                        <td>${dashboard.pendingCustomsDocs}</td>
                        <td>${awaitingClearance}</td>
                    </tr>
                </table>
            </c:if>

            <c:if test="${pageContext.request.isUserInRole('ADMIN')}">
                <h3>Vendor Performance (automated assessments)</h3>
                <table>
                    <tr>
                        <th>Vendor</th>
                        <th>Country</th>
                        <th>Rating</th>
                        <th>On-Time %</th>
                        <th>Defect %</th>
                        <th>Overall</th>
                    </tr>

                    <c:forEach var="v" items="${vendors}">
                        <tr>
                            <td>${v.name}</td>
                            <td>${v.country}</td>
                            <td>${v.rating}</td>

                            <c:set var="p" value="${latestScores[v.id]}"/>
                            <c:choose>
                                <c:when test="${p != null}">
                                    <td>${p.onTimeDeliveryRate}</td>
                                    <td>${p.defectRate}</td>
                                    <td>
                                        <b>${p.overallScore}</b>
                                    </td>
                                </c:when>
                                <c:otherwise><td colspan="3">No assessments yet</td></c:otherwise>
                            </c:choose>
                        </tr>
                    </c:forEach>
                </table>
            </c:if>

            <h3>Recent Alerts</h3>
            <table>
                <tr><th>Type</th><th>Message</th><th>Processed At</th></tr>
                <c:forEach var="a" items="${dashboard.recentAlerts}">
                    <tr>
                        <td><span class="badge">${a.alertType}</span></td>
                        <td>${a.message}</td>
                        <td>${a.processedAt}</td>
                    </tr>
                </c:forEach>
            </table>
        </c:if>
    </body>
</html>