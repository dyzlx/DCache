package com.dyz.infrastructure.dcache.aspect;


import com.dyz.infrastructure.dcache.annotations.DCacheEvict;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class DCacheEvictAspect {

    @Around("@annotation(dCacheEvict)")
    public Object aroundDCachePut(ProceedingJoinPoint point, DCacheEvict dCacheEvict) {
        Object result = null;
        try {
            result = point.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }
}
