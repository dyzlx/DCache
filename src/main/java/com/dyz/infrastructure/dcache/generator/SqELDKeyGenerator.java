package com.dyz.infrastructure.dcache.generator;


import com.dyz.infrastructure.dcache.DKeyGenerator;
import com.dyz.infrastructure.dcache.exception.KeyGenerateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Objects;


/**
 *
 * generate DCache key by SqEL
 *
 */
@Slf4j
public class SqELDKeyGenerator implements DKeyGenerator {

    private SpelExpressionParser parser = new SpelExpressionParser();

    private DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Override
    public String generateKey(String sqELKeyDescription, MethodSignature methodSignature, Object[] args) {
        if(Objects.isNull(methodSignature)) {
            throw new KeyGenerateException("key generate error, method signature is null");
        }
        if(StringUtils.isBlank(sqELKeyDescription)) {
            return generateKeyIfKeyDescIsEmpty(methodSignature, args);
        }
        return generateKeyBySpEL(sqELKeyDescription, methodSignature, args);
    }

    private String generateKeyBySpEL(String spELString, MethodSignature methodSignature, Object[] args) {
        String[] paramNames = nameDiscoverer.getParameterNames(methodSignature.getMethod());
        Expression expression = parser.parseExpression(spELString);
        EvaluationContext context = new StandardEvaluationContext();
        for(int i = 0 ; i < args.length ; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        return expression.getValue(context).toString();
    }
}
