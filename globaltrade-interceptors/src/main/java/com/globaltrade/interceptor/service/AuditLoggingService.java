package com.globaltrade.interceptor.service;

import com.globaltrade.core.entity.AuditLog;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class AuditLoggingService {

    @PersistenceContext(unitName = "GlobalTrade-PU")
    private EntityManager em;

    private static final Logger logger = Logger.getLogger(AuditLoggingService.class.getName());

    @Asynchronous
    public void persistRecord(AuditLog auditLog){
        try{
            em.persist(auditLog);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not persist audit record", e);
        }
    }
}
