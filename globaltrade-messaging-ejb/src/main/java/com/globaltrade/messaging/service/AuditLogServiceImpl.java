package com.globaltrade.messaging.service;

import com.globaltrade.core.entity.AuditLog;
import com.globaltrade.core.service.AuditLogService;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger logger = Logger.getLogger(AuditLogServiceImpl.class.getName());

    @PersistenceContext(unitName = "GlobalTrade-PU")
    private EntityManager em;

    @Override
    @Asynchronous
    public void persistAuditLog(AuditLog log) {
        try {

            em.persist(log);

        } catch (Exception e) {
            logger.log(Level.WARNING, "Audit persistence failed (non-fatal): " + e.getMessage());
        }
    }
}
