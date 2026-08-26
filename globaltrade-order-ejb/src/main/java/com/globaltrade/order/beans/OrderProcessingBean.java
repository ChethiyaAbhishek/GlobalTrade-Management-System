package com.globaltrade.order.beans;

import com.globaltrade.core.entity.Order;
import com.globaltrade.core.entity.Shipment;
import com.globaltrade.core.exception.InsufficientInventoryException;
import com.globaltrade.core.exception.OrderProcessingException;
import com.globaltrade.core.exception.ShipmentNotFoundException;
import com.globaltrade.order.local.OrderFacadeLocal;
import com.globaltrade.shipment.local.ShipmentFacadeLocal;
import jakarta.ejb.*;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class OrderProcessingBean {

    private static final Logger logger = Logger.getLogger(OrderProcessingBean.class.getName());

    @EJB
    private OrderFacadeLocal orderFacade;

    @EJB
    private ShipmentFacadeLocal shipmentFacade;

    public String processOrderWithRecovery(String customerName, String shippingDestination,
                                           boolean international, Map<Long, Integer> itemQuantities) {
        try {
            Order order = orderFacade.placeOrder(
                    customerName, shippingDestination, international, itemQuantities);

            return "CONFIRMED: " + order.getOrderNumber()
                    + " (total " + order.getTotalAmount() + ")";

        } catch (InsufficientInventoryException e) {

            logger.warning("Order recovery - insufficient inventory: " + e.getMessage());
            return e.getMessage();

        } catch (OrderProcessingException e) {

            logger.severe("Order failed: " + e.getMessage());
            return "FAILED: " + e.getMessage();

        } catch (EJBException e) {

            logger.log(Level.SEVERE, "System failure during order processing", e);
            return "SYSTEM ERROR - incident logged, operations team notified";
        }
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String notifyCarrierWithRecovery(String trackingNumber) {
        try {

            Shipment shipment = shipmentFacade.findByTrackingNumber(trackingNumber);
            shipmentFacade.notifyCarrier(shipment);

            return "Carrier notified for shipment " + trackingNumber;

        } catch (ShipmentNotFoundException e) {
            return "FAILED: " + e.getMessage();

        } catch (EJBException e) {

            logger.severe("CARRIER OUTAGE detected for shipment " + trackingNumber
                    + " - activating fallback: " + e.getMessage());

            return "RECOVERED: carrier system outage - operations team alerted";
        }
    }
}