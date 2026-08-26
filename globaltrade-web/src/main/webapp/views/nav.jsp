<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div style="background:#1f3864;padding:10px;color:#fff">
    <b>GlobalTrade SCM</b> &nbsp;|&nbsp;
    <a style="color:#fff" href="${pageContext.request.contextPath}/dashboard">Dashboard</a> |
    <a style="color:#fff" href="${pageContext.request.contextPath}/track">Shipments</a> |
    <a style="color:#fff" href="${pageContext.request.contextPath}/orders">Orders</a> |
    <a style="color:#fff" href="${pageContext.request.contextPath}/inventory">Inventory</a> |
    <a style="color:#fff" href="${pageContext.request.contextPath}/purchases">Purchases</a> |
    <a style="color:#fff" href="${pageContext.request.contextPath}/vendors">Vendors</a> |
    <a style="color:#fff" href="${pageContext.request.contextPath}/customs">Customs</a> |
    <a style="color:#fff" href="${pageContext.request.contextPath}/clearance">Clearance</a> |
    <a style="color:#fff" href="${pageContext.request.contextPath}/admin">Admin</a>
    <span style="float:right">User: <b>${pageContext.request.remoteUser}</b></span>
</div>
