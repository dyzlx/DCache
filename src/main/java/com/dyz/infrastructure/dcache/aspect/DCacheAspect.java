package com.dyz.infrastructure.dcache.aspect;


import com.dyz.infrastructure.dcache.DCache;
import com.dyz.infrastructure.dcache.DKeyGenerator;
import com.dyz.infrastructure.dcache.annotations.DCacheEvict;
import com.dyz.infrastructure.dcache.annotations.DCachePut;
import com.dyz.infrastructure.dcache.annotations.DCacheable;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Aspect
@Slf4j
public class DCacheAspect {

    @Autowired(required = false)
    private DCache dCache;

    @Autowired
    private DKeyGenerator dKeyGenerator;

    @Around("@annotation(dCacheable)")
    public Object aroundDCacheable(ProceedingJoinPoint point, DCacheable dCacheable) {
        Object result = null;
        try {
            MethodSignature methodSignature = (MethodSignature)point.getSignature();
            String key = dKeyGenerator.generateKey(dCacheable.key(),methodSignature,point.getArgs());
            result = dCache.getCache(key);
            if(Objects.nonNull(result)) {
                return result;
            }
            result = point.proceed();
            dCache.setCache(key, result, dCacheable.expire());
        } catch (Throwable e) {
            log.error("DCacheable join point process error", e);
        }
        return result;
    }

    @Around("@annotation(dCacheEvict)")
    public Object aroundDCacheEvict(ProceedingJoinPoint point, DCacheEvict dCacheEvict) {
        Object result = null;
        try {
            MethodSignature methodSignature = (MethodSignature)point.getSignature();
            String key = dKeyGenerator.generateKey(dCacheEvict.key(),methodSignature,point.getArgs());
            result = point.proceed();
            dCache.deleteCache(key);
        } catch (Throwable e) {
            log.error("DCacheEvict join point process error", e);
        }
        return result;
    }

    @Around("@annotation(dCachePut)")
    public Object aroundDCachePut(ProceedingJoinPoint point, DCachePut dCachePut) {
        Object result = null;
        try {
            MethodSignature methodSignature = (MethodSignature)point.getSignature();
            String key = dKeyGenerator.generateKey(dCachePut.key(),methodSignature,point.getArgs());
            result = point.proceed();
            dCache.setCache(key, result, dCachePut.expire());
        } catch (Throwable e) {
            log.error("DCachePut join point process error", e);
        }
        return result;
    }
}
