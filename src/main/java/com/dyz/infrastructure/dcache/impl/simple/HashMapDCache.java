package com.dyz.infrastructure.dcache.impl.simple;

import com.dyz.infrastructure.dcache.DCache;
import com.dyz.infrastructure.dcache.lock.DLock;
import com.dyz.infrastructure.dcache.lock.JDKLock;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class HashMapDCache implements DCache {

    private final Map<String, Object> store = new ConcurrentHashMap<>(16);

    public HashMapDCache() {
        log.info("hash simple cache init");
    }

    @Override
    public Object getCache(String key) {
        Object result = store.get(key);
        log.info("get cache from simple cache, key={}, value={}", key, result);
        return result;
    }

    @Override
    public void setCache(String key, Object value) {
        log.info("set cache to simple cache, key={}", key);
        store.put(key, value);
    }

    @Override
    public void setCache(String key, Object value, int expired) {
        setCache(key, value);
    }

    @Override
    public void deleteCache(String key) {
        log.info("delete cache from simple cache, key={}", key);
        store.remove(key);
    }

    @Override
    public DLock getDCacheLock() {
        return new JDKLock();
    }
}
