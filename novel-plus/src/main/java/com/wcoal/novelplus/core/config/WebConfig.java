package com.wcoal.novelplus.core.config;

import com.wcoal.novelplus.core.interceptor.FlowLimitInterceptor;
import com.wcoal.novelplus.core.interceptor.UserContextInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserContextInterceptor userContextInterceptor;
    private final FlowLimitInterceptor flowLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Token 解析拦截器
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/**")
                        .excludePathPatterns(
                                "/api/front/user/register",
                                "/api/front/user/login",
                                "/api/front/resource/img_verify_code",
                                "/api/front/home/books",
                                "/api/front/news/latest_list",
                                "/api/front/search/books",
                                "/swagger-ui/**",
                                "/api/admin/crawl/**",
                                "/v3/**",
                                "/crawl-admin.html",
                                "/api-docs",
                                "/error",
                                "/favicon.ico"
                        );

        // 流量限制拦截器
        registry.addInterceptor(flowLimitInterceptor)
                .addPathPatterns("/**");
    }
}
