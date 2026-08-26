package com.globaltrade.web.servlet;

import com.globaltrade.core.constants.Roles;
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

@WebServlet("/vendors")
@ServletSecurity(
        @HttpConstraint(rolesAllowed = {Roles.COORDINATOR, Roles.VENDOR_REP, Roles.ADMIN})
)
public class VendorServlet extends HttpServlet {

    @EJB
    private VendorFacadeLocal vendorFacade;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        String action = req.getParameter("action");
        String idParam = req.getParameter("id");

//        if ("report".equals(action) && idParam != null) {
//            try {
//                VendorReportDTO report = vendorFacade.getVendorReport(Long.parseLong(idParam));
//                req.setAttribute("report", report);
//
//            } catch (EJBAccessException e) {
//                req.setAttribute("error", "403: " + e.getMessage());
//            }
//        }

        if ("suspend".equals(action) && idParam != null) {
            try {
                vendorFacade.suspendVendor(Long.parseLong(idParam));
                req.setAttribute("message", "Vendor #" + idParam + " suspended");

            } catch (EJBAccessException e) {
                req.setAttribute("error", "ACCESS DENIED: only administrators may suspend vendors");

            } catch (IllegalArgumentException e) {
                req.setAttribute("error", e.getMessage());
            }
        }

        req.setAttribute("vendors", vendorFacade.findAll());
        req.setAttribute("identity", vendorFacade.whoAmI());

        req.getRequestDispatcher("/views/vendors.jsp").forward(req, resp);
    }
}
