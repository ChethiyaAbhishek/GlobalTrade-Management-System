package com.globaltrade.web.servlet;

import com.globaltrade.core.constants.Roles;
import com.globaltrade.core.dto.DashboardDTO;
import com.globaltrade.core.entity.Vendor;
import com.globaltrade.core.entity.VendorPerformance;
import com.globaltrade.core.enums.PurchaseRequestStatus;
import com.globaltrade.core.monitoring.MonitoringFacadeLocal;
import com.globaltrade.customs.local.CustomsFacadeLocal;
import com.globaltrade.inventory.local.InventoryFacadeLocal;
import com.globaltrade.inventory.local.PurchaseRequestFacadeLocal;
import com.globaltrade.order.local.OrderFacadeLocal;
import com.globaltrade.shipment.local.ShipmentFacadeLocal;
import com.globaltrade.vendor.local.VendorFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/dashboard")
@ServletSecurity(
        @HttpConstraint(rolesAllowed = {Roles.COORDINATOR, Roles.CUSTOMS_AGENT, Roles.WAREHOUSE_MANAGER, Roles.VENDOR_REPRESENTATIVE, Roles.ADMIN})
)
public class DashboardServlet extends HttpServlet {

    @EJB
    private ShipmentFacadeLocal shipmentFacade;

    @EJB
    private InventoryFacadeLocal inventoryFacade;

    @EJB
    private OrderFacadeLocal orderFacade;

    @EJB
    private CustomsFacadeLocal customsFacade;

    @EJB
    private PurchaseRequestFacadeLocal purchaseRequestFacade;

    @EJB
    private MonitoringFacadeLocal monitoringFacade;

    @EJB
    private VendorFacadeLocal vendorFacade;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        if (req.isUserInRole(Roles.VENDOR_REPRESENTATIVE)) {
            List<Vendor> mine = vendorFacade.getMyVendors();
            req.setAttribute("vendors", mine);
            req.setAttribute("portalMode", true);

            Map<Long, VendorPerformance> scores = new HashMap<>();
            for (Vendor v : mine) scores.put(v.getId(), vendorFacade.getLatestPerformance(v.getId()));
            req.setAttribute("myScores", scores);

            req.getRequestDispatcher("/views/dashboard.jsp").forward(req, resp);
            return;
        }

        boolean admin = req.isUserInRole(Roles.ADMIN);

        DashboardDTO dashboard = new DashboardDTO(
                shipmentFacade.count(),
                shipmentFacade.findDelayedShipments().size(),
                inventoryFacade.getItemsBelowReorderLevel().size(),
                customsFacade.getApproachingDeadlines().size(),
                monitoringFacade.getRecentAlerts(10));
        req.setAttribute("dashboard", dashboard);

        req.setAttribute("orderCount", orderFacade.count());
        req.setAttribute("pendingPurchases",
                purchaseRequestFacade.findByStatus(PurchaseRequestStatus.PENDING).size());
        req.setAttribute("awaitingClearance",
                customsFacade.getDocumentsAwaitingClearance().size());

        if (admin) {
            req.setAttribute("vendors", vendorFacade.findAll());
            req.setAttribute("latestScores", vendorFacade.getLatestPerformancesByVendor());
        }

        req.setAttribute("identity", vendorFacade.whoAmI());
        req.getRequestDispatcher("/views/dashboard.jsp").forward(req, resp);
    }

}
