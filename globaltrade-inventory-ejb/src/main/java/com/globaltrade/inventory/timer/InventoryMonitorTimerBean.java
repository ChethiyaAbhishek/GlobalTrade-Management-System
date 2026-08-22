package com.globaltrade.inventory.timer;

import com.globaltrade.core.entity.InventoryItem;
import com.globaltrade.core.messaging.AlertPublisherLocal;
import com.globaltrade.inventory.InventoryFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Schedules;
import jakarta.ejb.Stateless;

import java.util.List;
import java.util.logging.Logger;

@Stateless
public class InventoryMonitorTimerBean {

    private static final Logger logger =
            Logger.getLogger(InventoryMonitorTimerBean.class.getName());

    @EJB
    private InventoryFacadeLocal inventoryFacade;

    @EJB
    private AlertPublisherLocal alertPublisher;

    @Schedule(hour = "*/6", minute = "0", second = "0", persistent = true,
            info = "INVENTORY_MONITOR_TIMER")
    public void checkInventoryLevels() {
        logger.info("TIMER FIRED: inventory level check started");

        List<InventoryItem> lowStock = inventoryFacade.getItemsBelowReorderLevel();
        for (InventoryItem item : lowStock) {
            String message = item.getName() + " Item: " +item.getName()+ "(SKU " + item.getSku()
                    + ") below reorder level: " + item.getQuantity()
                    + " remaining";
            alertPublisher.sendAlert("LOW_STOCK", message, item.getId());
        }

        logger.info("TIMER COMPLETE: inventory check finished — "
                + lowStock.size() + " low-stock alert(s) published");
    }

    @Schedules({
            @Schedule(dayOfWeek = "Mon-Fri", hour = "8", minute = "0", second = "0",
                    persistent = true, info = "morningStockReport"),
            @Schedule(dayOfWeek = "Mon-Fri", hour = "17", minute = "0", second = "0",
                    persistent = true, info = "eveningStockReport")
    })
    public void dailyStockReport() {
        long totalItems = inventoryFacade.count();
        int lowStockCount = inventoryFacade.getItemsBelowReorderLevel().size();
        logger.info("STOCK REPORT: totalItems=" + totalItems
                + ", lowStockItems=" + lowStockCount);
    }
}
