package com.globaltrade.web.servlet;

import com.globaltrade.core.constants.Roles;
import com.globaltrade.core.monitoring.MonitoringFacadeLocal;
import com.globaltrade.shipment.timer.ShipmentStatusTimerBean;
import com.globaltrade.vendor.timer.VendorAssessmentTimerBean;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin")
@ServletSecurity(
        @HttpConstraint(rolesAllowed = {Roles.ADMIN})
)
public class AdminServlet extends HttpServlet {

    @EJB
    private MonitoringFacadeLocal monitoringFacade;

    @EJB
    private ShipmentStatusTimerBean shipmentTimers;

    @EJB
    private VendorAssessmentTimerBean vendorAssessmentTimer;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        if ("assessVendors".equals(req.getParameter("action"))) {
            vendorAssessmentTimer.runAssessmentNow();
            req.setAttribute("message", "Vendor performance assessment executed");
        }

        req.setAttribute("auditLogs", monitoringFacade.getRecentAuditLogs(50));
        req.setAttribute("alerts", monitoringFacade.getRecentAlerts(20));
        req.setAttribute("activeTimers", shipmentTimers.listActiveTimers());
        req.setAttribute("fleetReport", shipmentTimers.generateFleetStatusReport());

        req.getRequestDispatcher("/views/admin.jsp").forward(req, resp);
    }

}
