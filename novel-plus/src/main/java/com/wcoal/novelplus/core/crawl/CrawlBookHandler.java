package com.wcoal.novelplus.core.crawl;

import com.wcoal.novelplus.dao.entity.BookInfo;

/**
 * 小说信息处理回调接口
 *
 * @author wcoal
 * @since 2025-11-23
 */
@FunctionalInterface
public interface CrawlBookHandler {

    /**
     * 处理解析到的小说信息
     *
     * @param book 小说信息
     */
    void handle(BookInfo book);
}
