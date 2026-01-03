package com.wcoal.novelplus.core.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.wcoal.novelplus.core.auth.UserContext;
import com.wcoal.novelplus.manager.cache.AuthorInfoCacheManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserContextInterceptor implements HandlerInterceptor {

    private final AuthorInfoCacheManager authorInfoCacheManager;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        try {
            if (StpUtil.isLogin()) {
                Long userId = StpUtil.getLoginIdAsLong();
                UserContext.setUserId(userId);
                if (authorInfoCacheManager.getAuthor(userId) != null) {
                    UserContext.setAuthorId(authorInfoCacheManager.getAuthor(userId).getId());
                    log.info("作家id：{}", UserContext.getAuthorId());
                    log.info("请求路径：{}", requestURI);
                }
                log.info("用户id：{}", UserContext.getUserId());
                log.info("请求路径：{}", requestURI);

            } else {
                //未登录，返回401
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                handleUnauthorized(response, "请先登录");
                log.info("未登录，返回401");
                return false;
            }
        } catch (Exception e) {
            log.error("UserContextInterceptor preHandle error", e);
            handleUnauthorized(response, "认证失败");
            return false;
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            UserContext.clear();
        } catch (Exception e) {
            log.error("UserContextInterceptor afterCompletion error", e);
        }
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    private void handleUnauthorized(HttpServletResponse response, String message) {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        try {
            String jsonResponse = String.format(
                    "{\"code\":\"A0230\",\"message\":\"%s\",\"data\":null}",
                    message
            );
            response.getWriter().write(jsonResponse);
        } catch (IOException e) {
            log.error("写入响应失败", e);
        }
    }
}
