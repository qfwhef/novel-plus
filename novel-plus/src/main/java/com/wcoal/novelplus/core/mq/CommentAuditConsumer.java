package com.wcoal.novelplus.core.mq;

import com.rabbitmq.client.Channel;
import com.wcoal.novelplus.core.config.RabbitMQConfig;
import com.wcoal.novelplus.dao.entity.BookComment;
import com.wcoal.novelplus.dao.mapper.BookCommentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 评论审核消息消费者
 * 
 * 负责消费评论审核消息，执行审核逻辑
 * 
 * 审核规则（简化版）：
 * 1. 检查是否包含敏感词，包含则审核不通过
 * 2. 检查评论长度，过短或过长则审核不通过
 * 3. 其他情况审核通过
 * 
 * @author wcoal
 * @since 2025-10-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentAuditConsumer {

    private final BookCommentMapper bookCommentMapper;

    /**
     * 敏感词列表（实际项目中应该从配置中心或数据库读取）
     */
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            "垃圾", "傻逼", "sb", "fuck", "shit", "政治", "暴力"
    );

    /**
     * 消费评论审核消息
     * 
     * @param auditMessage 审核消息
     * @param message 原始消息
     * @param channel 消息通道
     */
    @RabbitListener(queues = RabbitMQConfig.COMMENT_AUDIT_QUEUE)
    public void handleCommentAudit(CommentAuditMessage auditMessage, Message message, Channel channel) {
        //deliveryTag 是 RabbitMQ 用于唯一标识消息的一个数字，用于确认消息是否被成功处理。
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        try {
            log.info("开始处理评论审核消息，commentId: {}, bookId: {}, userId: {}", 
                    auditMessage.getCommentId(), auditMessage.getBookId(), auditMessage.getUserId());

            // 执行审核逻辑
            boolean auditPassed = auditComment(auditMessage);

            // 更新评论审核状态
            BookComment bookComment = new BookComment();
            bookComment.setId(auditMessage.getCommentId());
            
            if (auditPassed) {
                // 审核通过：状态设置为1
                bookComment.setAuditStatus(1);
                log.info("评论审核通过，commentId: {}", auditMessage.getCommentId());
            } else {
                // 审核不通过：状态设置为2
                bookComment.setAuditStatus(2);
                log.warn("评论审核不通过，commentId: {}, 原因: 包含敏感词或不符合规范", 
                        auditMessage.getCommentId());
            }

            // 更新数据库
            bookCommentMapper.updateById(bookComment);

            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            
            log.info("评论审核消息处理完成，commentId: {}, 审核结果: {}", 
                    auditMessage.getCommentId(), auditPassed ? "通过" : "不通过");

        } catch (Exception e) {
            log.error("评论审核消息处理失败，commentId: {}, error: {}", 
                    auditMessage.getCommentId(), e.getMessage(), e);
            
            try {
                // 获取重试次数
                Integer retryCount = auditMessage.getRetryCount();
                if (retryCount == null) {
                    retryCount = 0;
                }

                // 如果重试次数小于3次，拒绝消息并重新入队
                if (retryCount < 3) {
                    auditMessage.setRetryCount(retryCount + 1);
                    log.info("评论审核消息重试，commentId: {}, 重试次数: {}", 
                            auditMessage.getCommentId(), retryCount + 1);
                    // requeue = true，消息重新入队
                    channel.basicNack(deliveryTag, false, true);
                } else {
                    // 重试次数超过3次，拒绝消息并转入死信队列
                    log.error("评论审核消息重试次数超过限制，转入死信队列，commentId: {}", 
                            auditMessage.getCommentId());
                    // requeue = false，消息不重新入队，会进入死信队列
                    channel.basicNack(deliveryTag, false, false);
                }
            } catch (IOException ioException) {
                log.error("消息确认失败，commentId: {}", auditMessage.getCommentId(), ioException);
            }
        }
    }

    /**
     * 审核评论内容
     * 
     * @param auditMessage 审核消息
     * @return true-审核通过，false-审核不通过
     */
    private boolean auditComment(CommentAuditMessage auditMessage) {
        String content = auditMessage.getCommentContent();

        // 1. 检查评论是否为空
        if (content == null || content.trim().isEmpty()) {
            log.warn("评论内容为空，审核不通过");
            return false;
        }

        // 2. 检查评论长度（10-200字符）
        if (content.length() < 10 || content.length() > 200) {
            log.warn("评论长度不符合要求，审核不通过，长度: {}", content.length());
            return false;
        }

        // 3. 检查是否包含敏感词
        String lowerContent = content.toLowerCase();// 转换为小写，忽略大小写
        for (String sensitiveWord : SENSITIVE_WORDS) {
            if (lowerContent.contains(sensitiveWord.toLowerCase())) {
                log.warn("评论包含敏感词: {}, 审核不通过", sensitiveWord);
                return false;
            }
        }

        return true;
    }

    /**
     * 处理死信队列中的消息
     * 这些是审核失败多次的消息，需要人工介入处理
     */
    @RabbitListener(queues = RabbitMQConfig.COMMENT_AUDIT_DLQ)
    public void handleDeadLetterMessage(CommentAuditMessage auditMessage, Message message, Channel channel) {
        try {
            log.error("收到死信消息，需要人工处理，commentId: {}, bookId: {}, userId: {}", 
                    auditMessage.getCommentId(), auditMessage.getBookId(), auditMessage.getUserId());

            // 这里可以：
            // 1. 记录到专门的错误日志表
            // 2. 发送告警通知（钉钉、邮件等）
            // 3. 将评论标记为"待人工审核"状态

            // 更新评论状态为待人工审核（假设状态3表示待人工审核）
            BookComment bookComment = new BookComment();
            bookComment.setId(auditMessage.getCommentId());
            bookComment.setAuditStatus(0); // 保持待审核状态，等待人工处理
            bookCommentMapper.updateById(bookComment);

            // 确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            
        } catch (Exception e) {
            log.error("处理死信消息失败，commentId: {}", auditMessage.getCommentId(), e);
        }
    }
}

