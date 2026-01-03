package com.wcoal.novelplus.core.aspect;

import com.wcoal.novelplus.core.annotation.ValidateSortOrder;
import com.wcoal.novelplus.core.common.req.PageReqDto;
import com.wcoal.novelplus.core.common.resp.PageRespDto;
import com.wcoal.novelplus.core.common.utils.SortWhitelistUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * 排序字段校验切面类。
 * 用于拦截所有 Mapper 方法的调用，检查参数中是否包含 @ValidateSortOrder 注解。
 * 如果有，则对参数中的 sort 和 order 字段进行安全校验和清理。
 */
@Aspect
@Component
@RequiredArgsConstructor
public class SortOrderValidationAspect {

    /**
     * 拦截所有 Mapper 方法的调用，检查参数中是否包含 @ValidateSortOrder 注解。
     * 如果有，则对参数中的 sort 和 order 字段进行安全校验和清理。
     *
     * @param joinPoint
     * @return 方法执行结果
     * @throws Throwable 方法执行过程中抛出的异常
     */
    @SneakyThrows
    @Around("execution(* com.wcoal.novelplus.dao.mapper.*Mapper.*(..))")
    public Object processSortOrderFields(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();// 获取方法参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();// 获取方法签名
        Method method = signature.getMethod();

        //获取方法参数上的所有注解
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        //遍历所有参数，检查是否有 @ValidateSortOrder 注解
        for (int i = 0; i < parameterAnnotations.length; i++) {
            boolean hasAnnotation = Arrays.stream(parameterAnnotations[i]).anyMatch(a -> a.annotationType().equals(ValidateSortOrder.class));

            if (hasAnnotation && args[i] != null) {
                //对带注解的参数进行处理
                handleAnnotatedParameter(args[i]);
            }
        }
        return joinPoint.proceed(args);// 继续执行原始方法
    }

    /**
     * 根据参数类型，分别处理不同形式的 sort/order 字段。
     */
    private void handleAnnotatedParameter(Object obj) {
        if (obj instanceof PageReqDto dto) {
            processPageReqDto(dto);
        } else if (obj instanceof Map<?, ?> map) {
            processMap(map);
        } else {
            processGenericObject(obj);
        }
    }

    /**
     * 处理 PageReqDto 类型参数中的 sort 和 order 字段。
     */
    private void processPageReqDto(PageReqDto dto) {
        // 校验 sort 和 order 字段
        if (dto.getSort() != null) {
            dto.setSort(SortWhitelistUtil.sanitizeColumn(dto.getSort()));
        }
        if (dto.getOrder() != null) {
            dto.setOrder(SortWhitelistUtil.sanitizeOrder(dto.getOrder()));
        }
    }

    /**
     * 处理 Map 类型参数中的 sort 和 order 字段。
     */
    private void processMap(Map map) {
        if (map.get("sort") instanceof String sortStr) {
            map.put("sort", SortWhitelistUtil.sanitizeColumn(sortStr));
        }
        if (map.get("order") instanceof String orderStr) {
            map.put("order", SortWhitelistUtil.sanitizeOrder(orderStr));
        }
    }

    /**
     * 使用反射处理任意对象中的 sort 和 order 字段。
     * 支持任何带有这两个字段的 POJO。
     */
    @SneakyThrows
    private void processGenericObject(Object obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            switch (field.getName()) {
                case "sort", "order" -> {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    if (value instanceof String strValue) {
                        String sanitized = "sort".equals(field.getName())
                                ? SortWhitelistUtil.sanitizeColumn(strValue)
                                : SortWhitelistUtil.sanitizeOrder(strValue);
                        field.set(obj, sanitized);
                    }
                }
                default -> {
                    // 忽略其他字段
                }
            }
        }
    }
}
