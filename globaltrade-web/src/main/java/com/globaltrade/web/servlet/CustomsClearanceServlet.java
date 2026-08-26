package com.globaltrade.web.servlet;

import com.globaltrade.core.constants.Roles;
import com.globaltrade.customs.beans.CustomsClearanceSessionBean;
import com.globaltrade.customs.local.CustomsFacadeLocal;
import com.globaltrade.web.util.JndiLookupHelper;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.NamingException;
import java.io.IOException;

@WebServlet("/clearance")
@ServletSecurity(
        @HttpConstraint(rolesAllowed = {Roles.CUSTOMS_AGENT, Roles.ADMIN})
)
public class CustomsClearanceServlet extends HttpServlet {

    private static final String SESSION_KEY = "clearanceSessionBean";

    @EJB
    private CustomsFacadeLocal customsFacade;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        HttpSession httpSession = req.getSession();
        CustomsClearanceSessionBean session = (CustomsClearanceSessionBean) httpSession.getAttribute(SESSION_KEY);

        String action = req.getParameter("action");

        try {
            if ("start".equals(action)) {
                if (session != null) {
                    try {
                        session.end();
                    } catch (Exception ignored) {
                    }
                }

                session = JndiLookupHelper.lookupClearanceSessionBean();
                session.startClearance(Long.parseLong(req.getParameter("id")));

                httpSession.setAttribute(SESSION_KEY, session);
                req.setAttribute("message", "Clearance session started");

            } else if (session != null && "approve".equals(action)) {

                session.approveClearance();
                session.end();

                httpSession.removeAttribute(SESSION_KEY);
                session = null;

                req.setAttribute("message", "Document cleared (APPROVED)");

            } else if (session != null && "reject".equals(action)) {

                session.rejectClearance();
                session.end();

                httpSession.removeAttribute(SESSION_KEY);
                session = null;

                req.setAttribute("message", "Document REJECTED");
            }

        } catch (IllegalStateException | IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());

        } catch (NamingException e) {
            req.setAttribute("error", "Clearance service unavailable: " + e.getMessage());
        }

        req.setAttribute("awaiting", customsFacade.getDocumentsAwaitingClearance());
        req.setAttribute("sessionActive", session != null);

        req.getRequestDispatcher("/views/clearance.jsp").forward(req, resp);
    }
}
