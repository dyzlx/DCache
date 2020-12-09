package com.dyz.infrastructure.dcache.aspect;


import com.dyz.infrastructure.dcache.annotations.DCachePut;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
public class DCachePutAspect {

    @Around("@annotation(dCachePut)")
    public Object aroundDCachePut(ProceedingJoinPoint point, DCachePut dCachePut) {
        Object result = null;
        try {
            result = point.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }
}
