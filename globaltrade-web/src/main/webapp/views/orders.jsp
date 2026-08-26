<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <title>Orders</title>
    </head>
    <body>

        <jsp:include page="/views/nav.jsp"/>

        <h2>Customer Order Placement</h2>

        <c:if test="${not empty message}">
            <p style="color:#060">
                <b>${message}</b>
            </p>
        </c:if>

        <form method="post" action="orders">
            <p>Customer: <input name="customerName" required/></p>
            <p>Shipping destination: <input name="shippingDestination" required/></p>
            <p>
                <label><input type="checkbox" name="international"/> International (files customs export docs)</label>
            </p>
            <table border="1" cellpadding="4" style="border-collapse:collapse">
                <tr>
                    <th>Item</th>
                    <th>Stock</th>
                    <th>Price</th>
                    <th>Quantity</th>
                </tr>
                <c:forEach var="item" items="${items}">
                    <tr>
                        <td>${item.name} (${item.sku})</td>
                        <td>${item.quantity}</td>
                        <td>${item.unitPrice}</td>
                        <td><input name="qty_${item.id}" value="0" size="4"/></td>
                    </tr>
                </c:forEach>
            </table>
            <p>
                <input type="submit" value="Place order"/>
            </p>
        </form>

        <h3>Existing orders</h3>

        <table border="1" cellpadding="4" style="border-collapse:collapse">
            <tr>
                <th>Order #</th>
                <th>Customer</th>
                <th>Destination</th>
                <th>Int'l</th>
                <th>Status</th>
                <th>Total</th>
            </tr>
            <c:forEach var="o" items="${orders}">
                <tr>
                    <td>${o.orderNumber}</td>
                    <td>${o.customerName}</td>
                    <td>${o.shippingDestination}</td>
                    <td>${o.international}</td>
                    <td>${o.status}</td>
                    <td>${o.totalAmount}</td>
                </tr>
            </c:forEach>
        </table>
    </body>
</html>