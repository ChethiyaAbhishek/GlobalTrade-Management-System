package com.globaltrade.messaging.beans;

import com.globaltrade.core.entity.Alert;
import com.globaltrade.core.entity.AuditLog;
import com.globaltrade.core.monitoring.MonitoringFacadeLocal;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class MonitoringBean implements MonitoringFacadeLocal {

    @PersistenceContext(unitName = "GlobalTrade-PU")
    private EntityManager em;

    @Override
    public List<Alert> getRecentAlerts(int limit) {
        return em.createNamedQuery("Alert.findAll", Alert.class)
                .setMaxResults(limit).getResultList();
    }

    @Override
    public List<AuditLog> getRecentAuditLogs(int limit) {
        return em.createNamedQuery("AuditLog.findAll", AuditLog.class)
                .setMaxResults(limit).getResultList();
    }
}
