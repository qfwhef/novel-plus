package com.wcoal.novelplus.core.annotation;

import com.wcoal.novelplus.core.common.enums.ErrorCodeEnum;

import java.lang.annotation.*;

/**
 * 锁注解
 *
 * @author wcoal
 * @since 2025-10-4
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lock {
    String prefix() default "";// 锁前缀

    boolean isWait() default false;// 是否等待获取锁

     long timeout() default 3L;// 锁超时时间，单位毫秒，默认不超时

    ErrorCodeEnum failCode() default ErrorCodeEnum.OK;// 锁获取失败时的返回码
}
