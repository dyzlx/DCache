package com.dyz.infrastructure.dcache;


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

import java.util.Arrays;

@Component
@Aspect
@Slf4j
public class DCacheAspect {

    @Autowired(required = false)
    private DCacheHandler handler;

    @Autowired
    private DKeyGenerator dKeyGenerator;

    @Around("@annotation(dCacheable)")
    public Object aroundDCacheable(ProceedingJoinPoint point, DCacheable dCacheable) {
        Object result = null;
        try {
            String key = dKeyGenerator.generateKey(dCacheable.key(),(MethodSignature) point.getSignature(),point.getArgs());
            System.out.println("key generate : "+key);
            result = point.proceed();
        } catch (Throwable e) {
            log.error("DCacheable join point process error", e);
        }
        return result;
    }

    @Around("@annotation(dCacheEvict)")
    public Object aroundDCacheEvict(ProceedingJoinPoint point, DCacheEvict dCacheEvict) {
        Object result = null;
        try {
            result = point.proceed();
        } catch (Throwable e) {
            log.error("DCacheEvict join point process error", e);
        }
        return result;
    }

    @Around("@annotation(dCachePut)")
    public Object aroundDCachePut(ProceedingJoinPoint point, DCachePut dCachePut) {
        Object result = null;
        try {
            result = point.proceed();
        } catch (Throwable e) {
            log.error("DCachePut join point process error", e);
        }
        return result;
    }
}
