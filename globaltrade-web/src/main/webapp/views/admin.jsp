<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
    <head>
        <title>Administration</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css"/>
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

        <table>
            <tr><th>Timer Bean</th><th>Timer Info</th><th>Next Fire</th></tr>
            <c:forEach var="t" items="${activeTimers}">
                <c:set var="parts" value="${fn:split(t, '|')}"/>
                <tr>
                    <td>${fn:trim(parts[0])}</td>
                    <td><span class="badge">${fn:trim(parts[1])}</span></td>
                    <td>${fn:trim(parts[2])}</td>
                </tr>
            </c:forEach>
        </table>

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
                    <th>Last Assessed</th>
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
                                <td><b>${p.overallScore}</b></td>
                                <td>${p.evaluatedAt}</td>
                            </c:when>
                            <c:otherwise><td colspan="4">No assessments yet</td></c:otherwise>
                        </c:choose>
                    </tr>
                </c:forEach>
            </table>
        </c:if>
    </body>
</html>