package com.dyz.infrastructure.dcache;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DefaultDKeyGenerator implements DKeyGenerator{

    @Override
    public String generateKey(String keyDescription, MethodSignature methodSignature, Object[] args) {
        return null;
    }
}
