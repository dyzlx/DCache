package com.dyz.infrastructure.dcache.annotations;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DCacheable {

    String key() default "";

    int expire() default 300;

    String keyGeneratorName() default "";

    boolean lockWhenQueryDB() default true;
}
