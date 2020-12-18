package com.dyz.infrastructure.dcache.generator;

import com.dyz.infrastructure.dcache.exception.KeyGenerateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.reflect.MethodSignature;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;


/**
 *
 * model:id:#id
 * <Code> @DCacheable(key="model:id:#id")
 *        public Model query(int id) {...} <Code/>
 *
 * model:name:#name:age:#age
 * <Code> @DCacheable(key="model:name:#name:age:#age")
 *        public Model query(String name, int age) {...} <Code/>
 *
 * model:id:#model.getId
 * <Code> @DCacheable(key="model:id:#model.getId")
 *        public Model query(Model model) {...} <Code/>
 *
 */
@Slf4j
public class OrdinaryDKeyGenerator implements DKeyGenerator {

    private static final String ARG_VALUE_REF = "#";

    private static final String ARG_VALUE_OBJECT_FIELD = ".";

    private static final String ARG_VALUE_OBJECT_FIELD_SPLIT = "\\.";

    @Override
    public String generateKey(String keyDescription, MethodSignature methodSignature, Object[] args) {
        if(Objects.isNull(methodSignature)) {
            throw new KeyGenerateException("key generate error, method signature is null");
        }
        String[] argNames = methodSignature.getParameterNames();
        if(StringUtils.isBlank(keyDescription)) {
            return generateKeyIfKeyDescIsEmpty(methodSignature, args);
        }
        String result = keyDescription;
        String[] kds = keyDescription.split(KEY_SPLIT);
        for(int i = 0 ; i < kds.length ; i++) {
            if(kds[i].startsWith(ARG_VALUE_REF)) {
                String argRef = kds[i].substring(1);
                if(!argRef.contains(ARG_VALUE_OBJECT_FIELD)) {
                    int argValueIndex = ArrayUtils.indexOf(argNames, argRef);
                    if(argValueIndex == -1) {
                        continue;
                    }
                    Object argValue = args[argValueIndex];
                    result = result.replaceAll(kds[i], argValue.toString());
                } else {
                    // we need the field value in this arg, not arg value itself
                    // like this:  model:id:#newModel.getId
                    // getId is a method in arg newModel
                    String[] argAndItsGetMethod = argRef.split(ARG_VALUE_OBJECT_FIELD_SPLIT);
                    String argObjectName = argAndItsGetMethod[0];
                    String getMethodName = argAndItsGetMethod[1];
                    int argValueIndex = ArrayUtils.indexOf(argNames, argObjectName);
                    if(argValueIndex == -1) {
                        continue;
                    }
                    Object argValue = args[argValueIndex];
                    try {
                        Method getMethod = argValue.getClass().getDeclaredMethod(getMethodName);
                        result = result.replaceAll(kds[i], getMethod.invoke(argValue).toString());
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                        throw new KeyGenerateException("key generate error, method "
                                + getMethodName + "() not exist or can't access in arg " + argObjectName
                                + "(type=" + argValue.getClass().getName() + ")" , e);
                    }
                }
            }
        }
        return result;
    }
}
