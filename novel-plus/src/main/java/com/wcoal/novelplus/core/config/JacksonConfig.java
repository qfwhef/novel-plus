package com.wcoal.novelplus.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson配置类
 * 解决JavaScript大整数精度丢失问题
 * 将Long类型序列化为字符串，避免前端精度丢失
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 注册Java 8时间模块，支持LocalDateTime等类型
        mapper.registerModule(new JavaTimeModule());
        
        // 创建自定义模块处理Long类型
        SimpleModule module = new SimpleModule();
        // 将Long类型序列化为字符串，解决JavaScript精度丢失问题
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        
        mapper.registerModule(module);
        return mapper;
    }
}