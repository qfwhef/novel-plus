package com.wcoal.novelplus.core.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.wcoal.novelplus.core.common.enums.ErrorCodeEnum;
import com.wcoal.novelplus.core.common.resp.RestResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.BindException;

import static com.wcoal.novelplus.core.common.enums.ErrorCodeEnum.USER_ERROR;

/**
 * 通用异常处理类
 *
 * @author wcoal
 * @since 2025-09-25
 **/
@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    /**
     * 处理404异常
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound() {
        return "404";
    }

    /**
     * 处理Sa-Token未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public RestResp<Void> handleNotLoginException(NotLoginException e) {
        log.warn("用户未登录: {}", e.getMessage());
        return RestResp.fail(USER_ERROR);
    }

    /**
     * 处理Sa-Token权限不足异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public RestResp<Void> handleNotPermissionException(NotPermissionException e) {
        log.warn("用户权限不足: {}", e.getMessage());
        return RestResp.fail(USER_ERROR);
    }

    /**
     * 处理Sa-Token角色不足异常
     */
    @ExceptionHandler(NotRoleException.class)
    public RestResp<Void> handleNotRoleException(NotRoleException e) {
        log.warn("用户角色不足: {}", e.getMessage());
        return RestResp.fail(USER_ERROR);
    }

    /**
     * 处理数据校验异常
     */
    @ExceptionHandler(BindException.class)
    public RestResp<Void> handleBindException(BindException e) {
        log.error(e.getMessage(), e);
        return RestResp.fail(ErrorCodeEnum.USER_REQUEST_PARAM_ERROR);
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public RestResp<Void> handleBusinessException(BusinessException e) {
        log.error(e.getMessage(), e);
        return RestResp.fail(e.getErrorCodeEnum());
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(Exception.class)
    public RestResp<Void> handleException(Exception e) {
        log.error(e.getMessage(), e);
        return RestResp.error();
    }
}
