package com.wcoal.novelplus.job;

import com.wcoal.novelplus.dao.mapper.UserBookshelfMapper;
import com.wcoal.novelplus.manager.redis.ReadProgressManager;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 阅读进度同步定时任务
 * 将 Redis 中的阅读进度数据定期批量同步到 MySQL
 *
 * @author wcoal
 * @since 2025-11-18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReadProgressSyncJob {

    private final ReadProgressManager readProgressManager;
    private final UserBookshelfMapper userBookshelfMapper;

    /**
     * 同步阅读进度到数据库
     * 建议 Cron: 0 0/5 * * * ? (每5分钟执行一次)
     * 或者: 0 0/10 * * * ? (每10分钟执行一次)
     */
    @XxlJob("readProgressSyncJob")
    public void execute() {
        long startTime = System.currentTimeMillis();
        XxlJobHelper.log("========== 开始同步阅读进度数据 ==========");

        try {
            // 1. 获取所有待同步的阅读进度
            Set<String> pendingKeys = readProgressManager.getPendingProgressKeys();

            if (pendingKeys == null || pendingKeys.isEmpty()) {
                XxlJobHelper.log("没有待同步的阅读进度数据");
                return;
            }

            XxlJobHelper.log("待同步阅读进度数量: " + pendingKeys.size());

            // 2. 批量获取阅读进度数据
            Map<String, Long> progressMap = readProgressManager.batchGetProgress(pendingKeys);

            if (progressMap.isEmpty()) {
                XxlJobHelper.log("获取阅读进度数据为空");
                return;
            }

            int successCount = 0;
            int failCount = 0;
            int skipCount = 0;

            // 3. 批量更新到数据库
            for (Map.Entry<String, Long> entry : progressMap.entrySet()) {
                String key = entry.getKey();
                Long chapterId = entry.getValue();

                try {
                    // 解析 userId:bookId
                    String[] parts = key.split(":");
                    if (parts.length != 2) {
                        log.warn("无效的Key格式: {}", key);
                        skipCount++;
                        continue;
                    }

                    Long userId = Long.parseLong(parts[0]);
                    Long bookId = Long.parseLong(parts[1]);

                    // 更新数据库
                    int updated = userBookshelfMapper.updateReadProgress(userId, bookId, chapterId);

                    if (updated > 0) {
                        successCount++;
                        // 更新成功后移除待同步标记
                        readProgressManager.removeSyncMark(userId, bookId);
                        
                        log.debug("同步成功: userId={}, bookId={}, chapterId={}", userId, bookId, chapterId);
                    } else {
                        // 更新失败可能是书籍不在书架中，也移除标记避免重复处理
                        skipCount++;
                        readProgressManager.removeSyncMark(userId, bookId);
                        
                        log.debug("书籍不在书架中，跳过: userId={}, bookId={}", userId, bookId);
                    }

                } catch (Exception e) {
                    failCount++;
                    log.error("同步阅读进度失败: key={}, chapterId={}", key, chapterId, e);
                    XxlJobHelper.log("同步失败: " + key + ", error=" + e.getMessage());
                }
            }

            long endTime = System.currentTimeMillis();
            String result = String.format(
                    "========== 同步完成 ========== 总数: %d, 成功: %d, 跳过: %d, 失败: %d, 耗时: %dms",
                    progressMap.size(), successCount, skipCount, failCount, (endTime - startTime)
            );

            XxlJobHelper.log(result);
            log.info(result);

        } catch (Exception e) {
            log.error("阅读进度同步任务执行失败", e);
            XxlJobHelper.log("任务执行失败: " + e.getMessage());
            XxlJobHelper.handleFail(e.getMessage());
        }
    }
}
