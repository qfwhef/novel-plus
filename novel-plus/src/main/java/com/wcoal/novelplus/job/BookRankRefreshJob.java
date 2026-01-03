package com.wcoal.novelplus.job;

import com.wcoal.novelplus.manager.cache.BookRankCacheManager;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 书籍排行榜定时刷新任务
 * 
 * 功能说明：
 * 1. 定时刷新点击榜、新书榜、更新榜缓存
 * 2. 解决原有手动清除缓存的问题
 * 3. 支持分片广播，提升处理效率
 * 
 * 任务配置建议（在XXL-Job管理后台配置）：
 * - 点击榜刷新：每小时执行一次，Cron: 0 0 * * * ?
 * - 新书榜刷新：每天凌晨2点执行，Cron: 0 0 2 * * ?
 * - 更新榜刷新：每天凌晨3点执行，Cron: 0 0 3 * * ?
 * 
 * 优化说明：
 * 旧版本：需要手动调用 evictVisitRankCache() 等方法清除缓存
 * 新版本：通过定时任务自动刷新，无需人工介入
 * 
 * @author wcoal
 * @since 2025-10-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookRankRefreshJob {

    private final BookRankCacheManager bookRankCacheManager;

    /**
     * 刷新点击榜缓存
     * 
     * 执行策略：每小时执行一次
     * 路由策略：第一个（单机执行即可）
     */
    @XxlJob("refreshVisitRankJob")
    public void refreshVisitRank() {
        long startTime = System.currentTimeMillis();
        
        try {
            XxlJobHelper.log("========== 开始刷新点击榜缓存 ==========");
            log.info("开始刷新点击榜缓存");

            // 清除旧缓存
            bookRankCacheManager.evictVisitRankCache();
            
            // 预加载新数据（调用查询方法会自动缓存）
            bookRankCacheManager.listVisitRankBooks();

            long costTime = System.currentTimeMillis() - startTime;
            String successMsg = String.format("点击榜缓存刷新成功，耗时: %d ms", costTime);
            XxlJobHelper.log(successMsg);
            log.info(successMsg);
            
            XxlJobHelper.handleSuccess(successMsg);
            
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            String errorMsg = String.format("点击榜缓存刷新失败，耗时: %d ms，错误: %s", 
                    costTime, e.getMessage());
            XxlJobHelper.log(errorMsg);
            log.error(errorMsg, e);
            
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    /**
     * 刷新新书榜缓存
     * 
     * 执行策略：每天凌晨2点执行cron
     * 路由策略：第一个（单机执行即可）
     */
    @XxlJob("refreshNewestRankJob")
    public void refreshNewestRank() {
        long startTime = System.currentTimeMillis();
        
        try {
            XxlJobHelper.log("========== 开始刷新新书榜缓存 ==========");
            log.info("开始刷新新书榜缓存");

            // 清除旧缓存
            bookRankCacheManager.evictNewestRankCache();
            
            // 预加载新数据
            bookRankCacheManager.listNewestRankBooks();

            long costTime = System.currentTimeMillis() - startTime;
            String successMsg = String.format("新书榜缓存刷新成功，耗时: %d ms", costTime);
            XxlJobHelper.log(successMsg);
            log.info(successMsg);
            
            XxlJobHelper.handleSuccess(successMsg);
            
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            String errorMsg = String.format("新书榜缓存刷新失败，耗时: %d ms，错误: %s", 
                    costTime, e.getMessage());
            XxlJobHelper.log(errorMsg);
            log.error(errorMsg, e);
            
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    /**
     * 刷新更新榜缓存
     * 
     * 执行策略：每天凌晨3点执行
     * 路由策略：第一个（单机执行即可）
     */
    @XxlJob("refreshUpdateRankJob")
    public void refreshUpdateRank() {
        long startTime = System.currentTimeMillis();
        
        try {
            XxlJobHelper.log("========== 开始刷新更新榜缓存 ==========");
            log.info("开始刷新更新榜缓存");

            // 清除旧缓存
            bookRankCacheManager.evictUpdateRankCache();
            
            // 预加载新数据
            bookRankCacheManager.listUpdateRankBooks();

            long costTime = System.currentTimeMillis() - startTime;
            String successMsg = String.format("更新榜缓存刷新成功，耗时: %d ms", costTime);
            XxlJobHelper.log(successMsg);
            log.info(successMsg);
            
            XxlJobHelper.handleSuccess(successMsg);
            
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            String errorMsg = String.format("更新榜缓存刷新失败，耗时: %d ms，错误: %s", 
                    costTime, e.getMessage());
            XxlJobHelper.log(errorMsg);
            log.error(errorMsg, e);
            
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    /**
     * 一键刷新所有排行榜
     * 
     * 用途：手动触发或特殊场景使用
     * 路由策略：第一个
     */
    @XxlJob("refreshAllRankJob")
    public void refreshAllRank() {
        long startTime = System.currentTimeMillis();
        
        try {
            XxlJobHelper.log("========== 开始刷新所有排行榜缓存 ==========");
            log.info("开始刷新所有排行榜缓存");

            // 依次刷新三个排行榜
            refreshVisitRank();
            refreshNewestRank();
            refreshUpdateRank();

            long costTime = System.currentTimeMillis() - startTime;
            String successMsg = String.format("所有排行榜缓存刷新成功，耗时: %d ms", costTime);
            XxlJobHelper.log(successMsg);
            log.info(successMsg);
            
            XxlJobHelper.handleSuccess(successMsg);
            
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            String errorMsg = String.format("所有排行榜缓存刷新失败，耗时: %d ms，错误: %s", 
                    costTime, e.getMessage());
            XxlJobHelper.log(errorMsg);
            log.error(errorMsg, e);
            
            XxlJobHelper.handleFail(errorMsg);
        }
    }
}

