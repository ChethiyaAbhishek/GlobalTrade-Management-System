package com.globaltrade.core.dto;

import com.globaltrade.core.entity.Alert;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DashboardDTO implements Serializable {

    private long totalShipments;
    private long delayedCount;
    private long lowStockCount;
    private long pendingCustomsDocs;
    private List<Alert> recentAlerts;

    public DashboardDTO() {
        recentAlerts = (recentAlerts == null) ? List.of() : List.copyOf(recentAlerts);
    }
}
