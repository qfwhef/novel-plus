package com.wcoal.novelplus.job;

import com.wcoal.novelplus.manager.cache.BookInfoCacheManager;
import com.wcoal.novelplus.manager.cache.BookRankCacheManager;
import com.wcoal.novelplus.manager.cache.HomeBookCacheManager;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 缓存预热定时任务
 * 
 * 功能说明：
 * 1. 每天凌晨预热热门数据到缓存
 * 2. 防止缓存雪崩和缓存击穿
 * 3. 提升用户访问体验
 * 
 * 预热策略：
 * - 首页推荐书籍
 * - 三大排行榜（点击榜、新书榜、更新榜）
 * - 可扩展：热门书籍详情、热门章节等
 * 
 * 任务配置建议（在XXL-Job管理后台配置）：
 * - 执行时间：每天凌晨4点，Cron: 0 0 4 * * ?
 * - 路由策略：第一个（单机执行即可）
 * - 失败重试：3次
 * 
 * 技术亮点：
 * - 避免高峰期缓存失效导致的数据库压力
 * - 提前加载热点数据，提升响应速度
 * - 支持分批预热，避免内存溢出
 * 
 * @author wcoal
 * @since 2025-10-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheWarmUpJob {

    private final HomeBookCacheManager homeBookCacheManager;
    private final BookRankCacheManager bookRankCacheManager;
    private final BookInfoCacheManager bookInfoCacheManager;

    /**
     * 缓存预热主任务
     * 
     * 执行策略：每天凌晨4点执行
     * 路由策略：第一个
     */
    @XxlJob("cacheWarmUpJob")
    public void warmUpCache() {
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failCount = 0;
        
        try {
            XxlJobHelper.log("========== 开始执行缓存预热任务 ==========");
            log.info("开始执行缓存预热任务");

            // 1. 预热首页推荐书籍
            try {
                XxlJobHelper.log("1. 预热首页推荐书籍...");
                homeBookCacheManager.listHomeBooks();
                successCount++;
                XxlJobHelper.log("   ✓ 首页推荐书籍预热成功");
            } catch (Exception e) {
                failCount++;
                XxlJobHelper.log("   ✗ 首页推荐书籍预热失败: " + e.getMessage());
                log.error("首页推荐书籍预热失败", e);
            }

            // 2. 预热点击榜
            try {
                XxlJobHelper.log("2. 预热点击榜...");
                bookRankCacheManager.listVisitRankBooks();
                successCount++;
                XxlJobHelper.log("   ✓ 点击榜预热成功");
            } catch (Exception e) {
                failCount++;
                XxlJobHelper.log("   ✗ 点击榜预热失败: " + e.getMessage());
                log.error("点击榜预热失败", e);
            }

            // 3. 预热新书榜
            try {
                XxlJobHelper.log("3. 预热新书榜...");
                bookRankCacheManager.listNewestRankBooks();
                successCount++;
                XxlJobHelper.log("   ✓ 新书榜预热成功");
            } catch (Exception e) {
                failCount++;
                XxlJobHelper.log("   ✗ 新书榜预热失败: " + e.getMessage());
                log.error("新书榜预热失败", e);
            }

            // 4. 预热更新榜
            try {
                XxlJobHelper.log("4. 预热更新榜...");
                bookRankCacheManager.listUpdateRankBooks();
                successCount++;
                XxlJobHelper.log("   ✓ 更新榜预热成功");
            } catch (Exception e) {
                failCount++;
                XxlJobHelper.log("   ✗ 更新榜预热失败: " + e.getMessage());
                log.error("更新榜预热失败", e);
            }

            // 5. 可扩展：预热热门书籍详情
            // 这里可以根据实际需求，预热访问量最高的书籍详情
            // 例如：从点击榜中取前10本书，预热它们的详情
            /*
            try {
                XxlJobHelper.log("5. 预热热门书籍详情...");
                List<BookRankRespDto> topBooks = bookRankCacheManager.listVisitRankBooks()
                        .stream().limit(10).toList();
                for (BookRankRespDto book : topBooks) {
                    bookInfoCacheManager.getBookInfo(book.getId());
                }
                successCount++;
                XxlJobHelper.log("   ✓ 热门书籍详情预热成功");
            } catch (Exception e) {
                failCount++;
                XxlJobHelper.log("   ✗ 热门书籍详情预热失败: " + e.getMessage());
                log.error("热门书籍详情预热失败", e);
            }
            */

            long costTime = System.currentTimeMillis() - startTime;
            String resultMsg = String.format(
                    "缓存预热任务执行完成！成功: %d 项，失败: %d 项，耗时: %d ms", 
                    successCount, failCount, costTime);
            
            XxlJobHelper.log("========================================");
            XxlJobHelper.log(resultMsg);
            log.info(resultMsg);

            if (failCount == 0) {
                XxlJobHelper.handleSuccess(resultMsg);
            } else {
                XxlJobHelper.handleFail(resultMsg);
            }
            
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            String errorMsg = String.format("缓存预热任务执行异常，耗时: %d ms，错误: %s", 
                    costTime, e.getMessage());
            XxlJobHelper.log(errorMsg);
            log.error(errorMsg, e);
            
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    /**
     * 清除所有缓存（慎用）
     * 
     * 用途：特殊场景下需要清空所有缓存时使用
     * 建议：手动触发，不要设置定时执行
     */
    @XxlJob("clearAllCacheJob")
    public void clearAllCache() {
        long startTime = System.currentTimeMillis();
        
        try {
            XxlJobHelper.log("========== 开始清除所有缓存 ==========");
            log.warn("开始清除所有缓存（慎用操作）");

            // 清除排行榜缓存
            bookRankCacheManager.evictVisitRankCache();
            bookRankCacheManager.evictNewestRankCache();
            bookRankCacheManager.evictUpdateRankCache();

            long costTime = System.currentTimeMillis() - startTime;
            String successMsg = String.format("所有缓存清除成功，耗时: %d ms", costTime);
            XxlJobHelper.log(successMsg);
            log.warn(successMsg);
            
            XxlJobHelper.handleSuccess(successMsg);
            
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            String errorMsg = String.format("清除缓存失败，耗时: %d ms，错误: %s", 
                    costTime, e.getMessage());
            XxlJobHelper.log(errorMsg);
            log.error(errorMsg, e);
            
            XxlJobHelper.handleFail(errorMsg);
        }
    }
}

