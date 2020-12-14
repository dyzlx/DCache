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
        Object result = store.get(key);
        log.info("get cache from map cache, key={}, value={}", key, result);
        return result;
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
    public Object missCacheResetWithLock(String key, ProceedingJoinPoint point, int expireTime, long timeout)
            throws Throwable {
        Object result;
        boolean isLock = false;
        try {
            isLock = lock.tryLock(timeout, TimeUnit.MILLISECONDS);
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
            if(isLock) {
                lock.unlock();
            }
        }
        return result;
    }
}
