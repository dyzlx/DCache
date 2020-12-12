package com.dyz.infrastructure.dcache;

import org.aspectj.lang.reflect.MethodSignature;

@FunctionalInterface
public interface DKeyGenerator {

    static final String KEY_SPLIT = ":";

    /**
     *
     * @param keyDescription  property key() in DCache annotation
     * @param methodSignature
     * @param args method args values array
     * @return
     */
    String generateKey(String keyDescription, MethodSignature methodSignature, Object[] args);

    default String generateKeyIfKeyDescIsEmpty(MethodSignature methodSignature, Object[] args) {
        String[] argNames = methodSignature.getParameterNames();
        StringBuilder sb = new StringBuilder();
        sb.append(methodSignature.getMethod().getName());
        for(int i = 0 ; i < argNames.length ; i++) {
            sb.append(KEY_SPLIT).append(argNames[i])
                    .append(KEY_SPLIT).append(args[i]);
        }
        return sb.toString();
    }
}
