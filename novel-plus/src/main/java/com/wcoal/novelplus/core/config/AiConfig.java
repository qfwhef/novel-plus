package com.wcoal.novelplus.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * AI配置类
 */
@Configuration
@Slf4j
public class AiConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();//
        //连接超时时间
        factory.setConnectTimeout(5000);
        //读取超时时间
        factory.setReadTimeout(60000);
        return RestClient.builder().requestFactory(factory);
    }

    /**
     * 配置AI客户端
     * @param chatClientBuilder
     * @return
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }

}
