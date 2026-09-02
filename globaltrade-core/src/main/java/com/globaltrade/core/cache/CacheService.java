package com.globaltrade.core.cache;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class CacheService {

    private static final int MAX_ENTRIES = 1000;

    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final Map<String, Long> expiry = new ConcurrentHashMap<>();

    public void put(String key, Object value, long ttlMs) {

        if (cache.size() >= MAX_ENTRIES) {
            evictExpired();
            if (cache.size() >= MAX_ENTRIES) {
                clear();
            }
        }

        cache.put(key, value);
        expiry.put(key, System.currentTimeMillis() + ttlMs);
    }

    public Object get(String key) {
        Long exp = expiry.get(key);
        if (exp == null || exp < System.currentTimeMillis()) {
            cache.remove(key);
            expiry.remove(key);
            return null;
        }
        return cache.get(key);
    }

    public void clear() {
        cache.clear();
        expiry.clear();
    }

    public void invalidate(String key) {
        cache.remove(key);
        expiry.remove(key);
    }

    public void evictExpired() {
        long now = System.currentTimeMillis();
        List<String> dead = new ArrayList<>();

        for (Map.Entry<String, Long> e : expiry.entrySet()) {
            if (e.getValue() < now) dead.add(e.getKey());
        }

        for (String k : dead) {
            cache.remove(k);
            expiry.remove(k);
        }
    }
}
