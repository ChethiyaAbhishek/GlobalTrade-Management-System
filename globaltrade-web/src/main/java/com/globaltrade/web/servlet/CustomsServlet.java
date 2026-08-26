package com.globaltrade.web.servlet;

import com.globaltrade.core.constants.Roles;
import com.globaltrade.customs.beans.CustomsDocumentationBean;
import com.globaltrade.customs.local.CustomsFacadeLocal;
import com.globaltrade.shipment.local.ShipmentFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/customs")
@ServletSecurity(
        @HttpConstraint(rolesAllowed = {Roles.CUSTOMS_AGENT, Roles.COORDINATOR, Roles.ADMIN})
)
public class CustomsServlet extends HttpServlet {

    @EJB
    private CustomsFacadeLocal customsFacade;

    @EJB
    private ShipmentFacadeLocal shipmentFacade;

    @Inject
    private CustomsDocumentationBean customsDocumentationBean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        if ("submit".equals(req.getParameter("action"))) {
            try {
                Long shipmentId = Long.parseLong(req.getParameter("shipmentId"));

                boolean forceFailure = "true".equals(req.getParameter("force"));

                customsDocumentationBean.submitDocumentationPackage(shipmentFacade.findById(shipmentId));

                req.setAttribute("message", "BMT COMMITTED: documentation package submitted for shipment " + shipmentId);

            } catch (Exception e) {
                req.setAttribute("error", "BMT ROLLED BACK: " + e.getMessage());
            }
        }

        req.setAttribute("deadlines", customsFacade.getApproachingDeadlines());
        req.setAttribute("awaiting", customsFacade.getDocumentsAwaitingClearance());
        req.setAttribute("shipments", shipmentFacade.findAll());

        req.getRequestDispatcher("/views/customs.jsp").forward(req, resp);
    }
}
