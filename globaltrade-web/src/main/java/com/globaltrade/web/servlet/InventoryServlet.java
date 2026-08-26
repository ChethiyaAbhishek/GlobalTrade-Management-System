package com.globaltrade.web.servlet;

import com.globaltrade.core.constants.Roles;
import com.globaltrade.inventory.local.InventoryFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/inventory")
@ServletSecurity(
        @HttpConstraint(rolesAllowed = {Roles.WAREHOUSE_MANAGER, Roles.COORDINATOR, Roles.ADMIN})
)
public class InventoryServlet extends HttpServlet {

    @EJB
    private InventoryFacadeLocal inventoryFacade;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        req.setAttribute("items", inventoryFacade.findAll());
        req.getRequestDispatcher("/views/inventory.jsp").forward(req, resp);
    }
}
