package com.wcoal.novelplus.core.crawl;

import lombok.Data;

import java.util.Map;

/**
 * 爬虫解析规则配置类
 *
 * @author wcoal
 * @since 2025-11-23
 */
@Data
public class RuleBean {

    /**
     * 网页字符编码
     */
    private String charset = "UTF-8";

    /**
     * 小说更新列表url
     */
    private String updateBookListUrl;

    /**
     * 分类列表页URL规则
     */
    private String bookListUrl;

    /**
     * 分类ID映射规则
     */
    private Map<String, String> catIdRule;

    /**
     * 书籍状态映射规则
     */
    private Map<String, Byte> bookStatusRule;

    /**
     * 书籍ID正则
     */
    private String bookIdPatten;

    /**
     * 页码正则
     */
    private String pagePatten;

    /**
     * 总页数正则
     */
    private String totalPagePatten;

    /**
     * 书籍详情页URL
     */
    private String bookDetailUrl;

    /**
     * 书名正则
     */
    private String bookNamePatten;

    /**
     * 作者名正则
     */
    private String authorNamePatten;

    /**
     * 封面图片URL正则
     */
    private String picUrlPatten;

    /**
     * 状态正则
     */
    private String statusPatten;

    /**
     * 评分正则
     */
    private String scorePatten;

    /**
     * 访问次数正则
     */
    private String visitCountPatten;

    /**
     * 简介开始标记
     */
    private String descStart;

    /**
     * 简介结束标记
     */
    private String descEnd;

    /**
     * 简介过滤规则
     */
    private String filterDesc;

    /**
     * 更新时间正则
     */
    private String updateTimePatten;

    /**
     * 更新时间格式
     */
    private String updateTimeFormatPatten;

    /**
     * 章节目录页URL
     */
    private String bookIndexUrl;

    /**
     * 章节ID正则
     */
    private String indexIdPatten;

    /**
     * 章节名正则
     */
    private String indexNamePatten;

    /**
     * 章节内容页URL
     */
    private String bookContentUrl;

    /**
     * 内容开始标记
     */
    private String contentStart;

    /**
     * 内容结束标记
     */
    private String contentEnd;

    /**
     * 封面图片URL前缀
     */
    private String picUrlPrefix;

    /**
     * 章节目录开始标记
     */
    private String bookIndexStart;

    /**
     * 内容过滤规则
     */
    private String filterContent;
}
