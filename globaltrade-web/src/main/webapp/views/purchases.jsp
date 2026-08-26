<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <title>Purchase Requests</title>
    </head>
    <body>
        <jsp:include page="/views/nav.jsp"/>

        <h2>Purchase Requests (Automated Replenishment)</h2>

        <c:if test="${not empty message}">
            <p style="color:#060">${message}</p>
        </c:if>

        <c:if test="${not empty error}">
            <p style="color:#a00">${error}</p>
        </c:if>

        <p>Filter:
            <a href="purchases">All</a> |
            <a href="purchases?status=PENDING">Pending</a> |
            <a href="purchases?status=APPROVED">Approved</a> |
            <a href="purchases?status=ORDERED">Ordered</a> |
            <a href="purchases?status=FULFILLED">Fulfilled</a>
        </p>

        <table border="1" cellpadding="4" style="border-collapse:collapse">

            <tr>
                <th>#</th>
                <th>Item</th>
                <th>Qty</th>
                <th>Vendor</th>
                <th>Trigger</th>
                <th>Unit cost</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>

            <c:forEach var="r" items="${requests}">
                <tr>
                    <td>${r.id}</td>
                    <td>${r.inventoryItem.name}</td>
                    <td>${r.requestedQty}</td>
                    <td>${r.vendor.name}</td>
                    <td>${r.triggerType}</td>
                    <td>${r.unitCost}</td>
                    <td><b>${r.requestStatus}</b></td>
                    <td>
                        <c:if test="${r.requestStatus == 'PENDING'}">
                            <a href="purchases?action=approve&id=${r.id}">Approve</a> |
                            <a href="purchases?action=reject&id=${r.id}">Reject</a>
                        </c:if>

                        <c:if test="${r.requestStatus == 'APPROVED'}">
                            <a href="purchases?action=order&id=${r.id}">Place order</a>
                        </c:if>

                        <c:if test="${r.requestStatus == 'ORDERED'}">
                            <a href="purchases?action=fulfill&id=${r.id}">Confirm receipt</a>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
        </table>

        <h3>Manual Override</h3>

        <form method="post" action="purchases">
            Item:
            <select name="itemId">
                <c:forEach var="item" items="${items}">
                    <option value="${item.id}">${item.name} (stock: ${item.quantity})</option>
                </c:forEach>
            </select>

            Vendor:
            <select name="vendorId">
                <c:forEach var="v" items="${vendors}">
                    <option value="${v.id}">${v.name}</option>
                </c:forEach>
            </select>

            Qty: <input name="quantity" size="4" value="10"/>

            <input type="submit" value="Create request"/>
        </form>
    </body>
</html>