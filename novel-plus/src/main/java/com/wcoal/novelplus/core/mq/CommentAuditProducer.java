package com.wcoal.novelplus.core.mq;

import com.wcoal.novelplus.core.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 评论审核消息生产者
 * 
 * 负责发送评论审核消息到 RabbitMQ
 * 
 * @author wcoal
 * @since 2025-10-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentAuditProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送评论审核消息
     * 
     * @param commentId 评论ID
     * @param bookId 书籍ID
     * @param userId 用户ID
     * @param commentContent 评论内容
     */
    public void sendCommentAuditMessage(Long commentId, Long bookId, Long userId, String commentContent) {
        CommentAuditMessage message = CommentAuditMessage.builder()
                .commentId(commentId)
                .bookId(bookId)
                .userId(userId)
                .commentContent(commentContent)
                .createTime(LocalDateTime.now())
                .retryCount(0)
                .build();

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.COMMENT_AUDIT_EXCHANGE,
                    RabbitMQConfig.COMMENT_AUDIT_ROUTING_KEY,
                    message
            );
            log.info("评论审核消息发送成功，commentId: {}, bookId: {}, userId: {}", 
                    commentId, bookId, userId);
        } catch (Exception e) {
            log.error("评论审核消息发送失败，commentId: {}, error: {}", commentId, e.getMessage(), e);
            // 这里可以记录到数据库，实现消息补偿机制
        }
    }
}

