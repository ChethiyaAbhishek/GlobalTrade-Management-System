package com.globaltrade.core.monitoring;

import com.globaltrade.core.entity.Alert;
import com.globaltrade.core.entity.AuditLog;
import jakarta.ejb.Local;

import java.util.List;

@Local
public interface MonitoringFacadeLocal {

    List<Alert> getRecentAlerts(int limit);

    List<AuditLog> getRecentAuditLogs(int limit);
}
