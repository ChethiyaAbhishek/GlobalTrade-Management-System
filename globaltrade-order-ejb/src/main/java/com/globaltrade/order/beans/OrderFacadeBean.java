package com.globaltrade.order.beans;

import com.globaltrade.core.annotations.Audited;
import com.globaltrade.core.annotations.Monitored;
import com.globaltrade.core.constants.AlertTypes;
import com.globaltrade.core.dto.OrderStatusDTO;
import com.globaltrade.core.entity.*;
import com.globaltrade.core.enums.*;
import com.globaltrade.core.exception.CustomsDeadlineException;
import com.globaltrade.core.exception.InsufficientInventoryException;
import com.globaltrade.core.exception.OrderProcessingException;
import com.globaltrade.core.facade.BaseFacade;
import com.globaltrade.core.messaging.AlertPublisherLocal;
import com.globaltrade.customs.local.CustomsFacadeLocal;
import com.globaltrade.inventory.local.InventoryFacadeLocal;
import com.globaltrade.order.local.OrderFacadeLocal;
import com.globaltrade.order.remote.OrderFacadeRemote;
import com.globaltrade.shipment.local.ShipmentFacadeLocal;
import jakarta.annotation.Resource;
import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Stateless
@Audited
@Monitored
public class OrderFacadeBean extends BaseFacade<Order> implements OrderFacadeLocal, OrderFacadeRemote {

    private static final Logger logger = Logger.getLogger(OrderFacadeBean.class.getName());

    private static final int DELIVERY_LEAD_TIME_DAYS = 5;

    private static final int CUSTOMS_DEADLINE_DAYS = 3;

    @PersistenceContext(unitName = "GlobalTrade-PU")
    private EntityManager em;

    @EJB
    private InventoryFacadeLocal inventoryFacade;

    @EJB
    private CustomsFacadeLocal customsFacade;

    @EJB
    private ShipmentFacadeLocal shipmentFacade;

    @EJB
    private AlertPublisherLocal alertPublisher;

    @Resource
    private SessionContext sessionContext;

    public OrderFacadeBean() {
        super(Order.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Order placeOrder(String customerName, String shippingDestination, boolean international, Map<Long, Integer> itemQuantities)
            throws OrderProcessingException, InsufficientInventoryException {

        if (customerName == null || customerName.trim().isEmpty()) {
            throw new OrderProcessingException("Customer name is required");
        }

        if (shippingDestination == null || shippingDestination.trim().isEmpty()) {
            throw new OrderProcessingException("Shipping destination is required");
        }

        if (itemQuantities == null || itemQuantities.isEmpty()) {
            throw new OrderProcessingException("Order must contain at least one item");
        }

        Order order = new Order();
        order.setOrderNumber("ORD-" + System.currentTimeMillis());
        order.setCustomerName(customerName.trim());
        order.setShippingDestination(shippingDestination.trim());
        order.setInternational(international);
        order.setStatus(OrderStatus.PROCESSING);
        em.persist(order);

        List<long[]> reserved = new ArrayList<>();

        BigDecimal total = BigDecimal.ZERO;

        OrderItem firstItem = null;

        try {
            for (Map.Entry<Long, Integer> entry : itemQuantities.entrySet()) {

                Long itemId = entry.getKey();
                int quantity = entry.getValue();

                if (quantity <= 0) {
                    throw new OrderProcessingException("Invalid quantity for item " + itemId);
                }

                InventoryItem item = em.find(InventoryItem.class, itemId);
                if (item == null) {
                    throw new OrderProcessingException("Inventory item not found: " + itemId);
                }

                OrderItem orderItem = OrderItem.builder()
                        .quantity(quantity)
                        .unitPrice(item.getUnitPrice())
                        .order(order)
                        .inventoryItem(item).build();

                em.persist(orderItem);

                if (firstItem == null) {
                    firstItem = orderItem;
                }

                inventoryFacade.adjustStock(itemId, -quantity);
                reserved.add(new long[]{itemId, quantity});

                total = total.add(item.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));

                em.persist(InventoryMovement.builder()
                        .inventoryItem(item)
                        .movementType(MovementType.DISPATCH)
                        .quantityChange(-quantity)
                        .unitCost(item.getUnitPrice())
                        .reference("ORDER#" + order.getId())
                        .performedBy(currentUser()).build());
            }

            order.setTotalAmount(total);

            Shipment shipment = buildOutboundShipment(order, firstItem);
            shipmentFacade.create(shipment);

            if (order.getInternational()) {
                CustomsDocument exportDeclaration = new CustomsDocument(shipment, DocumentType.EXPORT_DECLARATION);

                exportDeclaration.setDeadline(LocalDate.now().plusDays(CUSTOMS_DEADLINE_DAYS));

                customsFacade.fileDocument(exportDeclaration);
            }

            order.setStatus(OrderStatus.CONFIRMED);
            em.merge(order);

            alertPublisher.sendAlert(AlertTypes.ORDER_CONFIRMED,
                    "Order " + order.getOrderNumber() + " confirmed for "
                            + order.getCustomerName() + ": shipment "
                            + shipment.getTrackingNumber()
                            + (international ? " (international, customs filed)" : ""), order.getId());

            logger.info("Order " + order.getOrderNumber() + " confirmed: total:"
                    + total + ", international:" + international);
            return order;

        } catch (InsufficientInventoryException e) {
            compensateReservations(reserved);
            throw e;
        } catch (CustomsDeadlineException e) {
            compensateReservations(reserved);
            throw new OrderProcessingException("International order rejected - customs filing failed: " + e.getMessage());
        } catch (OrderProcessingException e) {
            compensateReservations(reserved);
            throw e;
        } catch (Exception e) {
            compensateReservations(reserved);
            throw new OrderProcessingException("Order placement failed unexpectedly: " + e.getMessage());
        }
    }

    private void compensateReservations(List<long[]> reserved) {

        for (long[] reservation : reserved) {
            try {
                inventoryFacade.adjustStock(reservation[0], (int) reservation[1]);
            } catch (Exception e) {
                logger.severe("COMPENSATION FAILED for item " + reservation[0]
                        + " - manual stock correction required: " + e.getMessage());
            }
        }
        if (!reserved.isEmpty()) {
            logger.info("Compensation complete: " + reserved.size() + " reservation(s) released");
        }
    }

    private Shipment buildOutboundShipment(Order order, OrderItem item) {

        InventoryItem firstItem = item.getInventoryItem();
        Warehouse warehouse = firstItem.getWarehouse();

        Shipment shipment = Shipment.builder()
                .trackingNumber("GT-OUT-" + order.getId() + "-" + System.currentTimeMillis())
                .status(ShipmentStatus.IN_TRANSIT)
                .shipmentType(ShipmentType.OUTBOUND)
                .origin(warehouse != null ? warehouse.getLocation() : "GlobalTrade Warehouse")
                .destination(order.getShippingDestination())
                .currentLocation(warehouse != null ? warehouse.getLocation() : "GlobalTrade Warehouse")
                .estimatedDelivery(LocalDate.now().plusDays(DELIVERY_LEAD_TIME_DAYS))
                .warehouse(warehouse)
                .order(order).build();

        em.persist(shipment);

        return shipment;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Order findById(Long id) {
        return super.find(id);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Order findByOrderNumber(String orderNumber) {
        List<Order> results = em.createNamedQuery("Order.findByOrderNumber", Order.class)
                .setParameter("orderNumber", orderNumber)
                .getResultList();

        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public OrderStatusDTO getOrderStatus(String orderNumber) {

        Order order = findByOrderNumber(orderNumber);
        if (order == null) {
            return null;
        }

        return new OrderStatusDTO(
                order.getOrderNumber(),
                order.getCustomerName(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getShippingDestination(),
                order.getCreatedAt());
    }

    private String currentUser() {
        try {
            if (sessionContext != null && sessionContext.getCallerPrincipal() != null) {
                return sessionContext.getCallerPrincipal().getName();
            }
        } catch (Exception ignored) {
        }
        return "anonymous";
    }
}
