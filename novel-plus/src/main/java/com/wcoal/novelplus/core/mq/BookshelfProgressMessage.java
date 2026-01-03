package com.wcoal.novelplus.core.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 书架阅读进度消息
 *
 * @author wcoal
 * @since 2025-10-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookshelfProgressMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 书籍ID
     */
    private Long bookId;

    /**
     * 章节ID
     */
    private Long chapterId;

    /**
     * 消息发送时间戳
     */
    private Long timestamp;
}
