package com.globaltrade.core.dto;

import com.globaltrade.core.entity.Alert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public record DashboardDTO(
        long totalShipments,
        long delayedCount,
        long lowStockCount,
        long pendingCustomsDocs,
        List<Alert> recentAlerts
) implements Serializable {
    public DashboardDTO {
        recentAlerts = (recentAlerts == null) ? List.of() : List.copyOf(recentAlerts);
    }
}
