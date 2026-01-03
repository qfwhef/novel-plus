package com.wcoal.novelplus.core.mq;

import com.rabbitmq.client.Channel;
import com.wcoal.novelplus.core.config.RabbitMQConfig;
import com.wcoal.novelplus.manager.redis.ReadProgressManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 书架阅读进度消息消费者
 * 优化方案：将阅读进度先写入 Redis，由定时任务批量同步到数据库
 *
 * @author wcoal
 * @since 2025-10-21
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookshelfProgressConsumer {

    private final ReadProgressManager readProgressManager;

    /**
     * 处理阅读进度更新消息
     * 优化：只更新 Redis，不直接操作数据库
     *
     * @param progressMessage 阅读进度消息
     * @param message         原始消息
     * @param channel         消息通道
     */
    @RabbitListener(queues = RabbitMQConfig.BOOKSHELF_PROGRESS_QUEUE, ackMode = "MANUAL")
    public void handleProgressUpdate(BookshelfProgressMessage progressMessage, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        try {
            log.debug("收到阅读进度更新消息: userId={}, bookId={}, chapterId={}", 
                    progressMessage.getUserId(), 
                    progressMessage.getBookId(), 
                    progressMessage.getChapterId());

            // 更新到 Redis（异步，不阻塞）
            readProgressManager.updateReadProgress(
                    progressMessage.getUserId(),
                    progressMessage.getBookId(),
                    progressMessage.getChapterId()
            );

            log.debug("阅读进度已更新到Redis: userId={}, bookId={}, chapterId={}", 
                    progressMessage.getUserId(), 
                    progressMessage.getBookId(), 
                    progressMessage.getChapterId());

            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            
        } catch (Exception e) {
            log.error("处理阅读进度更新消息失败: userId={}, bookId={}, chapterId={}", 
                    progressMessage.getUserId(), 
                    progressMessage.getBookId(), 
                    progressMessage.getChapterId(), e);
            
            try {
                // 消息处理失败，拒绝消息并重新入队
                // 第三个参数 requeue=true 表示重新入队，false 表示丢弃或进入死信队列
                channel.basicNack(deliveryTag, false, true);
            } catch (Exception ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }
}
