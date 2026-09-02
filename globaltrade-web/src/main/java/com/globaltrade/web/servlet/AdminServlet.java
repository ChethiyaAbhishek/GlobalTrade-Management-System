package com.globaltrade.web.servlet;

import com.globaltrade.core.constants.Roles;
import com.globaltrade.core.monitoring.MonitoringFacadeLocal;
import com.globaltrade.customs.timer.CustomsDeadlineTimerBean;
import com.globaltrade.customs.timer.CustomsDocumentationTimerBean;
import com.globaltrade.inventory.timer.InventoryMonitorTimerBean;
import com.globaltrade.shipment.timer.ShipmentStatusTimerBean;
import com.globaltrade.vendor.local.VendorFacadeLocal;
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
import java.util.ArrayList;
import java.util.List;

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
    private VendorFacadeLocal vendorFacade;

    @EJB
    private InventoryMonitorTimerBean inventoryTimer;

    @EJB
    private VendorAssessmentTimerBean vendorTimer;

    @EJB
    private CustomsDeadlineTimerBean customsDeadlineTimer;

    @EJB
    private CustomsDocumentationTimerBean customsDocTimer;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        if ("assessVendors".equals(req.getParameter("action"))) {
            vendorTimer.runAssessmentNow();
            req.setAttribute("message", "Vendor performance assessment executed");
        }

        req.setAttribute("auditLogs", monitoringFacade.getRecentAuditLogs(50));
        req.setAttribute("alerts", monitoringFacade.getRecentAlerts(20));
        req.setAttribute("fleetReport", shipmentTimers.generateFleetStatusReport());
        req.setAttribute("vendors", vendorFacade.findAll());
        req.setAttribute("latestScores", vendorFacade.getLatestPerformancesByVendor());

        List<String> allTimers = new ArrayList<>();
        allTimers.addAll(shipmentTimers.listActiveTimers());
        allTimers.addAll(inventoryTimer.listMyTimers());
        allTimers.addAll(vendorTimer.listMyTimers());
        allTimers.addAll(customsDeadlineTimer.listMyTimers());
        allTimers.addAll(customsDocTimer.listMyTimers());
        req.setAttribute("activeTimers", allTimers);

        req.getRequestDispatcher("/views/admin.jsp").forward(req, resp);
    }

}
