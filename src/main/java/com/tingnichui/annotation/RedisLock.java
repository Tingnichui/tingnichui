package com.tingnichui.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author  Geng Hui
 * @date  2022/9/21 10:40
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisLock {
    String key();
    String value() default "1";
    long expire() default 60L;
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}