package com.wcoal.novelplus.core.config;

import com.wcoal.novelplus.core.utils.R2Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cloudflare R2配置类:用于创建R2Utils对象
 */
@Slf4j
@Configuration
public class R2Configuration {

    @Bean
    @ConditionalOnMissingBean
    public R2Utils r2Utils(R2Properties r2Properties){
        log.info("开始初始化Cloudflare R2工具类对象: {}", r2Properties);
        return new R2Utils(
                r2Properties.getAccessKeyId(),
                r2Properties.getAccessKeySecret(),
                r2Properties.getEndpoint(),
                r2Properties.getBucketName(),
                r2Properties.getDomain());
    }
} 