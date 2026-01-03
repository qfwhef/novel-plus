package com.wcoal.novelplus.core.crawl;

import cn.hutool.core.util.IdUtil;
import com.wcoal.novelplus.dao.entity.BookChapter;
import com.wcoal.novelplus.dao.entity.BookContent;
import com.wcoal.novelplus.dao.entity.BookInfo;
import com.wcoal.novelplus.dao.entity.CrawlSingleTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 爬虫解析器
 * 核心功能: 解析小说信息、章节目录和章节内容
 *
 * @author wcoal
 * @since 2025-11-23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlParser {

    private final CrawlHttpClient crawlHttpClient;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 爬虫源采集章节数量缓存key
     */
    private static final String CRAWL_SOURCE_CHAPTER_COUNT_CACHE_KEY = "crawl Source:chapterCount:";

    /**
     * 爬虫任务进度缓存
     */
    private final Map<Long, Integer> crawlTaskProgress = new HashMap<>();

    /**
     * 获取爬虫任务进度
     */
    public Integer getCrawlTaskProgress(Long taskId) {
        return crawlTaskProgress.get(taskId);
    }

    /**
     * 移除爬虫任务进度
     */
    public void removeCrawlTaskProgress(Long taskId) {
        crawlTaskProgress.remove(taskId);
    }

    /**
     * 获取爬虫源采集的章节数量
     */
    public Long getCrawlSourceChapterCount(Integer sourceId) {
        return Optional.ofNullable(
                stringRedisTemplate.opsForValue().get(CRAWL_SOURCE_CHAPTER_COUNT_CACHE_KEY + sourceId))
                .map(v -> {
                    try {
                        return Long.parseLong(v);
                    } catch (NumberFormatException e) {
                        return 0L;
                    }
                }).orElse(0L);
    }

    /**
     * 解析小说基本信息
     *
     * @param ruleBean 爬虫规则
     * @param bookId   源站小说ID
     * @param handler  小说信息处理回调
     * @throws InterruptedException 线程中断异常
     */
    public void parseBook(RuleBean ruleBean, String bookId, CrawlBookHandler handler)
            throws InterruptedException {
        BookInfo book = new BookInfo();
        String bookDetailUrl = ruleBean.getBookDetailUrl().replace("{bookId}", bookId);
        String bookDetailHtml = crawlHttpClient.get(bookDetailUrl, ruleBean.getCharset());

        if (bookDetailHtml != null) {
            // 解析书名
            Pattern bookNamePattern = PatternFactory.getPattern(ruleBean.getBookNamePatten());
            Matcher bookNameMatch = bookNamePattern.matcher(bookDetailHtml);
            boolean isFindBookName = bookNameMatch.find();

            if (isFindBookName) {
                String bookName = bookNameMatch.group(1);
                book.setBookName(bookName);

                // 解析作者名
                Pattern authorNamePattern = PatternFactory.getPattern(ruleBean.getAuthorNamePatten());
                Matcher authorNameMatch = authorNamePattern.matcher(bookDetailHtml);
                boolean isFindAuthorName = authorNameMatch.find();

                if (isFindAuthorName) {
                    String authorName = authorNameMatch.group(1);
                    book.setAuthorName(authorName);

                    // 解析封面图片
                    if (StringUtils.isNotBlank(ruleBean.getPicUrlPatten())) {
                        Pattern picUrlPattern = PatternFactory.getPattern(ruleBean.getPicUrlPatten());
                        Matcher picUrlMatch = picUrlPattern.matcher(bookDetailHtml);
                        if (picUrlMatch.find()) {
                            String picUrl = picUrlMatch.group(1);
                            if (StringUtils.isNotBlank(picUrl) && StringUtils.isNotBlank(ruleBean.getPicUrlPrefix())) {
                                picUrl = ruleBean.getPicUrlPrefix() + picUrl;
                            }
                            book.setPicUrl(picUrl);
                        }
                    }

                    // 解析评分
                    if (StringUtils.isNotBlank(ruleBean.getScorePatten())) {
                        Pattern scorePattern = PatternFactory.getPattern(ruleBean.getScorePatten());
                        Matcher scoreMatch = scorePattern.matcher(bookDetailHtml);
                        if (scoreMatch.find()) {
                            String score = scoreMatch.group(1);
                            book.setScore((int) (Float.parseFloat(score) * 10)); // novel-plus存储的是10倍评分
                        }
                    }

                    // 解析访问次数
                    if (StringUtils.isNotBlank(ruleBean.getVisitCountPatten())) {
                        Pattern visitCountPattern = PatternFactory.getPattern(ruleBean.getVisitCountPatten());
                        Matcher visitCountMatch = visitCountPattern.matcher(bookDetailHtml);
                        if (visitCountMatch.find()) {
                            String visitCount = visitCountMatch.group(1);
                            book.setVisitCount(Long.parseLong(visitCount));
                        }
                    }

                    // 解析简介
                    String desc = bookDetailHtml.substring(
                            bookDetailHtml.indexOf(ruleBean.getDescStart()) + ruleBean.getDescStart().length());
                    desc = desc.substring(0, desc.indexOf(ruleBean.getDescEnd()));

                    // 过滤简介中的特殊标签
                    desc = desc.replaceAll("<a[^<]+</a>", "")
                            .replaceAll("<font[^<]+</font>", "")
                            .replaceAll("<p>\\s*</p>", "")
                            .replaceAll("<p>", "")
                            .replaceAll("</p>", "<br/>");

                    // 应用自定义过滤规则
                    String filterDesc = ruleBean.getFilterDesc();
                    if (StringUtils.isNotBlank(filterDesc)) {
                        String[] filterRules = filterDesc.replace("\r\n", "\n").split("\n");
                        for (String filterRule : filterRules) {
                            if (StringUtils.isNotBlank(filterRule)) {
                                desc = desc.replaceAll(filterRule, "");
                            }
                        }
                    }

                    // 去除首尾空格和末尾冗余的书名
                    desc = desc.trim();
                    if (desc.endsWith(bookName)) {
                        desc = desc.substring(0, desc.length() - bookName.length());
                    }
                    book.setBookDesc(desc);

                    // 解析书籍状态
                    if (StringUtils.isNotBlank(ruleBean.getStatusPatten())) {
                        Pattern bookStatusPattern = PatternFactory.getPattern(ruleBean.getStatusPatten());
                        Matcher bookStatusMatch = bookStatusPattern.matcher(bookDetailHtml);
                        if (bookStatusMatch.find()) {
                            String bookStatus = bookStatusMatch.group(1);
                            if (ruleBean.getBookStatusRule().get(bookStatus) != null) {
                                book.setBookStatus(ruleBean.getBookStatusRule().get(bookStatus).intValue());
                            }
                        }
                    }

                    // 解析更新时间
                    if (StringUtils.isNotBlank(ruleBean.getUpdateTimePatten())
                            && StringUtils.isNotBlank(ruleBean.getUpdateTimeFormatPatten())) {
                        Pattern updateTimePattern = PatternFactory.getPattern(ruleBean.getUpdateTimePatten());
                        Matcher updateTimeMatch = updateTimePattern.matcher(bookDetailHtml);
                        if (updateTimeMatch.find()) {
                            String updateTime = updateTimeMatch.group(1);
                            try {
                                Date date = new SimpleDateFormat(ruleBean.getUpdateTimeFormatPatten())
                                        .parse(updateTime);
                                book.setLastChapterUpdateTime(
                                        LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
                            } catch (ParseException e) {
                                log.error("解析最新章节更新时间出错", e);
                            }
                        }
                    }
                }

                // 如果评分和访问次数缺失,生成默认值
                if (book.getVisitCount() == null && book.getScore() != null) {
                    // 根据评分生成访问次数(简单算法)
                    book.setVisitCount((long) (book.getScore() * 10000));
                } else if (book.getVisitCount() != null && book.getScore() == null) {
                    // 根据访问次数生成评分(简单算法)
                    book.setScore((int) Math.min(100, 50 + book.getVisitCount() / 100000));
                } else if (book.getVisitCount() == null) {
                    // 都没有,设置默认值
                    book.setVisitCount(50000L);
                    book.setScore(65); // 6.5分
                }
            }
        }

        handler.handle(book);
    }

    /**
     * 解析章节目录和内容
     *
     * @param sourceBookId      源站小说ID
     * @param book              小说信息
     * @param ruleBean          爬虫规则
     * @param sourceId          爬虫源ID
     * @param existBookIndexMap 已存在的章节Map
     * @param handler           章节处理回调
     * @param task              采集任务
     * @return 是否成功
     * @throws InterruptedException 线程中断异常
     */
    public boolean parseBookIndexAndContent(String sourceBookId, BookInfo book, RuleBean ruleBean,
            Integer sourceId, Map<Integer, BookChapter> existBookIndexMap,
            CrawlBookChapterHandler handler, CrawlSingleTask task)
            throws InterruptedException {

        if (task != null) {
            // 开始采集, 初始化进度为0
            crawlTaskProgress.put(task.getId(), 0);
        }

        LocalDateTime currentDate = LocalDateTime.now();

        List<BookChapter> indexList = new ArrayList<>();
        List<BookContent> contentList = new ArrayList<>();

        // 读取目录页
        String indexListUrl = ruleBean.getBookIndexUrl().replace("{bookId}", sourceBookId);
        String indexListHtml = crawlHttpClient.get(indexListUrl, ruleBean.getCharset());

        if (indexListHtml != null) {
            // 如果有目录开始标记,截取目录部分
            if (StringUtils.isNotBlank(ruleBean.getBookIndexStart())) {
                indexListHtml = indexListHtml.substring(
                        indexListHtml.indexOf(ruleBean.getBookIndexStart()) + ruleBean.getBookIndexStart().length());
            }

            Pattern indexIdPattern = PatternFactory.getPattern(ruleBean.getIndexIdPatten());
            Matcher indexIdMatch = indexIdPattern.matcher(indexListHtml);

            Pattern indexNamePattern = PatternFactory.getPattern(ruleBean.getIndexNamePatten());
            Matcher indexNameMatch = indexNamePattern.matcher(indexListHtml);

            boolean isFindIndex = indexIdMatch.find() & indexNameMatch.find();

            int indexNum = 0;
            int totalWordCount = book.getWordCount() == null ? 0 : book.getWordCount();

            while (isFindIndex) {
                BookChapter hasIndex = existBookIndexMap.get(indexNum);
                String indexName = indexNameMatch.group(1);

                // 如果章节不存在或章节名不同,则需要采集
                if (hasIndex == null || !StringUtils.deleteWhitespace(hasIndex.getChapterName())
                        .equals(StringUtils.deleteWhitespace(indexName))) {

                    String sourceIndexId = indexIdMatch.group(1);
                    String bookContentUrl = ruleBean.getBookContentUrl();

                    // 处理需要计算的URL
                    int calStart = bookContentUrl.indexOf("{cal_");
                    if (calStart != -1) {
                        String calStr = bookContentUrl.substring(calStart,
                                calStart + bookContentUrl.substring(calStart).indexOf("}"));
                        String[] calArr = calStr.split("_");
                        int calType = Integer.parseInt(calArr[1]);

                        if (calType == 1) {
                            // 第一种计算规则:去除第x个参数的最后y个字符
                            int x = Integer.parseInt(calArr[2]);
                            int y = Integer.parseInt(calArr[3]);
                            String calResult;
                            if (x == 1) {
                                calResult = sourceBookId.substring(0, Math.max(0, sourceBookId.length() - y));
                            } else {
                                calResult = sourceIndexId.substring(0, Math.max(0, sourceIndexId.length() - y));
                            }

                            if (calResult.isEmpty()) {
                                calResult = "0";
                            }

                            bookContentUrl = bookContentUrl.replace(calStr + "}", calResult);
                        }
                    }

                    String contentUrl = bookContentUrl.replace("{bookId}", sourceBookId)
                            .replace("{indexId}", sourceIndexId);

                    // 查询章节内容
                    String contentHtml = crawlHttpClient.get(contentUrl, ruleBean.getCharset());
                    if (contentHtml != null && !contentHtml.contains("正在手打中")) {
                        String content = contentHtml.substring(
                                contentHtml.indexOf(ruleBean.getContentStart()) + ruleBean.getContentStart().length());
                        content = content.substring(0, content.indexOf(ruleBean.getContentEnd()));

                        // 应用内容过滤规则
                        String filterContent = ruleBean.getFilterContent();
                        if (StringUtils.isNotBlank(filterContent)) {
                            String[] filterRules = filterContent.replace("\r\n", "\n").split("\n");
                            for (String filterRule : filterRules) {
                                if (StringUtils.isNotBlank(filterRule)) {
                                    content = content.replaceAll(filterRule, "");
                                }
                            }
                        }

                        // 去除末尾的<br>标签
                        content = removeTrailingBrTags(content);

                        // 计算字数(简单按字符数统计)
                        int wordCount = content.replaceAll("<[^>]+>", "").replaceAll("\\s", "").length();

                        // 创建章节目录
                        BookChapter bookIndex = new BookChapter();
                        bookIndex.setChapterName(indexName);
                        bookIndex.setChapterNum(indexNum);
                        bookIndex.setWordCount(wordCount);
                        indexList.add(bookIndex);

                        // 创建章节内容
                        BookContent bookContent = new BookContent();
                        bookContent.setContent(content);
                        contentList.add(bookContent);

                        if (hasIndex != null) {
                            // 章节更新
                            bookIndex.setId(hasIndex.getId());
                            bookContent.setChapterId(hasIndex.getId());
                            totalWordCount = totalWordCount + wordCount - hasIndex.getWordCount();
                        } else {
                            // 章节插入
                            Long indexId = IdUtil.getSnowflakeNextId();
                            bookIndex.setId(indexId);
                            bookIndex.setBookId(book.getId());
                            bookIndex.setCreateTime(currentDate);
                            bookContent.setChapterId(indexId);
                            totalWordCount += wordCount;
                        }

                        bookIndex.setUpdateTime(currentDate);

                        if (task != null) {
                            // 更新任务进度
                            crawlTaskProgress.put(task.getId(), indexList.size());
                        }

                        // 更新爬虫源采集章节数
                        stringRedisTemplate.opsForValue().increment(CRAWL_SOURCE_CHAPTER_COUNT_CACHE_KEY + sourceId);
                    }
                }

                indexNum++;
                isFindIndex = indexIdMatch.find() & indexNameMatch.find();
            }

            if (!indexList.isEmpty()) {
                // 设置最新章节信息
                BookChapter lastIndex = indexList.get(indexList.size() - 1);
                book.setLastChapterId(lastIndex.getId());
                book.setLastChapterName(lastIndex.getChapterName());
                book.setLastChapterUpdateTime(currentDate);
            }

            book.setWordCount(totalWordCount);
            book.setUpdateTime(currentDate);

            if (indexList.size() == contentList.size() && !indexList.isEmpty()) {
                ChapterBean chapterBean = new ChapterBean();
                chapterBean.setBookIndexList(indexList);
                chapterBean.setBookContentList(contentList);
                handler.handle(chapterBean);
                return true;
            }
        }

        // 失败时返回空列表
        ChapterBean chapterBean = new ChapterBean();
        chapterBean.setBookIndexList(new ArrayList<>(0));
        chapterBean.setBookContentList(new ArrayList<>(0));
        handler.handle(chapterBean);
        return false;
    }

    /**
     * 删除字符串末尾的所有<br>
     * 类似标签
     */
    public static String removeTrailingBrTags(String str) {
        return str.replaceAll("(?i)(?:\\s*<\\s*br\\s*/?\\s*>)++(?:\\s|\\u3000)*$", "");
    }
}
