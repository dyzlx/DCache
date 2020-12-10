package com.dyz.infrastructure.dcache;

import org.aspectj.lang.reflect.MethodSignature;

@FunctionalInterface
public interface DKeyGenerator {

    /**
     *
     * @param keyDescription  property key() in DCache annotation
     * @param methodSignature
     * @param args method args values array
     * @return
     */
    String generateKey(String keyDescription, MethodSignature methodSignature, Object[] args);
}
