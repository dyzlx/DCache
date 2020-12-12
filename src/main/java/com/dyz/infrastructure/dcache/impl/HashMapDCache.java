package com.dyz.infrastructure.dcache.impl;

import com.dyz.infrastructure.dcache.DCache;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class HashMapDCache implements DCache {

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

    public int getHashMapCacheSize() {
        return store.size();
    }
}
