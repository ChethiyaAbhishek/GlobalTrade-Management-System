package com.globaltrade.web.servlet;

import com.globaltrade.core.constants.Roles;
import com.globaltrade.core.entity.Shipment;
import com.globaltrade.core.enums.ShipmentStatus;
import com.globaltrade.core.enums.ShipmentType;
import com.globaltrade.core.exception.ShipmentNotFoundException;
import com.globaltrade.shipment.local.ShipmentFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/track")
@ServletSecurity(
        @HttpConstraint(rolesAllowed = {Roles.COORDINATOR, Roles.WAREHOUSE_MANAGER, Roles.ADMIN})
)
public class ShipmentTrackingServlet extends HttpServlet {

    @EJB
    private ShipmentFacadeLocal shipmentFacade;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        String action = req.getParameter("action");

        boolean canUpdate = req.isUserInRole(Roles.ADMIN) || req.isUserInRole(Roles.WAREHOUSE_MANAGER);
        req.setAttribute("canUpdate", canUpdate);

        if (action != null && req.getParameter("id") != null) {
            if (!canUpdate) {
                req.setAttribute("error", "ACCESS DENIED: only warehouse managers update shipment statuses");
            } else {
                Long shipmentId = Long.parseLong(req.getParameter("id"));
                try {
                    switch (action) {
                        case "dispatch":
                            shipmentFacade.updateStatus(shipmentId, ShipmentStatus.IN_TRANSIT);
                            req.setAttribute("message", "Shipment marked IN_TRANSIT (dispatched)");
                            break;
                        case "deliver":
                            shipmentFacade.updateStatus(shipmentId, ShipmentStatus.DELIVERED);
                            req.setAttribute("message", "Shipment marked DELIVERED - actualDelivery stamped");
                            break;
                        case "delay":
                            shipmentFacade.updateStatus(shipmentId, ShipmentStatus.DELAYED);
                            req.setAttribute("message", "Shipment marked DELAYED - alert sent to coordinator");
                            break;
                        default:
                            break;
                    }
                } catch (ShipmentNotFoundException e) {
                    req.setAttribute("error", e.getMessage());
                } catch (IllegalArgumentException e) {
                    req.setAttribute("error", "Invalid status transition: " + e.getMessage());
                } catch (Exception e) {
                    req.setAttribute("error", "An Error Occurred: " + e.getMessage());
                }
            }
        }

        String trackingNumber = req.getParameter("trackingNumber");

        if (trackingNumber != null && !trackingNumber.trim().isEmpty()) {
            try {

                Shipment shipment = shipmentFacade.findByTrackingNumber(trackingNumber.trim());

                req.setAttribute("shipment", shipment);
                req.setAttribute("history", shipmentFacade.getTrackingHistory(shipment.getId()));

            } catch (ShipmentNotFoundException e) {
                req.setAttribute("error", e.getMessage());
            } catch (Exception e) {
                req.setAttribute("error", "An Error Occurred: " + e.getMessage());
            }
        }

        String type = req.getParameter("type");

        if ("INBOUND".equals(type) || "OUTBOUND".equals(type)) {

            req.setAttribute("shipments", shipmentFacade.findByType(ShipmentType.valueOf(type)));
            req.setAttribute("selectedType", type);

        } else {
            req.setAttribute("shipments", shipmentFacade.findAll());
        }

        req.getRequestDispatcher("/views/shipments.jsp").forward(req, resp);
    }
}
