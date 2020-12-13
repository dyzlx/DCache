package com.dyz.infrastructure.dcache.impl.map;

import com.dyz.infrastructure.dcache.DCache;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class HashMapDCache implements DCache {

    private Lock lock = new ReentrantLock();

    private final Map<String, Object> store = new ConcurrentHashMap<>(16);

    public HashMapDCache() {
        log.info("hash map cache init");
    }

    @Override
    public Object getCache(String key) {
        log.info("get cache from map cache, key={}", key);
        return store.get(key);
    }

    @Override
    public void setCache(String key, Object value) {
        log.info("set cache to map cache, key={}", key);
        store.put(key, value);
    }

    @Override
    public void setCache(String key, Object value, int expired) {
        setCache(key, value);
    }

    @Override
    public void deleteCache(String key) {
        log.info("delete cache from map cache, key={}", key);
        store.remove(key);
    }

    @Override
    public boolean lockForQueryDB(String requestId) {
        log.info("try to get a local lock");
        try {
            return lock.tryLock(500, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("try to get local lock error", e);
            return false;
        }
    }

    @Override
    public boolean unlockForQueryDB(String requestId) {
        log.info("trg to release local lock");
        try {
            lock.unlock();
            return true;
        } catch (Exception e) {
            log.error("try to release local lock error", e);
            return false;
        }
    }

    public int getHashMapCacheSize() {
        return store.size();
    }
}
