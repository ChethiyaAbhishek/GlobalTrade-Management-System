package com.globaltrade.web.servlet;

import com.globaltrade.core.constants.Roles;
import com.globaltrade.core.entity.InventoryItem;
import com.globaltrade.inventory.local.InventoryFacadeLocal;
import com.globaltrade.order.beans.OrderProcessingBean;
import com.globaltrade.order.local.OrderFacadeLocal;
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
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/orders")
@ServletSecurity(
        @HttpConstraint(rolesAllowed = {Roles.COORDINATOR, Roles.ADMIN})
)
public class OrderPlacementServlet extends HttpServlet {

    @EJB
    private OrderFacadeLocal orderFacade;

    @EJB
    private InventoryFacadeLocal inventoryFacade;

    @Inject
    private OrderProcessingBean orderProcessing;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        prepareView(req);
        req.getRequestDispatcher("/views/orders.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        String customerName = req.getParameter("customerName");
        String destination = req.getParameter("shippingDestination");
        boolean international = "on".equals(req.getParameter("international"));

        Map<Long, Integer> quantities = new LinkedHashMap<>();

        for (InventoryItem item : inventoryFacade.findAll()) {

            String value = req.getParameter("qty_" + item.getId());
            if (value != null && !value.trim().isEmpty()) {
                try {
                    int qty = Integer.parseInt(value.trim());
                    if (qty > 0) {
                        quantities.put(item.getId(), qty);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        String result = orderProcessing.processOrderWithRecovery(
                customerName, destination, international, quantities);
        req.setAttribute("message", result);

        prepareView(req);
        req.getRequestDispatcher("/views/orders.jsp").forward(req, resp);
    }

    private void prepareView(HttpServletRequest req) {
        req.setAttribute("items", inventoryFacade.findAll());
        req.setAttribute("orders", orderFacade.findAll());
    }
}

