package com.dyz.infrastructure.dcache;


import com.dyz.infrastructure.dcache.lock.DLock;

public interface DCache {

    Object getCache(String key);

    void setCache(String key, Object value);

    void setCache(String key, Object value, int expired);

    void deleteCache(String key);

    DLock getDCacheLock();
}
