package com.globaltrade.web.rest;

import com.globaltrade.core.dto.ShipmentStatusDTO;
import com.globaltrade.core.entity.Shipment;
import com.globaltrade.core.enums.ShipmentStatus;
import com.globaltrade.core.exception.ShipmentNotFoundException;
import com.globaltrade.shipment.local.ShipmentFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

@Path("/shipments")
@Produces(MediaType.APPLICATION_JSON)
public class ShipmentResource {

    @EJB
    private ShipmentFacadeLocal shipmentFacadeLocal;

    @GET
    @Path("/{trackingNumber}")
    public Response getStatus(@PathParam("trackingNumber") String trackingNumber) {
        try {
            ShipmentStatusDTO dto = shipmentFacadeLocal.getShipmentStatus(trackingNumber);
            return Response.ok(dto).build();
        } catch (ShipmentNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/delayed")
    public Response getDelayed() {

        List<ShipmentStatusDTO> delayed = new ArrayList<>();

        for (Shipment s : shipmentFacadeLocal.findDelayedShipments()) {
            delayed.add(new ShipmentStatusDTO(s.getTrackingNumber(), s.getStatus().name(), s.getCurrentLocation(), s.getUpdatedAt()));
        }

        return Response.ok(delayed).build();
    }

    @POST
    @Path("/{trackingNumber}/status")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateStatus(@PathParam("trackingNumber") String trackingNumber, @FormParam("status") String status) {
        try {
            Shipment shipment = shipmentFacadeLocal.findByTrackingNumber(trackingNumber);

            shipmentFacadeLocal.updateStatus(shipment.getId(), ShipmentStatus.valueOf(status));

            return Response.ok("{\"updated\":\"" + trackingNumber + "\",\"status\":\"" + status + "\"}").build();

        } catch (ShipmentNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"" + e.getMessage() + "\"}").build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid status value\"}").build();
        }
    }

}
