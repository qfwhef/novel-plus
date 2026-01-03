package com.wcoal.novelplus.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wcoal.novelplus.core.crawl.CrawlHttpClient;
import com.wcoal.novelplus.core.crawl.CrawlParser;
import com.wcoal.novelplus.core.crawl.PatternFactory;
import com.wcoal.novelplus.core.crawl.RuleBean;
import com.wcoal.novelplus.dao.entity.BookInfo;
import com.wcoal.novelplus.dao.entity.CrawlSingleTask;
import com.wcoal.novelplus.dao.entity.CrawlSource;
import com.wcoal.novelplus.dao.mapper.BookCategoryMapper;
import com.wcoal.novelplus.dao.mapper.BookInfoMapper;
import com.wcoal.novelplus.dao.mapper.CrawlSingleTaskMapper;
import com.wcoal.novelplus.dao.mapper.CrawlSourceMapper;
import com.wcoal.novelplus.service.CrawlService;
import com.wcoal.novelplus.service.IBookChapterService;
import com.wcoal.novelplus.service.IBookContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 爬虫服务实现
 *
 * @author wcoal
 * @since 2025-11-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlServiceImpl implements CrawlService {

    private final CrawlParser crawlParser;

    private final CrawlHttpClient crawlHttpClient;

    private final CrawlSourceMapper crawlSourceMapper;

    private final CrawlSingleTaskMapper crawlSingleTaskMapper;

    private final ObjectMapper objectMapper;

    private final BookInfoMapper bookInfoMapper;

    private final IBookChapterService bookChapterService;

    private final IBookContentService bookContentService;

    private final BookCategoryMapper bookCategoryMapper;

    /**
     * 爬虫源状态缓存
     */
    private final Map<Integer, Byte> crawlSourceStatusMap = new HashMap<>();

    /**
     * 运行中的爬虫线程
     */
    private final Map<Integer, Set<Long>> runningCrawlThread = new HashMap<>();

    @Override
    public void addCrawlSource(CrawlSource source) {
        LocalDateTime now = LocalDateTime.now();
        source.setCreateTime(now);
        source.setUpdateTime(now);
        crawlSourceMapper.insert(source);
    }

    @Override
    public void updateCrawlSource(CrawlSource source) {
        if (source.getId() != null) {
            CrawlSource crawlSource = crawlSourceMapper.selectById(source.getId());
            if (crawlSource != null) {
                if (crawlSource.getSourceStatus() == (byte) 1) {
                    // 如果正在运行,先关闭
                    openOrCloseCrawl(crawlSource.getId(), (byte) 0);
                }
                source.setUpdateTime(LocalDateTime.now());
                crawlSourceMapper.updateById(source);
            }
        }
    }

    @Override
    public Page<CrawlSource> listCrawlByPage(int page, int pageSize) {
        Page<CrawlSource> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<CrawlSource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(CrawlSource::getUpdateTime);
        return crawlSourceMapper.selectPage(pageInfo, queryWrapper);
    }

    @Override
    public void openOrCloseCrawl(Integer sourceId, Byte sourceStatus) {
        if (sourceStatus == (byte) 0) {
            // 关闭爬虫
            Set<Long> runningThreadIds = runningCrawlThread.get(sourceId);
            if (runningThreadIds != null) {
                for (Long threadId : runningThreadIds) {
                    // 查找并中断线程
                    Thread.getAllStackTraces().keySet().stream()
                            .filter(t -> t.threadId() == threadId)
                            .findFirst()
                            .ifPresent(Thread::interrupt);
                }
                runningCrawlThread.remove(sourceId);
            }
        } else {
            // 开启爬虫
            Byte realSourceStatus = crawlSourceStatusMap.getOrDefault(sourceId, (byte) 0);
            if (realSourceStatus == (byte) 0) {
                // 查询爬虫源规则
                CrawlSource source = queryCrawlSource(sourceId);
                try {
                    RuleBean ruleBean = objectMapper.readValue(source.getCrawlRule(), RuleBean.class);
                    Set<Long> threadIds = new HashSet<>();

                    // 按分类开始爬虫解析任务(7个分类)
                    for (int i = 1; i < 8; i++) {
                        final int catId = i;
                        Thread thread = new Thread(() -> parseBookList(catId, ruleBean, sourceId));
                        thread.start();
                        threadIds.add(thread.threadId());
                    }

                    runningCrawlThread.put(sourceId, threadIds);
                } catch (Exception e) {
                    log.error("启动爬虫失败", e);
                }
            }
        }

        crawlSourceStatusMap.put(sourceId, sourceStatus);
    }

    @Override
    public void updateCrawlSourceStatus(Integer sourceId, Byte sourceStatus) {
        CrawlSource source = new CrawlSource();
        source.setId(sourceId);
        source.setSourceStatus(sourceStatus);
        crawlSourceMapper.updateById(source);
    }

    @Override
    public boolean parseBookAndSave(int catId, RuleBean ruleBean, Integer sourceId, String bookId,
            CrawlSingleTask task) throws InterruptedException {
        final AtomicBoolean parseResult = new AtomicBoolean(false);

        crawlParser.parseBook(ruleBean, bookId, book -> {
            if (book.getBookName() == null || book.getAuthorName() == null) {
                return;
            }

            // 检查小说是否已存在 (根据书名和作者)
            LambdaQueryWrapper<BookInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(BookInfo::getBookName, book.getBookName())
                    .eq(BookInfo::getAuthorName, book.getAuthorName());
            BookInfo existBook = bookInfoMapper.selectOne(queryWrapper);

            if (existBook == null) {
                // 新书入库
                book.setCategoryId((long) catId);

                // 查询分类名称
                String categoryName = Optional.ofNullable(bookCategoryMapper.selectById(catId))
                        .map(cat -> cat.getName())
                        .orElse("未分类");
                book.setCategoryName(categoryName);

                book.setWorkDirection(catId == 7 ? 1 : 0); // 7为女频
                book.setId(IdUtil.getSnowflakeNextId());
                book.setCreateTime(LocalDateTime.now());
                book.setCommentCount(0);
                book.setIsVip(0); // 默认免费

                try {
                    // 解析章节目录和内容
                    boolean parseIndexContentResult = crawlParser.parseBookIndexAndContent(
                            bookId, book, ruleBean, sourceId, new HashMap<>(),
                            chapterBean -> {
                                // 保存小说信息
                                bookInfoMapper.insert(book);

                                // 批量保存章节目录
                                if (!chapterBean.getBookIndexList().isEmpty()) {
                                    bookChapterService.saveBatch(chapterBean.getBookIndexList());
                                }

                                // 批量保存章节内容
                                if (!chapterBean.getBookContentList().isEmpty()) {
                                    bookContentService.saveBatch(chapterBean.getBookContentList());
                                }

                                log.info("成功保存小说: {}, 章节数: {}", book.getBookName(),
                                        chapterBean.getBookIndexList().size());
                            }, task);
                    parseResult.set(parseIndexContentResult);
                } catch (InterruptedException e) {
                    log.error("解析章节失败: {}", book.getBookName(), e);
                    Thread.currentThread().interrupt();
                }
            } else {
                // 已存在,跳过
                log.info("小说已存在,跳过: {} - {}", book.getBookName(), book.getAuthorName());
                parseResult.set(true);
            }
        });

        return parseResult.get();
    }

    @Override
    public List<CrawlSource> queryCrawlSourceByStatus(Byte sourceStatus) {
        LambdaQueryWrapper<CrawlSource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CrawlSource::getSourceStatus, sourceStatus);
        return crawlSourceMapper.selectList(queryWrapper);
    }

    @Override
    public void parseBookList(int catId, RuleBean ruleBean, Integer sourceId) {
        String catIdRule = ruleBean.getCatIdRule().get("catId" + catId);
        if (org.apache.commons.lang3.StringUtils.isBlank(catIdRule)) {
            log.warn("分类ID: {} 没有配置规则,跳过", catId);
            return;
        }

        log.info("开始采集分类: {}, 爬虫源ID: {}", catId, sourceId);

        // 当前页码
        int page = 1;
        int totalPage = page;

        while (page <= totalPage) {
            try {
                // 构建分类列表URL
                String catBookListUrl;
                if (org.apache.commons.lang3.StringUtils.isNotBlank(ruleBean.getBookListUrl())) {
                    // 兼容老规则
                    catBookListUrl = ruleBean.getBookListUrl()
                            .replace("{catId}", catIdRule)
                            .replace("{page}", String.valueOf(page));
                } else {
                    // 新规则
                    catBookListUrl = catIdRule.replace("{page}", String.valueOf(page));
                }

                log.info("爬取URL: {}", catBookListUrl);

                // 获取列表页HTML
                String bookListHtml = crawlHttpClient.get(catBookListUrl, ruleBean.getCharset());
                if (bookListHtml != null) {
                    // 解析书籍ID列表
                    Pattern bookIdPattern = PatternFactory.getPattern(ruleBean.getBookIdPatten());
                    Matcher bookIdMatcher = bookIdPattern.matcher(bookListHtml);
                    boolean isFindBookId = bookIdMatcher.find();

                    while (isFindBookId) {
                        try {
                            // 检查线程是否被中断
                            if (Thread.currentThread().isInterrupted()) {
                                log.info("爬虫线程被中断,退出采集");
                                return;
                            }

                            String bookId = bookIdMatcher.group(1);
                            // 采集并保存单本小说
                            parseBookAndSave(catId, ruleBean, sourceId, bookId, null);
                        } catch (InterruptedException e) {
                            log.error("采集小说失败(被中断)", e);
                            return;
                        } catch (Exception e) {
                            log.error("采集小说失败", e);
                        }

                        isFindBookId = bookIdMatcher.find();
                    }

                    // 解析总页数
                    Pattern totalPagePattern = PatternFactory.getPattern(ruleBean.getTotalPagePatten());
                    Matcher totalPageMatcher = totalPagePattern.matcher(bookListHtml);
                    if (totalPageMatcher.find()) {
                        totalPage = Integer.parseInt(totalPageMatcher.group(1));
                    }
                }
            } catch (InterruptedException e) {
                log.error("爬取列表失败(被中断)", e);
                return;
            } catch (Exception e) {
                log.error("爬取列表失败", e);
            }

            if (page >= totalPage) {
                // 第一遍采集完成,重新从第一页开始,继续循环采集
                page = 1;
                try {
                    // 休眠1分钟后继续
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    log.error("休眠被中断", e);
                    return;
                }
            } else {
                page += 1;
            }
        }
    }

    @Override
    public CrawlSource queryCrawlSource(Integer sourceId) {
        return crawlSourceMapper.selectById(sourceId);
    }

    @Override
    public void addCrawlSingleTask(CrawlSingleTask singleTask) {
        singleTask.setId(IdUtil.getSnowflakeNextId());
        singleTask.setCreateTime(LocalDateTime.now());
        singleTask.setTaskStatus((byte) 2); // 排队中
        singleTask.setExcCount((byte) 0);
        crawlSingleTaskMapper.insert(singleTask);
    }

    @Override
    public Page<CrawlSingleTask> listCrawlSingleTaskByPage(int page, int pageSize) {
        Page<CrawlSingleTask> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<CrawlSingleTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(CrawlSingleTask::getCreateTime);
        return crawlSingleTaskMapper.selectPage(pageInfo, queryWrapper);
    }

    @Override
    public void delCrawlSingleTask(Long id) {
        crawlSingleTaskMapper.deleteById(id);
    }

    @Override
    public CrawlSingleTask getCrawlSingleTask() {
        LambdaQueryWrapper<CrawlSingleTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CrawlSingleTask::getTaskStatus, 2) // 排队中
                .orderByAsc(CrawlSingleTask::getCreateTime)
                .last("LIMIT 1");

        List<CrawlSingleTask> list = crawlSingleTaskMapper.selectList(queryWrapper);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void updateCrawlSingleTask(CrawlSingleTask task, Byte status) {
        byte excCount = task.getExcCount();
        excCount += 1;
        task.setExcCount(excCount);

        if (status == 1 || excCount == 5) {
            // 成功或重试5次后更新最终状态
            task.setTaskStatus(status);
        }

        if (status == 1) {
            // 成功时保存采集章节数
            task.setCrawlChapters(crawlParser.getCrawlTaskProgress(task.getId()));
        }

        crawlSingleTaskMapper.updateById(task);
        crawlParser.removeCrawlTaskProgress(task.getId());
    }

    @Override
    public CrawlSource getCrawlSource(Integer id) {
        return crawlSourceMapper.selectById(id);
    }

    @Override
    public Integer getTaskProgress(Long taskId) {
        return Optional.ofNullable(crawlParser.getCrawlTaskProgress(taskId)).orElse(0);
    }
}
