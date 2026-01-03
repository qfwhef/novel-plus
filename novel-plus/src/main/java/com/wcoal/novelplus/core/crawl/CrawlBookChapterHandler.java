package com.wcoal.novelplus.core.crawl;

/**
 * 章节信息处理回调接口
 *
 * @author wcoal
 * @since 2025-11-23
 */
@FunctionalInterface
public interface CrawlBookChapterHandler {

    /**
     * 处理解析到的章节信息
     *
     * @param chapterBean 章节数据
     */
    void handle(ChapterBean chapterBean);
}
