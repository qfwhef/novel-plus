package com.wcoal.novelplus.core.annotation;

import java.lang.annotation.*;

/**
 * 分布式锁-key注解
 *
 * @author wcoal
 * @since 2025-10-4
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Key {

    String expr() default "";// SpEL表达式，用于动态计算锁的key
}
