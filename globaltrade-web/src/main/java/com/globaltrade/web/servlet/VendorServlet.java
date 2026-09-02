package com.globaltrade.web.servlet;

import com.globaltrade.core.constants.Roles;
import com.globaltrade.core.entity.Vendor;
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
import java.util.List;

@WebServlet("/vendors")
@ServletSecurity(
        @HttpConstraint(rolesAllowed = {Roles.COORDINATOR, Roles.VENDOR_REPRESENTATIVE, Roles.ADMIN})
)
public class VendorServlet extends HttpServlet {

    @EJB
    private VendorFacadeLocal vendorFacade;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        String action = req.getParameter("action");
        String idParam = req.getParameter("id");

        boolean isAdmin = req.isUserInRole(Roles.ADMIN);

        if (action != null && idParam != null) {

            Long id = Long.parseLong(idParam);

            try {
                if ("report".equals(action)) {
                    req.setAttribute("report", vendorFacade.getVendorReport(id));

                } else if ("suspend".equals(action) || "activate".equals(action)) {

                    if (!isAdmin) {
                        req.setAttribute("error", "ACCESS DENIED: only administrators manage vendor lifecycle");

                    } else if ("suspend".equals(action)) {
                        vendorFacade.suspendVendor(id);
                        req.setAttribute("message", "Vendor #" + id + " suspended");

                    } else {
                        vendorFacade.reactivateVendor(id);
                        req.setAttribute("message", "Vendor #" + id + " reactivated");
                    }
                }

            } catch (EJBAccessException e) {
                req.setAttribute("error", "ACCESS DENIED: " + e.getMessage());
            } catch (Exception e) {
                req.setAttribute("error", e.getMessage());
            }
        }

        if (req.isUserInRole(Roles.VENDOR_REPRESENTATIVE)) {
            List<Vendor> mine = vendorFacade.getMyVendors();
            req.setAttribute("vendors", mine);
            req.setAttribute("portalMode", true);
        } else {
            req.setAttribute("vendors", vendorFacade.findAll());
        }

        req.setAttribute("isAdmin", isAdmin);
        req.setAttribute("latestScores", vendorFacade.getLatestPerformancesByVendor());
        req.setAttribute("identity", vendorFacade.whoAmI());

        req.getRequestDispatcher("/views/vendors.jsp").forward(req, resp);
    }
}
