package com.wcoal.novelplus.core.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * 
 * 功能说明：
 * 1. 配置评论审核队列、交换机、死信队列
 * 2. 配置消息序列化方式（JSON）
 * 3. 配置消息确认机制
 * 
 * @author wcoal
 * @since 2025-10-10
 */
@Configuration
public class RabbitMQConfig {

    // ==================== 评论审核相关配置 ====================
    
    /**
     * 评论审核队列名称
     */
    public static final String COMMENT_AUDIT_QUEUE = "novel.comment.audit.queue";
    
    /**
     * 评论审核交换机
     */
    public static final String COMMENT_AUDIT_EXCHANGE = "novel.comment.audit.exchange";
    
    /**
     * 评论审核路由键
     */
    public static final String COMMENT_AUDIT_ROUTING_KEY = "comment.audit";
    
    /**
     * 评论审核死信队列
     */
    public static final String COMMENT_AUDIT_DLQ = "novel.comment.audit.dlq";
    
    /**
     * 评论审核死信交换机
     */
    public static final String COMMENT_AUDIT_DLX = "novel.comment.audit.dlx";
    
    /**
     * 评论审核死信路由键
     */
    public static final String COMMENT_AUDIT_DLK = "comment.audit.dlk";

    // ==================== 书架阅读进度相关配置 ====================
    
    /**
     * 书架阅读进度队列名称
     */
    public static final String BOOKSHELF_PROGRESS_QUEUE = "novel.bookshelf.progress.queue";
    
    /**
     * 书架阅读进度交换机
     */
    public static final String BOOKSHELF_PROGRESS_EXCHANGE = "novel.bookshelf.progress.exchange";
    
    /**
     * 书架阅读进度路由键
     */
    public static final String BOOKSHELF_PROGRESS_ROUTING_KEY = "bookshelf.progress";

    /**
     * 声明评论审核队列
     * 配置死信交换机，当消息处理失败时转发到死信队列
     */
    @Bean
    public Queue commentAuditQueue() {
        return QueueBuilder.durable(COMMENT_AUDIT_QUEUE)
                .deadLetterExchange(COMMENT_AUDIT_DLX)
                .deadLetterRoutingKey(COMMENT_AUDIT_DLK)
                .build();
    }

    /**
     * 声明评论审核交换机（直连模式）
     */
    @Bean
    public DirectExchange commentAuditExchange() {
        return new DirectExchange(COMMENT_AUDIT_EXCHANGE, true, false);
    }

    /**
     * 绑定评论审核队列到交换机
     */
    @Bean
    public Binding commentAuditBinding() {
        return BindingBuilder
                .bind(commentAuditQueue())
                .to(commentAuditExchange())
                .with(COMMENT_AUDIT_ROUTING_KEY);
    }

    /**
     * 声明评论审核死信队列
     */
    @Bean
    public Queue commentAuditDeadLetterQueue() {
        return QueueBuilder.durable(COMMENT_AUDIT_DLQ).build();
    }

    /**
     * 声明评论审核死信交换机
     */
    @Bean
    public DirectExchange commentAuditDeadLetterExchange() {
        return new DirectExchange(COMMENT_AUDIT_DLX, true, false);
    }

    /**
     * 绑定死信队列到死信交换机
     */
    @Bean
    public Binding commentAuditDeadLetterBinding() {
        return BindingBuilder
                .bind(commentAuditDeadLetterQueue())
                .to(commentAuditDeadLetterExchange())
                .with(COMMENT_AUDIT_DLK);
    }

    // ==================== 书架阅读进度队列配置 ====================

    /**
     * 声明书架阅读进度队列
     */
    @Bean
    public Queue bookshelfProgressQueue() {
        return QueueBuilder.durable(BOOKSHELF_PROGRESS_QUEUE).build();
    }

    /**
     * 声明书架阅读进度交换机（直连模式）
     */
    @Bean
    public DirectExchange bookshelfProgressExchange() {
        return new DirectExchange(BOOKSHELF_PROGRESS_EXCHANGE, true, false);
    }

    /**
     * 绑定书架阅读进度队列到交换机
     */
    @Bean
    public Binding bookshelfProgressBinding() {
        return BindingBuilder
                .bind(bookshelfProgressQueue())
                .to(bookshelfProgressExchange())
                .with(BOOKSHELF_PROGRESS_ROUTING_KEY);
    }

    // ==================== 消息转换器配置 ====================

    /**
     * 配置消息转换器，使用 JSON 格式
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置 RabbitTemplate
     * 设置消息转换器和确认回调
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        
        // 设置消息发送确认回调
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                // 消息成功到达交换机
                System.out.println("消息发送成功：" + correlationData);
            } else {
                // 消息未到达交换机
                System.err.println("消息发送失败：" + cause);
            }
        });
        
        // 设置消息返回回调（当消息无法路由到队列时触发）
        template.setReturnsCallback(returned -> {
            System.err.println("消息路由失败：" + returned.getMessage());
        });
        
        return template;
    }

    /**
     * 配置监听器容器工厂
     * 设置消息转换器和手动确认模式
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        // 设置手动确认模式
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }
}

