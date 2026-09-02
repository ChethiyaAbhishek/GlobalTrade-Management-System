<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <title>Inventory</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css"/>
    </head>
    <body>

        <jsp:include page="/views/nav.jsp"/>

        <h2>Inventory</h2>

        <table border="1" cellpadding="4" style="border-collapse:collapse">
            <tr>
                <th>SKU</th>
                <th>Name</th>
                <th>Warehouse</th>
                <th>Qty</th>
                <th>Reorder level</th>
                <th>Unit price</th>
            </tr>

            <c:forEach var="item" items="${items}">
                <tr <c:if test="${item.quantity < item.reorderLevel}">style="background:#ffe0e0"</c:if>>
                    <td>${item.sku}</td>
                    <td>${item.name}</td>
                    <td>${item.warehouse.location}</td>
                    <td>${item.quantity}</td>
                    <td>${item.reorderLevel}</td>
                    <td>${item.unitPrice}</td>
                </tr>
            </c:forEach>
        </table>
    </body>
</html>