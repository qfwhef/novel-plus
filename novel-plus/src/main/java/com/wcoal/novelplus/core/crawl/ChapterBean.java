package com.wcoal.novelplus.core.crawl;

import com.wcoal.novelplus.dao.entity.BookChapter;
import com.wcoal.novelplus.dao.entity.BookContent;
import lombok.Data;

import java.util.List;

/**
 * 章节数据封装类
 *
 * @author wcoal
 * @since 2025-11-23
 */
@Data
public class ChapterBean {

    /**
     * 章节目录列表
     */
    private List<BookChapter> bookIndexList;

    /**
     * 章节内容列表
     */
    private List<BookContent> bookContentList;
}
