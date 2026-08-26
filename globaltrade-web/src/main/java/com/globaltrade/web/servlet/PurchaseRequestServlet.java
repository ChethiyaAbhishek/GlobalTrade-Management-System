package com.globaltrade.web.servlet;

import com.globaltrade.core.constants.Roles;
import com.globaltrade.core.enums.PurchaseRequestStatus;
import com.globaltrade.inventory.local.InventoryFacadeLocal;
import com.globaltrade.inventory.local.PurchaseRequestFacadeLocal;
import com.globaltrade.vendor.local.VendorFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBAccessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/purchases")
@ServletSecurity(
        @HttpConstraint(rolesAllowed = {Roles.COORDINATOR, Roles.WAREHOUSE_MANAGER, Roles.ADMIN})
)
public class PurchaseRequestServlet extends HttpServlet {

    @EJB
    private PurchaseRequestFacadeLocal purchaseRequestFacade;

    @EJB
    private InventoryFacadeLocal inventoryFacade;

    @EJB
    private VendorFacadeLocal vendorFacade;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        String action = req.getParameter("action");
        String idParam = req.getParameter("id");

        if (action != null && idParam != null) {

            Long id = Long.parseLong(idParam);

            try {
                switch (action) {
                    case "approve":
                        purchaseRequestFacade.updateRequestStatus(id, PurchaseRequestStatus.APPROVED);
                        break;
                    case "reject":
                        purchaseRequestFacade.updateRequestStatus(id, PurchaseRequestStatus.CANCELLED);
                        break;
                    case "order":
                        purchaseRequestFacade.placeOrder(id);
                        break;
                    case "fulfill":
                        purchaseRequestFacade.fulfillRequest(id);
                        break;
                    default:
                        break;
                }

                req.setAttribute("message", "Action '" + action + "' completed for request #" + id);

            } catch (EJBAccessException e) {
                req.setAttribute("error", "ACCESS DENIED: your role cannot perform '" + action + "'");

            } catch (IllegalStateException | IllegalArgumentException e) {
                req.setAttribute("error", e.getMessage());
            }
        }

        String status = req.getParameter("status");

        if (status != null) {
            req.setAttribute("requests", purchaseRequestFacade.findByStatus(PurchaseRequestStatus.valueOf(status)));
            req.setAttribute("selectedStatus", status);
        } else {
            req.setAttribute("requests", purchaseRequestFacade.findAll());
        }

        req.setAttribute("items", inventoryFacade.findAll());
        req.setAttribute("vendors", vendorFacade.findAll());

        req.getRequestDispatcher("/views/purchases.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {

            Long itemId = Long.parseLong(req.getParameter("itemId"));
            int quantity = Integer.parseInt(req.getParameter("quantity"));

            String vendorParam = req.getParameter("vendorId");

            Long vendorId = (vendorParam == null || vendorParam.isEmpty()) ? null : Long.parseLong(vendorParam);

            purchaseRequestFacade.createManualRequest(itemId, quantity, vendorId);
            req.setAttribute("message", "Manual purchase request created");

        } catch (EJBAccessException e) {
            req.setAttribute("error", "ACCESS DENIED: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
        }

        doGet(req, resp);
    }
}
