package com.wcoal.novelplus.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "novel-plus.cors")
public record CorsProperties(List<String> allowOrigins) {
}
