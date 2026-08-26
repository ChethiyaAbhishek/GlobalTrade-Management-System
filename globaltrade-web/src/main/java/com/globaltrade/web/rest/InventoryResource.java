package com.globaltrade.web.rest;

import com.globaltrade.core.entity.InventoryItem;
import com.globaltrade.inventory.local.InventoryFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/inventory")
@Produces(MediaType.APPLICATION_JSON)
public class InventoryResource {

    @EJB
    private InventoryFacadeLocal inventoryFacade;

    @GET
    @Path("/{itemId}/stock")
    public Response getStock(@PathParam("itemId") Long itemId) {

        InventoryItem item = inventoryFacade.findById(itemId);

        if (item == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"Item not found\"}").build();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("itemId", item.getId());
        result.put("sku", item.getSku());
        result.put("name", item.getName());
        result.put("quantity", item.getQuantity());
        result.put("reorderLevel", item.getReorderLevel());
        result.put("belowReorderLevel", item.getQuantity() < item.getReorderLevel());

        return Response.ok(result).build();
    }

    @GET
    @Path("/low-stock")
    public Response getLowStock() {

        List<Map<String, Object>> low = new ArrayList<>();

        for (InventoryItem item : inventoryFacade.getItemsBelowReorderLevel()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("itemId", item.getId());
            entry.put("name", item.getName());
            entry.put("quantity", item.getQuantity());
            entry.put("reorderLevel", item.getReorderLevel());
            low.add(entry);
        }

        return Response.ok(low).build();
    }
}
