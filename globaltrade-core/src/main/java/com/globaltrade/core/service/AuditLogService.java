package com.globaltrade.core.service;

import com.globaltrade.core.entity.AuditLog;

public interface AuditLogService {
    void persistAuditLog(AuditLog log);
}
