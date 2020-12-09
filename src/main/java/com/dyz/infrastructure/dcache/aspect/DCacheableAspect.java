package com.dyz.infrastructure.dcache.aspect;


import com.dyz.infrastructure.dcache.annotations.DCacheable;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class DCacheableAspect {

    @Around("@annotation(dCacheable)")
    public Object aroundDCacheable(ProceedingJoinPoint point, DCacheable dCacheable) {
        Object result = null;
        try {
            result = point.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }
}
