package com.globaltrade.inventory.beans;

import com.globaltrade.core.annotations.Audited;
import com.globaltrade.core.constants.AlertTypes;
import com.globaltrade.core.constants.Roles;
import com.globaltrade.core.entity.*;
import com.globaltrade.core.enums.*;
import com.globaltrade.core.facade.BaseFacade;
import com.globaltrade.core.messaging.AlertPublisherLocal;
import com.globaltrade.inventory.local.PurchaseRequestFacadeLocal;
import com.globaltrade.shipment.local.ShipmentFacadeLocal;
import com.globaltrade.vendor.local.VendorFacadeLocal;
import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

@Stateless
@Audited
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class PurchaseRequestFacadeBean extends BaseFacade<PurchaseRequest> implements PurchaseRequestFacadeLocal {

    @PersistenceContext(unitName = "GlobalTrade-PU")
    private EntityManager em;

    @Resource
    private SessionContext sessionContext;

    @EJB
    private AlertPublisherLocal alertPublisher;

    @EJB
    private ShipmentFacadeLocal shipmentFacade;

    @EJB
    private VendorFacadeLocal vendorFacade;

    private static final Logger logger = Logger.getLogger(PurchaseRequestFacadeBean.class.getName());

    public static final int REPLENISHMENT_QTY_DEFAULT = 30;
    public static final int SHIPMENT_DURATION_DEFAULT = 14;

    public PurchaseRequestFacadeBean() {
        super(PurchaseRequest.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    public PurchaseRequest createAutomatedRequest(InventoryItem item, Vendor vendor) {

        if (item == null) {
            logger.warning("REPLENISHMENT_SERVICE: Inventory item not found");
            alertPublisher.sendAlert(AlertTypes.AUTO_REPLENISHMENT_FAILURE, "Auto inventory replenishment failed. Inventory item not found", null);
            return null;
        }

        PurchaseRequest request = PurchaseRequest.builder()
                .inventoryItem(item)
                .requestedQty(REPLENISHMENT_QTY_DEFAULT)
                .unitCost(item.getUnitPrice())
                .triggerType(PurchaseRequestTriggerType.AUTOMATED_REPLENISHMENT)
                .vendor(vendor).build();

        em.persist(request);

        logger.info("REPLENISHMENT_SERVICE: AUTOMATED purchase request #" + request.getId()
                + " created for item " + item.getName() + " (qty " + REPLENISHMENT_QTY_DEFAULT + ")");
        return request;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public boolean hasOpenRequest(Long itemId) {
        Long count = em.createNamedQuery("PurchaseRequest.countOpenByItem", Long.class)
                .setParameter("itemId", itemId)
                .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public PurchaseRequest createManualRequest(Long itemId, int quantity, Long vendorId) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("A valid quantity required");
        }

        InventoryItem item = em.find(InventoryItem.class, itemId);
        if (item == null) {
            throw new IllegalArgumentException("Inventory item not found: " + itemId);
        }

        Vendor vendor = null;

        if (vendorId == null) {
            vendor = vendorFacade.findPreferredVendor();
        } else {
            vendor = em.find(Vendor.class, vendorId);
            if (vendor == null) {
                throw new IllegalArgumentException("No vendor found for vendorId: " + vendorId);
            }
        }

        PurchaseRequest request = PurchaseRequest.builder()
                .inventoryItem(item)
                .requestedQty(quantity)
                .unitCost(item.getUnitPrice())
                .triggerType(PurchaseRequestTriggerType.MANUAL_OVERRIDE)
                .vendor(vendor).build();

        em.persist(request);

        logger.info("MANUAL purchase request #" + request.getId()
                + " created for item " + item.getName());
        return request;
    }

    @Override
    @RolesAllowed({Roles.WAREHOUSE_MANAGER, Roles.ADMIN})
    public void updateRequestStatus(Long requestId, PurchaseRequestStatus requestStatus) {

        PurchaseRequest request = getPurchaseRequest(requestId);

        if (requestStatus == PurchaseRequestStatus.APPROVED) {
            checkRequestStatus(request, PurchaseRequestStatus.PENDING, PurchaseRequestStatus.APPROVED);
            request.setRequestStatus(requestStatus);

        } else if (requestStatus == PurchaseRequestStatus.CANCELLED) {
            checkRequestStatus(request, PurchaseRequestStatus.PENDING, PurchaseRequestStatus.CANCELLED);
            request.setRequestStatus(requestStatus);
        } else {
            throw new IllegalArgumentException("Invalid PR status change request");
        }
        em.merge(request);

        alertPublisher.sendAlert(AlertTypes.PURCHASE_REQUEST, "Purchase request #" + requestId + " " + requestStatus.name() + " ("
                + request.getInventoryItem().getName() + ")", requestId);
    }

    @Override
    @RolesAllowed({Roles.WAREHOUSE_MANAGER, Roles.ADMIN})
    public void placeOrder(Long requestId) {

        PurchaseRequest request = getPurchaseRequest(requestId);
        checkRequestStatus(request, PurchaseRequestStatus.APPROVED, PurchaseRequestStatus.ORDERED);

        List<Shipment> existing = em.createNamedQuery("Shipment.findByPurchaseRequest", Shipment.class)
                .setParameter("requestId", requestId)
                .getResultList();
        if (!existing.isEmpty()) {
            throw new IllegalStateException("Inbound shipment already exists for request #" + requestId);
        }

        request.setRequestStatus(PurchaseRequestStatus.ORDERED);
        em.merge(request);

        InventoryItem item = request.getInventoryItem();

        String trackingNo = "GT-IN-" + requestId + "-" + System.currentTimeMillis();

        Vendor vendor = request.getVendor();
        String origin = "Vendor";
        if (vendor != null) {
            origin = vendor.getName() + ": " + vendor.getCountry();
        }

        Warehouse warehouse = item.getWarehouse();
        String destination = "Warehouse";
        if (warehouse != null) {
            destination = warehouse.getName() + ": " + warehouse.getLocation();
        }

        LocalDate estimatedDelivery = LocalDate.now().plusDays(SHIPMENT_DURATION_DEFAULT);

        Shipment shipment = Shipment.builder()
                .trackingNumber(trackingNo)
                .status(ShipmentStatus.IN_TRANSIT)
                .shipmentType(ShipmentType.INBOUND)
                .origin(origin)
                .destination(destination)
                .currentLocation(origin)
                .estimatedDelivery(estimatedDelivery)
                .vendor(vendor)
                .warehouse(warehouse)
                .purchaseRequest(request).build();

        shipmentFacade.create(shipment);

        alertPublisher.sendAlert(AlertTypes.PURCHASE_REQUEST, "Purchase order transmitted for request #" + requestId
                + " — inbound shipment " + trackingNo + " in transit", requestId);

        logger.info("Inbound shipment " + trackingNo + " created for purchase request #" + requestId);
    }

    @Override
    @RolesAllowed({Roles.WAREHOUSE_MANAGER, Roles.ADMIN})
    public void fulfillRequest(Long requestId) {

        PurchaseRequest request = getPurchaseRequest(requestId);
        checkRequestStatus(request, PurchaseRequestStatus.ORDERED, PurchaseRequestStatus.FULFILLED);

        List<Shipment> shipments = em.createNamedQuery("Shipment.findByPurchaseRequest", Shipment.class)
                .setParameter("requestId", requestId)
                .getResultList();

        if (shipments.isEmpty()) {
            throw new IllegalStateException("No inbound shipment linked to request #" + requestId);
        }

        if (shipments.get(0).getStatus() != ShipmentStatus.DELIVERED) {
            throw new IllegalStateException("Cannot fulfil request #" + requestId + " — inbound shipment "
                    + shipments.get(0).getTrackingNumber() + " not delivered yet " +
                    "(status: " + shipments.get(0).getStatus() + ")");
        }

        InventoryItem item = request.getInventoryItem();
        int existingQty = item.getQuantity();
        item.setQuantity(existingQty + request.getRequestedQty());
        em.merge(item);

        InventoryMovement newInventoryAudit = InventoryMovement.builder()
                .inventoryItem(item)
                .movementType(MovementType.RECEIPT)
                .quantityChange(request.getRequestedQty())
                .unitCost(request.getUnitCost())
                .reference("PURCHASE_REQUEST #" + requestId)
                .performedBy(currentUser()).build();

        em.persist(newInventoryAudit);

        request.setRequestStatus(PurchaseRequestStatus.FULFILLED);
        em.merge(request);

        alertPublisher.sendAlert(AlertTypes.PURCHASE_REQUEST, "Purchase request #" + requestId + " FULFILLED — "
                + item.getName() + " stock " + existingQty + " -> " + item.getQuantity(), requestId);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<PurchaseRequest> findByStatus(PurchaseRequestStatus status) {
        return em.createNamedQuery("PurchaseRequest.findByStatus", PurchaseRequest.class)
                .setParameter("status", status)
                .getResultList();
    }

    private PurchaseRequest getPurchaseRequest(Long requestId) {
        PurchaseRequest request = em.find(PurchaseRequest.class, requestId);
        if (request == null) {
            throw new IllegalArgumentException("Purchase request not found: " + requestId);
        }
        return request;
    }

    private void checkRequestStatus(PurchaseRequest request, PurchaseRequestStatus expected, PurchaseRequestStatus actionStatus) {
        if (request.getRequestStatus() != expected) {
            throw new IllegalStateException("Request cannot be updated with " + actionStatus + " status due to existing status: "
                    + request.getRequestStatus() + ", expected: " + expected.name());
        }
    }

    private String currentUser() {
        try {
            if (sessionContext != null && sessionContext.getCallerPrincipal() != null) {
                return sessionContext.getCallerPrincipal().getName();
            }
        } catch (Exception e) {
        }
        return "SYSTEM";
    }
}
