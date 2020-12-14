package com.dyz.infrastructure.dcache;

import org.aspectj.lang.ProceedingJoinPoint;

public interface DCache {

    Object getCache(String key);

    void setCache(String key, Object value);

    void setCache(String key, Object value, int expired);

    void deleteCache(String key);

    Object missCacheResetWithLock(String key, ProceedingJoinPoint point,
                                  int expireTime, long timeout) throws Throwable;
}
