package com.wcoal.novelplus.core.config;

import com.wcoal.novelplus.core.common.constant.SystemConfigConsts;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * OpenApi 配置类
 *
 * @author wcoal
 * @since  2025-10-10
 */
@Configuration
@Profile("dev")// 仅在开发环境开启 OpenApi 文档
@OpenAPIDefinition(info = @Info(title = "novel 项目接口文档", version = "v3.2.0", license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")))// 配置 OpenApi 文档信息
@SecurityScheme(type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, name = SystemConfigConsts.HTTP_AUTH_HEADER_NAME, description = "登录 token")// 配置登录 token 作为 API 密钥
public class OpenApiConfig {

}