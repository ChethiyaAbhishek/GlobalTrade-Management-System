<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="gt-nav">
    <span class="brand">GlobalTrade Logistics</span>
    <a href="${pageContext.request.contextPath}/dashboard">Dashboard</a>

    <c:if test="${pageContext.request.isUserInRole('ADMIN') || pageContext.request.isUserInRole('COORDINATOR')
    || pageContext.request.isUserInRole('WAREHOUSE_MANAGER') || pageContext.request.isUserInRole('CUSTOMS_AGENT')}">
        <a href="${pageContext.request.contextPath}/track">Shipments</a>
    </c:if>

    <c:if test="${pageContext.request.isUserInRole('ADMIN') || pageContext.request.isUserInRole('COORDINATOR')
    || pageContext.request.isUserInRole('WAREHOUSE_MANAGER')}">
        <a href="${pageContext.request.contextPath}/orders">Orders</a>
        <a href="${pageContext.request.contextPath}/inventory">Inventory</a>
    </c:if>

    <c:if test="${pageContext.request.isUserInRole('ADMIN') || pageContext.request.isUserInRole('WAREHOUSE_MANAGER')}">
        <a href="${pageContext.request.contextPath}/purchases">Purchases</a>
    </c:if>

    <c:if test="${pageContext.request.isUserInRole('ADMIN') || pageContext.request.isUserInRole('COORDINATOR')}">
        <a href="${pageContext.request.contextPath}/vendors">Vendors</a>
    </c:if>

    <c:if test="${pageContext.request.isUserInRole('VENDOR_REPRESENTATIVE')}">
        <a href="${pageContext.request.contextPath}/vendors">My Vendor Portal</a>
    </c:if>

    <c:if test="${pageContext.request.isUserInRole('ADMIN') || pageContext.request.isUserInRole('CUSTOMS_AGENT')}">
        <a href="${pageContext.request.contextPath}/customs">Customs</a>
        <a href="${pageContext.request.contextPath}/clearance">Clearance</a>
    </c:if>

    <c:if test="${pageContext.request.isUserInRole('ADMIN')}">
        <a class="active" href="${pageContext.request.contextPath}/admin">Admin</a>
    </c:if>

    <span class="user">${pageContext.request.remoteUser} |
        <a href="${pageContext.request.contextPath}/logout">Logout</a></span>
</div>