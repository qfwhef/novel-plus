package com.wcoal.novelplus.core.mq;

import com.wcoal.novelplus.core.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 书架阅读进度消息生产者
 *
 * @author wcoal
 * @since 2025-10-21
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookshelfProgressProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送阅读进度更新消息
     *
     * @param userId    用户ID
     * @param bookId    书籍ID
     * @param chapterId 章节ID
     */
    public void sendProgressUpdate(Long userId, Long bookId, Long chapterId) {
        try {
            BookshelfProgressMessage message = new BookshelfProgressMessage(
                    userId,
                    bookId,
                    chapterId,
                    System.currentTimeMillis()
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.BOOKSHELF_PROGRESS_EXCHANGE,
                    RabbitMQConfig.BOOKSHELF_PROGRESS_ROUTING_KEY,
                    message
            );

            log.info("发送阅读进度更新消息成功: userId={}, bookId={}, chapterId={}", userId, bookId, chapterId);
        } catch (Exception e) {
            log.error("发送阅读进度更新消息失败: userId={}, bookId={}, chapterId={}", userId, bookId, chapterId, e);
        }
    }
}
