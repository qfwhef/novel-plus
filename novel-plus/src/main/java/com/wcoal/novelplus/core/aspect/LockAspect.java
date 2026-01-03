package com.wcoal.novelplus.core.aspect;

import com.wcoal.novelplus.core.annotation.Key;
import com.wcoal.novelplus.core.annotation.Lock;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁-切面类
 *
 * @author wcoal
 * @since 2025-10-4
 */
@Aspect
@Order(100)
@RequiredArgsConstructor
public class LockAspect {

    private final RedissonClient redissonClient;

    private static final String KEY_PREFIX = "Lock";

    private static final String KEY_SEPARATOR = "::";

    @Around(value = "@annotation(com.wcoal.novelplus.core.annotation.Lock)")
    @SneakyThrows
    public Object doAround(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();//获取方法签名
        Method targetMethod = methodSignature.getMethod();//获取目标方法
        Lock lock = targetMethod.getAnnotation(Lock.class);//获取方法上的Lock注解
        String lockKey = KEY_PREFIX + buildLockKey(lock.prefix(), targetMethod, joinPoint.getArgs());//构建锁键
        RLock rLock = redissonClient.getLock(lockKey);//获取锁
        if (lock.isWait() ? rLock.tryLock(lock.timeout(), TimeUnit.SECONDS) : rLock.tryLock()){//尝试获取锁
            try {
                return joinPoint.proceed();//执行目标方法
            } finally {
                rLock.unlock();//释放锁
            }
        }
        // 获取锁失败，根据 failCode 返回相应的结果
        if (lock.failCode() != com.wcoal.novelplus.core.common.enums.ErrorCodeEnum.OK) {
            // 如果返回类型是 RestResp，则返回失败响应
            Class<?> returnType = targetMethod.getReturnType();
            if (returnType.getName().contains("RestResp")) {
                return com.wcoal.novelplus.core.common.resp.RestResp.fail(lock.failCode());
            }
        }
        throw new RuntimeException("获取锁失败");//如果获取锁失败，抛出异常
    }

    /**
     * 构建锁键
     * @param prefix 锁前缀
     * @param targetMethod 目标方法
     * @param args 方法参数
     * @return 锁键
     */
    private String buildLockKey(String prefix, Method targetMethod, Object[] args) {
        StringBuilder sb = new StringBuilder(prefix);
        if (StringUtils.hasText(prefix)){
            sb.append(KEY_SEPARATOR).append(prefix);
        }
        Parameter[] parameters = targetMethod.getParameters();//获取方法参数
        for (int i = 0; i < parameters.length; i++) {
            sb.append(KEY_SEPARATOR);
            if (parameters[i].isAnnotationPresent(Key.class)){//如果参数上有Key注解
                Key key = parameters[i].getAnnotation(Key.class);//获取参数上的Key注解
                sb.append(parseKeyExpr(key.expr(), args[i]));//解析Key表达式，将参数值替换到表达式中
            }
        }
        return sb.toString();
    }

    /**
     * 解析Key表达式，将参数值替换到表达式中
     * @param expr Key表达式
     * @param arg 参数值
     * @return 解析后的表达式
     */
    private String parseKeyExpr(String expr, Object arg) {
        if (!StringUtils.hasText(expr)){//如果表达式为空
            return arg.toString();
        }
        SpelExpressionParser parser = new SpelExpressionParser();//创建Spel表达式解析器
        Expression expression = parser.parseExpression(expr, new TemplateParserContext());//解析表达式
        return expression.getValue(arg, String.class);//将参数值替换到表达式中，返回解析后的表达式
    }

}
