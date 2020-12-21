package com.dyz.infrastructure.dcache.impl.simple;

import com.dyz.infrastructure.dcache.DCache;
import com.dyz.infrastructure.dcache.lock.DJdkLock;
import com.dyz.infrastructure.dcache.lock.DLock;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class HashMapDCache implements DCache {

    private final Map<String, Cache> store = new ConcurrentHashMap<>(16);

    public HashMapDCache() {
        log.info("hash simple cache init");
        Timer expireCheckTimer = new ShutdownEnabledExpireCheckTimer(
                "DCache-SimpleCache-ExpireCheckTimer", true);
        // execute expire check every 10 seconds
        expireCheckTimer.schedule(new ExpireCheckTask(), 0, 5 * 1000);
    }

    @Override
    public Object getCache(String key) {
        Object result = null;
        Cache cache = store.get(key);
        if(Objects.nonNull(cache)) {
            if(this.isCacheExpired(cache)) {
                store.remove(key);
                return null;
            }
            result = cache.getValue();
        }
        log.info("get cache from simple cache, key={}, value={}", key, result);
        return result;
    }

    @Override
    public void setCache(String key, Object value) {
        log.info("set cache to simple cache, key={}", key);
        store.put(key, new Cache(value));
    }

    @Override
    public void setCache(String key, Object value, int expired) {
        log.info("set cache to simple cache, key={}, expire={}", key, expired);
        store.put(key, new Cache(value, expired));
    }

    @Override
    public void deleteCache(String key) {
        log.info("delete cache from simple cache, key={}", key);
        store.remove(key);
    }

    @Override
    public DLock getDCacheLock() {
        return new DJdkLock();
    }

    private boolean isCacheExpired(String key) {
        Cache cache = this.store.get(key);
        return isCacheExpired(cache);
    }

    private boolean isCacheExpired(Cache cache) {
        if(Objects.isNull(cache)) {
            return true;
        }
        Long expiredTime = cache.getExpireTime();
        if(Objects.isNull(expiredTime)) {
            return false;
        }
        return System.currentTimeMillis() >= expiredTime;
    }

    @Data
    static class Cache {
        Object value;
        Long expireTime;

        Cache(Object v) {
            this(v, null);
        }

        /**
         *
         * @param v
         * @param expireTime expire time, in seconds
         */
        Cache(Object v, Integer expireTime) {
            this.value = v;
            if(Objects.nonNull(expireTime)) {
                this.expireTime = System.currentTimeMillis() + (long)(expireTime * 1000);
            }
        }
    }

    class ExpireCheckTask extends TimerTask {
        @Override
        public void run() {
            log.info("simple cache expire check running...");
            List<String> expiredCacheList = new ArrayList<>();
            for(Map.Entry<String, Cache> entry : store.entrySet()) {
                String key = entry.getKey();
                Cache value = entry.getValue();
                if(isCacheExpired(value)) {
                    expiredCacheList.add(key);
                }
            }
            for(String key : expiredCacheList) {
                store.remove(key);
            }
        }
    }

    @Slf4j
    static class ShutdownEnabledExpireCheckTimer extends Timer {
        private Thread cancelThread;
        private String name;

        ShutdownEnabledExpireCheckTimer(String name, boolean daemon) {
            super(name, daemon);
            this.name = name;
            // must super.cancel(), can not this.cancel()
            cancelThread = new Thread(super::cancel);
            log.info("init a shutdown enable timer for expire check, name={}", this.name);
            Runtime.getRuntime().addShutdownHook(this.cancelThread);
        }

        @Override
        public void cancel() {
            super.cancel();
            log.info("shut down expire check timer, name={}", this.name);
            try {
                Runtime.getRuntime().removeShutdownHook(this.cancelThread);
            } catch (Exception e) {
                log.error("cancel expire check timer error", e);
            }
        }
    }
}
