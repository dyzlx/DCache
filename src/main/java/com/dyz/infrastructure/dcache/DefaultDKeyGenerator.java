package com.dyz.infrastructure.dcache;

import com.dyz.infrastructure.dcache.exception.KeyGenerateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class DefaultDKeyGenerator implements DKeyGenerator {

    private static final String KEY_SPLIT = ":";

    private static final String ARG_VALUE_REF = "#";

    @Override
    public String generateKey(String keyDescription, MethodSignature methodSignature, Object[] args) {
        if(Objects.isNull(methodSignature)) {
            throw new KeyGenerateException("key generate error, method signature is null");
        }
        String[] argNames = methodSignature.getParameterNames();
        if(StringUtils.isBlank(keyDescription)) {
            StringBuilder sb = new StringBuilder();
            sb.append(methodSignature.getMethod().getName());
            for(int i = 0 ; i < argNames.length ; i++) {
                sb.append(KEY_SPLIT).append(argNames[i])
                        .append(KEY_SPLIT).append(args[i]);
            }
            return sb.toString();
        }
        String result = keyDescription;
        String[] kds = keyDescription.split(KEY_SPLIT);
        for(int i = 0 ; i < kds.length ; i++) {
            if(kds[i].startsWith(ARG_VALUE_REF)) {
                String argRef = kds[i].substring(1);
                int argValueIndex = ArrayUtils.indexOf(argNames, argRef);
                if(argValueIndex != -1) {
                    result = result.replaceAll(kds[i], args[argValueIndex].toString());
                }
            }
        }
        return result;
    }
}
