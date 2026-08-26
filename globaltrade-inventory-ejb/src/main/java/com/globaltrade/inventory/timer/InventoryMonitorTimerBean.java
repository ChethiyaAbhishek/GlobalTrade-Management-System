package com.globaltrade.inventory.timer;

import com.globaltrade.core.constants.AlertTypes;
import com.globaltrade.core.entity.InventoryItem;
import com.globaltrade.core.entity.PurchaseRequest;
import com.globaltrade.core.entity.Vendor;
import com.globaltrade.core.enums.PurchaseRequestStatus;
import com.globaltrade.core.messaging.AlertPublisherLocal;
import com.globaltrade.inventory.beans.PurchaseRequestFacadeBean;
import com.globaltrade.inventory.local.InventoryFacadeLocal;
import com.globaltrade.inventory.local.PurchaseRequestFacadeLocal;
import com.globaltrade.vendor.local.VendorFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Schedules;
import jakarta.ejb.Stateless;

import java.util.List;
import java.util.logging.Logger;

@Stateless
public class InventoryMonitorTimerBean {

    private static final Logger logger = Logger.getLogger(InventoryMonitorTimerBean.class.getName());

    @EJB
    private InventoryFacadeLocal inventoryFacade;

    @EJB
    private PurchaseRequestFacadeLocal purchaseRequestFacade;

    @EJB
    private VendorFacadeLocal vendorFacade;

    @EJB
    private AlertPublisherLocal alertPublisher;

    @Schedule(hour = "*/6", persistent = true, info = "INVENTORY_MONITOR_TIMER")
    public void checkInventoryLevels() {
        logger.info("TIMER: inventory level check started");

        List<InventoryItem> lowStock = inventoryFacade.getItemsBelowReorderLevel();

        Vendor preferredVendor = vendorFacade.findPreferredVendor();

        int created = 0;
        int skipped = 0;

        for (InventoryItem item : lowStock) {
            if (purchaseRequestFacade.hasOpenRequest(item.getId())) {
                skipped++;
                alertPublisher.sendAlert(AlertTypes.LOW_STOCK,
                        "REMINDER: " + item.getName() + " still below reorder level ("
                                + item.getQuantity() + " remaining); open purchase request awaiting action",
                        item.getId());
                continue;
            }

            PurchaseRequest request = purchaseRequestFacade.createAutomatedRequest(item, preferredVendor);

            alertPublisher.sendAlert(AlertTypes.PURCHASE_REQUEST,
                    "Automated purchase request #" + request.getId() + " created for "
                            + item.getName() + " (qty " + PurchaseRequestFacadeBean.REPLENISHMENT_QTY_DEFAULT + ") - awaiting warehouse approval",
                    request.getId());
            created++;
        }

        logger.info("TIMER: inventory monitor complete: " + created
                + " purchase request(s) created, " + skipped + " skipped (already open)");
    }

    @Schedules({
            @Schedule(dayOfWeek = "Mon-Fri", hour = "8", persistent = true, info = "morningStockReport"),
            @Schedule(dayOfWeek = "Mon-Fri", hour = "17", persistent = true, info = "eveningStockReport")
    })
    public void dailyStockReport() {
        long totalItems = inventoryFacade.count();
        int lowStockCount = inventoryFacade.getItemsBelowReorderLevel().size();
        int pendingRequests = purchaseRequestFacade.findByStatus(PurchaseRequestStatus.PENDING).size();
        logger.info("STOCK REPORT: totalItems: " + totalItems
                + ", lowStockItems: " + lowStockCount
                + ", pendingPurchaseRequests: " + pendingRequests);
    }
}
