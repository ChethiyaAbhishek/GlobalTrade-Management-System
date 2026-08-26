package com.globaltrade.web.servlet;

import com.globaltrade.core.constants.Roles;
import com.globaltrade.core.entity.Shipment;
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

        String trackingNumber = req.getParameter("trackingNumber");

        if (trackingNumber != null && !trackingNumber.trim().isEmpty()) {
            try {

                Shipment shipment = shipmentFacade.findByTrackingNumber(trackingNumber.trim());

                req.setAttribute("shipment", shipment);
                req.setAttribute("history", shipmentFacade.getTrackingHistory(shipment.getId()));

            } catch (ShipmentNotFoundException e) {
                req.setAttribute("error", e.getMessage());
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
