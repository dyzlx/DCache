package com.dyz.infrastructure.dcache.impl.map;

import com.dyz.infrastructure.dcache.DCache;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.Map;
import java.util.Objects;
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
    public Object queryDBThenSetCacheWithLock(String key, ProceedingJoinPoint point, int expireTime) throws Throwable {
        Object result;
        try {
            lock.lock();
            result = this.getCache(key);
            if(Objects.nonNull(result)) {
                return result;
            }
            result = point.proceed();
            this.setCache(key, result, expireTime);
        } catch (Throwable e) {
            log.error("error when query db then set map cache with lock, key={}", key);
            throw e;
        } finally {
            lock.unlock();
        }
        return result;
    }
}
