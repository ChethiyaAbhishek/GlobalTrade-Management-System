package com.globaltrade.messaging.service;

import com.globaltrade.core.cache.CacheService;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

@Singleton
@Startup
public class CacheMaintenanceTimerBean {

    @Inject
    private CacheService cacheService;

    @Schedule(hour = "*", minute = "*/10", second = "0", persistent = false)
    public void evictExpiredCacheEntries() {
        cacheService.evictExpired();
    }
}
