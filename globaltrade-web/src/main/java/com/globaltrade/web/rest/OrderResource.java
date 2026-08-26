package com.globaltrade.web.rest;

import com.globaltrade.core.dto.OrderStatusDTO;
import com.globaltrade.core.entity.Order;
import com.globaltrade.core.exception.InsufficientInventoryException;
import com.globaltrade.core.exception.OrderProcessingException;
import com.globaltrade.order.local.OrderFacadeLocal;
import com.globaltrade.order.remote.OrderFacadeRemote;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.Map;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
public class OrderResource {

    @EJB
    private OrderFacadeLocal orderFacadeLocal;

    @EJB
    private OrderFacadeRemote orderFacadeRemote;

    @GET
    @Path("/{orderNumber}")
    public Response getStatus(@PathParam("orderNumber") String orderNumber) {

        OrderStatusDTO dto = orderFacadeRemote.getOrderStatus(orderNumber);

        if (dto == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"Order not found: " + orderNumber + "\"}")
                    .build();
        }

        return Response.ok(dto).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response placeOrder(@FormParam("customerName") String customerName,
                               @FormParam("shippingDestination") String shippingDestination,
                               @FormParam("international") @DefaultValue("false") boolean international,
                               @FormParam("itemId") Long itemId,
                               @FormParam("quantity") int quantity) {
        try {

            Map<Long, Integer> items = new LinkedHashMap<>();
            items.put(itemId, quantity);

            Order order = orderFacadeLocal.placeOrder(
                    customerName, shippingDestination, international, items);

            return Response.status(Response.Status.CREATED)
                    .entity("{\"orderNumber\":\"" + order.getOrderNumber()
                            + "\",\"total\":" + order.getTotalAmount() + "}").build();

        } catch (InsufficientInventoryException e) {

            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}").build();

        } catch (OrderProcessingException | IllegalArgumentException e) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}").build();

        }
    }
}
