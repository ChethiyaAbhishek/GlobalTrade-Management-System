<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <title>Vendors</title>
    </head>
    <body>

        <jsp:include page="/views/nav.jsp"/>

        <h2>Vendor Management</h2>
        <p><i>${identity}</i></p>

        <c:if test="${not empty message}">
            <p style="color:#060">${message}</p>
        </c:if>
        <c:if test="${not empty error}">
            <p style="color:#a00">${error}</p>
        </c:if>

        <c:if test="${not empty report}">
            <h3>Vendor Report</h3>
            <p>${report.vendorName} (${report.country}) - rating ${report.rating},
                on-time ${report.onTimeRate}%, overall score ${report.overallScore}</p>
        </c:if>

        <table border="1" cellpadding="4" style="border-collapse:collapse">
            <tr>
                <th>#</th>
                <th>Name</th>
                <th>Country</th>
                <th>Email</th>
                <th>Rating</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>

            <c:forEach var="v" items="${vendors}">
                <tr>
                    <td>${v.id}</td>
                    <td>${v.name}</td>
                    <td>${v.country}</td>
                    <td>${v.contactEmail}</td>
                    <td>${v.rating}</td>
                    <td>${v.status}</td>
                    <td>
                        <a href="vendors?action=report&id=${v.id}">Report</a> |
                        <a href="vendors?action=suspend&id=${v.id}">Suspend</a>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </body>
</html>