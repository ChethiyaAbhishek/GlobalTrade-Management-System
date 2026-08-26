package com.globaltrade.web.rest;

import com.globaltrade.core.dto.VendorReportDTO;
import com.globaltrade.vendor.remote.VendorFacadeRemote;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBAccessException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/vendors")
@Produces(MediaType.APPLICATION_JSON)
public class VendorResource {

    @EJB
    private VendorFacadeRemote vendorFacade;

    @GET
    @Path("/{vendorId}/report")
    public Response getReport(@PathParam("vendorId") Long vendorId) {

        try {
            VendorReportDTO report = vendorFacade.getVendorReport(vendorId);

            if (report == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"Vendor not found\"}").build();
            }

            return Response.ok(report).build();

        } catch (EJBAccessException e) {
            return Response.status(Response.Status.FORBIDDEN).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

}
