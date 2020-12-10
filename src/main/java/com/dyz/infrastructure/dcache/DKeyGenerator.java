package com.dyz.infrastructure.dcache;

import org.aspectj.lang.reflect.MethodSignature;

@FunctionalInterface
public interface DKeyGenerator {

    String generateKey(String keyDescription, MethodSignature methodSignature, Object[] args);
}
