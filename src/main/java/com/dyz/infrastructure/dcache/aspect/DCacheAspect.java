package com.dyz.infrastructure.dcache.aspect;


import com.dyz.infrastructure.dcache.DCache;
import com.dyz.infrastructure.dcache.DKeyGenerator;
import com.dyz.infrastructure.dcache.annotations.DCacheEvict;
import com.dyz.infrastructure.dcache.annotations.DCachePut;
import com.dyz.infrastructure.dcache.annotations.DCacheable;
import com.dyz.infrastructure.dcache.lock.DLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Aspect
@Slf4j
public class DCacheAspect {

    /**
     * will autowired in constructor method
     */
    private DCache dCache;

    @Autowired
    @Qualifier(value = "ordinaryDKeyGenerator")
    private DKeyGenerator defaultDKeyGenerator;

    @Autowired
    private ApplicationContext applicationContext;

    private DLock dLock;

    public DCacheAspect(DCache dCache) {
        this.dCache = dCache;
        dLock = dCache.getDCacheLock();
    }

    @Around("@annotation(dCacheable)")
    public Object aroundDCacheable(ProceedingJoinPoint point, DCacheable dCacheable) throws Throwable {
        Object result;
        try {
            String key = generateKey(point, dCacheable.key(), dCacheable.keyGeneratorName());
            result = dCache.getCache(key);
            if(Objects.nonNull(result)) {
                return result;
            }
            if(dCacheable.lockWhenQueryDB()) {
                missCacheThenResetCacheWithLock(point, dLock, key, dCacheable.expire());
            } else {
                result = point.proceed();
                dCache.setCache(key, result, dCacheable.expire());
            }
        } catch (Throwable e) {
            log.error("DCacheable join point process error", e);
            throw e;
        }
        return result;
    }

    @Around("@annotation(dCacheEvict)")
    public Object aroundDCacheEvict(ProceedingJoinPoint point, DCacheEvict dCacheEvict) throws Throwable {
        Object result;
        try {
            String key = generateKey(point, dCacheEvict.key(), dCacheEvict.keyGeneratorName());
            result = point.proceed();
            dCache.deleteCache(key);
        } catch (Throwable e) {
            log.error("DCacheEvict join point process error", e);
            throw e;
        }
        return result;
    }

    @Around("@annotation(dCachePut)")
    public Object aroundDCachePut(ProceedingJoinPoint point, DCachePut dCachePut) throws Throwable {
        Object result;
        try {
            String key = generateKey(point, dCachePut.key(), dCachePut.keyGeneratorName());
            result = point.proceed();
            if(dCachePut.deleteKeyInsteadOfUpdate()) {
                dCache.deleteCache(key);
            } else {
                dCache.setCache(key, result, dCachePut.expire());
            }
        } catch (Throwable e) {
            log.error("DCachePut join point process error", e);
            throw e;
        }
        return result;
    }

    private Object missCacheThenResetCacheWithLock(ProceedingJoinPoint point, DLock lock,
                                                   String key, int expireTime) throws Throwable {
        Object result;
        if(lock.lock()) {
            try {
                result = dCache.getCache(key);
                if(Objects.nonNull(result)) {
                    return result;
                }
                result = point.proceed();
                dCache.setCache(key, result, expireTime);
            } catch (Throwable e) {
                log.error("error when query db then set redis cache with lock, key={}", key);
                throw e;
            } finally {
                lock.unlock();
            }
        } else {
            result = missCacheThenResetCacheWithLock(point, lock, key, expireTime);
        }
        return result;
    }

    private String generateKey(ProceedingJoinPoint point, String keyDescription, String keyGeneratorName) {
        MethodSignature methodSignature = (MethodSignature)point.getSignature();
        if(StringUtils.isBlank(keyGeneratorName)) {
            return defaultDKeyGenerator.generateKey(keyDescription,methodSignature,point.getArgs());
        }
        try {
            DKeyGenerator dKeyGenerator = (DKeyGenerator) applicationContext.getBean(keyGeneratorName);
            return dKeyGenerator.generateKey(keyDescription, methodSignature, point.getArgs());
        } catch (NoSuchBeanDefinitionException e) {
            log.warn("no such DKeyGenerator found, bean name={}, use default key generator instead.", keyGeneratorName);
            return defaultDKeyGenerator.generateKey(keyDescription,methodSignature,point.getArgs());
        }
    }
}
